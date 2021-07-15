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

package org.sourcegrade.jagr.common.testing

import com.google.inject.Singleton
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.api.testing.extension.TestCycleResolver
import kotlin.reflect.KClass

sealed class ThreadedGlobalParameterResolver<T : Any>(private val type: KClass<T>) : ParameterResolver {

  private val valueStorage: InheritableThreadLocal<T> = InheritableThreadLocal()
  var value: T
    get() = valueStorage.get()
    set(value) = valueStorage.set(value)

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
    parameterContext.parameter.type == type.java

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): T = value
}

@Singleton
class TestCycleParameterResolver : ThreadedGlobalParameterResolver<TestCycle>(TestCycle::class), TestCycleResolver.Internal
