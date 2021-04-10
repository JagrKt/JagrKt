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

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.CriterionHolderPointCalculator
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.rubric.JUnitTestRef
import org.jagrkt.api.rubric.Rubric
import org.jagrkt.common.export.rubric.GermanCSVExporter
import org.jagrkt.common.export.rubric.GradedRubricExporter
import org.jagrkt.common.export.rubric.MoodleJSONExporter
import org.jagrkt.common.export.submission.EclipseSubmissionExporter
import org.jagrkt.common.export.submission.SubmissionExporter
import org.jagrkt.common.rubric.CriterionFactoryImpl
import org.jagrkt.common.rubric.CriterionHolderPointCalculatorFactoryImpl
import org.jagrkt.common.rubric.GradeResultFactoryImpl
import org.jagrkt.common.rubric.JUnitTestRefFactoryImpl
import org.jagrkt.common.rubric.RubricFactoryImpl
import org.jagrkt.common.rubric.grader.GraderFactoryImpl
import org.jagrkt.common.testing.RuntimeTester
import org.jagrkt.common.testing.JavaRuntimeTester
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JagrKtModule : AbstractModule() {
  override fun configure() {
    bind(Logger::class.java).toInstance(LoggerFactory.getLogger("JagrKt"))

    bind(Criterion.Factory::class.java).to(CriterionFactoryImpl::class.java)
    bind(CriterionHolderPointCalculator.Factory::class.java).to(CriterionHolderPointCalculatorFactoryImpl::class.java)
    bind(Grader.Factory::class.java).to(GraderFactoryImpl::class.java)
    bind(GradeResult.Factory::class.java).to(GradeResultFactoryImpl::class.java)
    bind(JUnitTestRef.Factory::class.java).to(JUnitTestRefFactoryImpl::class.java)
    bind(Rubric.Factory::class.java).to(RubricFactoryImpl::class.java)

    requestStaticInjection(
      Criterion.FactoryProvider::class.java,
      CriterionHolderPointCalculator.FactoryProvider::class.java,
      Grader.FactoryProvider::class.java,
      GradeResult.FactoryProvider::class.java,
      JUnitTestRef.FactoryProvider::class.java,
      Rubric.FactoryProvider::class.java,
    )

    with(Multibinder.newSetBinder(binder(), GradedRubricExporter::class.java)) {
      addBinding().to(GermanCSVExporter::class.java)
      addBinding().to(MoodleJSONExporter::class.java)
    }
    with(Multibinder.newSetBinder(binder(), SubmissionExporter::class.java)) {
      addBinding().to(EclipseSubmissionExporter::class.java)
    }
    with(Multibinder.newSetBinder(binder(), RuntimeTester::class.java)) {
      addBinding().to(JavaRuntimeTester::class.java)
    }
  }
}
