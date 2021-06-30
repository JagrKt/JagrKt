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

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

interface Transformer {
  val name: String
  fun transform(reader: ClassReader, writer: ClassWriter)
}

fun Transformer.transform(byteArray: ByteArray): ByteArray {
  val reader = ClassReader(byteArray)
  val writer = ClassWriter(reader, 0)
  transform(reader, writer)
  return writer.toByteArray()
}
