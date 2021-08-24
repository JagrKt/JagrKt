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

package org.sourcegrade.jagr.core

import org.slf4j.Logger
import java.io.File
import java.io.InputStream
import java.io.PrintWriter

fun File.ensure(logger: Logger? = null, logInfo: Boolean = true): File? {
  if (!exists()) {
    if (logInfo) {
      logger?.info("No $this dir! Creating...")
    }
    try {
      if (!mkdirs()) {
        logger?.error("Unable to create $absolutePath dir")
        return null
      }
    } catch (e: SecurityException) {
      logger?.error("Unable to create $absolutePath dir", e)
      return null
    }
  }
  return this
}

inline fun File.writeStream(stream: () -> InputStream): File {
  outputStream().use { fileStream ->
    stream().use { inputStream ->
      inputStream.copyTo(fileStream)
    }
  }
  return this
}

inline fun File.usePrintWriterSafe(logger: Logger? = null, block: PrintWriter.() -> Unit) {
  parentFile.ensure(logger, logInfo = false) ?: return
  try {
    PrintWriter(this, "UTF-8").use(block)
  } catch (e: Throwable) {
    logger?.error("Unable to export to $this", e)
    return
  }
}

fun File.writeTextSafe(content: String, logger: Logger? = null) {
  parentFile.ensure(logger, logInfo = false) ?: return
  writeText(content)
}
