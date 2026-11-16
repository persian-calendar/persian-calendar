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
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.ConeFlexibleType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
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
        if (expression.isNullAwareExtension(context.session)) return
        reportIfPlatformReceiver(expression.explicitReceiver, reporter)
    }
}

private object PlatformTypePropertyAccessChecker :
    FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        if (expression.isNullAwareExtension(context.session)) return
        reportIfPlatformReceiver(expression.explicitReceiver, reporter)
    }
}

private fun FirQualifiedAccessExpression.isNullAwareExtension(session: FirSession): Boolean {
    val symbol = toResolvedCallableSymbol(session) ?: return false
    if (symbol.resolvedReceiverType?.isMarkedNullable != true) return false
    val resultType = resolvedType
    return !resultType.isMarkedNullable && resultType !is ConeFlexibleType
}

context(context: CheckerContext)
private fun reportIfPlatformReceiver(
    receiver: FirExpression?,
    reporter: DiagnosticReporter,
) {
    if (receiver == null) return
    if (receiver.resolvedType !is ConeFlexibleType) return
    val callableKey = receiver.callableKey(context.session) ?: return
    if (callableKey.startsWith("kotlin.")) return
    if (callableKey.startsWith("_synthetic.")) return
    if (callableKey in ALWAYS_NON_NULL) return
    reporter.reportOn(
        receiver.source,
        FirErrors.OTHER_ERROR_WITH_REASON,
        "Platform type from $callableKey. Annotate the Java declaration or handle nullability explicitly.",
        context,
    )
}

private fun FirExpression.callableKey(session: FirSession): String? {
    val symbol = toResolvedCallableSymbol(session) ?: return null
    if (symbol is FirVariableSymbol<*>) {
        val initializer = symbol.resolvedInitializer ?: return null
        return initializer.callableKey(session)
    }
    return symbol.callableId.toString().replace('/', '.')
}

// These Java APIs are documented/guaranteed to return a non-null value even though the
// platform stubs do not carry that information.
private val ALWAYS_NON_NULL = setOf(
    "android.media.AudioAttributes.Builder.setContentType",
    "android.media.AudioAttributes.Builder.setFlags",
    "android.media.AudioAttributes.Builder.setLegacyStreamType",
    "android.media.AudioAttributes.Builder.setUsage",
    "android.media.AudioFormat.Builder.setEncoding",
    "android.media.AudioFormat.Builder.setSampleRate",
    "android.net.Uri.buildUpon",
    "android.view.InputDevice.getDeviceIds",
    "android.view.inputmethod.InputMethodManager.getEnabledInputMethodSubtypeList",
    "android.widget.Toast.makeText",
    "java.lang.Class.forName",
    "java.lang.Class.getDeclaredField",
    "java.lang.Class.getDeclaredMethod",
    "java.lang.Class.getMethod",
    "java.lang.Runtime.exec",
    "java.lang.Runtime.getRuntime",
    "java.nio.ByteBuffer.allocateDirect",
    "java.nio.ByteBuffer.asFloatBuffer",
    "java.nio.ByteBuffer.order",
    "java.nio.FloatBuffer.put",
    "java.text.Format.format",
    "java.util.Locale.getDefault",
    "java.util.TimeZone.getAvailableIDs",
    "java.util.TimeZone.getDefault",
)
