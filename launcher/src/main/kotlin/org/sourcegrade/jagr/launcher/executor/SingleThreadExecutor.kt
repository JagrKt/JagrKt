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

import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.runtimeGrader

class SingleThreadExecutor private constructor(
  private val rubricCollector: MutableRubricCollector,
  private val environment: Environment,
) : Executor {

  object Factory : Executor.Factory {
    override fun create(rubricCollector: MutableRubricCollector, environment: Environment): SingleThreadExecutor {
      return SingleThreadExecutor(rubricCollector, environment)
    }
  }

  private val runtimeGrader = environment.runtimeGrader
  private val scheduled = mutableListOf<GradingJob>()

  @Synchronized
  override fun schedule(request: GradingRequest) {
    scheduled += rubricCollector.schedule(request)
  }

  @Synchronized
  override suspend fun start() {
    // scheduled may not be modified during this method invocation
    // synchronization should take care of that
    scheduled.forEach(runtimeGrader::grade)
    scheduled.clear()
  }
}
