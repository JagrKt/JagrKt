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
import org.slf4j.Logger
import org.sourcegrade.jagr.core.compiler.java.CompiledClass
import org.sourcegrade.jagr.core.compiler.java.JavaCompileResult
import org.sourcegrade.jagr.core.compiler.java.RuntimeJarLoader
import org.sourcegrade.jagr.core.export.rubric.GradedRubricExportManager
import org.sourcegrade.jagr.core.export.submission.SubmissionExportManager
import org.sourcegrade.jagr.core.extra.ExtrasManager
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.testing.RuntimeGraderImpl
import org.sourcegrade.jagr.core.testing.TestJarImpl
import org.sourcegrade.jagr.core.transformer.TransformerManager
import org.sourcegrade.jagr.launcher.io.TestJar
import org.sourcegrade.jagr.launcher.io.createResourceContainer
import org.sourcegrade.jagr.launcher.io.nameWithoutExtension
import org.sourcegrade.jagr.launcher.opt.Config
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class JagrImpl @Inject constructor(
  private val config: Config,
  private val logger: Logger,
  private val runtimeJarLoader: RuntimeJarLoader,
  private val runtimeGrader: RuntimeGraderImpl,
  private val extrasManager: ExtrasManager,
  private val transformerManager: TransformerManager,
  private val gradedRubricExportManager: GradedRubricExportManager,
  private val submissionExportManager: SubmissionExportManager,
) {

  private fun loadTestJars(testJarsLocation: File, solutionsLocation: File): List<TestJar> {
    val solutionClasses = loadLibs(solutionsLocation)
    return testJarsLocation.listFiles { _, t -> t.endsWith(".jar") }!!.parallelMapNotNull {
      with(transformerManager.transform(runtimeJarLoader.loadSourcesJar(createResourceContainer(it), solutionClasses))) {
        printMessages(
          logger,
          { "Test jar ${resourceContainer.name} has $warnings warnings and $errors errors!" },
          { "Test jar ${resourceContainer.name} has $warnings warnings!" },
        )
        logger.info("Loaded test jar ${it.name}")
        TestJarImpl(logger, it, compiledClasses, sourceFiles, solutionClasses).takeIf { errors == 0 }
      }
    }
  }

  private fun loadLibs(libsLocation: File): Map<String, CompiledClass> {
    return libsLocation.listFiles { _, t -> t.endsWith(".jar") }!!
      .asSequence()
      .map {
        val classStorage = runtimeJarLoader.loadCompiledJar(createResourceContainer(it))
        logger.info("Loaded lib jar ${it.name}")
        classStorage
      }
      .ifEmpty { listOf(mapOf<String, CompiledClass>()).asSequence() }
      .reduce { a, b -> a + b }
  }

  private fun loadSubmissionJars(submissionJarsLocation: File, libsLocation: File): List<JavaSubmission> {
    val libsClasspath = loadLibs(libsLocation)
    return submissionJarsLocation.listFiles { _, t -> t.endsWith(".jar") }!!.parallelMapNotNull {
      val transformed = with(runtimeJarLoader.loadSourcesJar(createResourceContainer(it), libsClasspath)) {
        val original = runtimeJarLoader.loadSourcesJar(createResourceContainer(it), libsClasspath)
        val transformed = transformerManager.transform(original)

        @Suppress("BlockingMethodInNonBlockingContext")
        fun JavaCompileResult.exportCompilationResult(suffix: String) {
          val file = File("compilation/${resourceContainer.nameWithoutExtension}-$suffix.jar")
          file.parentFile.ensure()
          JarOutputStream(file.outputStream().buffered()).use { jar ->
            for ((className, compiledClass) in compiledClasses) {
              val entry = JarEntry("${className.replace(".", "/")}.class")
              entry.time = System.currentTimeMillis()
              jar.putNextEntry(entry)
              jar.write(compiledClass.byteArray)
              jar.closeEntry()
            }
          }
        }
        original.exportCompilationResult("original")
        transformed.exportCompilationResult("transformed")
        transformed
      }
      with(transformed) {
        if (submissionInfo == null) {
          logger.error("$it does not have a submission-info.json! Skipping...")
          return@parallelMapNotNull null
        }
        printMessages(
          logger,
          { "Submission $submissionInfo(${resourceContainer.name}) has $warnings warnings and $errors errors!" },
          { "Submission $submissionInfo(${resourceContainer.name}) has $warnings warnings!" },
        )
        JavaSubmission(resourceContainer, submissionInfo, this, compiledClasses, sourceFiles)
          .apply { logger.info("Loaded submission jar $this") }
      }
    }
  }
}
