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

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.launcher.executor.RuntimeInvoker
import org.sourcegrade.jagr.launcher.executor.RuntimeJarInvoker
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.io.File

interface LaunchConfiguration {
    val config: Config
    val logger: Logger
    val runtimeInvokerFactory: RuntimeInvoker.Factory

    object Standard : LaunchConfiguration {
        override val config: Config by lazy {
            val loader = HoconConfigurationLoader.builder()
                .file(File("jagr.conf"))
                .defaultOptions { options ->
                    options.serializers { builder ->
                        builder.registerAnnotatedObjects(objectMapperFactory())
                    }
                }
                .build()
            val result: Config? = loader.load().let { root ->
                if (root.empty()) {
                    val config = Config()
                    root.set(config)
                    loader.save(root)
                    config
                } else {
                    root[Config::class.java]
                }
            }
            checkNotNull(result) { "Failed to load configuration" }
        }
        override val logger: Logger by lazy {
            LogManager.getLogger("Jagr")
        }
        override val runtimeInvokerFactory: RuntimeInvoker.Factory = RuntimeInvoker.Factory { jagr -> RuntimeJarInvoker(jagr) }
    }
}
