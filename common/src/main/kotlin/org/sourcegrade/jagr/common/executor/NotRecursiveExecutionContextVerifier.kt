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

import org.opentest4j.AssertionFailedError
import org.sourcegrade.jagr.api.executor.ExecutionContext
import org.sourcegrade.jagr.api.executor.ExecutionContextVerifier

object NotRecursiveExecutionContextVerifier : ExecutionContextVerifier {

  /**
   * @throws [AssertionFailedError] if [context] is determined to contain recursive elements.
   */
  override fun verify(context: ExecutionContext) {
    val elements = HashSet<StackTraceElement>(context.stackTrace.size)
    for (element in context.stackTrace) {
      if (element == context.anchor) break
      if (!elements.add(element)) {
        throw AssertionFailedError("Recursive invocation detected @ $element")
      }
    }
  }
}
