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

package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.copyExecutor
import org.sourcegrade.jagr.launcher.env.copyTimeout
import org.sourcegrade.jagr.launcher.env.copyTransformers

abstract class GraderConfiguration(
    name: String,
    project: Project,
) : AbstractConfiguration(name, project, setOf(name)) {
    abstract val graderName: Property<String>
    abstract val rubricProviderName: Property<String>
    val parentConfiguration: Property<GraderConfiguration> = project.objects.property()

    private val submissionConfigurationConvention = parentConfiguration.flatMap { it.submissionConfiguration }
        .orElse(DefaultProvider { project.extensions.getByType<JagrExtension>().submissions.findByName("main") })

    val submissionConfiguration: Property<SubmissionConfiguration> = project.objects.property<SubmissionConfiguration>()
        .convention(submissionConfigurationConvention)

    val solutionConfiguration: Property<SubmissionConfiguration> =
        project.objects.property<SubmissionConfiguration>().convention(submissionConfiguration)

    private val compileClasspath: ConfigurableFileCollection = project.objects.fileCollection()
    private val runtimeClasspath: ConfigurableFileCollection = project.objects.fileCollection()

    var config: Config? = null
        private set

    init {
        project.afterEvaluate {
            configureProject()
            if (parentConfiguration.isPresent) {
                addAsDependency(parentConfiguration.get())
            }
            if (solutionConfiguration.isPresent) {
                addAsDependency(solutionConfiguration.get())
            } else {
                throw GradleException(
                    "Grader $name has no solution configuration and the default 'main' submission is not defined",
                )
            }
            for (sourceSet in sourceSets) {
                sourceSet.compileClasspath += compileClasspath
                sourceSet.runtimeClasspath += runtimeClasspath
            }
            // add jagr dependency
            it.dependencies {
                for (sourceSet in sourceSets) {
                    sourceSet.implementationConfigurationName("org.sourcegrade:jagr-launcher:${Jagr.version}") {
                        exclude("org.jetbrains", "annotations")
                    }
                }
            }
        }
    }

    internal fun getSourceSetNamesRecursive(): Set<ProjectSourceSetTuple> {
        val result = mutableSetOf<ProjectSourceSetTuple>()
        result.addAll(sourceSetNames.get())
        if (parentConfiguration.isPresent) {
            result.addAll(parentConfiguration.get().getSourceSetNamesRecursive())
        }
        return result
    }

    internal fun getSolutionSourceSetNamesRecursive(): Set<ProjectSourceSetTuple> {
        val result = mutableSetOf<ProjectSourceSetTuple>()
        result.addAll(solutionConfiguration.get().sourceSetNames.get())
        if (parentConfiguration.isPresent) {
            result.addAll(parentConfiguration.get().getSolutionSourceSetNamesRecursive())
        }
        return result
    }

    private fun addAsDependency(configuration: AbstractConfiguration) {
        // add parent configuration as dependency
        for (sourceSet in configuration.sourceSets) {
            compileClasspath.from(sourceSet.output)
            compileClasspath.from(sourceSet.compileClasspath)
            runtimeClasspath.from(sourceSet.output)
            runtimeClasspath.from(sourceSet.runtimeClasspath)
        }
    }

    fun submission(configuration: SubmissionConfiguration) {
        submissionConfiguration.set(configuration)
    }

    fun solution(configuration: SubmissionConfiguration) {
        solutionConfiguration.set(configuration)
    }

    fun parent(configuration: GraderConfiguration) {
        parentConfiguration.set(configuration)
    }

    fun mutateConfig(block: Config.() -> Config?) {
        config = block(config ?: Config())
    }

    fun disableTimeouts() = mutateConfig {
        copyTransformers { copyTimeout { copy(enabled = false) } }
    }

    fun enableDebug(port: Int = 5005) = mutateConfig {
        copyExecutor {
            copy(
                jvmArgs = jvmArgs +
                    "-agentlib:jdwp=transport=dt_socket,server=n,address=localhost:$port,suspend=y",
            )
        }
    }
}
