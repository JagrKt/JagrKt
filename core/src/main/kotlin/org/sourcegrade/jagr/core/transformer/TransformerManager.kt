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

package org.sourcegrade.jagr.core.transformer

import com.google.inject.Inject
import org.sourcegrade.jagr.core.compiler.java.CompiledClass
import org.sourcegrade.jagr.core.compiler.java.JavaCompileResult

class TransformerManager @Inject constructor(
  private val commonTransformer: CommonTransformer,
) {

  private fun MutableMap<String, CompiledClass>.transform(): MutableMap<String, CompiledClass> {
    for ((className, compiledClass) in this) {
      this[className] = CompiledClass.Existing(className, commonTransformer.transform(compiledClass.byteArray))
    }
    return this
  }

  fun transform(result: JavaCompileResult): JavaCompileResult {
    return result.copy(compiledClasses = result.compiledClasses.toMutableMap().transform())
  }
}
