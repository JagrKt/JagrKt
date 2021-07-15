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

package org.sourcegrade.jagr.common.export.submission

import com.google.common.collect.FluentIterable
import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import com.google.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.common.ensure
import org.sourcegrade.jagr.common.testing.JavaSubmission
import org.sourcegrade.jagr.common.testing.SubmissionInfoImpl
import org.sourcegrade.jagr.common.testing.TestJar
import org.sourcegrade.jagr.common.usePrintWriterSafe
import org.sourcegrade.jagr.common.writeStream
import org.sourcegrade.jagr.common.writeTextSafe
import java.io.File
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

@Singleton // stateful because of submissionMap
class GradleSubmissionExporter @Inject constructor(
  private val logger: Logger,
) : SubmissionExporter {
  override val name: String = "gradle"

  private val submissionMap: MutableMap<String?, MutableList<String>> = mutableMapOf()

  private fun File.writeGradleResource(
    classLoader: ClassLoader = ClassLoader.getSystemClassLoader(),
    resource: String,
    targetDir: String = "",
    targetName: String = resource
  ) {
    classLoader.getResourceAsStream("org/gradle/$resource")?.also {
      resolve(targetDir + targetName).writeStream { it }
    } ?: logger.error("Could not read gradle resource: $resource")
  }

  @Suppress("UnstableApiUsage")
  private fun File.writeSkeleton() {
    val classLoader = ClassLoader.getSystemClassLoader()
    val wrapperClasses: Set<ClassPath.ClassInfo> = FluentIterable.from(ClassPath.from(classLoader).resources)
      .filter(ClassPath.ClassInfo::class.java)
      .filter { it!!.packageName.startsWith("org.gradle") }
      .toSet()
    val wrapperDir = resolve("gradle/wrapper").ensure(logger, false) ?: return
    val manifest = Manifest()
    manifest.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
    manifest.mainAttributes[Attributes.Name.IMPLEMENTATION_TITLE] = "Gradle Wrapper"
    JarOutputStream(wrapperDir.resolve("gradle-wrapper.jar").outputStream(), manifest).use { jar ->
      for (wrapperClass in wrapperClasses) {
        val className = "${wrapperClass.name.replace('.', '/')}.class"
        val classStream = classLoader.getResourceAsStream(className)
        if (classStream == null) { // opened over *9* years ago, sigh https://youtrack.jetbrains.com/issue/KT-1436
          logger.error("Unable to read gradle wrapper class $className")
          continue
        }
        val entry = JarEntry(className)
        jar.putNextEntry(entry)
        jar.write(classStream.use { it.readBytes() })
        jar.closeEntry()
      }
    }
    writeGradleResource(classLoader, resource = "gradlew")
    writeGradleResource(classLoader, resource = "gradlew.bat")
    writeGradleResource(classLoader, resource = "build.gradle.kts_", targetName = "build.gradle.kts")
    writeGradleResource(classLoader, resource = "gradle-wrapper.properties", targetDir = "gradle/wrapper/")
  }

  override fun initialize(directory: File, testJar: TestJar?) {
    directory.writeSkeleton()
  }

  private fun writeSettings(directory: File, name: String?) {
    directory.resolve("settings.gradle.kts").usePrintWriterSafe(logger) {
      appendLine("rootProject.name = \"${directory.name}\"")
      submissionMap[name]?.forEach { submissionName ->
        appendLine("include(\"$submissionName\")")
      }
    }
  }

  override fun finalize(directory: File, testJar: TestJar?) {
    writeSettings(directory, testJar?.name)
  }

  override fun export(submission: Submission, directory: File, testJar: TestJar?) {
    if (submission !is JavaSubmission) return
    val submissionName = submission.info.toString()
    val file = directory.resolve(submissionName).ensure(logger, false) ?: return
    val mainResources = file.resolve("src/main/resources").ensure(logger, false) ?: return
    val mainSource = file.resolve("src/main/java").ensure(logger, false) ?: return
    val testSource = file.resolve("src/test/java").ensure(logger, false) ?: return
    (submission.info as? SubmissionInfoImpl)?.also {
      mainResources.resolve("submission-info.json").writeText(Json.encodeToString(it))
    }
    // sourceFile.name starts with a / and needs to be converted to a relative path
    submission.sourceFiles.forEach { (_, sourceFile) ->
      mainSource.resolve(".${sourceFile.name}").writeTextSafe(sourceFile.content, logger)
    }
    testJar?.sourceFiles?.forEach { (_, sourceFile) ->
      testSource.resolve(".${sourceFile.name}").writeTextSafe(sourceFile.content, logger)
    }
    submissionMap.computeIfAbsent(testJar?.name) { mutableListOf() }.add(submissionName)
  }
}
