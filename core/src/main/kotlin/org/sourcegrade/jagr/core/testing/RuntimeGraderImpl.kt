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

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.RubricProvider
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.launcher.executor.RuntimeGrader
import org.sourcegrade.jagr.launcher.io.TestJar

class RuntimeGraderImpl @Inject constructor(
  private val logger: Logger,
  private val testers: Set<RuntimeTester>,
) : RuntimeGrader {
  override fun grade(tests: List<TestJar>, submission: Submission): Map<GradedRubric, String> {
    val gradedRubrics: MutableMap<GradedRubric, String> = mutableMapOf()
    for (test in tests) {
      for (tester in testers) {
        test as TestJarImpl // for now, I want to keep the API as small as possible
        tester.createTestCycle(test, submission)?.collectResults()?.also { gradedRubrics += it }
      }
    }
    return gradedRubrics
  }

  private fun TestCycle.collectResults(): Map<GradedRubric, String> {
    val result: MutableMap<GradedRubric, String> = mutableMapOf()
    for (rubricProviderName in rubricProviderClassNames) {
      val rubricProvider = try {
        // rubric provider must first be loaded again together with submission classes
        classLoader.loadClass(rubricProviderName).getConstructor().newInstance() as RubricProvider
      } catch (e: Throwable) {
        logger.error("Failed to initialize rubricProvider $rubricProviderName for $submission", e)
        continue
      }
      val exportFileName = rubricProvider.getOutputFileName(submission) ?: submission.info.toString()
      result[rubricProvider.rubric.grade(this)] = exportFileName
    }
    return result
  }
}
