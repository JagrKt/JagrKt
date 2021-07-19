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

import org.sourcegrade.jagr.api.executor.ExecutionContext
import org.sourcegrade.jagr.api.testing.TestCycle


data class ExecutionContextImpl(
  private val anchor: StackTraceElement,
  private val stackTrace: Array<out StackTraceElement>,
  private val testCycle: TestCycle,
): ExecutionContext {
  override fun getAnchor(): StackTraceElement = anchor
  override fun getStackTrace(): Array<out StackTraceElement> = stackTrace
  override fun getTestCycle(): TestCycle = testCycle
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ExecutionContextImpl
    if (anchor != other.anchor) return false
    if (!stackTrace.contentEquals(other.stackTrace)) return false
    if (testCycle != other.testCycle) return false
    return true
  }

  override fun hashCode(): Int {
    var result = anchor.hashCode()
    result = 31 * result + stackTrace.contentHashCode()
    result = 31 * result + testCycle.hashCode()
    return result
  }
}
