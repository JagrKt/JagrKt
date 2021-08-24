/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.core

import com.google.inject.Inject
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.java.CompiledClass
import org.sourcegrade.jagr.core.compiler.java.RuntimeJarLoader
import org.sourcegrade.jagr.core.executor.WaterfallExecutor
import org.sourcegrade.jagr.core.export.rubric.GradedRubricExportManager
import org.sourcegrade.jagr.core.export.submission.SubmissionExportManager
import org.sourcegrade.jagr.core.extra.ExtrasManager
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.testing.RuntimeGrader
import org.sourcegrade.jagr.core.testing.TestJar
import org.sourcegrade.jagr.core.transformer.TransformerManager
import java.io.File

class JagrImpl @Inject constructor(
  private val config: Config,
  private val logger: Logger,
  private val runtimeJarLoader: RuntimeJarLoader,
  private val runtimeGrader: RuntimeGrader,
  private val extrasManager: ExtrasManager,
  private val transformerManager: TransformerManager,
  private val gradedRubricExportManager: GradedRubricExportManager,
  private val submissionExportManager: SubmissionExportManager,
) {

  private fun loadTestJars(testJarsLocation: File, solutionsLocation: File): List<TestJar> {
    val solutionClasses = loadLibs(solutionsLocation)
    return testJarsLocation.listFiles { _, t -> t.endsWith(".jar") }!!.parallelMapNotNull {
      with(transformerManager.transform(runtimeJarLoader.loadSourcesJar(it, solutionClasses))) {
        printMessages(
          logger,
          { "Test jar ${file.name} has $warnings warnings and $errors errors!" },
          { "Test jar ${file.name} has $warnings warnings!" },
        )
        logger.info("Loaded test jar ${it.name}")
        TestJar(logger, it, compiledClasses, sourceFiles, solutionClasses).takeIf { errors == 0 }
      }
    }
  }

  private fun loadLibs(libsLocation: File): Map<String, CompiledClass> {
    return libsLocation.listFiles { _, t -> t.endsWith(".jar") }!!
      .asSequence()
      .map {
        val classStorage = runtimeJarLoader.loadCompiledJar(it)
        logger.info("Loaded lib jar ${it.name}")
        classStorage
      }
      .ifEmpty { listOf(mapOf<String, CompiledClass>()).asSequence() }
      .reduce { a, b -> a + b }
  }

  private fun loadSubmissionJars(submissionJarsLocation: File, libsLocation: File): List<JavaSubmission> {
    val libsClasspath = loadLibs(libsLocation)
    return submissionJarsLocation.listFiles { _, t -> t.endsWith(".jar") }!!.parallelMapNotNull {
      with(transformerManager.transform(runtimeJarLoader.loadSourcesJar(it, libsClasspath))) {
        if (submissionInfo == null) {
          logger.error("$it does not have a submission-info.json! Skipping...")
          return@parallelMapNotNull null
        }
        printMessages(
          logger,
          { "Submission $submissionInfo(${file.name}) has $warnings warnings and $errors errors!" },
          { "Submission $submissionInfo(${file.name}) has $warnings warnings!" },
        )
        JavaSubmission(file, submissionInfo, this, compiledClasses, sourceFiles)
          .apply { logger.info("Loaded submission jar $this") }
      }
    }
  }

  fun run() {
    ensureDirs()
    extrasManager.runExtras()
    val testJars = loadTestJars(File(config.dir.tests), File(config.dir.solutions))
    val submissions = loadSubmissionJars(File(config.dir.submissions), File(config.dir.libs))
    val rubricExportLocation = File(config.dir.rubrics)
    gradedRubricExportManager.initialize(rubricExportLocation, testJars)
    val submissionExportLocation = File(config.dir.submissionsExport)
    submissionExportManager.initialize(submissionExportLocation, testJars)
    submissions.forEach { submission ->
      submissionExportManager.export(submission, submissionExportLocation, testJars)
    }
    submissionExportManager.finalize(submissionExportLocation, testJars)
    val executor = with(config.grading) {
      WaterfallExecutor(concurrentThreads, -1L, logger)
    }
    submissions.forEach { submission ->
      executor.schedule(submission.info.toString()) {
        handleSubmission(submission, testJars, rubricExportLocation)
      }
    }
    runBlocking {
      executor.execute()
    }
  }

  private fun handleSubmission(submission: Submission, testJars: List<TestJar>, rubricExportLocation: File) {
    val gradedRubrics = runtimeGrader.grade(testJars, submission)
    if (gradedRubrics.isEmpty()) {
      logger.warn("$submission :: No matching rubrics!")
      return
    }
    for ((gradedRubric, exportFileName) in gradedRubrics) {
      gradedRubricExportManager.export(gradedRubric, rubricExportLocation, exportFileName)
    }
  }

  private fun ensureDirs() {
    with(config.dir) {
      File("logs").ensure(logger)
      File(libs).ensure(logger)
      File(rubrics).ensure(logger)
      File(solutions).ensure(logger)
      File(submissions).ensure(logger)
      File(submissionsExport).ensure(logger)
      File(tests).ensure(logger)
    }
  }
}
