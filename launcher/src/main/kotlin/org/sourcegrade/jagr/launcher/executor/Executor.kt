/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.launcher.executor

import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.io.TestJar
import java.time.Instant

interface Executor {
  fun schedule(request: GradingRequest)
  suspend fun start()
  interface Factory {
    fun create(rubricCollector: MutableRubricCollector, environment: Environment): Executor
  }
}

fun interface RuntimeGrader {
  fun grade(tests: List<TestJar>, submission: Submission): Map<GradedRubric, String>
}

fun RuntimeGrader.grade(request: GradingRequest) = grade(request.testJars, request.submission)

fun RuntimeGrader.grade(job: GradingJob) {
  val startTime = Instant.now()
  val rubrics = grade(job.request)
  val endTime = Instant.now()
  job.result.complete(GradingResult(startTime, endTime, job.request, rubrics))
}
