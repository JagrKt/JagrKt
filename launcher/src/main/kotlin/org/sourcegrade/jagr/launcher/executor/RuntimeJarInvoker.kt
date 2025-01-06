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

package org.sourcegrade.jagr.launcher.executor

import org.apache.logging.log4j.Logger
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

class RuntimeJarInvoker(
    private val logger: Logger,
    private val jagrLocation: Path = Paths.get(RuntimeJarInvoker::class.java.protectionDomain.codeSource.location.toURI()),
    private val jvmArgs: List<String> = emptyList(),
) : RuntimeInvoker {

    private val commands: List<String> = buildList(5 + jvmArgs.size) {
        add("java")
        add("-Dlog4j.configurationFile=log4j2-child.xml")
        addAll(jvmArgs)
        add("-jar")
        add(jagrLocation.pathString)
        add("--child")
    }

    override fun createRuntime(): Process {
        logger.info("Starting process $commands")
        return ProcessBuilder().command(commands).start()
    }
}
