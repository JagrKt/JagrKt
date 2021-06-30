package org.jagrkt.common.export.rubric

import com.google.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jagrkt.api.rubric.GradedCriterion
import org.jagrkt.api.rubric.GradedRubric
import org.jagrkt.common.testing.SubmissionInfoImpl
import org.jagrkt.common.usePrintWriterSafe
import org.slf4j.Logger
import java.io.File

class MoodleJSONExporter @Inject constructor(
  private val logger: Logger,
) : GradedRubricExporter {
  override val name: String = "moodle-json"
  override fun export(gradedRubric: GradedRubric, directory: File, fileName: String) {
    val json = MoodleJSON(
      gradedRubric.testCycle.submission.info as SubmissionInfoImpl,
      gradedRubric.grade.correctPoints,
      StringBuilder().writeTable(gradedRubric).toString(),
    )
    val jsonString = Json.encodeToString(json)
    directory.resolve("$fileName.json").usePrintWriterSafe(logger) {
      println(jsonString)
      flush()
    }
  }

  private fun StringBuilder.writeTable(gradedRubric: GradedRubric): StringBuilder {
    // Open table
    append("<table border='1' cellpadding='5' cellspacing='0'>")
    append("<tbody>")

    // Title
    append("<tr>")
    append("<td><strong>${gradedRubric.rubric.title}</strong></td>")
    append("<td></td>")
    append("<td></td>")
    append("<td></td>")
    append("</tr>")
    appendEmptyHTMLTableRow()

    // Headings
    append("<tr>")
    append("<td><strong>Kriterium</strong></td>")
    append("<td><strong>Möglich</strong></td>")
    append("<td><strong>Erzielt</strong></td>")
    append("<td><strong>Kommentar</strong></td>")
    append("</tr>")
    appendEmptyHTMLTableRow()

    // Child criteria
    for (gradedCriterion in gradedRubric.childCriteria) {
      appendCriterion(gradedCriterion)
      appendEmptyHTMLTableRow()
    }

    val grade = gradedRubric.grade
    val comments = grade.comments

    append("<tr>")
    append("<td><strong>Gesamt:</strong></td>")
    append("<td>${gradedRubric.rubric.maxPoints}</td>")
    append("<td>${grade.correctPoints}</td>")
    append("<td>${comments.firstOrNull() ?: ""}</td>")
    append("</tr>")

    for (i in 1 until comments.size) {
      append("<tr>")
      append("<td></td>")
      append("<td></td>")
      append("<td></td>")
      append("<td>${comments[i]}</td>")
      append("</tr>")
    }

    // Close table
    append("</tbody>")
    append("</table>")
    return this
  }

  private fun StringBuilder.appendCriterion(gradedCriterion: GradedCriterion): StringBuilder {
    val criterion = gradedCriterion.criterion
    val grade = gradedCriterion.grade
    val comments = grade.comments.joinToString("<br />")
    val receivedPoints = if (grade.correctPoints == 0) {
      if (grade.incorrectPoints == 0) "" else (criterion.maxPoints - grade.incorrectPoints).toString()
    } else (criterion.minPoints + grade.correctPoints).toString()
    if (gradedCriterion.childCriteria.isEmpty()) {
      append("<tr>")
      append("<td>${criterion.shortDescription}</td>")
      append("<td>${criterion.maxPoints}</td>")
      append("<td>$receivedPoints</td>")
      append("<td>$comments</td>")
      append("</tr>")
    } else {
      append("<tr>")
      append("<td><strong>${criterion.shortDescription}</strong></td>")
      append("<td></td>")
      append("<td></td>")
      append("<td>$comments</td>")
      append("</tr>")
      for (childGradedCriterion in gradedCriterion.childCriteria) {
        appendCriterion(childGradedCriterion)
      }
      appendEmptyHTMLTableRow()
    }
    return this
  }

  private fun StringBuilder.appendEmptyHTMLTableRow() {
    append("<tr>")
    for (i in 0 until 4) {
      append("<td></td>")
    }
    append("</tr>")
  }

  @Serializable
  data class MoodleJSON(
    val submissionInfo: SubmissionInfoImpl,
    val totalPoints: Int,
    val feedbackComment: String,
  )
}
