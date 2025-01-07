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

package org.sourcegrade.jagr.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.sourcegrade.jagr.api.testing.TestCycle;

import java.util.List;

/**
 * A node in a tree-based structure that defines the achievable points for parts of an assignment.
 *
 * <p>
 * A criterion may be {@link #isTerminal() terminal} if it does not have {@link #getChildCriteria() children}. If this is the
 * case, the result produced by {@link Gradable#grade(TestCycle)} is directly dependant on only the criterion's grader as
 * defined by {@link Builder#grader(Grader)}.
 * </p>
 *
 * <p>
 * In the case that a criterion is not {@link #isTerminal() terminal} (i.e. has {@link #getChildCriteria() children})
 * {@link Gradable#grade(TestCycle)} first executes the criterion's own grader (which is usually undefined anyway if the
 * criterion is not terminal) and recursively combines the results into a final result which is then returned.
 * </p>
 */
@ApiStatus.NonExtendable
public interface Criterion extends Gradable<GradedCriterion>, CriterionHolder<Criterion> {

    /**
     * Creates a new {@link Builder} for the construction of a criterion.
     */
    static Builder builder() {
        return FactoryProvider.factory.builder();
    }

    /**
     * A short summary of the requirements for this criterion.
     */
    String getShortDescription();

    /**
     * Hidden notes are not included in the main rubric area and are not meant to be included in the final uploaded rubric.
     */
    @Nullable String getHiddenNotes();

    /**
     * A criterion is terminal iff it does not have any children.
     *
     * @return Whether this criterion is terminal
     */
    boolean isTerminal();

    /**
     * The {@link Rubric} parent of this criterion. This is not necessarily the direct parent.
     *
     * @return The {@link Rubric} parent of this criterion
     * @see #getParent()
     */
    Rubric getParentRubric();

    /**
     * The direct parent of this criterion. This may be a {@link Rubric} or a {@link Criterion}.
     *
     * @return The direct parent of this criterion
     */
    CriterionHolder<? extends Criterion> getParent();

    /**
     * The direct criterion parent of this criterion.
     *
     * @return The direct criterion parent or {@code null} if the parent is not a criterion
     */
    @Nullable Criterion getParentCriterion();

    /**
     * The {@link Grader} that will be used to calculate the points for the criterion.
     *
     * @return The {@link Grader} to use for this {@link Criterion}
     */
    @Nullable Grader getGrader();

    /**
     * The peers of this criterion.
     *
     * <p>
     * The returned list contains all children of this criterion's parent but does not include this criterion.
     * </p>
     *
     * @return The peers of this criterion
     */
    List<? extends Criterion> getPeers();

    /**
     * Used to build a new {@link Criterion} instance. Use {@link Criterion#builder()} to create an instance of this interface.
     *
     * @see Criterion#builder()
     */
    @ApiStatus.NonExtendable
    interface Builder {

        /**
         * Sets the short description for this criterion.
         *
         * <p>
         * This field is required.
         * </p>
         *
         * @param shortDescription The short description
         * @return {@code this}
         * @see #getShortDescription()
         */
        Builder shortDescription(String shortDescription);

        /**
         * Sets (or unsets) the notes that should not be visible to the student but may help with manual grading.
         *
         * @param hiddenNotes The hidden notes or {@code null} to unset
         * @return {@code this}
         * @see #getHiddenNotes()
         */
        Builder hiddenNotes(@Nullable String hiddenNotes);

        /**
         * Sets the {@link Grader} that will be used to calculate the points for the criterion.
         *
         * <p>
         * If the value of the grader is null when the criterion is constructed, the criterion
         * will always be graded with {@link GradeResult#ofNone()} unless it has {@link #getChildCriteria() children}.
         * </p>
         *
         * @param grader The {@link Grader} to use for this {@link Criterion}
         * @return {@code this}
         * @see Criterion#grade(TestCycle)
         */
        Builder grader(@Nullable Grader grader);

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
         * @see #getMaxPoints()
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
         * @see #getMaxPoints()
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
         * @see #getMinPoints()
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
         * @see #getMinPoints()
         */
        Builder minPoints(@Nullable CriterionHolderPointCalculator minPointsCalculator);

        /**
         * Adds child criteria to the criterion.
         *
         * <p>
         * A criterion with at least one child is considered not {@link #isTerminal() terminal}.
         * </p>
         *
         * @param criteria The criteria to add as children
         * @return {@code this}
         * @see #getChildCriteria()
         * @see #isTerminal()
         */
        Builder addChildCriteria(Criterion... criteria);

        /**
         * Constructs the criterion with the previously defined properties.
         *
         * @return A newly constructed criterion
         */
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
