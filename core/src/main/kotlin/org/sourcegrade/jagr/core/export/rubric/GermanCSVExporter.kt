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

package org.sourcegrade.jagr.core.export.rubric

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.buildResource
import java.io.BufferedWriter

class GermanCSVExporter @Inject constructor(
  private val logger: Logger,
) : GradedRubricExporter.CSV {

  companion object {
    // delimiter
    const val DEL = '\t'
  }

  override fun export(gradedRubric: GradedRubric): Resource {
    val rubric = gradedRubric.rubric
    val grade = gradedRubric.grade
    fun BufferedWriter.export() {
      appendLine("sep=$DEL")
      appendLine(rubric.title)
      append("Kriterium")
      append(DEL)
      append("Möglich")
      append(DEL)
      append("Erzielt")
      append(DEL)
      append("Kommentar")
      append(DEL)
      append("Extra")
      appendLine()
      for (gradedCriterion in gradedRubric.childCriteria) {
        appendCriterion(gradedCriterion)
        appendLine()
      }
      append("Gesamt")
      append(DEL)
      append(rubric.maxPoints.toString())
      append(DEL)
      append(grade.getInRange(rubric))
      append(DEL)
      append(grade.comments.firstOrNull() ?: "")
      appendLine()
      for (i in 1 until grade.comments.size) {
        appendLine("$DEL$DEL$DEL${grade.comments[i]}")
      }
    }
    return buildResource {
      name = "${gradedRubric.rubric.title}_${gradedRubric.testCycle.submission.info}.csv"
      outputStream.bufferedWriter().use { it.export() }
    }
  }

  private fun BufferedWriter.appendCriterion(gradedCriterion: GradedCriterion): BufferedWriter {
    val criterion = gradedCriterion.criterion
    val grade = gradedCriterion.grade
    val comments = grade.comments.joinToString("; ")
    if (gradedCriterion.childCriteria.isEmpty()) {
      append(criterion.shortDescription)
      append(DEL)
      append(criterion.minMax)
      append(DEL)
      append(grade.getInRange(criterion))
      append(DEL)
      append(comments)
      append(DEL)
      append(criterion.hiddenNotes ?: "")
      appendLine()
    } else {
      append(criterion.shortDescription)
      append(DEL)
      append(DEL)
      append(DEL)
      append(comments)
      append(criterion.hiddenNotes ?: "")
      appendLine()
      for (childGradedCriterion in gradedCriterion.childCriteria) {
        appendCriterion(childGradedCriterion)
      }
      appendLine()
    }
    return this
  }
}
