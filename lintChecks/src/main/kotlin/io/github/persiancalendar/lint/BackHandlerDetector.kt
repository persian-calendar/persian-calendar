package io.github.persiancalendar.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class BackHandlerDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames() = listOf("BackHandler")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.isMemberInClass(method, "androidx.activity.compose.BackHandlerKt")) {
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use `PredictiveBackHandler` instead of `BackHandler` for better user experience with predictive back gestures",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "UseBackHandlerInsteadOfPredictiveBackHandler",
            briefDescription = "BackHandler usage discouraged",
            explanation = """Use `PredictiveBackHandler` instead of `BackHandler` to support
predictive back gestures and provide a better user experience. The predictive back gesture allows
users to preview the back navigation before committing to it.""",
            category = Category.USABILITY,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                BackHandlerDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
