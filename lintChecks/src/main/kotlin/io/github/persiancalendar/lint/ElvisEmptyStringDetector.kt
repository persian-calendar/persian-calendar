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
import org.jetbrains.uast.ULiteralExpression

class ElvisEmptyStringDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(ULiteralExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitLiteralExpression(node: ULiteralExpression) {
            if (!node.isString) return
            val psiNode = node.sourcePsi ?: return
            if (psiNode.text != "\"\"") return
            // PSI parents are always properly connected, unlike UAST parents for wrapped literals
            val parentText = psiNode.parent?.text ?: return
            if (!parentText.contains(Regex("""\?:\s*""(?!")"""))) return
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Replace `?: \"\"` with `.orEmpty()`.",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ElvisEmptyString",
            briefDescription = "Elvis with empty string fallback",
            explanation = "Replace `?: \"\"` with `.orEmpty()` for more idiomatic Kotlin.",
            category = Category.CORRECTNESS,
            priority = 4,
            severity = Severity.WARNING,
            implementation = Implementation(
                ElvisEmptyStringDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
