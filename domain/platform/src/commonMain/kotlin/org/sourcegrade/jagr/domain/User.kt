/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2023 Alexander Städing
 *   Copyright (C) 2021-2023 Contributors
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

package org.sourcegrade.jagr.domain

import org.sourcegrade.kontour.Creates
import org.sourcegrade.kontour.DomainEntity
import org.sourcegrade.kontour.UUID
import org.sourcegrade.kontour.scope.CrudScope

data class User(override val id: UUID) : DomainEntity {

    data class CreateDto(
        val username: String,
        val email: String,
    ) : Creates<User>

    interface DbScope : CrudScope<User, CreateDto> {

        suspend fun User.getUsername(): String

        suspend fun User.getEmail(): String

        suspend fun DomainEntity.Repository<User>.findByUsername(username: String): User?
    }

    companion object Repository : DomainEntity.Repository<User>
}