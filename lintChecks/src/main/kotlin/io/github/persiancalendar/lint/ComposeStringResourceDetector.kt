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

class ComposeStringResourceDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames() = listOf("stringResource")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.isMemberInClass(method, "androidx.compose.ui.res.StringResources_androidKt")) {
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "Use `org.jetbrains.compose.resources.stringResource` instead of `androidx.compose.ui.res.stringResource`.",
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ComposeStringResourceUsage",
            briefDescription = "Use Compose Multiplatform stringResource",
            explanation = "The project is migrating to Compose Multiplatform resources. " +
                "Use `org.jetbrains.compose.resources.stringResource` with the generated " +
                "`Res.string.*` accessors instead of the Android-only " +
                "`androidx.compose.ui.res.stringResource`.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                ComposeStringResourceDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
