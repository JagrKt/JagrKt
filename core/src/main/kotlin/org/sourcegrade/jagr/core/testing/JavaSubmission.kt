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

import org.sourcegrade.jagr.api.testing.CompileResult
import org.sourcegrade.jagr.api.testing.SourceFile
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.core.compiler.java.CompiledClass
import org.sourcegrade.jagr.core.compiler.java.JavaSourceFile
import org.sourcegrade.jagr.launcher.io.ResourceContainer

data class JavaSubmission(
  val resourceContainer: ResourceContainer,
  private val info: SubmissionInfo,
  private val compileResult: CompileResult,
  val compiledClasses: Map<String, CompiledClass>,
  val sourceFiles: Map<String, JavaSourceFile>,
  val runtimeClassPath: Map<String, CompiledClass>,
  val resources: Map<String, ByteArray>
) : Submission {
  override fun getInfo(): SubmissionInfo = info
  override fun getCompileResult(): CompileResult = compileResult
  override fun getSourceFile(fileName: String): SourceFile? = sourceFiles[fileName]
  override fun toString(): String = "$info(${resourceContainer.name})"
}
