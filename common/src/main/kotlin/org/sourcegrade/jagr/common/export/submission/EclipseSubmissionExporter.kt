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

package org.sourcegrade.jagr.common.export.submission

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.common.ensure
import org.sourcegrade.jagr.common.testing.JavaSubmission
import org.sourcegrade.jagr.common.testing.TestJar
import org.sourcegrade.jagr.common.writeTextSafe
import java.io.File
import java.io.PrintWriter

class EclipseSubmissionExporter @Inject constructor(
  private val logger: Logger,
) : SubmissionExporter {
  override val name: String = "eclipse"
  override fun export(submission: Submission, directory: File, testJar: TestJar?) {
    if (submission !is JavaSubmission) return
    val file = directory.resolve(submission.info.toString()).ensure(logger, false) ?: return
    val src = file.resolve("src").ensure(logger, false) ?: return
    writeProjectFile(submission, file.resolve(".project"))
    writeClasspathFile(submission, file.resolve(".classpath"))
    // sourceFile.name starts with a / and needs to be converted to a relative path
    for ((_, sourceFile) in submission.sourceFiles) {
      src.resolve(".${sourceFile.name}").writeTextSafe(sourceFile.content, logger)
    }
  }

  private fun writeProjectFile(submission: JavaSubmission, file: File) {
    val writer = PrintWriter(file, "UTF-8")
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    writer.println("<projectDescription>")
    writer.println("\t<name>${submission.info}</name>")
    writer.println("<buildSpec>")
    writer.println("\t\t<buildCommand>")
    writer.println("\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>")
    writer.println("\t\t</buildCommand>")
    writer.println("\t</buildSpec>")
    writer.println("\t<natures>")
    writer.println("\t\t<nature>org.eclipse.jdt.core.javanature</nature>")
    writer.println("\t</natures>")
    writer.println("</projectDescription>")
    writer.flush()
  }

  private fun writeClasspathFile(submission: JavaSubmission, file: File) {
    val writer = PrintWriter(file, "UTF-8")
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    writer.println("<classpath>")
    writer.println("\t<classpathentry kind=\"src\" path=\"src\"/>")
    writer.println("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-11\">\n")
    writer.println("\t\t<attributes>\n")
    writer.println("\t\t\t<attribute name=\"module\" value=\"true\"/>")
    writer.println("\t\t</attributes>")
    writer.println("\t</classpathentry>")
    writer.println("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.junit.JUNIT_CONTAINER/5\"/>")
    writer.println("\t<classpathentry kind=\"output\" path=\"bin\"/>")
    writer.println("</classpath>")
    writer.flush()
  }
}
