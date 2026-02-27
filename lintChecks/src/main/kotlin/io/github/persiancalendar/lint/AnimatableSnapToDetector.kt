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

class AnimatableSnapToDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames() = listOf("snapTo")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val receiverType = node.receiverType?.canonicalText ?: return

        // Check if the receiver is Animatable<Float, AnimationVector1D> or similar
        if (!receiverType.startsWith("androidx.compose.animation.core.Animatable")) return

        context.report(
            issue = ISSUE,
            location = context.getLocation(node),
            message = EXPLANATION,
        )
    }

    companion object {
        private const val EXPLANATION = """Avoid using Animatable.snapTo().
If snapTo() is needed frequently, consider turning the state to use mutableFloatStateOf()
instead and use animateDecay() and animate() with it, as simple value update of a float state
doesn't require a coroutineScope.launch {}.

If this snapTo use is very limited and this is a valid use case,
suppress with @SuppressLint(\"AnimatableSnapTo\").

There are cases that some kind of mutex on animate*() calls are needed. Keeping Animatable
and suppressing the lint can make sense on that case. Or some custom solution using pointer
event.getPointerId might be needed.

A bit unrelated but if the Animatable's value needs to be saved using rememberSaveable, consider
using MutableFloatState as it provides saving mechanism without need of a custom saver."""
        val ISSUE = Issue.create(
            id = "AnimatableSnapTo",
            briefDescription = "Animatable.snapTo() usage",
            explanation = EXPLANATION,
            category = Category.PERFORMANCE,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                AnimatableSnapToDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
