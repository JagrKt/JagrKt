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

package org.sourcegrade.jagr.launcher.executor

/**
 * A worker is essentially a grading-specialized [Runnable] that provides
 *
 * A worker may only be used once nad u
 */
interface Worker {
  val job: GradingJob?
  val status: WorkerStatus
  val userTime: Long // TODO: maybe elapsedTime instead?

  fun assignJob(job: GradingJob)

  /**
   * Attempts to kill this worker and stop grading this job to free up resources.
   *
   * The goal of this method is to free up system resources from grading futile submissions. While a worker is normally able to
   * stop properly, there are unfortunately cases where this will not work.
   */
  fun kill()
}

enum class WorkerStatus {
  /**
   * Not ready to receive job
   */
  PREPARING,

  /**
   * Ready to receive job
   */
  READY,

  /**
   * Processing a job
   */
  RUNNING,

  /**
   * Finished processing a job
   */
  FINISHED,
}
