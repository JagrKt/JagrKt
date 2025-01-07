package org.sourcegrade.jagr.core.export.rubric

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sourcegrade.jagr.api.rubric.GradedCriterion
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.rubric.JUnitTestRef
import org.sourcegrade.jagr.core.rubric.JUnitTestRefFactoryImpl
import org.sourcegrade.jagr.core.rubric.grader.DescendingPriorityGrader
import org.sourcegrade.jagr.core.rubric.grader.TestAwareGraderImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import org.sourcegrade.jagr.launcher.io.buildResource

class LabExporter : GradedRubricExporter.Lab {

    override fun export(gradedRubric: GradedRubric): Resource {
        val jUnitResult = gradedRubric.testCycle.jUnitResult

        if (jUnitResult != null) {
            val testPlan = jUnitResult.testPlan
            val statusListener = jUnitResult.statusListener

            // Gather detailed test results
            val testResults = testPlan.roots.flatMap { root ->
                // Collect detailed information about each test
                testPlan.getDescendants(root).mapNotNull { testIdentifier ->
                    val testExecutionResult = statusListener.testResults[testIdentifier]

                    // If the test has a result, collect the information
                    testExecutionResult?.let {
                        TestResult(
                            id = testIdentifier.uniqueId,
                            name = testIdentifier.displayName,
                            type = testIdentifier.type.toString(),
                            status = testExecutionResult.status.toString(),
//                            duration = Duration.between(it.startTime, it.endTime).toMillis(),
                            message = testExecutionResult.throwable.orElse(null)?.message,
                            stackTrace = testExecutionResult.throwable.orElse(null)?.stackTraceToString(),
                        )
                    }
                }
            }

            // Get all relevant tests for a grader
            fun getRelevantTests(grader: Grader): List<String> {
                return when (grader) {
                    is TestAwareGraderImpl -> {
                        val testRefs: MutableSet<JUnitTestRef> = mutableSetOf()
                        testRefs.addAll(grader.requirePass.keys)
                        testRefs.addAll(grader.requireFail.keys)

                        testRefs.mapNotNull { ref ->
                            when (ref) {
                                is JUnitTestRefFactoryImpl.Default -> testPlan.roots.flatMap { testPlan.getDescendants(it) }.firstOrNull {
                                    it.source.isPresent && it.source.orElse(null) == ref.testSource
                                }?.uniqueId
                                else -> null
                            }
                        }
                    }
                    is DescendingPriorityGrader -> grader.graders.flatMap { getRelevantTests(it) }
                    else -> emptyList()
                }
            }

            // recursive function to get all criteria with children
            fun getCriteria(criterion: GradedCriterion): Criterion {
                val children = criterion.childCriteria.map { getCriteria(it) }
//                gradedRubric.grade.comments
                val relevantTests = children.flatMap { it.relevantTests ?: emptyList() }.toMutableSet()
                if (criterion.criterion.grader != null) {
                    relevantTests.addAll(getRelevantTests(criterion.criterion.grader!!))
                }
                return Criterion(
                    name = criterion.criterion.shortDescription,
                    archivedPointsMin = criterion.grade.minPoints,
                    archivedPointsMax = criterion.grade.maxPoints,
                    message = criterion.grade.comments.joinToString("<br>") { "<p>$it</p>" },
                    relevantTests = relevantTests.toList(),
                    children = children,
                )
            }

            // Serialize the results to JSON
            val testResultsJson = LabRubric(
                submissionInfo = (gradedRubric.testCycle.submission as JavaSubmission).submissionInfo,
                totalPointsMin = gradedRubric.grade.minPoints,
                totalPointsMax = gradedRubric.grade.maxPoints,
                criteria = gradedRubric.childCriteria.map { getCriteria(it) },
                tests = testResults,
            )
            val jsonString = Json.encodeToString(testResultsJson)

            // Build the Resource with the JSON string
            return buildResource {
                name = "${gradedRubric.testCycle.submission.info}.json"
                outputStream.bufferedWriter().use { it.write(jsonString) }
            }
        } else {
            throw IllegalArgumentException("No JUnitResult present in the test cycle.")
        }
    }

    @Serializable
    data class TestResult(
        val id: String,
        val name: String,
        val type: String,
        val status: String,
//        val duration: Long,
        val message: String? = null,
        val stackTrace: String? = null,
        val children: List<TestResult> = emptyList(),
    )

    @Serializable
    data class Criterion(
        val name: String,
        val archivedPointsMin: Int,
        val archivedPointsMax: Int,
        val message: String? = null,
        val relevantTests: List<String>? = emptyList(),
        val children: List<Criterion> = emptyList(),
    )

    @Serializable
    data class LabRubric(
        val submissionInfo: SubmissionInfo,
        val totalPointsMin: Int,
        val totalPointsMax: Int,
        val criteria: List<Criterion> = emptyList(),
        val tests: List<TestResult> = emptyList(),
    )
}
