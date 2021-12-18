package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
    private val moonBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_moon)

    private val ovalPath = Path()

    fun moon(
        canvas: Canvas, sunMoonPosition: SunMoonPosition, cx: Float, cy: Float, r: Float,
        moonPhase: Float = sunMoonPosition.moonPhase.toFloat()
    ) {
        moonRect.set(cx - r * 1.1f, cy - r * 1.1f, cx + r * 1.1f, cy + r * 1.1f)
        canvas.drawBitmap(moonBitmap, null, moonRect, moonPaint)
        canvas.withScale(x = if (sunMoonPosition.moonPhaseAscending) -1f else 1f, pivotX = cx) {
            val arcWidth = (moonPhase - .5f) * 2 * r
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect.set(cx - r, cy - r, cx + r, cy + r)
            moonOval.set(cx - abs(arcWidth), cy - r, cx + abs(arcWidth), cy + r)
            ovalPath.rewind()
            if (arcWidth < 0) {
                ovalPath.arcTo(moonOval, 90f, 180f)
                ovalPath.arcTo(moonRect, 90f, -180f)
            } else {
                ovalPath.arcTo(moonOval, 90f, -180f)
                ovalPath.arcTo(moonRect, 270f, 180f)
            }
            ovalPath.close()
            drawPath(ovalPath, moonPaint)
        }
    }

    // private val moonDrawable = context.getCompatDrawable(R.drawable.ic_moon)

    private val moonRect = RectF()
    private val moonOval = RectF()

    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x90000000.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
    }

    companion object {
        // Not used yet
        val moonPhasesEmojis = listOf("🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘", "🌑")
    }
}
