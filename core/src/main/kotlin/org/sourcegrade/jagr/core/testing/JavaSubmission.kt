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

package org.sourcegrade.jagr.core.testing

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import org.sourcegrade.jagr.api.testing.SourceFile
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.core.compiler.java.JavaCompileResult
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.read
import org.sourcegrade.jagr.launcher.io.write

data class JavaSubmission(
  private val info: SubmissionInfo,
  private val compileResult: JavaCompileResult,
  val runtimeLibraries: RuntimeResources,
) : Submission {
  companion object Factory : SerializerFactory<JavaSubmission> {
    override fun read(input: ByteArrayDataInput, scope: SerializationScope): JavaSubmission =
      JavaSubmission(scope[SubmissionInfo::class], input.read(scope), scope[RuntimeResources.Base])

    override fun write(obj: JavaSubmission, output: ByteArrayDataOutput, scope: SerializationScope) {
      output.write(obj.compileResult, scope)
    }
  }

  override fun getInfo(): SubmissionInfo = info
  override fun getCompileResult(): JavaCompileResult = compileResult
  override fun getSourceFile(fileName: String): SourceFile? = compileResult.sourceFiles[fileName]
  override fun toString(): String = "$info(${compileResult.container.name})"
}
