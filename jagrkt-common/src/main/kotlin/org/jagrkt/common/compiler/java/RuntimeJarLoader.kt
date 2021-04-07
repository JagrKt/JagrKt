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

package org.jagrkt.common.compiler.java

import com.google.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jagrkt.common.testing.SubmissionInfoImpl
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.Logger
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.jar.JarFile
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

class RuntimeJarLoader @Inject constructor(
  private val logger: Logger,
) {

  fun loadCompiledJar(file: File): Map<String, CompiledClass> {
    val jarFile = JarFile(file)
    val classStorage: MutableMap<String, CompiledClass> = mutableMapOf()
    for (entry in jarFile.entries()) {
      when {
        entry.isDirectory -> continue
        entry.name.endsWith(".class") -> {
          val className = entry.name.replace('/', '.').substring(0, entry.name.length - 6)
          classStorage[className] = CompiledClass.Existing(className, jarFile.getInputStream(entry).use { it.readBytes() })
        }
        entry.name.endsWith("MANIFEST.MF") -> { // ignore
        }
        else -> logger.warn("$file jar entry $entry is not a java class file!")
      }
    }
    return classStorage
  }

  fun loadSourcesJar(file: File, runtimeClassPath: Map<String, CompiledClass> = mapOf()): CompileJarResult {
    val jarFile = JarFile(file)
    val sourceFiles: MutableMap<String, JavaSourceFile> = mutableMapOf()
    var submissionInfo: SubmissionInfoImpl? = null
    for (entry in jarFile.entries()) {
      when {
        entry.isDirectory -> continue
        entry.name == "submission-info.json" -> {
          submissionInfo = try {
            Json.decodeFromString<SubmissionInfoImpl>(jarFile.getInputStream(entry).bufferedReader().use { it.readText() })
          } catch (e: Throwable) {
            logger.error("$file has invalid submission-info.json", e)
            return CompileJarResult(file)
          }
        }
        entry.name.endsWith(".java") -> {
          val className = entry.name.replace('/', '.').substring(0, entry.name.length - 5)
          val sourceFile = JavaSourceFile(className, entry.name, jarFile.getInputStream(entry))
          sourceFiles[entry.name] = sourceFile
        }
        entry.name.endsWith("MANIFEST.MF") -> { // ignore
        }
        else -> logger.warn("$file jar entry $entry is not a java source file!")
      }
    }
    if (sourceFiles.isEmpty()) {
      // no source files, skip compilation task
      return CompileJarResult(file, submissionInfo)
    }
    val compiledClasses: MutableMap<String, CompiledClass.Runtime> = mutableMapOf()
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
      return CompileJarResult(file, submissionInfo, compiledClasses, sourceFiles, messages, warnings, errors, other)
    }
    return CompileJarResult(file, submissionInfo, compiledClasses, sourceFiles)
  }

  private fun Map<String, CompiledClass>.linkSource(sourceFiles: Map<String, JavaSourceFile>) {
    for ((_, compiledClass) in this) {
      with(compiledClass.reader) {
        val packageName = className.substring(0, className.lastIndexOf('/'))
        accept(object : ClassVisitor(Opcodes.ASM9) {
          override fun visitSource(source: String?, debug: String?) {
            if (source == null) return
            compiledClass.source = sourceFiles["$packageName/$source"]
          }
        }, ClassReader.SKIP_CODE)
      }
    }
  }

  data class CompileJarResult(
    val file: File,
    val submissionInfo: SubmissionInfoImpl? = null,
    val compiledClasses: Map<String, CompiledClass.Runtime> = mapOf(),
    val sourceFiles: Map<String, JavaSourceFile> = mapOf(),
    val messages: List<String> = listOf(),
    val warnings: Int = 0,
    val errors: Int = 0,
    val other: Int = 0,
  ) {
    fun printMessages(logger: Logger, lazyError: () -> String, lazyWarning: () -> String) {
      when {
        errors > 0 -> {
          logger.error(lazyError())
          for (message in messages) {
            logger.error(message)
          }
        }
        warnings > 0 -> logger.warn(lazyWarning())
      }
    }
  }
}
