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
import org.jetbrains.uast.UTryExpression

class TryCatchDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UTryExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitTryExpression(node: UTryExpression) {
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use `runCatching` instead of `try/catch` for better functional error handling."
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "TryCatchUsage",
            briefDescription = "Try-catch block usage",
            explanation = "Use `runCatching { }.onFailure { }` instead of `try/catch` blocks for better functional error handling and consistency with Kotlin idioms.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                TryCatchDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            )
        )
    }
}
