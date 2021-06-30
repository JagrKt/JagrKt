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

package org.jagrkt.common.export.submission

import com.google.inject.Inject
import org.jagrkt.api.testing.Submission
import org.jagrkt.common.export.ExportManager
import org.jagrkt.common.testing.TestJar
import org.slf4j.Logger
import java.io.File

class SubmissionExportManager @Inject constructor(
  override val logger: Logger,
  override val exporters: Set<SubmissionExporter>,
) : ExportManager<SubmissionExporter>() {
  fun export(submission: Submission, directory: File, testJars: List<TestJar>) {
    for (exporter in exporters) {
      val exportDir = directory.resolve(exporter.name)
      exporter.export(submission, exportDir.resolve("default"))
      for (testJar in testJars) {
        exporter.export(submission, exportDir.resolve(testJar.name), testJar)
      }
    }
  }
}
