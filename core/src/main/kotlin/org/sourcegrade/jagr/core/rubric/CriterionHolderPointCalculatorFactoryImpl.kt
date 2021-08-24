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

package org.sourcegrade.jagr.core.rubric

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.CriterionHolderPointCalculator

class CriterionHolderPointCalculatorFactoryImpl : CriterionHolderPointCalculator.Factory {
  override fun fixed(points: Int): CriterionHolderPointCalculator =
    CriterionHolderPointCalculator { points }

  override fun maxOfChildren(defaultPoints: Int): CriterionHolderPointCalculator {
    return CriterionHolderPointCalculator {
      it.childCriteria.asSequence().map(Criterion::getMaxPoints)
        .ifEmpty { listOf(defaultPoints).asSequence() }.sum()
    }
  }

  override fun minOfChildren(defaultPoints: Int): CriterionHolderPointCalculator {
    return CriterionHolderPointCalculator {
      it.childCriteria.asSequence().map(Criterion::getMinPoints)
        .ifEmpty { listOf(defaultPoints).asSequence() }.sum()
    }
  }
}
