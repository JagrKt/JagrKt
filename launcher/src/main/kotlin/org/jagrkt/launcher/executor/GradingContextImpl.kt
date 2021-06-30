/*
 *   JagrKt - JagrKt.org
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

package org.jagrkt.launcher.executor

import org.jagrkt.api.testing.Submission

internal class GradingContextImpl : GradingContext {
  override val gradingRunning = mutableListOf<GradingProgress>()
  override val gradingFinished = mutableListOf<GradingElement>()
  override var completedCount = 0

  @Synchronized
  override fun beginGrading(submission: Submission): GradingProgress {
    return GradingProgressImpl(this, submission).also(gradingRunning::add)
  }

  @Synchronized
  fun endGrading(gradingProgress: GradingProgressImpl) {
    if (gradingRunning.remove(gradingProgress) && gradingFinished.add(gradingProgress.element)) {
      ++completedCount
    }
  }
}
