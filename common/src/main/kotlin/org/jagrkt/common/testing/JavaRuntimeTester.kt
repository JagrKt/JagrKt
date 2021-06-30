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

package org.jagrkt.common.testing

import com.google.inject.Inject
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle
import org.jagrkt.common.compiler.java.RuntimeClassLoader
import org.jagrkt.common.executor.TimeoutHandler
import org.junit.platform.commons.JUnitException
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.slf4j.Logger

class JavaRuntimeTester @Inject constructor(
  private val logger: Logger,
  private val testCycleParameterResolver: TestCycleParameterResolver,
) : RuntimeTester {
  override fun createTestCycle(testJar: TestJar, submission: Submission): TestCycle? {
    if (submission !is JavaSubmission) return null
    val info = submission.info
    val rubricProviders = testJar.rubricProviders[info.assignmentId] ?: return null
    val classLoader = RuntimeClassLoader(submission.compiledClasses + testJar.compiledClasses)
    val testCycle = JavaTestCycle(rubricProviders, submission, classLoader)
    testJar.testProviders[info.assignmentId]
      ?.map { DiscoverySelectors.selectClass(classLoader.loadClass(it)) }
      ?.runJUnit(testCycle)
      ?.also(testCycle::setJUnitResult)
    return testCycle
  }

  private fun List<ClassSelector>.runJUnit(testCycle: TestCycle): JUnitResultImpl? {
    testCycleParameterResolver.value = testCycle
    val info = testCycle.submission.info
    logger.info("Running JUnit @ $info :: [${joinToString { it.className }}]")
    val launcher = LauncherFactory.create()
    val testPlan = try {
      launcher.discover(LauncherDiscoveryRequestBuilder.request().selectors(this).build())
    } catch (e: JUnitException) {
      /*
       * If a LinkageError occurred in a JUnit test class, try again with the other test classes.
       * This may occur if a student did not implement a class or method a test class depends on.
       */
      if (e.cause is JUnitException && e.cause?.cause is LinkageError) {
        return partition { e.cause!!.message?.contains(it.className) == false }.let { (included, excluded) ->
          val excludedClasses = excluded.joinToString { it.className }
          val msg = e.cause!!.cause!!.message
          logger.error("Linkage error @ $info :: $msg, retrying without test classes [$excludedClasses]")
          included.runJUnit(testCycle)
        }
      }
      logger.error("Failed to discover JUnit tests for $info", e)
      return null
    }
    return try {
      val summaryListener = SummaryGeneratingListener()
      val statusListener = TestStatusListenerImpl(logger)
      TimeoutHandler.setClassNames(map { it.className })
      launcher.execute(testPlan, summaryListener, statusListener)
      // if total timeout has been reached, reset so that the rubric provider doesn't throw an error
      TimeoutHandler.disableTimeout()
      statusListener.logLinkageErrors(info)
      JUnitResultImpl(testPlan, summaryListener, statusListener)
    } catch (e: Throwable) {
      logger.error("Failed to run JUnit tests for $info", e)
      null
    }
  }
}
