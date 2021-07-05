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

package org.jagrkt.launcher

import org.jagrkt.launcher.configuration.LaunchConfiguration
import org.jagrkt.launcher.executor.GradingContext
import org.jagrkt.launcher.locate.ImplLocator
import kotlin.reflect.jvm.isAccessible

internal class StandardLauncher : Launcher {
  override fun launch(
    configuration: LaunchConfiguration,
    context: GradingContext,
    implLocator: ImplLocator,
  ) {
    val launchWrapperClass = implLocator.locate()
    val constructor = launchWrapperClass.constructors.firstOrNull { it.isAccessible && it.parameters.isEmpty() }
      ?: throw IllegalArgumentException(
        "LaunchWrapper implementation class ${launchWrapperClass.qualifiedName} does not have an accessible no-args constructor"
      )
    constructor.call().launch(configuration, context)
  }
}
