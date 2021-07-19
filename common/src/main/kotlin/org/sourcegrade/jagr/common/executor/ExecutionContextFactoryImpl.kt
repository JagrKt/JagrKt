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

package org.sourcegrade.jagr.common.executor

import com.google.inject.Inject
import com.google.inject.Singleton
import org.sourcegrade.jagr.api.executor.ExecutionContext
import org.sourcegrade.jagr.api.executor.ExecutionContextVerifier
import org.sourcegrade.jagr.common.testing.TestCycleParameterResolver
import java.util.ArrayDeque
import java.util.Deque

@Singleton
class ExecutionContextFactoryImpl @Inject constructor(
  private val testCycleParameterResolver: TestCycleParameterResolver
) : ExecutionContext.Factory {

  private val stacks: InheritableThreadLocal<Deque<StackTraceVerifier>> = InheritableThreadLocal()

  fun getOrCreateStack(): Deque<StackTraceVerifier> {
    return stacks.get() ?: ArrayDeque<StackTraceVerifier>().also(stacks::set)
  }

  override fun runWithVerifiers(runnable: Runnable, vararg verifiers: ExecutionContextVerifier) {
    val stack = getOrCreateStack()
    val context = LightExecutionContext(
      Thread.currentThread().stackTrace[2],
      testCycleParameterResolver.value,
    )
    stack.push(verifiers.withAnchor(context))
    try {
      runnable.run()
    } finally {
      stack.pop()
    }
  }

}
