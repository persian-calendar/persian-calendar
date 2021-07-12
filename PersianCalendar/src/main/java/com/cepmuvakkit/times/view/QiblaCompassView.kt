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

    private val mPath = Path()
    private val trueNorthArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.RED
        it.style = Paint.Style.FILL
        it.alpha = 100
    }
    private val markerPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonRect = RectF()
    private val moonOval = RectF()
    private val qiblaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val kaaba = BitmapFactory.decodeResource(resources, R.drawable.kaaba)

    @ColorInt
    val qiblaColor = ContextCompat.getColor(context, R.color.qibla_color)

    private var px = 0
    private var py = 0 // Center of Compass (px,py)
    private var radius = 0 // radius of Compass dial
    private var r = 0 // radius of Sun and Moon
    private val northString = "N"
    private val eastString = "E"
    private val southString = "S"
    private val westString = "W"
    private val dashPath = DashPathEffect(floatArrayOf(2f, 5f), 1f)
    private var dashedPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.pathEffect = dashPath
        it.strokeWidth = 2f
        it.pathEffect = dashPath
        it.color = qiblaColor
    }
    private var bearing = 0f
    private val sunMoonPosition = SunMoonPosition(
        AstroLib.calculateJulianDay(GregorianCalendar()),
        coordinates?.latitude ?: 0.0,
        coordinates?.longitude ?: 0.0,
        coordinates?.elevation ?: 0.0,
        0.0
    )
    private val qiblaInfo = sunMoonPosition.destinationHeading
    private val textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = ContextCompat.getColor(context, R.color.qibla_color)
        it.textSize = 20f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // The compass is a circle that fills as much space as possible.
        // Set the measured dimensions by figuring out the shortest boundary,
        // height or width.
        val measuredWidth = measure(widthMeasureSpec)
        val measuredHeight = measure(heightMeasureSpec)

        // int d = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    private fun measure(measureSpec: Int): Int {
        // Decode the measurement specifications.
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            600
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            specSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        radius = min(px - px / 12, py - py / 12)
        r = radius / 10 // Sun Moon radius;
        // over here
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = qiblaColor
        // Attach and Detach capability lies
        canvas.withRotation(-bearing, px.toFloat(), py.toFloat()) {
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
        val r = radius / 12
        // Construct a wedge-shaped path
        mPath.reset()
        mPath.moveTo(px.toFloat(), (py - radius).toFloat())
        mPath.lineTo((px - r).toFloat(), py.toFloat())
        mPath.lineTo(px.toFloat(), (py + r).toFloat())
        mPath.lineTo((px + r).toFloat(), py.toFloat())
        mPath.addCircle(px.toFloat(), py.toFloat(), r.toFloat(), Path.Direction.CCW)
        mPath.close()
        drawPath(mPath, trueNorthArrowPaint)
        dashedPaint.color = Color.RED
        drawLine(
            px.toFloat(), (py - radius).toFloat(), px.toFloat(), (py + radius).toFloat(),
            dashedPaint
        )
        drawCircle(px.toFloat(), py.toFloat(), 5f, dashedPaint)
    }

    private fun Canvas.drawDial() {
        // over here
        circlePaint.reset()
        circlePaint.color = ContextCompat.getColor(context, R.color.qibla_color)
        circlePaint.strokeWidth = 1f
        circlePaint.style = Paint.Style.STROKE // Sadece Cember ciziyor.
        val textHeight = textPaint.measureText("yY").toInt()
        markerPaint.reset()
        markerPaint.color = ContextCompat.getColor(context, R.color.qibla_color)
        // Draw the background
        drawCircle(px.toFloat(), py.toFloat(), radius.toFloat(), circlePaint)
        drawCircle(px.toFloat(), py.toFloat(), (radius - 20).toFloat(), circlePaint)
        // Rotate our perspective so that the "top" is
        // facing the current bearing.
        val textWidth = textPaint.measureText("W").toInt()
        val cardinalX = px - textWidth / 2
        val cardinalY = py - radius + textHeight

        // Draw the marker every 15 degrees and text every 45.
        (0..23).forEach {
            // Draw a marker.
            drawLine(
                px.toFloat(), (py - radius).toFloat(), px.toFloat(), (py - radius + 10).toFloat(),
                markerPaint
            )
            withRotation(15f * it, px.toFloat(), py.toFloat()) {
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
                        drawText(dirString, cardinalX.toFloat(), cardinalY.toFloat(), textPaint)
                    } else if (it % 3 == 0) {
                        // Draw the text every alternate 45deg
                        val angle = (it * 15).toString()
                        val angleTextWidth = textPaint.measureText(angle)
                        val angleTextX = (px - angleTextWidth / 2).toInt()
                        val angleTextY = py - radius + textHeight
                        drawText(angle, angleTextX.toFloat(), angleTextY.toFloat(), textPaint)
                    }
                }
            }
        }
    }

    private fun Canvas.drawSun() {
        sunPaint.reset()
        sunPaint.color = Color.YELLOW
        sunPaint.style = Paint.Style.FILL_AND_STROKE
        // Horizontal sunPosition = new Horizontal(225, 45);
        if (sunMoonPosition.sunPosition.altitude > -10) {
            withRotation(
                sunMoonPosition.sunPosition.azimuth.toFloat() - 360, px.toFloat(), py.toFloat()
            ) {
                sunPaint.pathEffect = dashPath
                val ry = ((90 - sunMoonPosition.sunPosition.altitude) / 90 * radius).toInt()
                drawCircle(px.toFloat(), (py - ry).toFloat(), r.toFloat(), sunPaint)
                dashedPaint.color = Color.YELLOW
                drawLine(
                    px.toFloat(), (py - radius).toFloat(),
                    px.toFloat(), (py + radius).toFloat(),
                    dashedPaint
                )
                sunPaint.pathEffect = null
            }
        }
    }

    private fun Canvas.drawMoon() {
        moonPaint.reset()
        moonPaint.color = Color.WHITE
        moonPaint.style = Paint.Style.FILL_AND_STROKE
        moonPaintB.reset() // moon Paint Black
        moonPaintB.color = Color.BLACK
        moonPaintB.style = Paint.Style.FILL_AND_STROKE
        moonPaintO.reset() // moon Paint for Oval
        moonPaintO.color = Color.WHITE
        moonPaintO.style = Paint.Style.FILL_AND_STROKE
        moonPaintD.reset() // moon Paint for Diameter
        // draw
        moonPaintD.color = Color.GRAY
        moonPaintD.style = Paint.Style.STROKE
        val moonPhase = sunMoonPosition.moonPhase
        if (sunMoonPosition.moonPosition.altitude <= -5) return
        withRotation(
            sunMoonPosition.moonPosition.azimuth.toFloat() - 360, px.toFloat(), py.toFloat()
        ) {
            val eOffset = (sunMoonPosition.moonPosition.altitude / 90 * radius).toInt()
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect[(px - r).toFloat(), (py + eOffset - radius - r).toFloat(), (px + r).toFloat()] =
                (py + eOffset - radius + r).toFloat()
            drawArc(moonRect, 90f, 180f, false, moonPaint)
            drawArc(moonRect, 270f, 180f, false, moonPaintB)
            val arcWidth = ((moonPhase - 0.5) * (4 * r)).toInt()
            moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
            moonOval[(px - abs(arcWidth) / 2).toFloat(), (py + eOffset - radius - r).toFloat(), (
                    px + abs(arcWidth) / 2).toFloat()] = (py + eOffset - radius + r).toFloat()
            drawArc(moonOval, 0f, 360f, false, moonPaintO)
            drawArc(moonRect, 0f, 360f, false, moonPaintD)
            moonPaintD.pathEffect = dashPath
            drawLine(
                px.toFloat(), (py - radius).toFloat(), px.toFloat(), (py + radius).toFloat(),
                moonPaintD
            )
            moonPaintD.pathEffect = null
        }
    }

    private fun Canvas.drawQibla() {
        withRotation(qiblaInfo.heading.toFloat() - 360, px.toFloat(), py.toFloat()) {
            qiblaPaint.reset()
            qiblaPaint.color = Color.GREEN
            qiblaPaint.style = Paint.Style.FILL_AND_STROKE
            qiblaPaint.pathEffect = dashPath
            qiblaPaint.strokeWidth = 5.5f
            drawLine(
                px.toFloat(), (py - radius).toFloat(), px.toFloat(), (py + radius).toFloat(),
                qiblaPaint
            )
            qiblaPaint.pathEffect = null
            drawBitmap(
                kaaba, (px - kaaba.width / 2).toFloat(), (py - radius - kaaba.height / 2).toFloat(),
                qiblaPaint
            )
        }
    }

    fun setBearing(bearing: Float) {
        this.bearing = bearing
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
        if (isNearToDegree(bearing, 0f)) {
            if (!isCurrentlyNorth) {
                a11yAnnounceAndClick(this, R.string.north)
                isCurrentlyNorth = true
            }
        } else {
            isCurrentlyNorth = false
        }
        if (isNearToDegree(bearing, 90f)) {
            if (!isCurrentlyEast) {
                a11yAnnounceAndClick(this, R.string.east)
                isCurrentlyEast = true
            }
        } else {
            isCurrentlyEast = false
        }
        if (isNearToDegree(bearing, 180f)) {
            if (!isCurrentlySouth) {
                a11yAnnounceAndClick(this, R.string.south)
                isCurrentlySouth = true
            }
        } else {
            isCurrentlySouth = false
        }
        if (isNearToDegree(bearing, 270f)) {
            if (!isCurrentlyWest) {
                a11yAnnounceAndClick(this, R.string.west)
                isCurrentlyWest = true
            }
        } else {
            isCurrentlyWest = false
        }
        if (coordinates != null) {
            if (isNearToDegree(bearing, qiblaInfo.heading.toFloat())) {
                if (!isCurrentlyQibla) {
                    a11yAnnounceAndClick(this, R.string.qibla)
                    isCurrentlyQibla = true
                }
            } else {
                isCurrentlyQibla = false
            }
        }
    }

    fun setScreenResolution(widthPixels: Int, heightPixels: Int) {
        px = widthPixels / 2
        py = heightPixels / 2
    }

    companion object {
        fun isNearToDegree(angle: Float, compareTo: Float): Boolean {
            val difference = abs(angle - compareTo)
            return if (difference > 180) 360 - difference < 3f else difference < 3f
        }
    }
}
