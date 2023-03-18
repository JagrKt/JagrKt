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

package org.sourcegrade.jagr.core.testing

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.RubricProvider
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.launcher.executor.RuntimeGrader
import org.sourcegrade.jagr.launcher.io.GraderJar

class RuntimeGraderImpl @Inject constructor(
    private val logger: Logger,
    private val tester: RuntimeTester,
    private val fallbackRuntimeTester: FallbackRuntimeTester,
) : RuntimeGrader {
    override fun grade(graders: List<GraderJar>, submission: Submission): Map<GradedRubric, String> {
        throw UnsupportedOperationException("Not implemented any more, use grade(grader, submission) instead")
        /*
        val gradedRubrics: MutableMap<GradedRubric, String> = mutableMapOf()
        for (grader in graders) {
            grader as GraderJarImpl // for now, I want to keep the API as small as possible
            tester.createTestCycle(grader, submission)?.collectResults()?.also { gradedRubrics += it }
        }
        return gradedRubrics
        */
    }

    override fun grade(grader: GraderJar, submission: Submission): GradedRubric {
        grader as GraderJarImpl // for now, I want to keep the API as small as possible

        return tester.createTestCycle(grader, submission)?.collectResults()?.first
            ?: throw IllegalStateException("Failed to grade submission $submission with grader $grader")
    }

    override fun gradeFallback(graders: List<GraderJar>, submission: Submission): Map<GradedRubric, String> {
        return graders.asSequence()
            .mapNotNull { fallbackRuntimeTester.createTestCycle(it as GraderJarImpl, submission)?.collectResults() }
            .fold(emptyMap()) { a, b -> a + b }
    }

    private fun TestCycle.collectResults(): Pair<GradedRubric, String>? {
        val rubricProvider = try {
            // rubric provider must first be loaded again together with submission classes
            classLoader.loadClass(rubricProviderName).getConstructor().newInstance() as RubricProvider
        } catch (e: Throwable) {
            logger.error("Failed to initialize rubric provider $rubricProviderName for $submission", e)
            return null
        }
        val exportFileName = rubricProvider.getOutputFileName(submission) ?: submission.info.toString()
        return rubricProvider.rubric.grade(this) to exportFileName
    }
}
