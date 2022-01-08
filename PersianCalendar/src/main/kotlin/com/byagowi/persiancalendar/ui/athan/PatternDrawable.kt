package com.byagowi.persiancalendar.ui.athan

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
import androidx.core.graphics.ColorUtils
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.ui.utils.dp

class PatternDrawable(prayerKey: String = FAJR_KEY) : Drawable() {

    private val tintColor = when (prayerKey) {
        FAJR_KEY -> 0xFF009788
        DHUHR_KEY -> 0xFFFBC02D
        ASR_KEY -> 0xFFF57C01
        MAGHRIB_KEY -> 0xFF5E35B1
        ISHA_KEY -> 0xFF283593
        else -> 0xFF283593
    }.toInt()

    private val paint = Paint().also { paint ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return@also paint.setColor(Color.TRANSPARENT)
        val size = 80.dp
        val path = Path().also { path ->
            val basePath = Path().apply {
                moveTo(0f, size / 2); lineTo(size / 2, 0f)
                lineTo(size, size / 2); lineTo(size / 2, size); close()
            }
            val rotated = Path().apply {
                addPath(basePath, Matrix().apply { setRotate(45f, size / 2, size / 2) })
            }
            path.op(basePath, rotated, Path.Op.UNION)
        }
        val bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also { canvas ->
            canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = ColorUtils.setAlphaComponent(tintColor, 0x20)
            })
        }
        paint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private val backgroundPaint: Paint = Paint()
    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        backgroundPaint.shader = LinearGradient(
            0f, 0f, 0f, (bounds ?: return).bottom.toFloat(),
            tintColor, Color.WHITE, Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)
        canvas.drawPaint(paint)
    }

    override fun setAlpha(alpha: Int) = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
