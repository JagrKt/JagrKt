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

package org.jagrkt.common

import com.google.inject.Guice
import org.jagrkt.launcher.LaunchWrapper
import org.jagrkt.launcher.configuration.LaunchConfiguration
import org.jagrkt.launcher.executor.GradingContext

class CommonLaunchWrapper : LaunchWrapper {
  override fun launch(configuration: LaunchConfiguration, context: GradingContext) {
    val injector = Guice.createInjector(CommonModule(configuration))
    injector.getInstance(JagrKtImpl::class.java).launch(configuration, context)
  }
}
