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
import org.jetbrains.uast.UCallExpression

/** Prefer the `in` operator over `contains(...)` when the receiver supports it. */
class ContainsOperatorDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            if (context.isVendoredSource()) return
            if (node.methodName != "contains") return
            if (node.valueArguments.size != 1) return
            if (node.valueArguments.singleOrNull()?.getExpressionType()?.canonicalText ==
                "kotlin.text.Regex"
            ) return
            // A safe call such as `collection?.contains(item)` has no direct `in` equivalent.
            if (node.sourcePsi?.parent?.javaClass?.simpleName
                    ?.contains("SafeQualified") == true
            ) return
            val containingClass = node.resolve()?.containingClass?.qualifiedName ?: return
            if (containingClass != "androidx.compose.ui.geometry.Rect" &&
                !containingClass.startsWith("kotlin.collections.") &&
                !containingClass.startsWith("kotlin.ranges.") &&
                !containingClass.startsWith("kotlin.sequences.") &&
                !containingClass.startsWith("kotlin.text.")
            ) return
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use the `in` operator instead of `contains(...)`.",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ContainsUsage",
            briefDescription = "`contains(...)` usage where `in` works",
            explanation = "Use the `in` operator (`item in collection`, `point in bounds`) instead of the equivalent `contains(...)` call.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                ContainsOperatorDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
