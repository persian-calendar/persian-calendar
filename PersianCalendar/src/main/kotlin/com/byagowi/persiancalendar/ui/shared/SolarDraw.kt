package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlin.math.abs

class SolarDraw(context: Context) {

    fun sunColor(progress: Float) =
        ArgbEvaluatorCompat.getInstance().evaluate(progress, 0xFFFFF9C4.toInt(), 0xFFFF9100.toInt())

    fun sun(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int? = null) {
        if (color != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sunDrawable.setTint(color)
        }
        sunDrawable.setBounds(
            (cx - r).toInt(), (cy - r).toInt(), (cx + r).toInt(), (cy + r).toInt()
        )
        sunDrawable.draw(canvas)
    }

    private val sunDrawable = context.getCompatDrawable(R.drawable.ic_sun)

    fun moon(canvas: Canvas, sunMoonPosition: SunMoonPosition, cx: Float, cy: Float, r: Float) {
        moonRect.set(cx - r, cy - r, cx + r, cy + r)
        canvas.drawBitmap(moonBitmap, null, moonRect, null)
        val arcWidth = (sunMoonPosition.moonPhase.toFloat() - .5f) * 2 * r
        moonOval.set(cx - abs(arcWidth), cy - r, cx + abs(arcWidth), cy + r)
        ovalPath.rewind()
        ovalPath.arcTo(moonOval, 90f, if (arcWidth < 0) 180f else -180f)
        ovalPath.arcTo(moonRect, 270f, 180f)
        ovalPath.close()
        canvas.withScale(x = if (sunMoonPosition.moonAgeInDegrees > 180) -1f else 1f, pivotX = cx) {
            drawPath(ovalPath, moonPaint)
        }
    }

    private val moonBitmap = context.getCompatDrawable(R.drawable.ic_moon).toBitmap(192, 192)
    private val ovalPath = Path()
    private val moonRect = RectF()
    private val moonOval = RectF()

    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x90000000.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val earthDrawable by lazy(LazyThreadSafetyMode.NONE) {
        context.getCompatDrawable(R.drawable.ic_earth).toBitmap(128, 128)
    }

    fun earth(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        earthRect.set(cx - r, cy - r, cx + r, cy + r)
        canvas.drawBitmap(earthDrawable, null, earthRect, null)
    }
    private val earthRect = RectF()
}
