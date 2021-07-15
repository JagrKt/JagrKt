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

package org.sourcegrade.jagr.common.rubric

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.testing.TestCycle

data class GradedCriterionImpl(
  private val testCycle: TestCycle,
  private val grade: GradeResult,
  private val criterion: Criterion,
  private val childCriteria: List<GradedCriterion> = listOf(),
) : GradedCriterion {
  override fun getTestCycle(): TestCycle = testCycle
  override fun getGrade(): GradeResult = grade
  override fun getChildCriteria(): List<GradedCriterion> = childCriteria
  override fun getCriterion(): Criterion = criterion
}
