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

package org.sourcegrade.jagr.core.testing

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.api.testing.TestStatusListener
import java.util.Collections

class TestStatusListenerImpl(
  private val logger: Logger,
) : TestExecutionListener, TestStatusListener {

  private val testResults: MutableMap<TestIdentifier, TestExecutionResult> = mutableMapOf()
  private val linkageErrors: MutableSet<Pair<String?, String>> = LinkedHashSet()

  override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
    testResults[testIdentifier] = testExecutionResult
    testExecutionResult.throwable.orElse(null)?.also { throwable ->
      if (throwable is LinkageError) {
        linkageErrors.add(throwable::class.simpleName to throwable.message + " @ " + throwable.stackTrace.firstOrNull())
      }
    }
  }

  fun logLinkageErrors(info: SubmissionInfo) {
    for (errorMessage in linkageErrors) {
      logger.error("Linkage error @ $info :: $errorMessage")
    }
  }

  override fun getTestResults(): Map<TestIdentifier, TestExecutionResult> = Collections.unmodifiableMap(testResults)
}
