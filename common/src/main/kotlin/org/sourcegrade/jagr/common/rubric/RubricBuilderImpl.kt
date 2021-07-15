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

package org.sourcegrade.jagr.common.rubric

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.Rubric

class RubricBuilderImpl : Rubric.Builder {

  private var title: String? = null
  private val criteria: MutableList<CriterionImpl> = mutableListOf()

  override fun title(title: String): RubricBuilderImpl {
    this.title = title
    return this
  }

  override fun addChildCriteria(vararg criteria: Criterion): RubricBuilderImpl {
    for (criterion in criteria) {
      this.criteria.add(criterion as CriterionImpl)
    }
    return this
  }

  override fun addChildCriteria(criteria: Iterable<Criterion>): RubricBuilderImpl {
    for (criterion in criteria) {
      this.criteria.add(criterion as CriterionImpl)
    }
    return this
  }

  override fun build(): RubricImpl {
    return RubricImpl(
      requireNotNull(title) { "title is null" },
      criteria,
    )
  }
}
