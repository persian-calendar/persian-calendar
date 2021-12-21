package com.byagowi.persiancalendar.ui.compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withRotation
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import kotlin.math.min

class SolarView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var sunMoonPosition: SunMoonPosition? = null
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        val sunMoonPosition = sunMoonPosition ?: return
        val radius = min(width, height) / 2f
        val cr = radius / 8f
        canvas.drawCircle(radius, radius, cr / 1.5f, earthPaint)
        val sunDegree = sunMoonPosition.sunEcliptic.λ.toFloat()
        canvas.withRotation(pivotX = radius, pivotY = radius, degrees = -sunDegree) {
            solarDraw.sun(this, radius, radius / 6, cr)
        }
        val moonDegree = sunMoonPosition.moonEcliptic.λ.toFloat()
        canvas.withRotation(pivotX = radius, pivotY = radius, degrees = -moonDegree) {
            solarDraw.moon(this, sunMoonPosition, radius, radius / 1.7f, cr / 1.9f)
        }
    }

    private val earthPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = Color.GRAY
    }
    private val solarDraw = SolarDraw(context)
}
