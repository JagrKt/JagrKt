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

package org.sourcegrade.jagr.core.executor;

import com.google.inject.Inject;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.sourcegrade.jagr.core.Config;
import org.sourcegrade.jagr.core.transformer.CommonTransformer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Injected via ASM by {@link CommonTransformer}.
 *
 * @see #checkTimeout() for more information.
 */
@SuppressWarnings("unused")
public final class TimeoutHandler {

  private static final ThreadLocal<AtomicLong> LAST_TIMEOUT = ThreadLocal.withInitial(AtomicLong::new);
  private static final ThreadLocal<List<String>> TEST_CLASS_NAMES = ThreadLocal.withInitial(Collections::emptyList);
  private static final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

  @Inject
  private static Config config;

  @Inject
  private static Logger logger;

  /**
   * Nested class for lazy initialization.
   */
  private static final class Lazy {
    private static final long INDIVIDUAL_TIMEOUT = config.getTransformers().getTimeout().getIndividualTimeout();
    private static final long TOTAL_TIMEOUT = config.getTransformers().getTimeout().getTotalTimeout();
  }

  public static void disableTimeout() {
    LAST_TIMEOUT.get().set(-1);
  }

  public static void resetTimeout() {
    LAST_TIMEOUT.get().set(0);
  }

  public static void setClassNames(final List<String> classNames) {
    TEST_CLASS_NAMES.set(classNames);
  }

  /**
   * Checks whether the current {@link Thread} has reached a timeout, throwing an {@link AssertionFailedError} if so.
   *
   * @throws AssertionFailedError If the timeout has been reached.
   */
  public static void checkTimeout() {
    final long lastTimeout = LAST_TIMEOUT.get().get();
    if (lastTimeout == -1) {
      // do nothing
      return;
    }
    final Thread currentThread = Thread.currentThread();
    final long userTime = mxBean.getThreadUserTime(currentThread.getId()) / 1_000_000;
    if (lastTimeout == 0) {
      LAST_TIMEOUT.get().set(userTime);
    } else if (userTime - lastTimeout > Lazy.INDIVIDUAL_TIMEOUT) {
      if (userTime > Lazy.TOTAL_TIMEOUT) {
        logger.error("Total timeout after " + Lazy.TOTAL_TIMEOUT + "ms @ " + currentThread.getName());
        // do not reset LAST_TIMEOUT
        throw new AssertionFailedError("Total timeout after " + Lazy.TOTAL_TIMEOUT + "ms");
      } else {
        logger.error("Timeout after " + Lazy.INDIVIDUAL_TIMEOUT + "ms @ " + currentThread.getName() +
          " in " + getTimeoutLocation(currentThread));
        // reset LAST_TIMEOUT so that the next JUnit test doesn't immediately fail
        LAST_TIMEOUT.get().set(userTime);
        throw new AssertionFailedError("Timeout after " + Lazy.INDIVIDUAL_TIMEOUT + "ms");
      }
    }
  }

  /**
   * @return The String representing the StackElement of the Test, that produced the timeout
   */
  private static String getTimeoutLocation(final Thread currentThread) {
    final StackTraceElement[] trace = currentThread.getStackTrace();
    for (var element : trace) {
      if (TEST_CLASS_NAMES.get().contains(element.getClassName())) {
        return element.toString();
      }
    }
    return "I don't know how you got here. But now that you're here... Let's play?";
  }
}
