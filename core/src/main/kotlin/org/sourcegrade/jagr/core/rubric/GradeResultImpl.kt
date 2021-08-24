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

package org.sourcegrade.jagr.core.rubric

import org.sourcegrade.jagr.api.rubric.GradeResult

data class GradeResultImpl(
  private val correctPoints: Int,
  private val incorrectPoints: Int,
  private val comments: List<String> = listOf(),
) : GradeResult {
  override fun getCorrectPoints(): Int = correctPoints
  override fun getIncorrectPoints(): Int = incorrectPoints
  override fun getComments(): List<String> = comments
}
