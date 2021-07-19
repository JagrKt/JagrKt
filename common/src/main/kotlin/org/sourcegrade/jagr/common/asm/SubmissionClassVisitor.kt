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


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.sourcegrade.jagr.common.Config

class SubmissionClassVisitor(private val config: Config) : ClassVisitor(Opcodes.ASM9) {
  private lateinit var className: String
  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    className = name
  }

  override fun visitMethod(
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor {
    return SubmissionMethodVisitor(className, name, descriptor)
  }

  private inner class SubmissionMethodVisitor(
    private val callerClass: String,
    private val callerMethod: String,
    private val callerDescriptor: String
  ) : MethodVisitor(Opcodes.ASM9) {
    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
      // println("Normal $owner.$name$descriptor")
      // verify(callerClass, callerMethod, callerDescriptor, owner, name, descriptor)

      if (config.instrumentations.systemExitRemoveBytecode.enabled && owner == "java/lang/System" && name == "exit") {
        println("System.exit DETECTED!")
        throw BytecodeSecurityException("System.exit DETECTED!!!!1111elf")
      }
    }
  }
}


private val illegal: List<String> = mutableListOf(
  "org/jagrkt", // the autograder should definitely not be referenced in submissions
  "java/lang/reflect", // reflection is bad
  "java/lang/Runtime", // runtime.exec and other stuff that shouldn't be touched
  "javax/tools", // javac
)

private fun verify(
  callerClass: String,
  callerMethod: String,
  callerDescriptor: String,
  owner: String,
  name: String,
  descriptor: String
) {
  if (illegal.any { owner.contains(it) || descriptor.contains(it) }) {
    println("Illegal usage in $callerClass.$callerMethod$callerDescriptor :: $owner.$name$descriptor")
  }
}
