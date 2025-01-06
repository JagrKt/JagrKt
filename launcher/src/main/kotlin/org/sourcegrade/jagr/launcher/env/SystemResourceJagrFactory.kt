/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2024 Alexander Städing
 *   Copyright (C) 2021-2024 Contributors
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

import kotlinx.serialization.json.Json

object SystemResourceJagrFactory : Jagr.Factory {
    private const val RESOURCE_NAME = "/jagr.json"

    override fun create(configuration: LaunchConfiguration): Jagr {
        javaClass.getResourceAsStream(RESOURCE_NAME).use {
            checkNotNull(it) { "Could not find resource $RESOURCE_NAME" }
            val json = Json.decodeFromString<JagrJson>(it.bufferedReader().readText())
            return DeferredJagr(json, configuration)
        }
    }
}
