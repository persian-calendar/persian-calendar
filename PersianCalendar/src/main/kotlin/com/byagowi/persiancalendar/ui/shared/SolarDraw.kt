package com.byagowi.persiancalendar.ui.shared

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlin.math.abs

class SolarDraw {

    fun sunColor(progress: Float) =
        ArgbEvaluatorCompat.getInstance().evaluate(progress, minSunColor, maxSunColor)

    fun sun(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        sunPaint.color = color
        canvas.drawCircle(cx, cy, r, sunPaint)
    }

    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL_AND_STROKE
    }

    fun moon(canvas: Canvas, moonPhase: Double, cx: Float, cy: Float, r: Float) {
        // This is separated from QiblaCompassView with some modifications
        val arcWidth = ((moonPhase - .5) * (4 * r)).toInt()
        // elevation Offset 0 for 0 degree; r for 90 degree
        moonRect.set(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(moonRect, 90f, 180f, false, moonPaint)
        canvas.drawArc(moonRect, 270f, 180f, false, moonPaintB)
        moonOval.set(
            cx - abs(arcWidth) / 2f, cy - r,
            cx + abs(arcWidth) / 2f, cy + r
        )
        moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
        canvas.drawArc(moonOval, 0f, 360f, false, moonPaintO)
        canvas.drawArc(moonRect, 0f, 360f, false, moonPaintD)
    }

    private val moonRect = RectF()
    private val moonOval = RectF()

    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.style = Paint.Style.FILL_AND_STROKE
    }

    // moon Paint Black
    private val moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.BLACK
        it.style = Paint.Style.FILL_AND_STROKE
    }

    // moon Paint for Oval
    private val moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.style = Paint.Style.FILL_AND_STROKE
    }

    // moon Paint for Diameter
    private val moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GRAY
        it.style = Paint.Style.STROKE
    }

    companion object {
        // Not used yet
        val moonPhasesEmojis = listOf("🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘", "🌑")

        @ColorInt
        private val minSunColor = Color.parseColor("#FFFFF9C4")

        @ColorInt
        private val maxSunColor = Color.parseColor("#FFFF9100")
    }
}
