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

import com.google.common.base.MoreObjects
import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.CriterionHolder
import org.sourcegrade.jagr.api.rubric.CriterionHolderPointCalculator
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Graded
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.rubric.Rubric
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.readNullable
import org.sourcegrade.jagr.launcher.io.writeList
import org.sourcegrade.jagr.launcher.io.writeNullable

/**
 * This implementation relies on the fact that its internal state is not modified after a call to any of the methods
 * implemented from [Criterion]. Calling those methods triggers the lazy initialization of the internal fields; If the internal
 * state is modified after these calls, the behavior of this instance is no longer correct. This means that instance methods
 * declared in this class and not in [Criterion] must be called before a call to any implemented method from [Criterion] unless
 * explicitly stated otherwise in the method's documentation
 */
class CriterionImpl(
    private val shortDescription: String,
    private val hiddenNotes: String?,
    private val grader: Grader?,
    private val maxCalculator: CriterionHolderPointCalculator,
    private val minCalculator: CriterionHolderPointCalculator,
    private val childCriteria: List<CriterionImpl>,
) : Criterion {

    private val minPointsKt = minCalculator.getPoints(this)
    private val maxPointsKt = maxCalculator.getPoints(this)

    init {
        require(minPoints <= maxPoints) {
            "minPoints ($minPoints) for criterion may not be greater than maxPoints ($maxPoints)"
        }
        for (criterion in childCriteria) {
            criterion.setParent(this)
        }
    }

    private val terminal: Boolean by lazy { childCriteria.isEmpty() }
    private lateinit var parentKt: CriterionHolder<CriterionImpl>
    private val parentRubricKt: RubricImpl by lazy {
        var current: Criterion = this
        while (current.parent !is Rubric) {
            current = current.parent as Criterion
        }
        current.parent as RubricImpl
    }
    private val parentCriterionKt: CriterionImpl? by lazy { parentKt as? CriterionImpl }
    private val peersKt: List<CriterionImpl> by lazy { parentKt.childCriteria - this }

    fun setParent(parent: CriterionHolder<Criterion>) {
        this.parentKt = parent as CriterionHolder<CriterionImpl>
    }

    override fun getShortDescription(): String = shortDescription
    override fun getHiddenNotes(): String? = hiddenNotes
    override fun isTerminal(): Boolean = terminal
    override fun getMinPoints(): Int = minPointsKt
    override fun getMaxPoints(): Int = maxPointsKt
    override fun getParentRubric(): RubricImpl = parentRubricKt
    override fun getParent(): CriterionHolder<CriterionImpl> = parentKt
    override fun getParentCriterion(): Criterion? = parentCriterionKt
    override fun getPeers(): List<CriterionImpl> = peersKt
    override fun getChildCriteria(): List<CriterionImpl> = childCriteria
    override fun getGrader(): Grader? = grader
    override fun grade(testCycle: TestCycle): GradedCriterion {
        val graderResult = GradeResult.clamped(
            grader?.grade(testCycle, this)
                ?: GradeResult.of(this, "No grader provided"),
            this,
        )
        if (childCriteria.isEmpty()) {
            return GradedCriterionImpl(testCycle, graderResult, this)
        }
        val childGraded = childCriteria.map { it.grade(testCycle) }
        val gradeResult = childGraded.asSequence()
            .map(Graded::getGrade)
            .sum()
            .withoutComments()
            .clamped(this)
        return GradedCriterionImpl(testCycle, gradeResult, this, childGraded)
    }

    private val stringRep: String by lazy {
        MoreObjects.toStringHelper(this)
            .add("shortDescription", shortDescription)
            .add("maxPoints", maxPointsKt)
            .add("minPoints", minPointsKt)
            .add("childCriteria", childCriteria)
            .toString()
    }

    override fun toString(): String = stringRep

    companion object Factory : SerializerFactory<CriterionImpl> {
        override fun read(scope: SerializationScope.Input) = CriterionImpl(
            scope.input.readUTF(),
            scope.readNullable(),
            // The next line is *technically* incorrect, but it won't be used anyways so this is ok.
            // Serializing a grader would require cooperation from the implementer. This doesn't really make any
            // sense though, as it won't be used after serialization (which happens after rubrics have been graded).
            null,
            CriterionHolderPointCalculator.fixed(scope.input.readInt()),
            CriterionHolderPointCalculator.fixed(scope.input.readInt()),
            scope.readList(),
        )

        override fun write(obj: CriterionImpl, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.shortDescription)
            scope.writeNullable(obj.hiddenNotes)
            scope.output.writeInt(obj.maxPointsKt)
            scope.output.writeInt(obj.minPointsKt)
            scope.writeList(obj.childCriteria)
        }
    }
}
