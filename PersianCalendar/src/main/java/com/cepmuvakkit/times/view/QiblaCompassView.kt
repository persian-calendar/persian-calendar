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
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.a11yAnnounceAndClick
import com.byagowi.persiancalendar.utils.coordinates
import com.cepmuvakkit.times.posAlgo.AstroLib
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class QiblaCompassView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val dashPath = DashPathEffect(floatArrayOf(2f, 5f), 1f)
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
        it.strokeWidth = 1f
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
        it.strokeWidth = 5.5f
    }
    private val kaaba = BitmapFactory.decodeResource(resources, R.drawable.kaaba)

    @ColorInt
    val qiblaColor = ContextCompat.getColor(context, R.color.qibla_color)

    private var cx = 0f
    private var cy = 0f // Center of Compass (cx, cy)
    private var radius = 0f // radius of Compass dial
    private var r = 0f // radius of Sun and Moon
    private val northString = "N"
    private val eastString = "E"
    private val southString = "S"
    private val westString = "W"
    private var dashedPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.pathEffect = dashPath
        it.strokeWidth = 2f
        it.pathEffect = dashPath
    }
    private var degree = 0f
    private val sunMoonPosition = SunMoonPosition(
        AstroLib.calculateJulianDay(GregorianCalendar()), coordinates?.latitude ?: 0.0,
        coordinates?.longitude ?: 0.0, coordinates?.elevation ?: 0.0, 0.0
    )
    private val qiblaInfo = sunMoonPosition.destinationHeading
    private val textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = ContextCompat.getColor(context, R.color.qibla_color)
        it.textSize = 20f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = (h - h / 4) / 2f
        radius = min(cx - cx / 12, cy - cy / 12)
        r = radius / 10 // Sun Moon radius

        // Construct a wedge-shaped path
        val r = radius / 12
        northwardShapePath.reset()
        northwardShapePath.moveTo(cx, cy - radius)
        northwardShapePath.lineTo(cx - r, cy)
        northwardShapePath.lineTo(cx, cy + r)
        northwardShapePath.lineTo(cx + r, cy)
        northwardShapePath.addCircle(cx, cy, r, Path.Direction.CCW)
        northwardShapePath.close()
    }

    override fun onDraw(canvas: Canvas) {
        // over here
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = qiblaColor
        // Attach and Detach capability lies
        canvas.withRotation(-degree, cx, cy) {
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
        drawCircle(cx, cy, (radius - 20), circlePaint)
        // Rotate our perspective so that the "top" is
        // facing the current bearing.
        val textWidth = textPaint.measureText("W").toInt()
        val cardinalX = cx - textWidth / 2
        val cardinalY = cy - radius + textHeight

        // Draw the marker every 15 degrees and text every 45.
        (0..23).forEach {
            // Draw a marker.
            drawLine(
                cx, (cy - radius), cx, (cy - radius + 10),
                markerPaint
            )
            withRotation(15f * it, cx, cy) {
                withTranslation(0f, textHeight.toFloat()) {
                    // Draw the cardinal points
                    if (it % 6 == 0) {
                        val dirString = when (it) {
                            0 -> northString
                            6 -> eastString
                            12 -> southString
                            18 -> westString
                            else -> ""
                        }
                        drawText(dirString, cardinalX, cardinalY, textPaint)
                    } else if (it % 3 == 0) {
                        // Draw the text every alternate 45deg
                        val angle = (it * 15).toString()
                        val angleTextWidth = textPaint.measureText(angle)
                        val angleTextX = (cx - angleTextWidth / 2).toInt()
                        val angleTextY = cy - radius + textHeight
                        drawText(angle, angleTextX.toFloat(), angleTextY, textPaint)
                    }
                }
            }
        }
    }

    private fun Canvas.drawSun() {
        sunPaint.color = Color.YELLOW
        sunPaint.style = Paint.Style.FILL_AND_STROKE
        // Horizontal sunPosition = new Horizontal(225, 45);
        if (sunMoonPosition.sunPosition.altitude > -10) {
            withRotation(
                sunMoonPosition.sunPosition.azimuth.toFloat() - 360, cx, cy
            ) {
                val ry = ((90 - sunMoonPosition.sunPosition.altitude) / 90 * radius).toInt()
                drawCircle(cx, (cy - ry), r, sunPaint)
                dashedPaint.color = Color.YELLOW
                drawLine(cx, (cy - radius), cx, (cy + radius), dashedPaint)
            }
        }
    }

    private fun Canvas.drawMoon() {
        val moonPhase = sunMoonPosition.moonPhase
        if (sunMoonPosition.moonPosition.altitude <= -5) return
        withRotation(sunMoonPosition.moonPosition.azimuth.toFloat() - 360, cx, cy) {
            val eOffset = (sunMoonPosition.moonPosition.altitude / 90 * radius).toInt()
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect[(cx - r), (cy + eOffset - radius - r), (cx + r)] =
                (cy + eOffset - radius + r)
            drawArc(moonRect, 90f, 180f, false, moonPaint)
            drawArc(moonRect, 270f, 180f, false, moonPaintB)
            val arcWidth = ((moonPhase - 0.5) * (4 * r)).toInt()
            moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
            moonOval[(cx - abs(arcWidth) / 2), (cy + eOffset - radius - r),
                    (cx + abs(arcWidth) / 2)] = (cy + eOffset - radius + r)
            drawArc(moonOval, 0f, 360f, false, moonPaintO)
            drawArc(moonRect, 0f, 360f, false, moonPaintD)
            moonPaintD.pathEffect = dashPath
            drawLine(cx, (cy - radius), cx, (cy + radius), moonPaintD)
            moonPaintD.pathEffect = null
        }
    }

    private fun Canvas.drawQibla() {
        withRotation(qiblaInfo.heading.toFloat() - 360, cx, cy) {
            qiblaPaint.pathEffect = dashPath
            drawLine(cx, (cy - radius), cx, (cy + radius), qiblaPaint)
            qiblaPaint.pathEffect = null
            drawBitmap(kaaba, (cx - kaaba.width / 2), (cy - radius - kaaba.height / 2), qiblaPaint)
        }
    }

    fun setCompassDegree(degree: Float) {
        this.degree = degree
        postInvalidate()
    }

    // deliberately true
    private var isCurrentlyNorth = true
    private var isCurrentlyEast = true
    private var isCurrentlyWest = true
    private var isCurrentlySouth = true
    private var isCurrentlyQibla = true
    fun onDirectionAction() {
        // 0=North, 90=East, 180=South, 270=West
        isCurrentlyNorth = if (isNearToDegree(0f, degree)) {
            if (!isCurrentlyNorth) a11yAnnounceAndClick(this, R.string.north)
            true
        } else false
        isCurrentlyEast = if (isNearToDegree(90f, degree)) {
            if (!isCurrentlyEast) a11yAnnounceAndClick(this, R.string.east)
            true
        } else false
        isCurrentlySouth = if (isNearToDegree(180f, degree)) {
            if (!isCurrentlySouth) a11yAnnounceAndClick(this, R.string.south)
            true
        } else false
        isCurrentlyWest = if (isNearToDegree(270f, degree)) {
            if (!isCurrentlyWest) a11yAnnounceAndClick(this, R.string.west)
            true
        } else false
        if (coordinates != null) {
            isCurrentlyQibla = if (isNearToDegree(qiblaInfo.heading.toFloat(), degree)) {
                if (!isCurrentlyQibla) a11yAnnounceAndClick(this, R.string.qibla)
                true
            } else false
        }
    }

    companion object {
        fun isNearToDegree(compareTo: Float, degree: Float): Boolean {
            val difference = abs(degree - compareTo)
            return if (difference > 180) 360 - difference < 3f else difference < 3f
        }
    }
}
