package com.byagowi.persiancalendar.ui.common

import android.graphics.BitmapShader
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.entities.PrayTime
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.tan

@Composable
fun PatternCanvas(
    patternDrawable: PatternDrawable,
    modifier: Modifier = Modifier,
) {
    val direction = remember { listOf(1, -1).random() }
    val infiniteTransition = rememberInfiniteTransition()
    val animationSpec = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 360_000, easing = LinearEasing),
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = animationSpec,
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        drawIntoCanvas { patternDrawable.draw(it, rotation * direction) }
    }
}

class PatternDrawable(
    prayerKey: PrayTime = PrayTime.athans.random(),
    preferredTintColor: Color? = prayerKey.tint,
    var rotationDegree: Float = 0f,
    private val darkBaseColor: Boolean = false,
    private val dp: Float,
) {
    private val tintColor = preferredTintColor ?: prayerKey.tint

    private val backgroundPaint = Paint()
    private val foregroundPaint = Paint()
    private var centerX = 0f
    private var centerY = 0f
    private var oldWidth = 0
    private var oldHeight = 0

    fun setSize(width: Int, height: Int) {
        if (width == oldWidth && height == oldHeight) return
        oldWidth = width; oldHeight = height

        backgroundPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            tintColor.toArgb(), (if (darkBaseColor) Color.Black else Color.White).toArgb(),
            Shader.TileMode.CLAMP,
        )
        val pattern = Pattern(tintColor, 80 * dp)
        val bitmap = createBitmap(pattern.width.toInt(), pattern.height.toInt())
            .applyCanvas { pattern.draw(Canvas(this)) }
        foregroundPaint.shader = BitmapShader(bitmap, pattern.tileModeX, pattern.tileModeY)
        centerX = listOf(-.5f, .5f, 1.5f).random() * width
        centerY = listOf(-.5f, .5f, 1.5f).random() * height
    }

    fun draw(canvas: Canvas, rotationDegree: Float = this.rotationDegree) =
        draw(canvas.nativeCanvas, rotationDegree)

    fun draw(canvas: android.graphics.Canvas, rotationDegree: Float = this.rotationDegree) {
        canvas.drawPaint(backgroundPaint.nativePaint)
        canvas.withRotation(
            rotationDegree, centerX, centerY,
        ) { drawPaint(foregroundPaint.nativePaint) }
    }
}

private class Pattern(private val tintColor: Color, size: Float) {
    val width = size / 2
    val height = size / 2
    val tileModeX = Shader.TileMode.MIRROR
    val tileModeY = Shader.TileMode.MIRROR

    private val t = tan(PI.toFloat() / 8)
    private val s = sin(PI.toFloat() / 4) / 2

    private fun Path.rotateBy(degrees: Float, pivotX: Float, pivotY: Float): Path {
        val matrix = Matrix()
        matrix.resetToPivotedTransform(pivotX = pivotX, pivotY = pivotY, rotationZ = degrees)
        return copy().also { it.transform(matrix) }
    }

    private fun path(order: Boolean): Path {
        val triangle = Path().also {
            val list = listOf(0f to .5f, 1f to .5f + t, 1f to .5f - t)
            it.moveTo(list[0].first, list[0].second)
            list.drop(1).forEach { (x, y) -> it.lineTo(x, y) }
            it.close()
        }
        val sumOfTwo = triangle or triangle.rotateBy(180f, .5f, .5f)
        val sum = sumOfTwo and sumOfTwo.rotateBy(90f, .5f, .5f)
        return if (order) sum + sum.rotateBy(45f, .5f, .5f)
        else sum xor sum.rotateBy(45f, .5f, .5f)
    }

    private val path1 = path(true)
    private val path2 = path(false)

    private val paint1 = Paint().also { it.color = tintColor.copy(alpha = .05f) }
    private val paint2 = Paint().also { it.color = tintColor.copy(alpha = .10f) }
    private val cornerPath = Path().also {
        val list = listOf(0f to 0f, 0f to .5f - t, .5f - s to .5f - s, .5f - t to 0f)
        it.moveTo(list[0].first, list[0].second)
        list.drop(1).forEach { (x, y) -> it.lineTo(x, y) }
        it.close()
    }

    fun draw(canvas: Canvas) {
        canvas.nativeCanvas.withScale(width * 2, height * 2) {
            canvas.drawPath(path1, paint1)
            canvas.drawPath(path2, paint1)
            canvas.drawPath(cornerPath, paint2)
        }
    }
}
