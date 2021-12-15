// copyedited from https://code.google.com/p/android-salat-times/source/browse/src/com/cepmuvakkit/times/view/QiblaCompassView.java
// licensed under GPLv3
package com.cepmuvakkit.times.view

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import net.androgames.level.AngleDisplay
import java.util.*
import kotlin.math.min
import kotlin.math.round

class QiblaCompassView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var angle = 0f
        set(value) {
            if (value != field) {
                field = value
                postInvalidate()
            }
        }

    private val northwardShapePath = Path()
    private val trueNorthArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.RED
        it.style = Paint.Style.FILL
        it.alpha = 100
    }
    private val markerPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = ContextCompat.getColor(context, R.color.qibla_color)
    }
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = ContextCompat.getColor(context, R.color.qibla_color)
        it.strokeWidth = 0.5.dp
        it.style = Paint.Style.STROKE // Sadece Cember ciziyor.
    }
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { // Diameter
        it.color = Color.LTGRAY
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1.dp
    }
    private val moonPaintShade = Paint(Paint.ANTI_ALIAS_FLAG).also { // Diameter
        it.color = Color.LTGRAY
        it.style = Paint.Style.STROKE
        it.strokeWidth = 3.dp
    }
    private var sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1.dp
    }
    private var sunPaintShade = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 3.dp
    }
    private val qiblaPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF009000.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = 1.dp
    }
    private val kaaba = BitmapFactory.decodeResource(resources, R.drawable.kaaba)

    private var cx = 0f
    private var cy = 0f // Center of Compass (cx, cy)
    private var radius = 0f // radius of Compass dial
    private var r = 0f // radius of Sun and Moon
    private var northPaint = Paint().also {
        it.strokeWidth = 1.5.dp
        it.color = Color.RED
    }

    private var sunMoonPosition = coordinates?.calculateSunMoonPosition(GregorianCalendar())

    private val fullDay = Clock(24, 0).toMinutes().toFloat()
    private var sunProgress = Clock(Calendar.getInstance()).toMinutes() / fullDay

    fun setTime(time: GregorianCalendar) {
        sunMoonPosition = coordinates?.calculateSunMoonPosition(time)
        sunProgress = Clock(time).toMinutes() / fullDay
        postInvalidate()
    }

    val qiblaHeading = sunMoonPosition?.destinationHeading?.heading?.toFloat()
    private val textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = ContextCompat.getColor(context, R.color.qibla_color)
        it.textSize = 12.sp
        it.textAlign = Paint.Align.CENTER
    }

    private val angleDisplay = AngleDisplay(context, "0", "888")

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        angleDisplay.updatePlacement(w / 2, h)

        cx = w / 2f
        cy = h / 2f - angleDisplay.lcdHeight
        radius = min(cx - cx / 12, cy - cy / 12)
        r = radius / 10 // Sun Moon radius

        // Construct a wedge-shaped path
        northwardShapePath.also {
            val r = radius / 12
            it.reset()
            it.moveTo(cx, cy - radius)
            it.lineTo(cx - r, cy)
            it.lineTo(cx, cy + r)
            it.lineTo(cx + r, cy)
            it.addCircle(cx, cy, r, Path.Direction.CCW)
            it.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        angleDisplay.draw(canvas, (round(angle) + 360f) % 360f)
        canvas.withRotation(-angle, cx, cy) {
            drawDial()
            drawTrueNorthArrow()
            if (coordinates != null) {
                drawQibla()
                drawMoon()
                drawSun()
            }
        }
    }

    private fun Canvas.drawTrueNorthArrow() {
        drawPath(northwardShapePath, trueNorthArrowPaint)
        drawLine(cx, (cy - radius), cx, (cy + radius), northPaint)
        drawCircle(cx, cy, 5f, northPaint)
    }

    private fun Canvas.drawDial() {
        val textHeight = textPaint.measureText("yY").toInt()
        // Draw the background
        drawCircle(cx, cy, radius, circlePaint)
        drawCircle(cx, cy, radius * .975f, circlePaint)
        // Rotate our perspective so that the "top" is
        // facing the current bearing.
        val cardinalX = cx
        val cardinalY = cy - radius + textHeight

        // Draw the marker every 15 degrees and text every 45.
        (0..23).forEach {
            withRotation(15f * it, cx, cy) {
                drawLine(cx, (cy - radius), cx, (cy - radius * .975f), markerPaint)
                withTranslation(0f, textHeight.toFloat()) {
                    // Draw the cardinal points
                    if (it % 6 == 0) {
                        val dirString = when (it) {
                            0 -> "N"
                            6 -> "E"
                            12 -> "S"
                            18 -> "W"
                            else -> ""
                        }
                        drawText(dirString, cardinalX, cardinalY, textPaint)
                    } else if (it % 3 == 0) {
                        // Draw the text every alternate 45deg
                        val angle = (it * 15).toString()
                        val angleTextY = cy - radius + textHeight
                        drawText(angle, cardinalX, angleTextY, textPaint)
                    }
                }
            }
        }
    }

    private val solarDraw = SolarDraw(context)

    private val shadeFactor = 3

    private fun Canvas.drawSun() {
        val sunMoonPosition = sunMoonPosition ?: return
        if (sunMoonPosition.sunPosition.altitude <= -10) return
        val rotation = sunMoonPosition.sunPosition.azimuth.toFloat() - 360
        withRotation(rotation, cx, cy) {
            val sunHeight = (sunMoonPosition.sunPosition.altitude.toFloat() / 90 - 1) * radius
            val sunColor = solarDraw.sunColor(sunProgress)
            sunPaint.color = sunColor
            drawLine(cx, cy - radius, cx, cy + radius, sunPaint)
            sunPaintShade.color = sunColor
            drawLine(cx, cy, cx, cy - sunHeight / shadeFactor, sunPaintShade)
            solarDraw.sun(this, cx, cy + sunHeight, r, sunColor)
        }
    }

    private fun Canvas.drawMoon() {
        val sunMoonPosition = sunMoonPosition ?: return
        if (sunMoonPosition.moonPosition.altitude <= -5) return
        withRotation(sunMoonPosition.moonPosition.azimuth.toFloat() - 360, cx, cy) {
            val moonHeight = (sunMoonPosition.moonPosition.altitude.toFloat() / 90 - 1) * radius
            drawLine(cx, cy - radius, cx, cy + radius, moonPaint)
            drawLine(cx, cy, cx, cy - moonHeight / shadeFactor, moonPaintShade)
            solarDraw.moon(this, sunMoonPosition, cx, cy + moonHeight, r * .8f)
        }
    }

    private fun Canvas.drawQibla() {
        val qiblaHeading = qiblaHeading ?: return
        withRotation(qiblaHeading - 360, cx, cy) {
            drawLine(cx, (cy - radius), cx, (cy + radius), qiblaPaint)
            drawBitmap(kaaba, cx - kaaba.width / 2, cy - radius - kaaba.height / 2, qiblaPaint)
        }
    }
}
