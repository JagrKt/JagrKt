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

package org.sourcegrade.jagr.launcher.executor

import org.sourcegrade.jagr.launcher.env.Jagr
import java.io.Closeable

/**
 * A resource which may, or may not give you another [Worker] depending on available resources.
 */
interface WorkerPool : Closeable {

    suspend fun <T> withActiveWorkers(block: (List<Worker>) -> T): T

    /**
     * Creates up to [maxCount] workers depending on availability.
     */
    fun createWorkers(maxCount: Int): List<Worker>

    /**
     * Closes this [WorkerPool] and any resources associated with it. (e.g. Extra monitor/IO threads)
     */
    override fun close()

    interface Factory {
        fun create(jagr: Jagr): WorkerPool
    }
}
