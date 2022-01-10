package com.byagowi.persiancalendar.ui.athan

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.animation.LinearInterpolator
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.MAGHRIB_KEY
import kotlin.math.min

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
        val size = min(bounds.width(), bounds.height()) / 5f
        val bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also { canvas ->
            canvas.withScale(size, size) {
                canvas.drawPath(pattern(Path()), Paint(Paint.ANTI_ALIAS_FLAG).also {
                    it.style = Paint.Style.FILL
                    it.color = ColorUtils.setAlphaComponent(tintColor, 0x20)
                })
            }
        }
        foregroundPaint.shader =
            BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        centerX = listOf(-.5f, .5f, 1.5f).random() * bounds.width()
        centerY = listOf(-.5f, .5f, 1.5f).random() * bounds.height()
    }

    private fun FloatArray.toPath() = Path().also {
        if (size >= 2) it.moveTo(this[0], this[1])
        this.drop(2).chunked(2).map { (x, y) -> it.lineTo(x, y) }
        it.close()
    }

    private fun pattern(path: Path): Path {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return path
        val diamond = floatArrayOf(0f, .5f, .5f, 0f, 1f, .5f, .5f, 1f).toPath() // ◇
        val square = Path().also { // makes the above shae a straight square, □
            it.addPath(diamond, Matrix().apply { setRotate(45f, .5f, .5f) })
        }
        path.op(diamond, square, Path.Op.UNION) // adds the two shapes together
        return path
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
        canvas.withRotation(degree, centerX, centerY) {
            drawPaint(foregroundPaint)
        }
    }

    override fun setAlpha(alpha: Int) = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
