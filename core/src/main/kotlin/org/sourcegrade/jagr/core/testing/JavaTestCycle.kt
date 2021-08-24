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

import org.sourcegrade.jagr.api.testing.TestCycle

data class JavaTestCycle(
  private val rubricProviderClassNames: List<String>,
  private val submission: JavaSubmission,
  private val classLoader: ClassLoader,
) : TestCycle {
  private var jUnitResult: TestCycle.JUnitResult? = null
  override fun getRubricProviderClassNames(): List<String> = rubricProviderClassNames
  override fun getClassLoader(): ClassLoader = classLoader
  override fun getSubmission(): JavaSubmission = submission
  override fun getJUnitResult(): TestCycle.JUnitResult? = jUnitResult
  fun setJUnitResult(jUnitResult: TestCycle.JUnitResult?) {
    this.jUnitResult = jUnitResult
  }
}
