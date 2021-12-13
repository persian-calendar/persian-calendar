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
        val arcWidth = (moonPhase.toFloat() - .5f) * 2 * r
        // elevation Offset 0 for 0 degree; r for 90 degree
        moonRect.set(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(moonRect, 90f, 180f, false, moonBrightPaint)
        canvas.drawArc(moonRect, 270f, 180f, false, moonDarkPaint)
        moonOval.set(cx - abs(arcWidth), cy - r, cx + abs(arcWidth), cy + r)
        val ovalPaint = if (arcWidth < 0) moonDarkPaint else moonBrightPaint
        canvas.drawArc(moonOval, 0f, 360f, false, ovalPaint)
        canvas.drawArc(moonRect, 0f, 360f, false, moonDiameterPaint)
    }

    private val moonRect = RectF()
    private val moonOval = RectF()

    private val moonBrightPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = moonBrightColor
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val moonDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = moonDarkColor
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val moonDiameterPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GRAY
        it.style = Paint.Style.STROKE
    }

    companion object {
        // Not used yet
        val moonPhasesEmojis = listOf("🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘", "🌑")

        @ColorInt
        private val minSunColor = Color.parseColor("#FFF9C4")

        @ColorInt
        private val maxSunColor = Color.parseColor("#FF9100")

        @ColorInt
        private val moonBrightColor = Color.parseColor("#DDE7EF")

        @ColorInt
        private val moonDarkColor = Color.parseColor("#22404C")
    }
}
