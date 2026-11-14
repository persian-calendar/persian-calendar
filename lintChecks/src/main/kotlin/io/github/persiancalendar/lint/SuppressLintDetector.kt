package io.github.persiancalendar.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UAnnotation

/**
 * Prefer `kotlin.Suppress` over `android.annotation.SuppressLint` so suppressions work
 * across Kotlin targets.
 */
class SuppressLintDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UAnnotation::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitAnnotation(node: UAnnotation) {
            if (context.isVendoredSource()) return
            if (node.qualifiedName != "android.annotation.SuppressLint") return
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use `kotlin.Suppress(...)` instead of `android.annotation.SuppressLint(...)`.",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "SuppressLintUsage",
            briefDescription = "`android.annotation.SuppressLint` usage",
            explanation = "Prefer `kotlin.Suppress(...)` over `android.annotation.SuppressLint(...)` so suppressions work across Kotlin targets.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                SuppressLintDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
