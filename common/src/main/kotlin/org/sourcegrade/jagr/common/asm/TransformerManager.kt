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

package org.sourcegrade.jagr.common.asm

import com.google.inject.Inject

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.sourcegrade.jagr.common.Config
import org.sourcegrade.jagr.common.compiler.java.CompiledClass
import org.sourcegrade.jagr.common.compiler.java.RuntimeJarLoader

class TransformerManager @Inject constructor(
  private val config: Config,
) {

  private inline fun transform(byteArray: ByteArray, visitor: (ClassWriter) -> ClassVisitor): ByteArray {
    val reader = ClassReader(byteArray)
    val writer = ClassWriter(reader, 0)
    reader.accept(visitor(writer), 0)
    return writer.toByteArray()
  }

  private inline fun Map<String, CompiledClass>.transform(visitor: (ClassWriter) -> ClassVisitor): Map<String, CompiledClass> {
    return mapValues { (className, compiledClass) ->
      CompiledClass.Existing(className, transform(compiledClass.byteArray, visitor))
    }
  }

  private fun Map<String, CompiledClass>.read(classVisitor: ClassVisitor) {
    return forEach { (_, compiledClass) ->
      compiledClass.reader.accept(classVisitor, 0)
    }
  }

  @Throws(BytecodeSecurityException::class)
  fun transformSubmission(result: RuntimeJarLoader.CompileJarResult): RuntimeJarLoader.CompileJarResult {
    result.compiledClasses.read(SubmissionClassVisitor(config))
    return result.copy(compiledClasses = result.compiledClasses.transform { CommonClassVisitor(config, it) })
  }

  fun transformGrader(result: RuntimeJarLoader.CompileJarResult): RuntimeJarLoader.CompileJarResult {
    return result.copy(compiledClasses = result.compiledClasses.transform { CommonClassVisitor(config, it) })
  }
}

class BytecodeSecurityException(message: String) : RuntimeException(message)
