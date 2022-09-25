package org.sourcegrade.jagr.gradle.task.submission

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.JagrExtension
import org.sourcegrade.jagr.gradle.SubmissionConfiguration
import org.sourcegrade.jagr.gradle.getFiles
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import java.io.File

@Suppress("LeakingThis")
abstract class SubmissionWriteInfoTask : DefaultTask(), SubmissionTask {

    @get:OutputFile
    val submissionInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/submission-info.json") })

    init {
        dependsOn("compileJava")
    }

    @TaskAction
    fun runTask() {
        val jagr = project.extensions.getByType<JagrExtension>()
        val submissionInfo = SubmissionInfo(
            assignmentId.get(),
            studentId.get(),
            firstName.get(),
            lastName.get(),
            jagr.submissions[configurationName.get()].sourceSets.map { SourceSetInfo(it.name, it.getFiles()) },
        )
        submissionInfoFile.get().apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(submissionInfo))
        }
    }

    internal object Factory : JagrTaskFactory<SubmissionWriteInfoTask, SubmissionConfiguration> {
        override fun determineTaskName(name: String) = "${name}WriteSubmissionInfo"
        override fun configureTask(task: SubmissionWriteInfoTask, project: Project, configuration: SubmissionConfiguration) {
            task.description = "Writes the submission info to a file"
        }
    }
}
