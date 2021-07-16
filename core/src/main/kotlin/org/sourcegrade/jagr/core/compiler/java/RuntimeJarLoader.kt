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

package org.sourcegrade.jagr.core.compiler.java

import com.google.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.Logger
import org.sourcegrade.jagr.core.compiler.readEncoded
import org.sourcegrade.jagr.core.testing.SubmissionInfoImpl
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

class RuntimeJarLoader @Inject constructor(
  private val logger: Logger,
) {

  fun loadCompiledJar(container: ResourceContainer): Map<String, CompiledClass> {
    val classStorage: MutableMap<String, CompiledClass> = mutableMapOf()
    for (resource in container) {
      when {
        resource.name.endsWith(".class") -> {
          val className = resource.name.replace('/', '.').substring(0, resource.name.length - 6)
          classStorage[className] = CompiledClass.Existing(className, resource.inputStream.use { it.readBytes() })
        }
        resource.name.endsWith("MANIFEST.MF") -> { // ignore
        }
        else -> logger.warn("$container resource ${resource.name} is not a java class file!")
      }
    }
    return classStorage
  }

  fun loadSourcesJar(container: ResourceContainer, runtimeClassPath: Map<String, CompiledClass> = mapOf()): JavaCompileResult {
    val sourceFiles: MutableMap<String, JavaSourceFile> = mutableMapOf()
    var submissionInfo: SubmissionInfoImpl? = null
    for (resource in container) {
      when {
        resource.name == "submission-info.json" -> {
          submissionInfo = try {
            Json.decodeFromString<SubmissionInfoImpl>(resource.inputStream.bufferedReader().use { it.readText() })
          } catch (e: Throwable) {
            logger.error("$resource has invalid submission-info.json", e)
            return JavaCompileResult(container)
          }
        }
        resource.name.endsWith(".java") -> {
          val className = resource.name.replace('/', '.').substring(0, resource.name.length - 5)
          val content = resource.inputStream.use { it.readEncoded() }
          val sourceFile = JavaSourceFile(className, resource.name, content)
          sourceFiles[resource.name] = sourceFile
        }
        resource.name.endsWith("MANIFEST.MF") -> { // ignore
        }
        else -> logger.warn("$container resource ${resource.name} is not a java source file!")
      }
    }
    if (sourceFiles.isEmpty()) {
      // no source files, skip compilation task
      return JavaCompileResult(container, submissionInfo)
    }
    val compiledClasses: MutableMap<String, CompiledClass> = mutableMapOf()
    val collector = DiagnosticCollector<JavaFileObject>()
    val compiler = ToolProvider.getSystemJavaCompiler()
    val fileManager = ExtendedStandardJavaFileManager(
      compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8),
      runtimeClassPath,
      compiledClasses,
    )
    val result = compiler.getTask(null, fileManager, collector, null, null, sourceFiles.values).call()
    compiledClasses.linkSource(sourceFiles)
    if (!result || collector.diagnostics.isNotEmpty()) {
      val messages = mutableListOf<String>()
      var warnings = 0
      var errors = 0
      var other = 0
      for (diag in collector.diagnostics) {
        when (diag.kind) {
          Diagnostic.Kind.NOTE,
          Diagnostic.Kind.MANDATORY_WARNING,
          Diagnostic.Kind.WARNING,
          -> ++warnings
          Diagnostic.Kind.ERROR,
          -> ++errors
          else -> ++other
        }
        messages += "${diag.source.name}:${diag.lineNumber} ${diag.kind} :: ${diag.getMessage(Locale.getDefault())}"
      }
      return JavaCompileResult(container, submissionInfo, compiledClasses, sourceFiles, messages, warnings, errors, other)
    }
    return JavaCompileResult(container, submissionInfo, compiledClasses, sourceFiles)
  }

  private fun Map<String, CompiledClass>.linkSource(sourceFiles: Map<String, JavaSourceFile>) {
    for ((_, compiledClass) in this) {
      with(compiledClass.reader) {
        val packageName = with(className) {
          lastIndexOf('/').let {
            if (it == -1) null else substring(0, it)
          }
        }
        accept(object : ClassVisitor(Opcodes.ASM9) {
          override fun visitSource(source: String?, debug: String?) {
            if (source == null) return
            compiledClass.source = if (packageName == null) {
              sourceFiles["$source"]
            } else {
              sourceFiles["$packageName/$source"]
            }
          }
        }, ClassReader.SKIP_CODE)
      }
    }
  }
}
