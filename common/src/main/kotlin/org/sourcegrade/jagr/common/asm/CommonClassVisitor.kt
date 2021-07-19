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

package org.sourcegrade.jagr.common.asm

import org.objectweb.asm.*
import org.sourcegrade.jagr.common.Config

class CommonClassVisitor(
  private val config: Config,
  writer: ClassWriter,
) : ClassVisitor(Opcodes.ASM9, writer) {
  override fun visitMethod(
    access: Int,
    name: String?,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor {
    return CommonMethodVisitor(
      super.visitMethod(access, name, descriptor, signature, exceptions)
    )
  }

  private inner class CommonMethodVisitor(
    methodVisitor: MethodVisitor?,
  ) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
    override fun visitCode() {
      visitExecutionContextInsn()
      visitTimeoutIsns()
      super.visitCode()
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
      visitTimeoutIsns()
      super.visitJumpInsn(opcode, label)
    }

    private fun visitExecutionContextInsn() {
      if (!config.instrumentations.notRecursiveBytecode.enabled) return
      visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "jagrinternal/instrumentation/ExecutionContextHandler",
        "checkExecutionContext",
        "()V",
        false
      )
    }

    private fun visitTimeoutIsns() {
      if (!config.instrumentations.timeoutBytecode.enabled) return
      visitMethodInsn(Opcodes.INVOKESTATIC, "jagrinternal/instrumentation/TimeoutHandler", "checkTimeout", "()V", false)
    }
  }
}


