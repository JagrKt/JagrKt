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

package org.jagrkt.api.testing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.util.List;

@ApiStatus.NonExtendable
public interface TestCycle {

  List<String> getRubricProviderClassNames();

  /**
   * Every test cycle uses a unique {@link ClassLoader} that loads the grader jar's classes and the {@link Submission}'s
   * classes if it is a java submission.
   *
   * @return The {@link ClassLoader} used in this test cycle
   */
  ClassLoader getClassLoader();

  Submission getSubmission();

  @Nullable JUnitResult getJUnitResult();

  @ApiStatus.NonExtendable
  interface JUnitResult {
    TestPlan getTestPlan();

    SummaryGeneratingListener getSummaryListener();

    TestStatusListener getStatusListener();
  }
}
