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

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL
import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.rubric.JUnitTestRef
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.core.rubric.message

class TestAwareGraderImpl(
    private val graderPassed: Grader,
    private val graderFailed: Grader,
    val requirePass: Map<JUnitTestRef, String?>,
    val requireFail: Map<JUnitTestRef, String?>,
    private val commentIfFailed: String?,
) : Grader {

    override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult {
        val testResults = (testCycle.jUnitResult ?: return GradeResult.ofNone()).statusListener.testResults
        fun Map<JUnitTestRef, String?>.must(predicate: (TestExecutionResult) -> Boolean): GradeResult? {
            val comments: MutableList<String> = mutableListOf()
            var failed = false
            for ((testRef, comment) in this) {
                val testExecutionResult = testRef[testResults]
                if (testExecutionResult == null || !predicate(testExecutionResult)) {
                    failed = true
                    // a comment supplied to requirePass or requireFail overrides the default comment from the result's throwable
                    if (comments.isEmpty()) {
                        (comment ?: testExecutionResult?.message)?.also { comments += it }
                    }
                }
            }
            return if (failed) {
                // general comment goes after more specific test comments
                commentIfFailed?.also { comments += it }
                GradeResult.withComments(graderFailed.grade(testCycle, criterion), comments)
            } else {
                null
            }
        }
        requirePass.must { it.status == SUCCESSFUL }?.also { return it }
        requireFail.must { it.status == FAILED }?.also { return it }
        return graderPassed.grade(testCycle, criterion)
    }
}
