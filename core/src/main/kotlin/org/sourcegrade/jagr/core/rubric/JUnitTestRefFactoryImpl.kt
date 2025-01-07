/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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

package org.sourcegrade.jagr.core.rubric

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestIdentifier
import org.opentest4j.AssertionFailedError
import org.sourcegrade.jagr.api.rubric.JUnitTestRef
import java.lang.reflect.Method
import java.util.concurrent.Callable

class JUnitTestRefFactoryImpl @Inject constructor(
    private val logger: Logger,
) : JUnitTestRef.Factory {

    override fun ofClass(clazz: Class<*>): JUnitTestRef = Default(ClassSource.from(clazz))

    override fun ofClass(clazzSupplier: Callable<Class<*>>): JUnitTestRef {
        return try {
            ofClass(clazzSupplier.call())
        } catch (e: Throwable) {
            logger.error("Could not create JUnitClassTestRef :: ${e::class.qualifiedName}: ${e.message}")
            JUnitNoOpTestRef
        }
    }

    override fun ofMethod(method: Method): JUnitTestRef = Default(MethodSource.from(method))

    override fun ofMethod(methodSupplier: Callable<Method>): JUnitTestRef {
        return try {
            ofMethod(methodSupplier.call())
        } catch (e: Throwable) {
            logger.error("Could not create JUnitMethodTestRef :: ${e::class.qualifiedName}: ${e.message}")
            JUnitNoOpTestRef
        }
    }

    override fun and(vararg testRefs: JUnitTestRef): JUnitTestRef = And(*testRefs)
    override fun or(vararg testRefs: JUnitTestRef): JUnitTestRef = Or(*testRefs)
    override fun not(testRef: JUnitTestRef): JUnitTestRef = Not(testRef)

    object JUnitNoOpTestRef : JUnitTestRef {
        class NoOpFailedError : AssertionFailedError("Failed to evaluate test")

        override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult =
            TestExecutionResult.aborted(NoOpFailedError())
    }

    class Default(val testSource: TestSource) : JUnitTestRef {
        inner class TestNotFoundError : AssertionFailedError("Test result not found")

        override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult {
            val applicableTestResults: Map<TestIdentifier, TestExecutionResult> = testResults
                .filter { (id, _) -> testSource == id.source.orElse(null) }
                .toMap()
            if (applicableTestResults.isEmpty()) {
                return TestExecutionResult.failed(TestNotFoundError())
            } else {
                // first search for a failed container, then a failed child test
                val failedContainer = applicableTestResults.entries.find { (id, result) ->
                    result.status == TestExecutionResult.Status.FAILED && id.type.isContainer
                }
                if (failedContainer != null) {
                    return failedContainer.value
                }
                val failedTests = applicableTestResults.entries.filter { (_, result) ->
                    result.status == TestExecutionResult.Status.FAILED
                }
                return if (failedTests.isNotEmpty()) {
                    failedTests.first().value
                } else {
                    TestExecutionResult.successful()
                }
            }
        }
    }

    class And(private vararg val testRefs: JUnitTestRef) : JUnitTestRef {
        override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult =
            testRefs.execute(testResults, Collection<*>::isNotEmpty)
    }

    class Or(private vararg val testRefs: JUnitTestRef) : JUnitTestRef {
        override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult =
            testRefs.execute(testResults) { it.size == testRefs.size }
    }

    class Not(private val testRef: JUnitTestRef) : JUnitTestRef {
        class NotFailedError : AssertionFailedError("Not expression false")

        override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult {
            return if (testRef[testResults].status == TestExecutionResult.Status.SUCCESSFUL) {
                TestExecutionResult.failed(NotFailedError())
            } else {
                TestExecutionResult.successful()
            }
        }
    }

    companion object {
        fun Array<out JUnitTestRef>.execute(
            testResults: Map<TestIdentifier, TestExecutionResult>,
            predicate: (List<TestExecutionResult>) -> Boolean,
        ): TestExecutionResult {
            val notSuccessful = asSequence()
                .map { it[testResults] }
                .filter { it.status != TestExecutionResult.Status.SUCCESSFUL }
                .toList()
            return if (predicate(notSuccessful)) {
                notSuccessful
                    .first { it.throwable.isPresent }
                    .let { TestExecutionResult.failed(it.throwable.get()) }
            } else {
                TestExecutionResult.successful()
            }
        }
    }
}
