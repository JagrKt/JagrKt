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
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.util.List;

@ApiStatus.NonExtendable
public interface Criterion extends Gradable<GradedCriterion>, CriterionHolder<Criterion> {

  static Builder builder() {
    return FactoryProvider.factory.builder();
  }

  String getShortDescription();

  /**
   * Hidden notes are not included in the main rubric area and are not meant to be included in the final uploaded rubric.
   */
  @Nullable String getHiddenNotes();

  /**
   * A criterion is terminal iff it does not have any children.
   *
   * @return Whether this criterion is terminal.
   */
  boolean isTerminal();

  /**
   * @return The {@link Rubric} parent. This is not necessarily the direct parent.
   */
  Rubric getParentRubric();

  /**
   * @return The direct parent, this may be a {@link Rubric} or a {@link Criterion}.
   */
  CriterionHolder<? extends Criterion> getParent();

  /**
   * @return The direct {@link Criterion} parent. {@code null} if the parent is not a {@link Criterion}.
   */
  @Nullable Criterion getParentCriterion();

  List<? extends Criterion> getPeers();

  @ApiStatus.NonExtendable
  interface Builder {

    /**
     * This field is required.
     *
     * @param shortDescription The short description
     * @return {@code this}
     */
    Builder shortDescription(String shortDescription);

    /**
     * Sets (or unsets) the notes that should not be visible to the student but may help with manual grading.
     *
     * @param hiddenNotes The hidden notes or {@code null} to unset
     * @return {@code this}
     */
    Builder hiddenNotes(@Nullable String hiddenNotes);

    /**
     * Sets the {@link Grader} for this criterion.
     *
     * @param pointCalculator@return {@code this}
     * @see DiscoverySelectors
     */
    Builder grader(@Nullable Grader pointCalculator);

    /**
     * Sets the maximum points for the built {@link Criterion}. Pass {@code null} to
     * {@link #maxPoints(CriterionHolderPointCalculator)} to reset this value to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 1. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#maxOfChildren(int)}
     * </p>
     *
     * @param maxPoints The maximum points
     * @return {@code this}
     */
    default Builder maxPoints(int maxPoints) {
      return maxPoints(CriterionHolderPointCalculator.fixed(maxPoints));
    }

    /**
     * Sets the maximum points for this section. Passing {@code null} resets it to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 1. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#maxOfChildren(int)}
     * </p>
     *
     * @param maxPointsCalculator The {@link CriterionHolderPointCalculator}
     * @return {@code this}
     */
    Builder maxPoints(@Nullable CriterionHolderPointCalculator maxPointsCalculator);

    /**
     * Sets the minimum points for the built {@link Criterion}. Pass {@code null} to
     * {@link #minPoints(CriterionHolderPointCalculator)} to reset this value to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 0. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#minOfChildren(int)}
     * </p>
     *
     * @param minPoints The minimum points
     * @return {@code this}
     */
    default Builder minPoints(int minPoints) {
      return minPoints(CriterionHolderPointCalculator.fixed(minPoints));
    }

    /**
     * Sets the minimum points for this section. Passing {@code null} resets this value to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 0. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#minOfChildren(int)}
     * </p>
     *
     * @param minPointsCalculator The {@link CriterionHolderPointCalculator}
     * @return {@code this}
     */
    Builder minPoints(@Nullable CriterionHolderPointCalculator minPointsCalculator);

    Builder addChildCriteria(Criterion... criteria);

    Criterion build();
  }

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    Builder builder();
  }
}
