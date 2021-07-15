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

package org.sourcegrade.jagr.common

import com.google.inject.multibindings.Multibinder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sourcegrade.jagr.common.export.rubric.GermanCSVExporter
import org.sourcegrade.jagr.common.export.rubric.GradedRubricExporter
import org.sourcegrade.jagr.common.export.rubric.MoodleJSONExporter
import org.sourcegrade.jagr.common.export.submission.EclipseSubmissionExporter
import org.sourcegrade.jagr.common.export.submission.GradleSubmissionExporter
import org.sourcegrade.jagr.common.export.submission.SubmissionExporter
import org.sourcegrade.jagr.common.testing.JavaRuntimeTester
import org.sourcegrade.jagr.common.testing.RuntimeTester
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.nio.file.Paths

class JagrModule : CommonModule() {
  override fun configure() {
    super.configure()
    bind(Logger::class.java).toInstance(LoggerFactory.getLogger("Jagr"))

    with(Multibinder.newSetBinder(binder(), GradedRubricExporter::class.java)) {
      addBinding().to(GermanCSVExporter::class.java)
      addBinding().to(MoodleJSONExporter::class.java)
    }
    with(Multibinder.newSetBinder(binder(), SubmissionExporter::class.java)) {
      addBinding().to(EclipseSubmissionExporter::class.java)
      addBinding().to(GradleSubmissionExporter::class.java)
    }
    with(Multibinder.newSetBinder(binder(), RuntimeTester::class.java)) {
      addBinding().to(JavaRuntimeTester::class.java)
    }

    val loader = HoconConfigurationLoader.builder().path(Paths.get("./jagr.conf")).build()
    val rootNode = loader.load()
    val config: Config
    if (rootNode.empty()) {
      rootNode.set(Config().apply { config = this })
      loader.save(rootNode)
    } else {
      config = rootNode[Config::class.java]!!
    }
    bind(Config::class.java).toInstance(config)
  }
}
