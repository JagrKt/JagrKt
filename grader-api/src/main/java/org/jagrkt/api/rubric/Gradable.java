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

import org.jagrkt.api.testing.TestCycle;
import org.jetbrains.annotations.ApiStatus;

/**
 * Something that can be graded via a {@link TestCycle}
 *
 * @param <G> The resulting graded type
 */
@ApiStatus.NonExtendable
public interface Gradable<G extends Graded> {

  int getMaxPoints();

  int getMinPoints();

  /**
   * Grade the provided {@link TestCycle}
   */
  G grade(TestCycle testCycle);
}
