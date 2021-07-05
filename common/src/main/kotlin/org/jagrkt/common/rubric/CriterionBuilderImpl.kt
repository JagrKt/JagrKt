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

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.CriterionHolderPointCalculator
import org.jagrkt.api.rubric.Grader

class CriterionBuilderImpl : Criterion.Builder {
  private var shortDescription: String? = null
  private var hiddenNotes: String? = null
  private var grader: Grader? = null
  private var maxCalculator: CriterionHolderPointCalculator? = null
  private var minCalculator: CriterionHolderPointCalculator? = null
  private val children: MutableList<CriterionImpl> = mutableListOf()

  override fun shortDescription(shortDescription: String): CriterionBuilderImpl {
    this.shortDescription = shortDescription
    return this
  }

  override fun hiddenNotes(hiddenNotes: String?): CriterionBuilderImpl {
    this.hiddenNotes = hiddenNotes
    return this
  }

  override fun grader(grader: Grader?): Criterion.Builder {
    this.grader = grader
    return this
  }

  override fun maxPoints(maxPointsCalculator: CriterionHolderPointCalculator?): CriterionBuilderImpl {
    maxCalculator = maxPointsCalculator
    return this
  }

  override fun minPoints(minPointsCalculator: CriterionHolderPointCalculator?): CriterionBuilderImpl {
    minCalculator = minPointsCalculator
    return this
  }

  override fun addChildCriteria(vararg criteria: Criterion): CriterionBuilderImpl {
    for (criterion in criteria) {
      this.children.add(criterion as CriterionImpl)
    }
    return this
  }

  override fun build(): CriterionImpl {
    return CriterionImpl(
      requireNotNull(shortDescription) { "shortDescription is null" },
      hiddenNotes,
      grader,
      maxCalculator ?: CriterionHolderPointCalculator.maxOfChildren(1),
      minCalculator ?: CriterionHolderPointCalculator.minOfChildren(0),
      children,
    )
  }
}
