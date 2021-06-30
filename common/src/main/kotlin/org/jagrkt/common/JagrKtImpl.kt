/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common

import com.google.inject.Inject
import kotlinx.coroutines.runBlocking
import org.jagrkt.api.testing.Submission
import org.jagrkt.common.compiler.java.CompiledClass
import org.jagrkt.common.compiler.java.RuntimeJarLoader
import org.jagrkt.common.executor.WaterfallExecutor
import org.jagrkt.common.export.rubric.GradedRubricExportManager
import org.jagrkt.common.export.submission.SubmissionExportManager
import org.jagrkt.common.extra.ExtrasManager
import org.jagrkt.common.testing.JavaSubmission
import org.jagrkt.common.testing.RuntimeGrader
import org.jagrkt.common.testing.TestJar
import org.jagrkt.common.transformer.TransformerManager
import org.jagrkt.launcher.configuration.LaunchConfiguration
import org.jagrkt.launcher.executor.GradingContext
import org.slf4j.Logger
import java.io.File

class JagrKtImpl @Inject constructor(
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

  fun launch(configuration: LaunchConfiguration, context: GradingContext) {
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
        handleSubmission(context, submission, testJars, rubricExportLocation)
      }
    }
    runBlocking {
      executor.execute()
    }
  }

  private fun handleSubmission(
    context: GradingContext,
    submission: Submission,
    testJars: List<TestJar>,
    rubricExportLocation: File,
  ) {
    val progress = context.beginGrading(submission)
    val gradedRubrics = runtimeGrader.grade(testJars, submission)
    progress.endGrading(gradedRubrics)
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
