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

package org.sourcegrade.jagr.launcher.env

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.sourcegrade.jagr.launcher.LaunchWrapper
import org.sourcegrade.jagr.launcher.executor.RuntimeGrader
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * Service bindings from common
 */
interface Environment {
  val injector: Injector

  interface Factory {
    fun create(): Environment
  }
}

operator fun <T : Any> Environment.get(type: KClass<T>): T = injector.getInstance(type.java)

inline fun <reified T : Any> Environment.get(): T = get(T::class)

inline fun <reified T : Any> injected(): ReadOnlyProperty<Environment, T> = ReadOnlyProperty { e, _ -> e.get() }

val Environment.launchWrapper: LaunchWrapper by injected()

val Environment.runtimeGrader: RuntimeGrader by injected()

fun JagrJson.createEnvironment(): Environment {
  val launchWrapper = coerceClass<LaunchWrapper>(launchWrapper)
  val runtimeGrader = coerceClass<RuntimeGrader>(runtimeGrader)
  val modules = moduleFactories.map { coerceClass<ModuleFactory>(it).getConstructor().newInstance().create() }.toTypedArray()
  val injector = Guice.createInjector(BaseModule(launchWrapper, runtimeGrader), *modules)
  return EnvironmentImpl(injector)
}

private inline fun <reified T : Any> coerceClass(implementation: String): Class<out T> {
  return Class.forName(implementation).asSubclass(T::class.java)
}

private class BaseModule(
  private val launchWrapper: Class<out LaunchWrapper>,
  private val runtimeGrader: Class<out RuntimeGrader>,
) : AbstractModule() {
  override fun configure() {
    bind(LaunchWrapper::class.java).to(launchWrapper)
    bind(RuntimeGrader::class.java).to(runtimeGrader)
  }
}

private data class EnvironmentImpl(override val injector: Injector) : Environment
