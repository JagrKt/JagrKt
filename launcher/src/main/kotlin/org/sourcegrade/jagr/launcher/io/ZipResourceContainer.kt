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

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipResourceContainer(
  override val name: String,
  private val input: InputStream,
) : ResourceContainer {
  constructor(file: File) : this(file.name, file.inputStream())

  override fun iterator(): Iterator<Resource> = ZipResourceIterator(ZipInputStream(input))
  private inner class ZipResourceIterator(
    private val zip: ZipInputStream
  ) : Iterator<Resource> {
    private var next: ZipEntry? = null
    override fun hasNext(): Boolean {
      if (next == null) {
        next = zip.nextEntry
        return next == null
      }
      return true
    }

    override fun next(): Resource {
      return next?.let {
        // return cached next from hasNext() and reset it
        ZipEntryResource(it).also { next = null }
      } // no cached next, calculate and return
        ?: ZipEntryResource(requireNotNull(zip.nextEntry) { "No next entry!" })
    }

    private inner class ZipEntryResource(entry: ZipEntry) : Resource {
      private val bytes: ByteArray = zip.readBytes()
      override val name: String = entry.name
      override val inputStream: InputStream
        get() = ByteArrayInputStream(bytes)
    }
  }
}
