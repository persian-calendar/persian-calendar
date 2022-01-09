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
    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds ?: return)
        backgroundPaint.shader = LinearGradient(
            0f, 0f, 0f, bounds.bottom.toFloat(),
            tintColor, Color.WHITE, Shader.TileMode.CLAMP
        )
        val path = Path().also { path ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return@also
            val basePath = Path().also { // defines a rotated square, ◇
                it.moveTo(0f, .5f); it.lineTo(.5f, 0f)
                it.lineTo(1f, .5f); it.lineTo(.5f, 1f); it.close()
            }
            val rotated = Path().also { // makes the defined square straight, □
                it.addPath(basePath, Matrix().apply { setRotate(45f, .5f, .5f) })
            }
            path.op(basePath, rotated, Path.Op.UNION) // adds the two shapes together
        }
        val size = min(bounds.width(), bounds.height()) / 5f
        val bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also { canvas ->
            canvas.withScale(size, size) {
                canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).also {
                    it.style = Paint.Style.FILL
                    it.color = ColorUtils.setAlphaComponent(tintColor, 0x20)
                })
            }
        }
        foregroundPaint.shader =
            BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).also {
        it.duration = 120000L
        it.interpolator = LinearInterpolator()
        it.repeatMode = ValueAnimator.RESTART
        it.repeatCount = ValueAnimator.INFINITE
        it.addUpdateListener { invalidateSelf() }
        listOf(it::start, it::reverse).random()()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)
        val degree = valueAnimator.animatedValue as? Float ?: 0f
        canvas.withRotation(degree, bounds.exactCenterX(), bounds.exactCenterY()) {
            drawPaint(foregroundPaint)
        }
    }

    override fun setAlpha(alpha: Int) = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
