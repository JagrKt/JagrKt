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

package org.jagrkt.common.export

import org.jagrkt.common.ensure
import org.jagrkt.common.testing.TestJar
import org.slf4j.Logger
import java.io.File

abstract class ExportManager<E : Exporter> {

  protected abstract val logger: Logger
  protected abstract val exporters: Set<E>

  fun initialize(directory: File, testJars: List<TestJar>) {
    for (exporter in exporters) {
      val exportDir = directory.resolve(exporter.name).ensure(logger) ?: continue
      exportDir.resolve("default").ensure(logger) ?: continue
      exporter.initialize(exportDir.resolve("default"))
      for (testJar in testJars) {
        exportDir.ensure(logger) ?: continue
        exporter.initialize(exportDir.resolve(testJar.name), testJar)
      }
    }
  }

  fun finalize(directory: File, testJars: List<TestJar>) {
    for (exporter in exporters) {
      val exportDir = directory.resolve(exporter.name)
      exporter.finalize(exportDir.resolve("default"))
      for (testJar in testJars) {
        exporter.finalize(exportDir.resolve(testJar.name), testJar)
      }
    }
  }
}
