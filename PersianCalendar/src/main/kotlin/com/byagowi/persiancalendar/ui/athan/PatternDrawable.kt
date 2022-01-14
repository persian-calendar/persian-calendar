package com.byagowi.persiancalendar.ui.athan

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.and
import androidx.core.graphics.or
import androidx.core.graphics.plus
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.xor
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.rotateBy
import com.byagowi.persiancalendar.ui.utils.toPath
import com.byagowi.persiancalendar.ui.utils.translateBy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class PatternDrawable(prayerKey: String = FAJR_KEY) : Drawable() {

    private val tintColor = when (prayerKey) {
        FAJR_KEY -> 0xFF009788
        DHUHR_KEY -> 0xFFF1A42A
        ASR_KEY -> 0xFFF57C01
        MAGHRIB_KEY -> 0xFF5E35B1
        ISHA_KEY -> 0xFF283593
        else -> 0xFF283593
    }.toInt()

    private val backgroundPaint = Paint()
    private val foregroundPaint = Paint()
    private var centerX = 0f
    private var centerY = 0f
    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds ?: return)
        backgroundPaint.shader = LinearGradient(
            0f, 0f, 0f, bounds.bottom.toFloat(),
            tintColor, Color.WHITE, Shader.TileMode.CLAMP
        )
        val pattern = listOf(
            ::FirstPattern, ::SecondPattern, ::ThirdPattern, ::FourthPattern
        ).random()(tintColor, 80.dp)
        val bitmap = Bitmap.createBitmap(
            pattern.width.toInt(), pattern.height.toInt(), Bitmap.Config.ARGB_8888
        )
        Canvas(bitmap).also(pattern::draw)
        foregroundPaint.shader = BitmapShader(bitmap, pattern.tileModeX, pattern.tileModeY)
        centerX = listOf(-.5f, .5f, 1.5f).random() * bounds.width()
        centerY = listOf(-.5f, .5f, 1.5f).random() * bounds.height()
    }

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).also {
        it.duration = 180000L
        it.interpolator = LinearInterpolator()
        it.repeatMode = ValueAnimator.RESTART
        it.repeatCount = ValueAnimator.INFINITE
        it.addUpdateListener { invalidateSelf() }
        listOf(it::start, it::reverse).random()()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)
        val degree = valueAnimator.animatedValue as? Float ?: 0f
        canvas.withRotation(degree, centerX, centerY) { drawPaint(foregroundPaint) }
    }

    override fun setAlpha(alpha: Int) = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}

interface Pattern {
    val width: Float
    val height: Float
    val tileModeX: Shader.TileMode
    val tileModeY: Shader.TileMode
    fun draw(canvas: Canvas)
}

private class FirstPattern(@ColorInt private val tintColor: Int, size: Float) : Pattern {
    // http://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-pattern/
    override val width = size / 2
    override val height = size / 2
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    private val t = tan(Math.PI.toFloat() / 8)
    private val s = sin(Math.PI.toFloat() / 4) / 2

    private fun path(order: Boolean): Path {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return Path()
        val triangle = listOf(0f to .5f, 1f to .5f + t, 1f to .5f - t).toPath(true)
        val sumOfTwo = triangle or triangle.rotateBy(180f, .5f, .5f)
        val sum = sumOfTwo and sumOfTwo.rotateBy(90f, .5f, .5f)
        return if (order) sum + sum.rotateBy(45f, .5f, .5f)
        else sum xor sum.rotateBy(45f, .5f, .5f)
    }

    override fun draw(canvas: Canvas) {
        canvas.withScale(width * 2, height * 2) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.FILL
            paint.color = ColorUtils.setAlphaComponent(tintColor, 0x10)
            canvas.drawPath(path(true), paint)
            canvas.drawPath(path(false), paint)
            paint.color = ColorUtils.setAlphaComponent(tintColor, 0x20)
            val corner =
                listOf(0f to 0f, 0f to .5f - t, .5f - s to .5f - s, .5f - t to 0f).toPath(true)
            canvas.drawPath(corner, paint)
        }
    }
}

private class SecondPattern(@ColorInt private val tintColor: Int, private val size: Float) :
    Pattern {
    // http://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-patterns-4/
    override val width = size * tan(Math.toRadians(30.0).toFloat())
    override val height = size
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    override fun draw(canvas: Canvas) {
        val paint = Paint().also {
            it.style = Paint.Style.STROKE
            it.color = ColorUtils.setAlphaComponent(tintColor, 0x40)
            it.strokeWidth = width / 40
        }
        val s = .5f - sin(Math.PI.toFloat() / 8) / 2
        val t = tan(Math.PI.toFloat() / 6) * s
        val lines =
            listOf(0f to s * size, width / 2 to height / 2, t * size to s / 2 * size, width to 0f)
                .toPath(false)
        canvas.drawPath(lines, paint)
        canvas.withRotation(180f, width / 2, height / 2) { drawPath(lines, paint) }
    }
}

private class ThirdPattern(@ColorInt private val tintColor: Int, size: Float) : Pattern {
    // http://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-patterns-3/
    override val width = size / 2
    override val height = size / 2
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    private fun splitPath(path: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        return (0..path.size - 2).flatMap {
            val w = path[it + 1].first - path[it].first
            val h = path[it + 1].second - path[it].second
            val r = sqrt(w * w + h * h)
            val angle = atan2(h, w)
            val c = (1 - cos(Math.PI.toFloat() / 4)) * r
            listOf(0, -1, 1).runningFold(path[it].first to path[it].second) { (x, y), i ->
                val degree = angle + i * Math.PI.toFloat() / 4
                x + cos(degree) * c to y + sin(degree) * c
            }
        } + listOf(path.last())
    }

    override fun draw(canvas: Canvas) {
        val path = (0..0).fold(listOf(0f to 1f, 1f to 0f)) { acc, _ -> splitPath(acc) }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = ColorUtils.setAlphaComponent(tintColor, 0x20)
        }
        canvas.withScale(width, height) { drawPath((path + listOf(1f to 1f)).toPath(true), paint) }
    }
}

private class FourthPattern(@ColorInt private val tintColor: Int, size: Float) : Pattern {
    // http://www.sigd.org/resources/islamic-geometric-patterns/islamic-geometric-patterns-2/
    override val width = size * tan(Math.toRadians(30.0).toFloat())
    override val height = size
    override val tileModeX = Shader.TileMode.MIRROR
    override val tileModeY = Shader.TileMode.MIRROR

    override fun draw(canvas: Canvas) {
        val paint = Paint().also {
            it.style = Paint.Style.STROKE
            it.color = ColorUtils.setAlphaComponent(tintColor, 0x40)
            it.strokeWidth = width / 40
        }
        val lines = floatArrayOf(
            width / 2, 0f, width, height / 2,
            0f, height / 2, width / 4, height / 4,
            width / 4, height / 4, width, height / 4
        )
        canvas.drawLines(lines, paint)
        canvas.withRotation(180f, width / 2, height / 2) { drawLines(lines, paint) }
    }
}

private class SpiralPattern(@ColorInt private val tintColor: Int, size: Float) : Pattern {
    // Not enabled, just as an experiment on how spiral pattern and hexagon tiling would work
    // https://docs.microsoft.com/en-us/xamarin/xamarin-forms/user-interface/graphics/skiasharp/paths/polylines
    override val width = size
    override val height = size * 1.7f
    override val tileModeX = Shader.TileMode.REPEAT
    override val tileModeY = Shader.TileMode.REPEAT

    private fun pattern(): Path {
        val result = Path()
        (0..3600).forEach { angle ->
            val scaledRadius = width / 2 * angle / 3600
            val radians = Math.toRadians(angle.toDouble()).toFloat()
            val x = width / 2f + scaledRadius * cos(radians)
            val y = width / 2f + scaledRadius * sin(radians)
            if (angle == 0) result.moveTo(x, y) else result.lineTo(x, y)
        }
        return result
    }

    override fun draw(canvas: Canvas) {
        val path = pattern()
        val paint = Paint().also {
            it.style = Paint.Style.STROKE
            it.color = ColorUtils.setAlphaComponent(tintColor, 0x80)
            it.strokeWidth = width / 80
        }
        canvas.drawPath(path, paint)
        canvas.drawPath(path.translateBy(width / 2, height / 2), paint)
        canvas.drawPath(path.translateBy(-width / 2, height / 2), paint)
        canvas.drawPath(path.translateBy(width / 2, -height / 2), paint)
        canvas.drawPath(path.translateBy(-width / 2, -height / 2), paint)
    }
}
