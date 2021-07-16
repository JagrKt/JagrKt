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

package org.sourcegrade.jagr.launcher.io

import java.io.File
import java.io.FilenameFilter

interface GradingBatch {
  val graders: Sequence<ResourceContainer>
  val submissions: Sequence<ResourceContainer>
  val expectedSubmissions: Int
}

fun buildGradingBatch(block: GradingBatchBuilder.() -> Unit): GradingBatch = GradingBatchBuilder().apply(block).build()

class GradingBatchBuilder internal constructor() {
  private val graders = mutableListOf<ResourceContainer>()
  private val submissions = mutableListOf<ResourceContainer>()
  private var totalExpectedCount: Int = 0

  fun discoverGraders(file: File, filter: FilenameFilter? = null) {
    for (candidate in checkNotNull(file.listFiles(filter)) { "Could not find $file" }) {
      graders += createResourceContainer(candidate)
    }
  }

  fun discoverGraders(file: String, filter: FilenameFilter? = null) = discoverGraders(File(file), filter)

  fun discoverSubmissions(file: File, filter: FilenameFilter? = null) {
    for (candidate in checkNotNull(file.listFiles(filter)) { "Could not find $file" }) {
      graders += createResourceContainer(candidate)
      ++totalExpectedCount
    }
  }

  fun discoverSubmissions(file: String, filter: FilenameFilter? = null) = discoverSubmissions(File(file), filter)

  fun addGrader(resourceContainer: ResourceContainer): Boolean {
    return graders.add(resourceContainer)
  }
  fun addSubmission(resourceContainer: ResourceContainer): Boolean {
    ++totalExpectedCount
    return graders.add(resourceContainer)
  }

  fun build(): GradingBatch = GradingBatchImpl(graders.asSequence(), submissions.asSequence(), totalExpectedCount)
}

private data class GradingBatchImpl(
  override val graders: Sequence<ResourceContainer>,
  override val submissions: Sequence<ResourceContainer>,
  override val expectedSubmissions: Int
) : GradingBatch
