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
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression

class DebugAssertNotNullSafeCallDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UQualifiedReferenceExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
            val selector = node.selector as? USimpleNameReferenceExpression ?: return
            if (selector.identifier != "debugAssertNotNull") return

            val sourceText = node.sourcePsi?.text ?: return
            if ("?.debugAssertNotNull" in sourceText) context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use `.debugAssertNotNull` instead of `?.debugAssertNotNull`.",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "DebugAssertNotNullSafeCall",
            briefDescription = "debugAssertNotNull called with safe call operator",
            explanation = "Use `.debugAssertNotNull` instead of `?.debugAssertNotNull`.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                DebugAssertNotNullSafeCallDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
