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

package org.jagrkt.common.testing

import org.jagrkt.api.testing.CompileResult
import org.jagrkt.api.testing.SourceFile
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.SubmissionInfo
import org.jagrkt.common.compiler.java.CompiledClass
import org.jagrkt.common.compiler.java.JavaSourceFile
import java.io.File

data class JavaSubmission(
  val file: File,
  private val info: SubmissionInfo,
  private val compileResult: CompileResult,
  val compiledClasses: Map<String, CompiledClass>,
  val sourceFiles: Map<String, JavaSourceFile>,
) : Submission {
  override fun getInfo(): SubmissionInfo = info
  override fun getCompileResult(): CompileResult = compileResult
  override fun getSourceFile(fileName: String): SourceFile? = sourceFiles[fileName]
  override fun toString(): String = "$info(${file.name})"
}
