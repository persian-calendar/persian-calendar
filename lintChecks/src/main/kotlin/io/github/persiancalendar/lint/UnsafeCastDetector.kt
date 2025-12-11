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
import org.jetbrains.uast.UBinaryExpressionWithType

class UnsafeCastDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UBinaryExpressionWithType::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitBinaryExpressionWithType(node: UBinaryExpressionWithType) {
            if (node.sourcePsi?.text?.contains(" as ") == true) context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use safe cast `as?` instead of unsafe cast `as` to avoid ClassCastException"
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "UnsafeCast",
            briefDescription = "Unsafe cast usage",
            explanation = "Use safe cast `as?` instead of unsafe cast `as` to avoid ClassCastException",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                UnsafeCastDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}

