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

package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Grader

abstract class AbstractGraderBuilder<B : Grader.Builder<B>> : Grader.Builder<B> {

  var graderPassed: Grader? = null
  var graderFailed: Grader? = null
  var commentIfFailed: String? = null

  abstract fun getThis(): B

  override fun pointsPassed(grader: Grader?): B {
    graderPassed = grader
    return getThis()
  }

  override fun pointsFailed(grader: Grader?): B {
    graderFailed = grader
    return getThis()
  }

  override fun commentIfFailed(comment: String?): B {
    commentIfFailed = comment
    return getThis()
  }
}
