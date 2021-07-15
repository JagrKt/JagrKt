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

package org.sourcegrade.jagr.common

import com.google.inject.Guice
import org.slf4j.Logger

fun main(vararg args: String) {
  val startTime = System.currentTimeMillis()
  val injector = Guice.createInjector(JagrKtModule())
  val logger = injector.getInstance(Logger::class.java)
  logger.info("Starting JagrKt")
  injector.getInstance(JagrKtImpl::class.java).run()
  val timeTaken = System.currentTimeMillis() - startTime
  logger.info("Finished! Time taken: ${timeTaken}ms")
}
