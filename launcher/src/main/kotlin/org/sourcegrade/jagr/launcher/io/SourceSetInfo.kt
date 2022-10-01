/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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

package org.sourcegrade.jagr.launcher.io

import kotlinx.serialization.Serializable

@Serializable
data class SourceSetInfo(
    val name: String,
    val files: Map<String, Set<String>>,
) {
    companion object Factory : SerializerFactory<SourceSetInfo> {
        override fun read(scope: SerializationScope.Input) = SourceSetInfo(scope.input.readUTF(), scope.readMap())

        override fun write(obj: SourceSetInfo, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.name)
            scope.writeMap(obj.files)
        }
    }
}
