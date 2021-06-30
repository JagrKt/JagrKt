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

package org.jagrkt.common.export.rubric

import com.google.inject.Inject
import org.jagrkt.api.rubric.GradedRubric
import org.jagrkt.common.export.ExportManager
import org.slf4j.Logger
import java.io.File

class GradedRubricExportManager @Inject constructor(
  override val logger: Logger,
  override val exporters: Set<GradedRubricExporter>,
) : ExportManager<GradedRubricExporter>() {
  fun export(gradedRubric: GradedRubric, directory: File?, fileName: String) {
    val rubric = gradedRubric.rubric
    val grade = gradedRubric.grade
    val listener = gradedRubric.testCycle.jUnitResult?.summaryListener
    val succeeded = listener?.summary?.testsSucceededCount
    val total = listener?.summary?.testsStartedCount
    logger.info(
      "${gradedRubric.testCycle.submission} ::"
        + if (listener == null) " (no tests found)" else " ($succeeded / $total tests)"
        + " points=${grade.correctPoints} -points=${grade.incorrectPoints} maxPoints=${rubric.maxPoints}"
        + " from '${gradedRubric.rubric.title}'"
    )
    if (directory != null) {
      for (exporter in exporters) {
        exporter.export(gradedRubric, directory.resolve(exporter.name), fileName)
      }
    }
  }
}
