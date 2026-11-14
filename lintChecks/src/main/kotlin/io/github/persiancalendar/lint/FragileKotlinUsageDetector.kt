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
import org.jetbrains.uast.UPostfixExpression
import org.jetbrains.uast.UThrowExpression

/**
 * Guards against Kotlin shortcuts which fail hard instead of modelling an outcome: `!!`,
 * `error(...)`, `require(...)`/`requireNotNull(...)`, `check(...)`/`checkNotNull(...)`,
 * `TODO()` and any `throw` statement. Vendored sources under `libs/` are exempt. Debug-only
 * assertions belong in `(null).debugAssertNotNull` or `assert(...)` instead.
 *
 * Assertions which must fail only in debug builds belong in `(null).debugAssertNotNull`.
 */
class FragileKotlinUsageDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(
        UPostfixExpression::class.java,
        UCallExpression::class.java,
        UThrowExpression::class.java,
    )

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitPostfixExpression(node: UPostfixExpression) {
            if (node.operator.text != "!!") return
            context.report(
                issue = DOUBLE_BANG_ISSUE,
                location = context.getLocation(node),
                message = "Avoid Use of `!!`, use (`?.`) or (`?:`) with proper fallback, then consider use of `.debugAssertNotNull` if is needed.",
            )
        }

        override fun visitCallExpression(node: UCallExpression) {
            if (context.isVendoredSource()) return
            if (node.methodName !in setOf(
                    "error", "require", "requireNotNull", "check", "checkNotNull", "TODO",
                )
            ) return
            val containingClass = node.resolve()?.containingClass?.qualifiedName
            if (containingClass == null || !containingClass.startsWith("kotlin.")) return
            when (node.methodName) {
                "error" -> context.report(
                    issue = ERROR_FUNCTION_ISSUE,
                    location = context.getLocation(node),
                    message = "Avoid `error(...)`, return a fallback value or a `Result` instead.",
                )
                "require", "requireNotNull" -> context.report(
                    issue = REQUIRE_FUNCTION_ISSUE,
                    location = context.getLocation(node),
                    message = "Avoid `require(...)`/`requireNotNull(...)`, return a fallback value or a `Result` instead.",
                )
                "check", "checkNotNull" -> context.report(
                    issue = CHECK_FUNCTION_ISSUE,
                    location = context.getLocation(node),
                    message = "Avoid `check(...)`/`checkNotNull(...)`, return a fallback value or a `Result` instead.",
                )
                else -> context.report(
                    issue = TODO_FUNCTION_ISSUE,
                    location = context.getLocation(node),
                    message = "Replace `TODO()` with a real implementation or a safe fallback.",
                )
            }
        }

        override fun visitThrowExpression(node: UThrowExpression) {
            if (context.isVendoredSource()) return
            context.report(
                issue = THROW_ISSUE,
                location = context.getLocation(node),
                message = "Avoid `throw`, return a fallback value instead, or use `(null).debugAssertNotNull` for assertions which must only fail in debug builds.",
            )
        }
    }

    companion object {
        val DOUBLE_BANG_ISSUE = Issue.create(
            id = "DoubleBangUsage",
            briefDescription = "Not-null assertion operator usage",
            explanation = "Avoid Use of `!!`, use (`?.`) or (`?:`) with proper fallback, then consider use of `.debugAssertNotNull` if is needed.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                FragileKotlinUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val ERROR_FUNCTION_ISSUE = Issue.create(
            id = "ErrorFunctionUsage",
            briefDescription = "`error()` function usage",
            explanation = "Avoid `error(...)`, return a fallback value or propagate a `Result` instead of failing.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                FragileKotlinUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val REQUIRE_FUNCTION_ISSUE = Issue.create(
            id = "RequireUsage",
            briefDescription = "`require()`/`requireNotNull()` usage",
            explanation = "Avoid `require(...)`/`requireNotNull(...)`; return a fallback value or propagate a `Result` instead of failing.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                FragileKotlinUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val CHECK_FUNCTION_ISSUE = Issue.create(
            id = "CheckUsage",
            briefDescription = "`check()`/`checkNotNull()` usage",
            explanation = "Avoid `check(...)`/`checkNotNull(...)`; return a fallback value or propagate a `Result` instead of failing.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                FragileKotlinUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val TODO_FUNCTION_ISSUE = Issue.create(
            id = "TodoUsage",
            briefDescription = "`TODO()` usage",
            explanation = "`TODO()` throws NotImplementedError at runtime; replace it with a real implementation or a safe fallback.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                FragileKotlinUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val THROW_ISSUE = Issue.create(
            id = "ThrowUsage",
            briefDescription = "Throw statement usage",
            explanation = "Avoid `throw`; return a fallback value or a `Result` instead. For assertions which must only fail in debug builds use `(null).debugAssertNotNull`.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                FragileKotlinUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
