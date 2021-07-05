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

package org.jagrkt.common.transformer

import com.google.inject.Inject
import org.jagrkt.common.Config
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class CommonTransformer @Inject constructor(
  private val config: Config,
) : Transformer {
  override val name: String = "common-transformer"

  override fun transform(reader: ClassReader, writer: ClassWriter) {
    reader.accept(CommonClassVisitor(config, writer), 0)
  }
}

private class CommonClassVisitor(
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
    return CommonMethodVisitor(config, super.visitMethod(access, name, descriptor, signature, exceptions))
  }
}

private class CommonMethodVisitor(
  private val config: Config,
  methodVisitor: MethodVisitor,
) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
  override fun visitCode() {
    visitTimeoutIsns()
    super.visitCode()
  }

  override fun visitJumpInsn(opcode: Int, label: Label?) {
    visitTimeoutIsns()
    super.visitJumpInsn(opcode, label)
  }

  private fun visitTimeoutIsns() {
    if (!config.transformers.timeout.enabled) return
    visitMethodInsn(Opcodes.INVOKESTATIC, "org/jagrkt/common/executor/TimeoutHandler", "checkTimeout", "()V", false)
  }
}
