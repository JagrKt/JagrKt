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

package org.sourcegrade.jagr

import kotlinx.coroutines.runBlocking
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.config
import org.sourcegrade.jagr.launcher.env.extrasManager
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.GradingResult
import org.sourcegrade.jagr.launcher.executor.MultiWorkerExecutor
import org.sourcegrade.jagr.launcher.executor.ProcessWorkerPool
import org.sourcegrade.jagr.launcher.executor.ProgressBar
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.ThreadWorkerPool
import org.sourcegrade.jagr.launcher.executor.createProgressBarProvider
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.ProgressAwareOutputStream
import org.sourcegrade.jagr.launcher.io.SubmissionExporter
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.export
import org.sourcegrade.jagr.launcher.io.logGradedRubric
import org.sourcegrade.jagr.launcher.io.logHistogram
import org.sourcegrade.jagr.launcher.io.writeAsDirIn
import org.sourcegrade.jagr.launcher.io.writeIn
import java.io.File

class StandardGrading(
    private val progressBarName: String?,
    private val jagr: Jagr = Jagr,
) {
    private val config = jagr.config
    private val rubricsFile = File(config.dir.rubrics).ensure(jagr.logger)!!
    private val csvDir = checkNotNull(rubricsFile.resolve("csv").ensure(jagr.logger)) { "rubrics/csv directory" }
    private val moodleDir = checkNotNull(rubricsFile.resolve("moodle").ensure(jagr.logger)) { "rubrics/moodle directory" }
    private val labDir = checkNotNull(rubricsFile.resolve("lab").ensure(jagr.logger)) { "rubrics/lab directory" }
    private val csvExporter = jagr.injector.getInstance(GradedRubricExporter.CSV::class.java)
    private val moodleExporter = jagr.injector.getInstance(GradedRubricExporter.Moodle::class.java)
    private val labExporter = jagr.injector.getInstance(GradedRubricExporter.Lab::class.java)

    fun grade(noExport: Boolean, exportOnly: Boolean) = runBlocking {
        jagr.logger.info("Starting Jagr v${Jagr.version}")
        File(config.dir.submissions).ensure(jagr.logger)
        jagr.extrasManager.runExtras()
        val batch = buildGradingBatch {
            discoverGraders(config.dir.graders) { _, n -> n.endsWith("jar") }
            discoverSubmissions(config.dir.submissions) { _, n -> n.endsWith("jar") }
            discoverLibraries(config.dir.libs) { _, n -> n.endsWith("jar") }
        }
        val queue = jagr.gradingQueueFactory.create(batch)
        if (!noExport) {
            exportSubmissions(queue)
        }
        if (exportOnly) {
            jagr.logger.info("Only exporting, finished!")
            return@runBlocking
        }
        val mode = config.executor.mode
        jagr.logger.info("Executor mode '$mode' :: expected submission: ${batch.expectedSubmissions}")
        val executor = if (mode == "single") {
            SyncExecutor(jagr)
        } else {
            MultiWorkerExecutor.Factory {
                workerPoolFactory = when (mode) {
                    "process" -> ProcessWorkerPool.Factory { concurrency = config.executor.concurrency }
                    "thread" -> ThreadWorkerPool.Factory { concurrency = config.executor.concurrency }
                    else -> error("Invalid executor mode $mode. Must be one of \"single\", \"thread\" or \"process\".")
                }
            }.create(jagr)
        }
        val collector = emptyCollector(jagr)
        val progress = ProgressBar(collector, createProgressBarProvider(progressBarName))
        ProgressAwareOutputStream.progressBar = progress
        collector.setListener { result ->
            result.rubrics.keys.forEach { it.logGradedRubric(jagr) }
            exportRubrics(result)
        }
        collector.allocate(queue)
        executor.schedule(queue)
        executor.start(collector)
        ProgressAwareOutputStream.progressBar = null
        Environment.cleanupMainProcess()
        val rubricCount = collector.withGradingFinished { gradingFinished ->
            gradingFinished.logHistogram(jagr)
            gradingFinished.sumOf { it.rubrics.size }
        }
        if (rubricCount == 0) {
            jagr.logger.warn("No rubrics!")
        } else {
            jagr.logger.info("Exported $rubricCount rubrics")
        }
    }

    private fun exportSubmissions(queue: GradingQueue) {
        jagr.logger.info("Beginning export")
        val submissionExportFile = File(config.dir.submissionsExport).ensure(jagr.logger)!!
        for (resourceContainer in jagr.injector.getInstance(SubmissionExporter.Gradle::class.java).export(queue)) {
            resourceContainer.writeAsDirIn(submissionExportFile)
        }
    }

    private fun exportRubrics(result: GradingResult) {
        for ((gradedRubric, _) in result.rubrics) {
            csvExporter.exportSafe(gradedRubric, csvDir)
            moodleExporter.exportSafe(gradedRubric, moodleDir)
            labExporter.exportSafe(gradedRubric, labDir)
        }
    }

    private fun GradedRubricExporter.exportSafe(gradedRubric: GradedRubric, file: File) {
        val resource = try {
            export(gradedRubric)
        } catch (e: Exception) {
            jagr.logger.error("Could not create export resource for ${gradedRubric.testCycle.submission.info}", e)
            return
        }
        try {
            resource.writeIn(file)
        } catch (e: Exception) {
            jagr.logger.error("Could not export resource ${resource.name}", e)
        }
    }
}
