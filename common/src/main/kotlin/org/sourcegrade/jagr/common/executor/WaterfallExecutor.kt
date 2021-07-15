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

package org.sourcegrade.jagr.common.executor

import kotlinx.coroutines.delay
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import java.util.LinkedList
import java.util.Queue
import kotlin.concurrent.thread

class WaterfallExecutor(
  concurrentWorkers: Int,
  private val maxTime: Long,
  private val logger: Logger,
) {

  private val scheduled: Queue<ScheduledTask> = LinkedList()
  private val completed: Queue<CompletedTask> = LinkedList()
  private val running: Array<RunningTask?> = arrayOfNulls(concurrentWorkers)

  @Synchronized
  fun schedule(name: String, block: () -> Unit) {
    scheduled.add(ScheduledTask(name, block))
  }

  suspend fun execute() {
    val originalSystemOut = System.out
    System.setOut(ThreadAwarePrintStream(originalSystemOut))
    val progressBar = ProgressBar()
    for (task in scheduled) {
      task.progressElement = progressBar.createElement(task.name)
    }
    val mxBean = ManagementFactory.getThreadMXBean()
    while (true) {
      var done = true
      for (i in running.indices) {
        // remove task from scheduling if finished
        running[i]?.also { task ->
          val userTime = mxBean.getThreadCpuTime(task.thread.id)
          // userTime values:
          // == -1 : Thread finished
          // > 0 : Thread running
          if (userTime == -1L) {
            task.progressElement.complete()
            completed.add(CompletedTask(task.name))
            running[i] = null
          } else if (maxTime > 0 && userTime / 1_000_000L > maxTime) {
            task.thread.priority = Thread.MIN_PRIORITY
            // Thread#stop() is deprecated because it is "unsafe".
            // However, it is exactly what we need in this case as
            // it is impossible (or at least impractical) to check
            // a variable during the execution of these threads.
            @Suppress("deprecation")
            task.thread.stop()
            progressBar.clear()
            logger.error("${task.name} timed out after ${maxTime}ms")
            task.progressElement.complete()
            completed.add(CompletedTask(task.name, timedOut = true))
            running[i] = null
          }
        }

        // schedule new task if possible
        if (running[i] == null) {
          running[i] = scheduled.poll()?.let { task ->
            val thread = thread(
              isDaemon = true,
              name = GRADING_THREAD_PREFIX + task.name,
              priority = 3,
            ) {
              task.block()
            }
            RunningTask(task.name, task.progressElement, thread)
          }
        }

        // check if task still running
        if (running[i] != null) {
          done = false
        }
      }
      // exit loop if no more tasks
      if (done) {
        break
      }
      progressBar.print()
      delay(timeMillis = 5)
    }
    System.setOut(originalSystemOut)
    progressBar.clear()
  }

  private data class ScheduledTask(
    val name: String,
    val block: () -> Unit,
  ) {
    lateinit var progressElement: ProgressBar.ProgressElement
  }

  private data class RunningTask(
    val name: String,
    val progressElement: ProgressBar.ProgressElement,
    val thread: Thread,
  )

  private data class CompletedTask(
    val name: String,
    val timedOut: Boolean = false,
  )
}
