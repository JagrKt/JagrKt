package org.sourcegrade.jagr.gradle.task.grader

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.extension.GraderConfiguration
import org.sourcegrade.jagr.gradle.extension.JagrDownloadExtension
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.extension.ProjectSourceSetTuple
import org.sourcegrade.jagr.gradle.extension.relative
import org.sourcegrade.jagr.gradle.task.JagrDownloadTask
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.gradle.task.submission.SubmissionWriteInfoTask
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.SystemResourceJagrFactory
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.Executor
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProcessWorkerPool
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.GradingBatch
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.addResource
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.buildResourceContainer
import org.sourcegrade.jagr.launcher.io.buildResourceContainerInfo
import org.sourcegrade.jagr.launcher.io.createResourceContainer
import org.sourcegrade.jagr.launcher.io.logGradedRubric
import org.sourcegrade.jagr.launcher.io.logHistogram
import org.sourcegrade.jagr.launcher.io.writeAsDirIn
import org.sourcegrade.jagr.launcher.io.writeIn
import java.io.File
import java.net.URI
import java.nio.file.Path

@Suppress("LeakingThis")
abstract class GraderRunTask : DefaultTask(), GraderTask {

    @get:InputFile
    val graderInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/grader-info.json") })

    @get:InputFile
    val submissionInfoFile: Property<File> = project.objects.property<File>()
        .value(submissionConfigurationName.map { project.buildDir.resolve("resources/jagr/$it/submission-info.json") })

    @get:Input
    val jagrJar: Property<Path> = project.objects.property()

    init {
        group = "verification"
        dependsOn(submissionConfigurationName.map(SubmissionWriteInfoTask.Factory::determineTaskName))
        dependsOn(configurationName.map(GraderWriteInfoTask.Factory::determineTaskName))
        dependsOn("jagrDownload")
    }

    private fun GraderConfiguration.getConfigRecursive(): Config {
        return if (config.isPresent) {
            config.get()
        } else if (parentConfiguration.isPresent) {
            parentConfiguration.get().getConfigRecursive()
        } else {
            Config()
        }
    }

    @TaskAction
    fun runTask() {
        runBlocking {
            grade()
        }
    }

    private suspend fun grade() {
        val jagrExtension = project.extensions.getByType<JagrExtension>()
        val configuration = jagrExtension.graders[configurationName.get()]
        val jagr = SystemResourceJagrFactory.create(GradleLaunchConfiguration(configuration.getConfigRecursive(), jagrJar.get()))
        jagr.logger.info("Starting Jagr v${Jagr.version}")
        val exporterHTML = jagr.injector.getInstance(GradedRubricExporter.HTML::class.java)
        val exporterMoodle = jagr.injector.getInstance(GradedRubricExporter.Moodle::class.java)
        val batch: GradingBatch = createGradingBatch(jagrExtension, configuration)
        val queue = jagr.gradingQueueFactory.create(batch)
        jagr.logger.info("Executor mode 'gradle' :: expected submission: ${batch.expectedSubmissions}")
        val executor: Executor = MultiWorkerExecutor.Factory {
            workerPoolFactory = ProcessWorkerPool.Factory { concurrency = 1 }
        }.create(jagr)
        val collector = emptyCollector(jagr)
        // TODO: Properly configure task output
        val rubricOutputDir = project.buildDir.resolve("resources/jagr/${configurationName.get()}/rubrics/")
        val rubrics = mutableMapOf<String, Boolean>()
        collector.setListener { result ->
            result.rubrics.keys.forEach {
                it.logGradedRubric(jagr)
                val resource = exporterHTML.export(it)
                resource.writeIn(rubricOutputDir)
                // whether the given rubric failed
                rubrics[resource.name] = it.grade.maxPoints < it.rubric.maxPoints
                val moodleResource = exporterMoodle.export(it)
                moodleResource.writeIn(rubricOutputDir)
            }
        }
        collector.allocate(queue)
        executor.schedule(queue)
        executor.start(collector)
        Environment.cleanupMainProcess()
        collector.withGradingFinished { gradingFinished ->
            gradingFinished.logHistogram(jagr)
        }
        fun String.toRubricLink() =
            URI("file", "", rubricOutputDir.toURI().path + this, null, null).toString()
        if (rubrics.isEmpty()) {
            jagr.logger.warn("No rubrics!")
        } else {
            jagr.logger.info("Exported ${rubrics.size} rubrics:")
            rubrics.forEach { (name, failed) ->
                val rubricLink = name.toRubricLink()
                jagr.logger.info(" > $rubricLink ${if (failed) " (failed)" else ""}")
            }
            if (rubrics.any { (_, failed) -> failed }) {
                throw GradleException(
                    """
                    Grading completed with failing tests! See the rubric${if (rubrics.size == 1) "" else "s"} at:
                    ${rubrics.filter { (_, failed) -> failed }.keys.joinToString("\n") { " > ${it.toRubricLink()}" }}
                    """.trimIndent(),
                )
            }
        }
    }

    private fun createGradingBatch(jagrExtension: JagrExtension, configuration: GraderConfiguration) = buildGradingBatch {
        addGrader(
            buildResourceContainer {
                info = buildResourceContainerInfo {
                    name = "grader"
                }
                val allSourceSets = configuration.getSourceSetNamesRecursive() + configuration.getSolutionSourceSetNamesRecursive()
                allSourceSets.map { (projectPath, sourceSetName) ->
                    projectPath to checkNotNull(project.relative(projectPath).extensions.getByType<SourceSetContainer>()[sourceSetName])
                }.sortedByDescending { (projectPath, sourceSet) ->
                    getRecursiveDepthOfSourceSet(configuration, ProjectSourceSetTuple(projectPath, sourceSet.name), 0)
                }.forEach { (_, sourceSet) -> writeSourceSet(sourceSet) }
                addResource {
                    name = "grader-info.json"
                    graderInfoFile.get().inputStream().use { input ->
                        outputStream.use { output ->
                            input.transferTo(output)
                        }
                    }
                }
            },
        )
        addSubmission(
            buildResourceContainer {
                info = buildResourceContainerInfo {
                    name = "submission"
                }
                for (sourceSet in jagrExtension.submissions[submissionConfigurationName.get()].sourceSets) {
                    writeSourceSet(sourceSet)
                }
                addResource {
                    name = "submission-info.json"
                    submissionInfoFile.get().inputStream().use { input ->
                        outputStream.use { output ->
                            input.transferTo(output)
                        }
                    }
                }
            },
        )
        val allSourceSets: Set<ProjectSourceSetTuple> = configuration.getSourceSetNamesRecursive() +
            jagrExtension.submissions[solutionConfigurationName.get()].sourceSetNames.get()
        allSourceSets.map { (projectPath, sourceSetName) ->
            project.relative(projectPath).let { it to checkNotNull(it.extensions.getByType<SourceSetContainer>()[sourceSetName]) }
        }.flatMap { (project, sourceSet) ->
            sequenceOf(
                project.configurations[sourceSet.runtimeClasspathConfigurationName],
                project.configurations[sourceSet.compileClasspathConfigurationName],
            ).requireNoNulls()
        }
            .flatMap { it.resolvedConfiguration.resolvedArtifacts }
            .filter {
                !(
                    it.id.componentIdentifier.displayName.startsWith("org.sourcegrade") &&
                        it.id.componentIdentifier.displayName.contains("jagr")
                    )
            }
            .map { it.file }
            .forEach { addLibrary(createResourceContainer(it)) }
    }

    /**
     * Returns the recursive depth of the [sourceSetName] in the parent configurations of the given [graderConfiguration] starting with [depth]
     */
    private fun getRecursiveDepthOfSourceSet(
        graderConfiguration: GraderConfiguration,
        projectSourceSetTuple: ProjectSourceSetTuple,
        depth: Int,
    ): Int {
        if (projectSourceSetTuple in graderConfiguration.getSourceSetNamesRecursive()) {
            return depth
        }
        if (graderConfiguration.parentConfiguration.isPresent) {
            return getRecursiveDepthOfSourceSet(graderConfiguration.parentConfiguration.get(), projectSourceSetTuple, depth + 1)
        }
        return -1
    }

    private fun ResourceContainer.Builder.writeSourceSet(sourceSet: SourceSet) =
        sourceSet.allSource.sourceDirectories.writeToContainer(this)

    private fun FileCollection.writeToContainer(builder: ResourceContainer.Builder) {
        forEach { sourceDirectory ->
            sourceDirectory.walk().filter { it.isFile }.forEach { file ->
                builder.addResource {
                    name = file.relativeTo(sourceDirectory).invariantSeparatorsPath
                    file.inputStream().use { input ->
                        outputStream.use { output ->
                            input.transferTo(output)
                        }
                    }
                }
            }
        }
    }

    internal object Factory : JagrTaskFactory<GraderRunTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}Run"
        override fun configureTask(task: GraderRunTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Runs the ${task.sourceSetNames.get()} grader"
            task.jagrJar.set(
                project.extensions.getByType<JagrExtension>()
                    .extensions.getByType<JagrDownloadExtension>()
                    .destName.map { JagrDownloadTask.JAGR_CACHE.resolve(it) },
            )
        }
    }
}
