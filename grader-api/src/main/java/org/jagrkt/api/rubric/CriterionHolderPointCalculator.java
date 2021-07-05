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

package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;

/**
 * A functional interface that calculates a number based on the children of a {@link CriterionHolder}
 */
@FunctionalInterface
public interface CriterionHolderPointCalculator {

  /**
   * @param points The points that the created calculator should return
   * @return A calculator that always returns the provided {@code points}
   */
  static CriterionHolderPointCalculator fixed(int points) {
    return FactoryProvider.factory.fixed(points);
  }

  /**
   * @return A calculator that returns the sum of the maximum points of the children of the provided {@link Criterion}
   */
  static CriterionHolderPointCalculator maxOfChildren(int defaultPoints) {
    return FactoryProvider.factory.maxOfChildren(defaultPoints);
  }

  /**
   * @return A calculator that returns the sum of the minimum points of the children of the provided {@link Criterion}
   */
  static CriterionHolderPointCalculator minOfChildren(int defaultPoints) {
    return FactoryProvider.factory.minOfChildren(defaultPoints);
  }

  int getPoints(CriterionHolder<Criterion> criterionHolder);

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    CriterionHolderPointCalculator fixed(int points);

    CriterionHolderPointCalculator maxOfChildren(int defaultPoints);

    CriterionHolderPointCalculator minOfChildren(int defaultPoints);
  }
}
