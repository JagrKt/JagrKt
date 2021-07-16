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

interface ResourceContainer : Sequence<Resource> {
  val name: String
}

val ResourceContainer.nameWithoutExtension: String
  get() = name.substringBeforeLast(".")

fun createResourceContainer(file: File): ResourceContainer = when (file.extension) {
  "zip",
  "jar"
  -> ZipResourceContainer(file)
  else -> throw IllegalArgumentException("Could not an appropriate resource container for $file")
}
