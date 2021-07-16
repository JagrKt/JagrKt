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

internal class RubricCollectorImpl(override val totalExpectedCount: Int) : MutableRubricCollector {
  override val gradingScheduled = mutableListOf<GradingJob>()
  override val gradingRunning = mutableListOf<GradingJob>()
  override val gradingFinished = mutableListOf<GradingResult>()
  override val remainingExpectedCount: Int
    get() = totalExpectedCount - gradingFinished.size

  @Synchronized
  override fun schedule(request: GradingRequest): GradingJob {
    return GradingJob(request)
      .also(gradingRunning::add)
      .also { job -> job.result.invokeOnCompletion { endGrading(job) } }
  }

  @Synchronized
  private fun endGrading(job: GradingJob) {
    check(job.result.isCompleted) { "$job is not finished grading" }
    gradingRunning.remove(job) && gradingFinished.add(job.result.getCompleted())
  }
}
