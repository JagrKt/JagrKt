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

package org.jagrkt.launcher.locate

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jagrkt.launcher.LaunchWrapper
import kotlin.reflect.KClass

internal class SystemResourceImplLocator : ImplLocator {
  companion object {
    const val RESOURCE_NAME = "/jagrkt.json"
  }

  override fun locate(): KClass<out LaunchWrapper> {
    javaClass.getResourceAsStream(RESOURCE_NAME).use {
      checkNotNull(it) { "Could not find resource $RESOURCE_NAME" }
      val jagrKtJson = Json.decodeFromString<JagrKtJson>(it.bufferedReader().readText())
      return Class.forName(jagrKtJson.implClass).asSubclass(LaunchWrapper::class.java).kotlin
    }
  }
}
