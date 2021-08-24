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

package org.sourcegrade.jagr.core.executor

import java.io.PrintStream
import java.util.Locale

const val GRADING_THREAD_PREFIX = "G_Thread-"

class ThreadAwarePrintStream(private val delegate: PrintStream) : PrintStream(delegate) {
  private inline fun <T> check(default: T, block: () -> T): T =
    if (!Thread.currentThread().name.startsWith(GRADING_THREAD_PREFIX)) block() else default

  override fun print(b: Boolean) = check(Unit) { delegate.print(b) }
  override fun print(c: Char) = check(Unit) { delegate.print(c) }
  override fun print(i: Int) = check(Unit) { delegate.print(i) }
  override fun print(l: Long) = check(Unit) { delegate.print(l) }
  override fun print(f: Float) = check(Unit) { delegate.print(f) }
  override fun print(d: Double) = check(Unit) { delegate.print(d) }
  override fun print(s: CharArray) = check(Unit) { delegate.print(s) }
  override fun print(s: String?) = check(Unit) { delegate.print(s) }
  override fun print(obj: Any?) = check(Unit) { delegate.print(obj) }
  override fun println() = check(Unit) { delegate.println() }
  override fun println(x: Boolean) = check(Unit) { delegate.println(x) }
  override fun println(x: Char) = check(Unit) { delegate.println(x) }
  override fun println(x: Int) = check(Unit) { delegate.println(x) }
  override fun println(x: Long) = check(Unit) { delegate.println(x) }
  override fun println(x: Float) = check(Unit) { delegate.println(x) }
  override fun println(x: Double) = check(Unit) { delegate.println(x) }
  override fun println(x: CharArray) = check(Unit) { delegate.println(x) }
  override fun println(x: String?) = check(Unit) { delegate.println(x) }
  override fun println(x: Any?) = check(Unit) { delegate.println(x) }
  override fun printf(format: String, vararg args: Any?): PrintStream =
    check(this) { delegate.printf(format, *args) }

  override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream =
    check(this) { delegate.printf(l, format, *args) }
}
