/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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

package org.sourcegrade.jagr.core.rubric.grader

import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.core.rubric.GradeResultImpl

class DescendingPriorityGrader(
    private val logger: Logger,
    vararg val graders: Grader,
) : Grader {

    override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult {
        // quick exit if only 0 or 1 grader
        if (graders.isEmpty()) {
            return GradeResult.of(criterion)
        }
        if (graders.size == 1) {
            return graders[0].grade(testCycle, criterion)
        }
        var minPoints = 0
        var maxPoints = 0
        val comments: MutableList<String> = mutableListOf()
        for (grader in graders) {
            val result = grader.grade(testCycle, criterion)
            minPoints += result.maxPoints
            maxPoints += result.minPoints
            comments += result.comments
            if (maxPoints >= criterion.maxPoints || maxPoints <= criterion.minPoints) break
        }
        if (minPoints < criterion.minPoints || maxPoints > criterion.maxPoints) {
            logger.error(
                """
Descending priority grader for submission ${testCycle.submission.info} has surpassed point limits
minPoints $minPoints should be >= ${criterion.minPoints} and maxPoints $maxPoints should be <= ${criterion.maxPoints}
for criterion ${criterion.shortDescription}! This is caused by a misconfigured rubric provider!
""".trim().replace('\n', ' '),
            )
        }
        return GradeResult.clamped(GradeResultImpl(maxPoints, minPoints, comments), criterion)
    }
}
