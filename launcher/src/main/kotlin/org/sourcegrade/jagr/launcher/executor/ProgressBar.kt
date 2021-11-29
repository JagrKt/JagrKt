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

package org.sourcegrade.jagr.launcher.executor

import java.io.PrintStream
import java.text.DecimalFormat

class ProgressBar(
  private val rubricCollector: RubricCollector,
  private val showElementsIfLessThan: Int = 3,
) {

  private val decimalFormat = DecimalFormat("00.00")
  private val barLengthFull = 50
  private val barChar = '='
  private val sideChar = '|'
  private val tipChar = '>'
  private val whitespaceChar = ' '
  private val width = 120
  private val clearText = " ".repeat(width) + '\r'

  fun print(out: PrintStream) {
    val finished = rubricCollector.gradingFinished.size
    val total = rubricCollector.total
    val progressDecimal = finished.toDouble() / total.toDouble().coerceAtLeast(0.0)
    val formattedPercentage = decimalFormat.format(progressDecimal * 100.0)
    val barCount = barLengthFull * progressDecimal
    val sb = StringBuilder(30)
    sb.append(sideChar)
    val actualBarCount = barCount.toInt()
    for (i in 0 until actualBarCount) {
      sb.append(barChar)
    }
    if (progressDecimal < 1.0) {
      sb.append(tipChar)
    }
    for (i in actualBarCount until barLengthFull) {
      sb.append(whitespaceChar)
    }
    sb.append(sideChar)
    sb.append(whitespaceChar)
    sb.append(formattedPercentage)
    sb.append('%')
    sb.append(" ($finished/$total)")
    if (rubricCollector.gradingScheduled.size in 1 until showElementsIfLessThan) {
      sb.append(" Remaining: [${rubricCollector.gradingScheduled.joinToString { it.request.submission.toString() }}]")
    }
    // pad with spaces
    sb.append(" ".repeat((width - sb.length).coerceAtLeast(0)))
    out.print(sb.toString() + '\r')
  }

  fun clear(out: PrintStream) = out.print(clearText)
}
