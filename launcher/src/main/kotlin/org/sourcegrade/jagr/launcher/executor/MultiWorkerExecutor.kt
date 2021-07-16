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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sourcegrade.jagr.launcher.env.Environment

class MultiWorkerExecutor internal constructor(
  private val rubricCollector: MutableRubricCollector,
  private val environment: Environment,
  private val workerPool: WorkerPool,
) : Executor {

  open class Factory internal constructor(private val workerPoolFactory: WorkerPool.Factory) : Executor.Factory {
    companion object Default : Factory(ProcessWorkerPool.Factory)

    override fun create(rubricCollector: MutableRubricCollector, environment: Environment): Executor {
      return MultiWorkerExecutor(rubricCollector, environment, workerPoolFactory.create())
    }
  }

  class FactoryBuilder {
    var workerPoolFactory: WorkerPool.Factory = ProcessWorkerPool.Factory
    fun factory() = Factory(workerPoolFactory)
  }

  private val jobMutex = Mutex()
  private val scheduled = mutableListOf<GradingJob>()
  private val finished = mutableListOf<GradingJob>()

  override fun schedule(request: GradingRequest) {
    runBlocking {
      jobMutex.withLock {
        scheduled += rubricCollector.schedule(request)
      }
    }
  }

  private fun checkWorkers() {
    for (worker in workerPool.createWorkers(scheduled.size)) {
      val job = scheduled.removeFirst()
      job.result.invokeOnCompletion {
        finished += job
      }
      worker.assignJob(job)
    }
    // Removal from scheduled could technically happen directly in the completion listener
    // for job.result, but we want to avoid extra synchronization overhead and just handle
    // all finished jobs at once here (instead of each in their own thread).
    finished.forEach(scheduled::remove)
    finished.clear()
  }

  @Synchronized
  override suspend fun start() {
    val originalSystemOut = System.out
    System.setOut(ThreadAwarePrintStream(originalSystemOut))
    while (scheduled.isNotEmpty() && workerPool.activeWorkers.isNotEmpty()) {
      jobMutex.withLock {
        checkWorkers()
      }
      delay(timeMillis = 5)
    }
    System.setOut(originalSystemOut)
  }
}
