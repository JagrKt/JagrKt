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

package org.sourcegrade.jagr.core

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.slf4j.Logger
import org.sourcegrade.jagr.api.rubric.*
import org.sourcegrade.jagr.api.testing.extension.*
import org.sourcegrade.jagr.core.executor.*
import org.sourcegrade.jagr.core.export.rubric.*
import org.sourcegrade.jagr.core.export.submission.EclipseSubmissionExporter
import org.sourcegrade.jagr.core.export.submission.GradleSubmissionExporter
import org.sourcegrade.jagr.core.io.*
import org.sourcegrade.jagr.core.rubric.*
import org.sourcegrade.jagr.core.rubric.grader.*
import org.sourcegrade.jagr.core.testing.*
import org.sourcegrade.jagr.launcher.env.*
import org.sourcegrade.jagr.launcher.executor.*
import org.sourcegrade.jagr.launcher.io.*

/**
 * Shared bindings between main and testing guice modules
 */
@Suppress("unused") // src/main/resources/jagr.json
class CommonModule(private val configuration: LaunchConfiguration) : AbstractModule() {
  object Factory : ModuleFactory {
    override fun create(configuration: LaunchConfiguration) = CommonModule(configuration)
  }

  override fun configure() {
    bind(GradingQueue.Factory::class.java).to(GradingQueueFactoryImpl::class.java)
    bind(RuntimeGrader::class.java).to(RuntimeGraderImpl::class.java)
    bind(SerializerFactory.Locator::class.java).to(SerializationFactoryLocatorImpl::class.java)

    bind(Criterion.Factory::class.java).to(CriterionFactoryImpl::class.java)
    bind(CriterionHolderPointCalculator.Factory::class.java).to(CriterionHolderPointCalculatorFactoryImpl::class.java)
    bind(Grader.Factory::class.java).to(GraderFactoryImpl::class.java)
    bind(GradeResult.Factory::class.java).to(GradeResultFactoryImpl::class.java)
    bind(JUnitTestRef.Factory::class.java).to(JUnitTestRefFactoryImpl::class.java)
    bind(Rubric.Factory::class.java).to(RubricFactoryImpl::class.java)
    bind(TestCycleResolver.Internal::class.java).to(TestCycleParameterResolver::class.java)

    bind(GradedRubricExporter.CSV::class.java).to(GermanCSVExporter::class.java)
    bind(GradedRubricExporter.HTML::class.java).to(MoodleJSONExporter::class.java)
    bind(SubmissionExporter.Gradle::class.java).to(GradleSubmissionExporter::class.java)
    bind(SubmissionExporter.Eclipse::class.java).to(EclipseSubmissionExporter::class.java)
    with(Multibinder.newSetBinder(binder(), RuntimeTester::class.java)) {
      addBinding().to(JavaRuntimeTester::class.java)
    }

    bind(Logger::class.java).toInstance(configuration.logger)
    with(configuration.configurationLoader) {
      load().let { root ->
        if (root.empty()) Config().also { root.set(it).also(::save) }
        else root[Config::class.java]
      }.also(bind(Config::class.java)::toInstance)
    }

    requestStaticInjection(
      Criterion.FactoryProvider::class.java,
      CriterionHolderPointCalculator.FactoryProvider::class.java,
      Grader.FactoryProvider::class.java,
      GradeResult.FactoryProvider::class.java,
      JUnitTestRef.FactoryProvider::class.java,
      Rubric.FactoryProvider::class.java,
      TestCycleResolver.Provider::class.java,
      TimeoutHandler::class.java,
    )
  }
}
