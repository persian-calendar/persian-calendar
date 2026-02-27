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

class HapticFeedbackDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames() = listOf("performHapticFeedback")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val message = when (method.containingClass?.qualifiedName) {
            "android.view.View" -> {
                "Direct call to View.performHapticFeedback() is not allowed.\nUse com.byagowi.persiancalendar.ui.utils provided alternatives."
            }

            "androidx.compose.ui.hapticfeedback.HapticFeedback" -> {
                "Direct call to HapticFeedback.performHapticFeedback() is not allowed.\nUse com.byagowi.persiancalendar.ui.utils provided alternatives."
            }

            else -> return
        }

        context.report(
            issue = ISSUE,
            location = context.getLocation(node),
            message = message,
        )
    }

    companion object {
        private val ISSUE = Issue.create(
            id = "DirectHapticFeedbackUsage",
            briefDescription = "Direct haptic feedback call",
            explanation = "Direct calls to `performHapticFeedback()` should be avoided.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HapticFeedbackDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}

