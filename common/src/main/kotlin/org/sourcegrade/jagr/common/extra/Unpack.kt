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

package org.sourcegrade.jagr.common.extra

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.sourcegrade.jagr.common.Config
import org.sourcegrade.jagr.common.testing.SubmissionInfoImpl
import java.io.File
import java.nio.file.FileSystems
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

abstract class Unpack : Extra {

  protected abstract val config: Config
  protected abstract val logger: Logger

  @OptIn(ExperimentalPathApi::class)
  private fun SubmissionInfoVerification.verify() {
    if (assignmentId == null && studentId == null && firstName == null && lastName == null) return
    logger.info(this.toString())
    try {
      FileSystems.newFileSystem(file.toPath(), null as ClassLoader?)
    } catch (e: Exception) {
      logger.warn("InVaLiD ZiP dEtEcTeD")
      return
    }.use { fs ->
      val submissionInfoPath = fs.getPath("submission-info.json")
      val replacedSubmissionInfo = try {
        submissionInfoPath.bufferedReader()
      } catch (e: Throwable) {
        logger.error("Unable to read submission-info for $this")
        null
      }?.use useReader@{ reader ->
        val submissionInfo = try {
          Json { ignoreUnknownKeys = true }.decodeFromString<SubmissionInfoImpl>(reader.readText())
        } catch (e: SerializationException) {
          return@useReader null
        }
        val replaceAssignmentId = assignmentId != null && assignmentId != submissionInfo.assignmentId
        val replaceStudentId = studentId != null && studentId != submissionInfo.studentId
        val replaceFirstName = firstName != null && firstName != submissionInfo.firstName
        val replaceLastName = lastName != null && lastName != submissionInfo.lastName
        if (replaceAssignmentId || replaceStudentId || replaceFirstName || replaceLastName) {
          logger.warn(
            StringBuilder().apply {
              append("$submissionInfo has incorrect submission-info! Replacing:")
              if (replaceAssignmentId) append(" assignmentId(${submissionInfo.assignmentId} -> $assignmentId)")
              if (replaceStudentId) append(" studentId(${submissionInfo.studentId} -> $studentId)")
              if (replaceFirstName) append(" firstName(${submissionInfo.firstName} -> $firstName)")
              if (replaceLastName) append(" lastName(${submissionInfo.lastName} -> $lastName)")
            }.toString()
          )
          SubmissionInfoImpl(
            if (replaceAssignmentId) assignmentId!! else submissionInfo.assignmentId,
            if (replaceStudentId) studentId!! else submissionInfo.studentId,
            if (replaceFirstName) firstName!! else submissionInfo.firstName,
            if (replaceLastName) lastName!! else submissionInfo.lastName,
          )
        } else return
      } ?: SubmissionInfoImpl(
        assignmentId = assignmentId ?: "none",
        studentId = studentId ?: "none",
        firstName = firstName ?: "none",
        lastName = lastName ?: "none",
      )
      submissionInfoPath.bufferedWriter().use { writer ->
        writer.write(Json.encodeToString(replacedSubmissionInfo))
      }
    }
  }

  fun List<SubmissionInfoVerification>.verify() {
    runBlocking {
      with(GlobalScope) {
        asSequence().map {
          async { it.verify() }
        }
      }.forEach { it.await() }
    }
  }

  data class SubmissionInfoVerification(
    val file: File,
    val assignmentId: String? = null,
    val studentId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
  )
}
