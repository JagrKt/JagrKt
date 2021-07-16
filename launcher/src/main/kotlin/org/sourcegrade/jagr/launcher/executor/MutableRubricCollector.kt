package org.sourcegrade.jagr.launcher.executor

sealed interface MutableRubricCollector : RubricCollector {
  fun schedule(request: GradingRequest): GradingJob
}
