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

class GradeResultFactoryImpl : GradeResult.Factory {
  private val none = GradeResultImpl(0, 0);
  override fun ofCorrect(points: Int): GradeResult = GradeResultImpl(points, 0)
  override fun ofIncorrect(points: Int): GradeResult = GradeResultImpl(0, points)
  override fun ofNone(): GradeResult = none
  override fun of(correctPoints: Int, incorrectPoints: Int): GradeResult = GradeResultImpl(correctPoints, incorrectPoints)
  override fun of(correctPoints: Int, incorrectPoints: Int, comment: String): GradeResult {
    return GradeResultImpl(correctPoints, incorrectPoints, listOf(comment))
  }

  override fun of(grade: GradeResult, vararg otherGrades: GradeResult): GradeResult = of(grade, otherGrades.asIterable())

  override fun of(grade: GradeResult, otherGrades: Iterable<GradeResult>): GradeResult {
    var correctPoints = grade.correctPoints
    var incorrectPoints = grade.incorrectPoints
    for (otherGrade in otherGrades) {
      correctPoints += otherGrade.correctPoints
      incorrectPoints += otherGrade.incorrectPoints
    }
    return GradeResultImpl(correctPoints, incorrectPoints)
  }

  override fun ofMax(criterion: Criterion): GradeResult = ofCorrect(criterion.maxPoints - criterion.minPoints)
  override fun ofMin(criterion: Criterion): GradeResult = ofIncorrect(criterion.maxPoints - criterion.minPoints)

  override fun withComments(grade: GradeResult, comments: Iterable<String>): GradeResult {
    return GradeResultImpl(grade.correctPoints, grade.incorrectPoints, grade.comments + comments)
  }
}
