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

package org.sourcegrade.jagr.launcher

import org.sourcegrade.jagr.launcher.configuration.LaunchConfiguration
import org.sourcegrade.jagr.launcher.configuration.StandardLaunchConfiguration
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.SystemResourceEnvironmentFactory
import org.sourcegrade.jagr.launcher.env.launchWrapper
import org.sourcegrade.jagr.launcher.executor.Executor
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProgressBar
import org.sourcegrade.jagr.launcher.executor.RubricCollector
import org.sourcegrade.jagr.launcher.executor.RubricCollectorImpl
import org.sourcegrade.jagr.launcher.io.GradingBatch

open class Jagr(
  val environment: Environment,
  val configuration: LaunchConfiguration,
  val executorFactory: Executor.Factory,
) {

  companion object Default : Jagr(
    SystemResourceEnvironmentFactory.create(),
    StandardLaunchConfiguration,
    MultiWorkerExecutor.Factory,
  )

  fun launch(batch: GradingBatch): RubricCollector {
    val collector = RubricCollectorImpl(batch.expectedSubmissions)
    val progressBar = ProgressBar(collector)
    environment.launchWrapper.launch(configuration, batch, executorFactory.create(collector, environment))
    return collector // TODO: Maybe different return type?
  }
}

fun Jagr(from: Jagr = Jagr.Default, builderAction: JagrBuilder.() -> Unit): Jagr = JagrBuilder(from).also(builderAction).build()

class JagrBuilder internal constructor(jagr: Jagr) {
  var environment: Environment = jagr.environment
  var configuration: LaunchConfiguration = jagr.configuration
  var executorFactory: Executor.Factory = jagr.executorFactory
  fun build() = Jagr(environment, configuration, executorFactory)
}
