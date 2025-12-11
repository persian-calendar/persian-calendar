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
import org.jetbrains.uast.UPostfixExpression

class DoubleBangDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UPostfixExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitPostfixExpression(node: UPostfixExpression) {
            if (node.operator.text == "!!") context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Avoid Use of `!!`, use (`?.`) or (`?:`) with proper fallback and consider `debugAssertNotNull`."
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "DoubleBangUsage",
            briefDescription = "Not-null assertion operator usage",
            explanation = "Avoid Use of `!!`, use (`?.`) or (`?:`) with proper fallback and consider `debugAssertNotNull`.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                DoubleBangDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            )
        )
    }
}

