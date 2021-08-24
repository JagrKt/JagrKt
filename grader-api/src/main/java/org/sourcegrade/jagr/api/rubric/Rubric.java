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

/**
 * A rubric (or template) used for grading an assignment.
 */
@ApiStatus.NonExtendable
public interface Rubric extends Gradable<GradedRubric>, CriterionHolder<Criterion> {

  static Builder builder() {
    return FactoryProvider.factory.builder();
  }

  /**
   * @return The title of the rubric or exercise, e.g. "H00"
   */
  String getTitle();

  /**
   * The maximum number of points is the sum of the maximum points of the {@link #getChildCriteria() criteria} of this rubric.
   *
   * @return The maximum number of points for this rubric.
   * @see CriterionHolderPointCalculator#maxOfChildren(int)
   */
  int getMaxPoints();

  /**
   * The minimum number of points is the min of the minimum points of the {@link #getChildCriteria() criteria} of this rubric.
   *
   * @return The minimum number of points for this rubric.
   * @see CriterionHolderPointCalculator#minOfChildren(int)
   */
  int getMinPoints();

  @ApiStatus.NonExtendable
  interface Builder {

    Builder title(String title);

    Builder addChildCriteria(Criterion... criterion);

    Builder addChildCriteria(Iterable<? extends Criterion> criterion);

    Rubric build();
  }

  @ApiStatus.Internal
  class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    Builder builder();
  }
}
