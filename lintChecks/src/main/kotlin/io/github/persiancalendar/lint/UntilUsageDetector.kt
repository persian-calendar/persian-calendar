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
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression

/** Prefer Kotlin's `..<` range operator over the equivalent `until` infix function. */
class UntilUsageDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(
        UBinaryExpression::class.java,
        UCallExpression::class.java,
    )

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitBinaryExpression(node: UBinaryExpression) {
            if (context.isVendoredSource()) return
            if (node.operatorIdentifier?.name != "until") return
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use the `..<` range operator instead of `until`.",
            )
        }

        override fun visitCallExpression(node: UCallExpression) {
            if (context.isVendoredSource()) return
            if (node.methodName != "until") return
            val containingClass = node.resolve()?.containingClass?.qualifiedName ?: return
            if (!containingClass.startsWith("kotlin.ranges.")) return
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use the `..<` range operator instead of `until`.",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "UntilUsage",
            briefDescription = "`until` range operator usage",
            explanation = "Prefer `..<` over `until` for the same half-open range; it is clearer and idiomatic Kotlin.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                UntilUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
