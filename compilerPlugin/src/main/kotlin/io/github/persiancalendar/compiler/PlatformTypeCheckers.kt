package io.github.persiancalendar.compiler

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.types.ConeFlexibleType
import org.jetbrains.kotlin.fir.types.resolvedType

class PlatformTypeCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers = setOf(PlatformTypeFunctionCallChecker)
        override val propertyAccessExpressionCheckers = setOf(PlatformTypePropertyAccessChecker)
    }
}

private object PlatformTypeFunctionCallChecker :
    FirFunctionCallChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        reportIfPlatformReceiver(expression.explicitReceiver, reporter)
    }
}

private object PlatformTypePropertyAccessChecker :
    FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        reportIfPlatformReceiver(expression.explicitReceiver, reporter)
    }
}

context(context: CheckerContext)
private fun reportIfPlatformReceiver(
    receiver: FirExpression?,
    reporter: DiagnosticReporter,
) {
    if (receiver == null) return
    if (receiver.resolvedType !is ConeFlexibleType) return
    reporter.reportOn(
        receiver.source,
        FirErrors.OTHER_ERROR_WITH_REASON,
        "Dereference of a Java platform type. Annotate the Java declaration or handle nullability explicitly.",
        context,
    )
}
