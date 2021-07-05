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

import org.jagrkt.api.testing.Submission;
import org.jetbrains.annotations.Nullable;

public interface RubricProvider {

  /**
   * The filename to use when writing this rubric to disk. By default, it is:
   *
   * <pre><code>
   * assignmentId + "_Rubric_" + studentLastName + "_" + studentFirstName
   * </code></pre>
   * <p>
   * If there are multiple rubrics with conflicting names, a number will be added on the end
   */
  default @Nullable String getOutputFileName(Submission submission) {
    return null;
  }

  Rubric getRubric();
}
