// copyedited from https://code.google.com/p/android-salat-times/source/browse/src/com/cepmuvakkit/times/view/QiblaCompassView.java
// licensed under GPLv3
package com.cepmuvakkit.times.view

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.sp
import com.cepmuvakkit.times.posAlgo.AstroLib
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import net.androgames.level.AngleDisplay
import java.util.*
import kotlin.math.abs
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

    private val dashPath = DashPathEffect(floatArrayOf(0.5.dp, 2.dp), 2.dp)
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
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.YELLOW
        it.style = Paint.Style.FILL_AND_STROKE
        it.pathEffect = dashPath
    }
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.style = Paint.Style.FILL_AND_STROKE
    }
    private val moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.BLACK
        it.style = Paint.Style.FILL_AND_STROKE
    }
    private val moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG).also { // Oval
        it.color = Color.WHITE
        it.style = Paint.Style.FILL_AND_STROKE
    }
    private val moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG).also { // Diameter
        it.color = Color.GRAY
        it.style = Paint.Style.STROKE
    }
    private val moonRect = RectF()
    private val moonOval = RectF()
    private val qiblaPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GREEN
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = 2.dp
    }
    private val kaaba = BitmapFactory.decodeResource(resources, R.drawable.kaaba)

    private var cx = 0f
    private var cy = 0f // Center of Compass (cx, cy)
    private var radius = 0f // radius of Compass dial
    private var r = 0f // radius of Sun and Moon
    private var dashedPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.pathEffect = dashPath
        it.strokeWidth = 1.5.dp
    }
    private val sunMoonPosition = SunMoonPosition(
        AstroLib.calculateJulianDay(GregorianCalendar()), coordinates?.latitude ?: 0.0,
        coordinates?.longitude ?: 0.0, coordinates?.elevation ?: 0.0, 0.0
    )
    val qiblaHeading = sunMoonPosition.destinationHeading.heading.toFloat()
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
        dashedPaint.color = Color.RED
        drawLine(cx, (cy - radius), cx, (cy + radius), dashedPaint)
        drawCircle(cx, cy, 5f, dashedPaint)
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

    private fun Canvas.drawSun() {
        if (sunMoonPosition.sunPosition.altitude <= -10) return
        withRotation(sunMoonPosition.sunPosition.azimuth.toFloat() - 360, cx, cy) {
            val ry = ((90 - sunMoonPosition.sunPosition.altitude) / 90 * radius).toInt()
            drawCircle(cx, (cy - ry), r, sunPaint)
            dashedPaint.color = Color.YELLOW
            drawLine(cx, (cy - radius), cx, (cy + radius), dashedPaint)
        }
    }

    private fun Canvas.drawMoon() {
        if (sunMoonPosition.moonPosition.altitude <= -5) return
        withRotation(sunMoonPosition.moonPosition.azimuth.toFloat() - 360, cx, cy) {
            val eOffset = (sunMoonPosition.moonPosition.altitude / 90 * radius).toInt()
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect.set(
                cx - r, cy + eOffset - radius - r,
                cx + r, cy + eOffset - radius + r
            )
            drawArc(moonRect, 90f, 180f, false, moonPaint)
            drawArc(moonRect, 270f, 180f, false, moonPaintB)
            val arcWidth = ((sunMoonPosition.moonPhase - 0.5) * (4 * r)).toInt()
            moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
            moonOval.set(
                cx - abs(arcWidth) / 2, cy + eOffset - radius - r,
                cx + abs(arcWidth) / 2, cy + eOffset - radius + r
            )
            drawArc(moonOval, 0f, 360f, false, moonPaintO)
            drawArc(moonRect, 0f, 360f, false, moonPaintD)
            moonPaintD.pathEffect = dashPath
            drawLine(cx, (cy - radius), cx, (cy + radius), moonPaintD)
            moonPaintD.pathEffect = null
        }
    }

    private fun Canvas.drawQibla() {
        withRotation(qiblaHeading - 360, cx, cy) {
            qiblaPaint.pathEffect = dashPath
            drawLine(cx, (cy - radius), cx, (cy + radius), qiblaPaint)
            qiblaPaint.pathEffect = null
            drawBitmap(kaaba, cx - kaaba.width / 2, cy - radius - kaaba.height / 2, qiblaPaint)
        }
    }
}
