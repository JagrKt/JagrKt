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

package org.sourcegrade.jagr.common.rubric.grader

import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.common.rubric.GradeResultImpl

class DescendingPriorityGrader(
  private val logger: Logger,
  private vararg val graders: Grader,
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult {
    // quick exit if only 0 or 1 grader
    if (graders.isEmpty()) {
      return GradeResult.ofNone()
    }
    if (graders.size == 1) {
      return graders[0].grade(testCycle, criterion)
    }
    val maxPoints = criterion.maxPoints
    // negation is on purpose
    // criterion.minPoints is always negative but it needs to be positive for comparison later
    val minPoints = -criterion.minPoints
    var correctPoints = 0
    var incorrectPoints = 0
    val comments: MutableList<String> = mutableListOf()
    for (grader in graders) {
      val result = grader.grade(testCycle, criterion)
      correctPoints += result.correctPoints
      incorrectPoints += result.incorrectPoints
      comments += result.comments
      if (correctPoints + incorrectPoints >= maxPoints + minPoints) break
    }
    if (correctPoints + incorrectPoints > maxPoints + minPoints) {
      logger.error(
        "Descending priority grader for submission ${testCycle.submission.info} has surpassed point limits"
          + " correctPoints: $correctPoints (max $maxPoints) and incorrectPoints: $incorrectPoints (max $minPoints)"
          + " for criterion ${criterion.shortDescription}! This is caused by a misconfigured rubric provider!"
      )
    }
    return GradeResultImpl(correctPoints, incorrectPoints, comments)
  }
}
