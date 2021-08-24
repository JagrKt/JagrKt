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

package org.sourcegrade.jagr.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.sourcegrade.jagr.api.testing.TestCycle;

@FunctionalInterface
public interface Grader {

  static TestAwareBuilder testAwareBuilder() {
    return FactoryProvider.factory.testAwareBuilder();
  }

  static Grader descendingPriority(Grader... graders) {
    return FactoryProvider.factory.descendingPriority(graders);
  }

  GradeResult grade(TestCycle testCycle, Criterion criterion);

  @ApiStatus.NonExtendable
  interface Builder<B extends Builder<B>> {

    default B pointsPassedMax() {
      return pointsPassed((ignored, criterion) -> GradeResult.ofMax(criterion));
    }

    default B pointsPassedMin() {
      return pointsPassed((ignored, criterion) -> GradeResult.ofMin(criterion));
    }

    B pointsPassed(@Nullable Grader grader);

    default B pointsFailedMax() {
      return pointsFailed((ignored, criterion) -> GradeResult.ofMax(criterion));
    }

    default B pointsFailedMin() {
      return pointsFailed((ignored, criterion) -> GradeResult.ofMin(criterion));
    }

    B pointsFailed(@Nullable Grader grader);

    /**
     * Sets (or unsets) the general "if failed" comment on this grader. This is added after comments set by other methods on
     * this builder and can only be removed by invoking this method with {@code null}.
     *
     * @param comment The comment to write in the exported rubric if this grader fails. Passing {@code null} unsets the value.
     * @return {@code this}
     */
    B commentIfFailed(@Nullable String comment);

    Grader build();
  }

  /**
   * Builds a grader that uses JUnit to determine a grade
   */
  @ApiStatus.NonExtendable
  interface TestAwareBuilder extends Builder<TestAwareBuilder> {

    default TestAwareBuilder requirePass(JUnitTestRef testRef) {
      return requirePass(testRef, null);
    }

    TestAwareBuilder requirePass(JUnitTestRef testRef, @Nullable String comment);

    default TestAwareBuilder requireFail(JUnitTestRef testRef) {
      return requireFail(testRef, null);
    }

    TestAwareBuilder requireFail(JUnitTestRef testRef, @Nullable String comment);
  }

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    TestAwareBuilder testAwareBuilder();

    Grader descendingPriority(Grader... graders);
  }
}
