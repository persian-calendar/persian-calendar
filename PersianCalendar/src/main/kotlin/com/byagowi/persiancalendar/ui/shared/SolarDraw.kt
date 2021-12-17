package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlin.math.abs

class SolarDraw(context: Context) {

    fun sunColor(progress: Float) =
        ArgbEvaluatorCompat.getInstance().evaluate(progress, 0xFFFFF9C4.toInt(), 0xFFFF9100.toInt())

    fun sun(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sunDrawable.setTint(color)
        }
        sunDrawable.setBounds(
            (cx - r).toInt(), (cy - r).toInt(), (cx + r).toInt(), (cy + r).toInt()
        )
        sunDrawable.draw(canvas)
    }

    private val sunDrawable = context.getCompatDrawable(R.drawable.ic_compass_sun)

    fun moon(
        canvas: Canvas, sunMoonPosition: SunMoonPosition, cx: Float, cy: Float, r: Float,
        moonPhase: Float = sunMoonPosition.moonPhase.toFloat()
    ) {
        // canvas.withRotation(sunMoonPosition.moonPosition.azimuth.toFloat(), cx, cy) {
        // moonDrawable.setBounds(
        //     (cx - r).toInt(), (cy - r).toInt(), (cx + r).toInt(), (cy + r).toInt()
        // )
        // moonDrawable.draw(canvas)
        // This is separated from QiblaCompassView with some modifications
        canvas.withScale(x = if (sunMoonPosition.moonPhaseAscending) -1f else 1f, pivotX = cx) {
            val arcWidth = (moonPhase - .5f) * 2 * r
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect.set(cx - r, cy - r, cx + r, cy + r)
            drawArc(moonRect, 90f, 180f, false, moonBrightPaint)
            drawArc(moonRect, 270f, 180f, false, moonDarkPaint)
            moonOval.set(cx - abs(arcWidth), cy - r, cx + abs(arcWidth), cy + r)
            val ovalPaint = if (arcWidth < 0) moonDarkPaint else moonBrightPaint
            drawArc(moonOval, 0f, 360f, false, ovalPaint)
        }
    }

    // private val moonDrawable = context.getCompatDrawable(R.drawable.ic_moon)

    private val moonRect = RectF()
    private val moonOval = RectF()

    private val moonBrightPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFFDDE7EF.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val moonDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF22404C.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
    }

    companion object {
        // Not used yet
        val moonPhasesEmojis = listOf("🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘", "🌑")
    }
}
