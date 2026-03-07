package com.byagowi.persiancalendar.ui.athan

import android.graphics.BitmapShader
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.entities.PrayTime
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.tan

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
        val pattern = listOf(
            ::FirstPattern,
            // ::SecondPattern, This won't look as great as others when rotated, let's disable it for now
            ::ThirdPattern,
            // ::FourthPattern
        ).random()(tintColor, 80 * dp)
        val bitmap = createBitmap(pattern.width.toInt(), pattern.height.toInt())
            .applyCanvas { pattern.draw(Canvas(this)) }
        foregroundPaint.shader = BitmapShader(bitmap, pattern.tileModeX, pattern.tileModeY)
        centerX = listOf(-.5f, .5f, 1.5f).random() * width
        centerY = listOf(-.5f, .5f, 1.5f).random() * height
    }

    fun draw(canvas: Canvas, rotationDegree: Float = this.rotationDegree) =
        draw(canvas.nativeCanvas, rotationDegree)

    fun draw(canvas: android.graphics.Canvas, rotationDegree: Float = this.rotationDegree) {
        canvas.drawPaint(backgroundPaint.asFrameworkPaint())
        canvas.withRotation(
            rotationDegree, centerX, centerY,
        ) { drawPaint(foregroundPaint.asFrameworkPaint()) }
    }
}

private interface Pattern {
    val width: Float
    val height: Float
    val tileModeX: Shader.TileMode
    val tileModeY: Shader.TileMode
    fun draw(canvas: Canvas)
}

private class FirstPattern(private val tintColor: Color, size: Float) : Pattern {
    // https://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-pattern/
    override val width = size / 2
    override val height = size / 2
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

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

    override fun draw(canvas: Canvas) {
        canvas.nativeCanvas.withScale(width * 2, height * 2) {
            canvas.drawPath(path1, paint1)
            canvas.drawPath(path2, paint1)
            canvas.drawPath(cornerPath, paint2)
        }
    }
}

private class SecondPattern(private val tintColor: Color, private val size: Float) : Pattern {
    // https://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-patterns-4/
    override val width = size * tan(Math.toRadians(30.0).toFloat())
    override val height = size
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    private val paint = Paint().also {
        it.style = PaintingStyle.Stroke
        it.color = tintColor.copy(alpha = .25f)
        it.strokeWidth = width / 40
    }
    private val lines = Path().also {
        val s = .5f - sin(PI.toFloat() / 8) / 2
        val t = tan(PI.toFloat() / 6) * s
        val listOf = listOf(
            0f to s * size, width / 2 to height / 2, t * size to s / 2 * size, width to 0f,
        )
        it.moveTo(listOf[0].first, listOf[0].second)
        listOf.drop(1).forEach { (x, y) -> it.lineTo(x, y) }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(lines, paint)
        canvas.nativeCanvas.withRotation(180f, width / 2, height / 2) {
            canvas.drawPath(lines, paint)
        }
    }
}

private class ThirdPattern(private val tintColor: Color, size: Float) : Pattern {
    // https://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-patterns-3/
    override val width = size / 2
    override val height = size / 2
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    private fun splitPath(path: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        return (0..path.size - 2).flatMap {
            val w = path[it + 1].first - path[it].first
            val h = path[it + 1].second - path[it].second
            val r = hypot(w, h)
            val angle = atan2(h, w)
            val c = (1 - cos(PI.toFloat() / 4)) * r
            listOf(0, -1, 1).runningFold(path[it].first to path[it].second) { (x, y), i ->
                val degree = angle + i * PI.toFloat() / 4
                x + cos(degree) * c to y + sin(degree) * c
            }
        } + path.last()
    }

    private val paint = Paint().also { it.color = tintColor.copy(alpha = .125f) }
    private val path = Path().also {
        val path = (0..0).fold(listOf(0f to 1f, 1f to 0f)) { acc, _ -> splitPath(acc) }
        val pairs = (path + (1f to 1f))
        if (pairs.isNotEmpty()) it.moveTo(pairs[0].first, pairs[0].second)
        pairs.drop(1).forEach { (x, y) -> it.lineTo(x, y) }
        it.close()
    }

    override fun draw(canvas: Canvas) {
        canvas.nativeCanvas.withScale(width, height) {
            canvas.drawPath(path, paint)
        }
    }
}

private class FourthPattern(private val tintColor: Color, size: Float) : Pattern {
    // https://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-patterns-2/
    override val width = size * tan(Math.toRadians(30.0).toFloat())
    override val height = size
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    private val paint = Paint().also {
        it.style = PaintingStyle.Stroke
        it.color = tintColor.copy(alpha = .25f)
        it.strokeWidth = width / 40
    }.asFrameworkPaint()
    private val lines = floatArrayOf(
        width / 2, 0f, width, height / 2,
        0f, height / 2, width / 4, height / 4,
        width / 4, height / 4, width, height / 4,
    )

    override fun draw(canvas: Canvas) {
        canvas.nativeCanvas.drawLines(lines, paint)
        canvas.nativeCanvas.withRotation(180f, width / 2, height / 2) {
            drawLines(lines, paint)
        }
    }
}

private class SpiralPattern(private val tintColor: Color, size: Float) : Pattern {
    // Not enabled, just as an experiment on how spiral pattern and hexagon tiling would work
    // https://docs.microsoft.com/en-us/xamarin/xamarin-forms/user-interface/graphics/skiasharp/paths/polylines
    override val width = size
    override val height = size * 1.7f
    override val tileModeX = Shader.TileMode.REPEAT
    override val tileModeY = Shader.TileMode.REPEAT

    private val paint = Paint().also {
        it.style = PaintingStyle.Stroke
        it.color = tintColor.copy(alpha = .5f)
        it.strokeWidth = width / 80
    }

    private fun pattern(): Path {
        val result = Path()
        repeat(3600) { angle ->
            val scaledRadius = width / 2 * angle / 3600
            val radians = Math.toRadians(angle.toDouble()).toFloat()
            val x = width / 2f + scaledRadius * cos(radians)
            val y = width / 2f + scaledRadius * sin(radians)
            if (angle == 0) result.moveTo(x, y) else result.lineTo(x, y)
        }
        return result
    }

    private val path1 = pattern()
    private val path2 = path1.copy().also { it.translate(Offset(width / 2, height / 2)) }
    private val path3 = path1.copy().also { it.translate(Offset(-width / 2, height / 2)) }
    private val path4 = path1.copy().also { it.translate(Offset(width / 2, -height / 2)) }
    private val path5 = path1.copy().also { it.translate(Offset(-width / 2, -height / 2)) }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path1, paint)
        canvas.drawPath(path2, paint)
        canvas.drawPath(path3, paint)
        canvas.drawPath(path4, paint)
        canvas.drawPath(path5, paint)
    }
}
