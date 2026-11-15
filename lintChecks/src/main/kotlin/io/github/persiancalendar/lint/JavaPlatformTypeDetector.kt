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
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiPrimitiveType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.USimpleNameReferenceExpression

/**
 * Java methods without nullability annotations return platform types in Kotlin. Dereferencing
 * such a result can NPE, so ask callers to treat it as nullable.
 *
 * This is intentionally narrower than full platform-type analysis: UAST does not expose
 * Kotlin's flexible/platform nullability, so only direct chains and resolvable locals are
 * covered. A compiler plugin is required for exhaustive enforcement.
 */
class JavaPlatformTypeDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            if (context.isVendoredSource()) return
            if (node.isSafeCall()) return
            val receiver = node.receiver ?: return
            val platformCall = when (receiver) {
                is UCallExpression -> receiver.takeIf { it.hasPlatformReturnType() }
                is USimpleNameReferenceExpression -> {
                    val local = receiver.resolve() as? ULocalVariable ?: return
                    local.uastInitializer as? UCallExpression
                }
                else -> null
            }?.takeIf { it.hasPlatformReturnType() } ?: return
            context.report(
                issue = ISSUE,
                location = context.getLocation(node),
                message = "`${platformCall.sourcePsi?.text ?: platformCall.methodName}` " +
                        "returns a Java platform type; treat it as nullable before dereferencing.",
            )
        }
    }

    private fun UCallExpression.isSafeCall(): Boolean =
        sourcePsi?.parent?.javaClass?.simpleName?.contains("SafeQualified") == true

    private fun UCallExpression.hasPlatformReturnType(): Boolean {
        val method = resolve() ?: return false
        if (method.isConstructor) return false
        val containingClass = method.containingClass ?: return false
        if (containingClass.qualifiedName?.startsWith("kotlin.") == true ||
            containingClass.qualifiedName?.startsWith("kotlinx.") == true ||
            containingClass.hasAnnotation("kotlin.Metadata")
        ) return false
        if (!method.language.isKindOf(JavaLanguage.INSTANCE)) return false
        val returnType = method.returnType ?: return false
        if (returnType is PsiPrimitiveType) return false
        val annotations = (method.annotations + returnType.annotations)
            .mapNotNull { it.qualifiedName }
        if (annotations.any { it in NULLABILITY_ANNOTATIONS }) return false
        return true
    }

    companion object {
        private val NULLABILITY_ANNOTATIONS = setOf(
            "org.jetbrains.annotations.Nullable",
            "org.jetbrains.annotations.NotNull",
            "androidx.annotation.Nullable",
            "androidx.annotation.NonNull",
            "android.annotation.Nullable",
            "android.annotation.NonNull",
            "android.annotation.RecentlyNullable",
            "android.annotation.RecentlyNonNull",
        )

        val ISSUE = Issue.create(
            id = "JavaPlatformTypeUsage",
            briefDescription = "Java platform type used as non-null",
            explanation = "A Java method without a nullability annotation returns a Kotlin platform type. Treat the result as nullable (`?.`/`?:`) before calling members on it.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                JavaPlatformTypeDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
