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

package org.sourcegrade.jagr.core.executor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.ClassTransformer
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.extractorOf
import org.sourcegrade.jagr.core.compiler.java.RuntimeJarLoader
import org.sourcegrade.jagr.core.compiler.java.compile
import org.sourcegrade.jagr.core.compiler.java.loadCompiled
import org.sourcegrade.jagr.core.compiler.java.plus
import org.sourcegrade.jagr.core.compiler.submissionInfo
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.testing.SubmissionInfoImpl
import org.sourcegrade.jagr.core.transformer.SubmissionVerificationTransformer
import org.sourcegrade.jagr.core.transformer.applierOf
import org.sourcegrade.jagr.core.transformer.plus
import org.sourcegrade.jagr.core.transformer.useWhen
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.GradingBatch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

class GradingQueueImpl(
  logger: Logger,
  runtimeJarLoader: RuntimeJarLoader,
  commonTransformer: ClassTransformer,
  batch: GradingBatch,
) : GradingQueue {

  /**
   * Libraries that should always be included on the classpath
   * (unlike solutions that should only be present during grader compilation).
   */
  private val baseRuntimeLibraries = runtimeJarLoader.loadCompiled(batch.submissionLibraries)

  /**
   * Libraries that should be used for grader compilation.
   */
  private val graderRuntimeLibraries = baseRuntimeLibraries + runtimeJarLoader.loadCompiled(batch.graderLibraries)

  private val commonTransformerApplier = applierOf(commonTransformer)

  override val graders: List<GraderJar> = batch.graders.compile(
    logger, commonTransformerApplier, runtimeJarLoader, graderRuntimeLibraries, "grader",
  ) {
    if (errors == 0) GraderJarImpl(logger, this, graderRuntimeLibraries) else null
  }

  private val baseSubmissionTransformerApplier = applierOf(SubmissionVerificationTransformer(), commonTransformer)

  /**
   * Create a transformer applier that selectively applies transformations to
   * submissions only if the grader contains a rubric for it
   */
  private val submissionTransformerApplier = graders.map { graderJar ->
    graderJar.configuration.transformers useWhen { result ->
      result.submissionInfo?.assignmentId?.let(graderJar.rubricProviders::containsKey) == true
    }
  }.fold(baseSubmissionTransformerApplier) { a, b -> a + b }

  override val submissions: List<Submission> = batch.submissions.compile(
    logger, submissionTransformerApplier, runtimeJarLoader, baseRuntimeLibraries, "submission",
    extractorOf(SubmissionInfoImpl.Extractor),
  ) {
    val submissionInfo = this.submissionInfo
    if (submissionInfo == null) {
      logger.error("${container.name} does not have a submission-info.json! Skipping...")
      return@compile null
    }
    JavaSubmission(submissionInfo, this, baseRuntimeLibraries)
  }

  private val submissionIterator: Iterator<Submission> = submissions.iterator()
  private val mutex = Mutex()

  override val total: Int = submissions.size

  private val _remaining = AtomicInteger(total)

  override val remaining: Int
    get() = _remaining.get()

  override val startedUtc: Instant = OffsetDateTime.now(ZoneOffset.UTC).toInstant()

  @Volatile
  override var finishedUtc: Instant? = null

  override suspend fun next(): GradingRequest? {
    if (finishedUtc != null) return null
    return mutex.withLock {
      // check if next() was called while the last grading request is being processed by another worker
      if (finishedUtc != null) {
        null
      } else if (submissionIterator.hasNext()) {
        _remaining.getAndDecrement()
        GradingRequestImpl(submissionIterator.next(), graders, baseRuntimeLibraries, graderRuntimeLibraries)
      } else {
        finishedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
        null
      }
    }
  }
}
