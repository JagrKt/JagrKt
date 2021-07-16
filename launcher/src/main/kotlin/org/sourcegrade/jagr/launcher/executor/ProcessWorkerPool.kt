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

class ProcessWorkerPool(
  private val concurrency: Int,
) : WorkerPool {

  open class Factory internal constructor(private val concurrency: Int) : WorkerPool.Factory {
    companion object Default : Factory(concurrency = 4)
    override fun create(): WorkerPool = ProcessWorkerPool(concurrency)
  }

  override val activeWorkers: MutableList<Worker> = mutableListOf()

  override fun createWorkers(maxCount: Int): List<Worker> {
    if (maxCount == 0) return emptyList()
    val workerCount = minOf(maxCount - concurrency, maxCount)
    return List(workerCount) { ProcessWorker() }
  }
}
