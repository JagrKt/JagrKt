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

package org.jagrkt.common.rubric

import com.google.common.base.MoreObjects
import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.CriterionHolder
import org.jagrkt.api.rubric.CriterionHolderPointCalculator
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Graded
import org.jagrkt.api.rubric.GradedCriterion
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.rubric.Rubric
import org.jagrkt.api.testing.TestCycle

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

  init {
    for (criterion in childCriteria) {
      criterion.setParent(this)
    }
  }

  private val terminal: Boolean by lazy { childCriteria.isEmpty() }
  private val maxPointsKt: Int by lazy { maxCalculator.getPoints(this) }
  private val minPointsKt: Int by lazy { minCalculator.getPoints(this) }
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
  override fun getMaxPoints(): Int = maxPointsKt
  override fun getMinPoints(): Int = minPointsKt
  override fun getParentRubric(): RubricImpl = parentRubricKt
  override fun getParent(): CriterionHolder<CriterionImpl> = parentKt
  override fun getParentCriterion(): Criterion? = parentCriterionKt
  override fun getPeers(): List<CriterionImpl> = peersKt
  override fun getChildCriteria(): List<CriterionImpl> = childCriteria
  override fun grade(testCycle: TestCycle): GradedCriterion {
    val graderResult = grader?.grade(testCycle, this) ?: GradeResult.ofNone()
    if (childCriteria.isEmpty()) {
      return GradedCriterionImpl(testCycle, graderResult, this)
    }
    val childGraded = childCriteria.map { it.grade(testCycle) }
    val gradeResult = GradeResult.of(graderResult, childGraded.map(
      Graded::getGrade))
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
}
