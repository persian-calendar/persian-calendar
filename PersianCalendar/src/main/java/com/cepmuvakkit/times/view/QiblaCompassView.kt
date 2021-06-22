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
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.a11yAnnounceAndClick
import com.cepmuvakkit.times.posAlgo.AstroLib
import com.cepmuvakkit.times.posAlgo.EarthHeading
import com.cepmuvakkit.times.posAlgo.Horizontal
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import java.util.*

class QiblaCompassView : View {
    private val mPath = Path()
    private val trueNorthArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
    var qiblaColor = 0

    // deliberately true
    var isCurrentlyNorth = true
    var isCurrentlyEast = true
    var isCurrentlyWest = true
    var isCurrentlySouth = true
    var isCurrentlyQibla = true
    private var dashedPaint: Paint? = null
    private var px = 0
    private var py // Center of Compass (px,py)
            = 0
    private var radius // radius of Compass dial
            = 0
    private var r // radius of Sun and Moon
            = 0
    private var northString: String? = null
    private var eastString: String? = null
    private var southString: String? = null
    private var westString: String? = null
    private var dashPath: DashPathEffect? = null
    private var bearing = 0f
    private var qiblaInfo: EarthHeading? = null
    private var sunMoonPosition: SunMoonPosition? = null
    private var sunPosition: Horizontal? = null
    private var moonPosition: Horizontal? = null
    private var longitude = 0.0
    private var latitude = 0.0
    private var textPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        initCompassView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initCompassView()
    }

    constructor(context: Context?, ats: AttributeSet?, defaultStyle: Int) : super(
        context,
        ats,
        defaultStyle
    ) {
        initCompassView()
    }

    private fun initAstronomicParameters() {
        val c = GregorianCalendar()
        val jd = AstroLib.calculateJulianDay(c)
        val ΔT = 0.0
        val altitude = 0.0
        sunMoonPosition = SunMoonPosition(
            jd, latitude, longitude,
            altitude, ΔT
        )
        sunPosition = sunMoonPosition.getSunPosition()
        moonPosition = sunMoonPosition.getMoonPosition()
    }

    fun initCompassView() {
        isFocusable = true
        initAstronomicParameters()
        northString = "N"
        eastString = "E"
        southString = "S"
        westString = "W"
        dashPath = DashPathEffect(floatArrayOf(2f, 5f), 1)
        dashedPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
        dashedPaint!!.pathEffect = dashPath
        dashedPaint!!.strokeWidth = 2f
        dashedPaint!!.pathEffect = dashPath
        qiblaColor = ContextCompat.getColor(context, R.color.qibla_color)
        dashedPaint!!.color = qiblaColor
        textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
        textPaint!!.color = ContextCompat.getColor(context, R.color.qibla_color)
        textPaint!!.textSize = 20f
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
        val result: Int

        // Decode the measurement specifications.
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        result = if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            600
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            specSize
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        radius = Math.min(px - px / 12, py - py / 12)
        r = radius / 10 // Sun Moon radius;
        // over here
        qiblaInfo = sunMoonPosition.getDestinationHeading()
        textPaint!!.textAlign = Paint.Align.LEFT
        textPaint!!.color = qiblaColor
        canvas.rotate(-bearing, px.toFloat(), py.toFloat()) // Attach and Detach capability lies
        canvas.save()
        drawDial(canvas)
        if (isLongLatAvailable) {
            canvas.save()
            drawQibla(canvas)
        }
        canvas.save()
        drawTrueNorthArrow(canvas, bearing)
        if (isLongLatAvailable) {
            canvas.save()
            drawMoon(canvas)
            canvas.save()
            drawSun(canvas)
        }
        canvas.save()
    }

    val isLongLatAvailable: Boolean
        get() = longitude != 0.0 && latitude != 0.0

    fun drawTrueNorthArrow(canvas: Canvas, drawnAngle: Float) {
        trueNorthArrowPaint.reset()
        trueNorthArrowPaint.color = Color.RED
        trueNorthArrowPaint.style = Paint.Style.FILL
        trueNorthArrowPaint.alpha = 100
        val r = radius / 12
        // Construct a wedge-shaped path
        mPath.reset()
        mPath.moveTo(px.toFloat(), (py - radius).toFloat())
        mPath.lineTo((px - r).toFloat(), py.toFloat())
        mPath.lineTo(px.toFloat(), (py + r).toFloat())
        mPath.lineTo((px + r).toFloat(), py.toFloat())
        mPath.addCircle(px.toFloat(), py.toFloat(), r.toFloat(), Path.Direction.CCW)
        mPath.close()
        canvas.drawPath(mPath, trueNorthArrowPaint)
        dashedPaint!!.color = Color.RED
        canvas.drawLine(
            px.toFloat(),
            (py - radius).toFloat(),
            px.toFloat(),
            (py + radius).toFloat(),
            dashedPaint!!
        )
        canvas.drawCircle(px.toFloat(), py.toFloat(), 5f, dashedPaint!!)
        canvas.restore()
    }

    fun drawDial(canvas: Canvas) {
        // over here
        circlePaint.reset()
        circlePaint.color = ContextCompat.getColor(context, R.color.qibla_color)
        circlePaint.strokeWidth = 1f
        circlePaint.style = Paint.Style.STROKE // Sadece Cember ciziyor.
        val textHeight = textPaint!!.measureText("yY").toInt()
        markerPaint.reset()
        markerPaint.color = ContextCompat.getColor(context, R.color.qibla_color)
        // Draw the background
        canvas.drawCircle(px.toFloat(), py.toFloat(), radius.toFloat(), circlePaint)
        canvas.drawCircle(px.toFloat(), py.toFloat(), (radius - 20).toFloat(), circlePaint)
        // Rotate our perspective so that the "top" is
        // facing the current bearing.
        val textWidth = textPaint!!.measureText("W").toInt()
        val cardinalX = px - textWidth / 2
        val cardinalY = py - radius + textHeight

        // Draw the marker every 15 degrees and text every 45.
        for (i in 0..23) {
            // Draw a marker.
            canvas.drawLine(
                px.toFloat(),
                (py - radius).toFloat(),
                px.toFloat(),
                (py - radius + 10).toFloat(),
                markerPaint
            )
            canvas.save()
            canvas.translate(0f, textHeight.toFloat())
            // Draw the cardinal points
            if (i % 6 == 0) {
                var dirString: String? = ""
                when (i) {
                    0 -> dirString = northString
                    6 -> dirString = eastString
                    12 -> dirString = southString
                    18 -> dirString = westString
                }
                canvas.drawText(dirString!!, cardinalX.toFloat(), cardinalY.toFloat(), textPaint!!)
            } else if (i % 3 == 0) {
                // Draw the text every alternate 45deg
                val angle = (i * 15).toString()
                val angleTextWidth = textPaint!!.measureText(angle)
                val angleTextX = (px - angleTextWidth / 2).toInt()
                val angleTextY = py - radius + textHeight
                canvas.drawText(angle, angleTextX.toFloat(), angleTextY.toFloat(), textPaint!!)
            }
            canvas.restore()
            canvas.rotate(15f, px.toFloat(), py.toFloat())
        }
    }

    fun drawSun(canvas: Canvas) {
        sunPaint.reset()
        sunPaint.color = Color.YELLOW
        sunPaint.style = Paint.Style.FILL_AND_STROKE
        // Horizontal sunPosition = new Horizontal(225, 45);
        if (sunPosition.getElevation() > -10) {
            canvas.rotate(sunPosition.getAzimuth() as Float - 360, px.toFloat(), py.toFloat())
            sunPaint.pathEffect = dashPath
            val ry = ((90 - sunPosition.getElevation()) / 90 * radius) as Int
            canvas.drawCircle(px.toFloat(), (py - ry).toFloat(), r.toFloat(), sunPaint)
            dashedPaint!!.color = Color.YELLOW
            canvas.drawLine(
                px.toFloat(),
                (py - radius).toFloat(),
                px.toFloat(),
                (py + radius).toFloat(),
                dashedPaint!!
            )
            sunPaint.pathEffect = null
            canvas.restore()
        }
    }

    fun drawMoon(canvas: Canvas) {
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
        val moonPhase = sunMoonPosition.getMoonPhase()
        if (moonPosition.getElevation() > -5) {
            canvas.rotate(moonPosition.getAzimuth() as Float - 360, px.toFloat(), py.toFloat())
            val eOffset = (moonPosition.getElevation() / 90 * radius) as Int
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect[(px - r).toFloat(), (py + eOffset - radius - r).toFloat(), (px + r).toFloat()] =
                (py + eOffset - radius + r).toFloat()
            canvas.drawArc(moonRect, 90f, 180f, false, moonPaint)
            canvas.drawArc(moonRect, 270f, 180f, false, moonPaintB)
            val arcWidth = ((moonPhase - 0.5) * (4 * r)).toInt()
            moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
            moonOval[(px - Math.abs(arcWidth) / 2).toFloat(), (py + eOffset - radius - r).toFloat(), (
                    px + Math.abs(arcWidth) / 2).toFloat()] =
                (py + eOffset - radius + r).toFloat()
            canvas.drawArc(moonOval, 0f, 360f, false, moonPaintO)
            canvas.drawArc(moonRect, 0f, 360f, false, moonPaintD)
            moonPaintD.pathEffect = dashPath
            canvas.drawLine(
                px.toFloat(),
                (py - radius).toFloat(),
                px.toFloat(),
                (py + radius).toFloat(),
                moonPaintD
            )
            moonPaintD.pathEffect = null
            canvas.restore()
        }
    }

    fun drawQibla(canvas: Canvas) {
        canvas.rotate(qiblaInfo.getHeading() as Float - 360, px.toFloat(), py.toFloat())
        qiblaPaint.reset()
        qiblaPaint.color = Color.GREEN
        qiblaPaint.style = Paint.Style.FILL_AND_STROKE
        qiblaPaint.pathEffect = dashPath
        qiblaPaint.strokeWidth = 5.5f
        canvas.drawLine(
            px.toFloat(),
            (py - radius).toFloat(),
            px.toFloat(),
            (py + radius).toFloat(),
            qiblaPaint
        )
        qiblaPaint.pathEffect = null
        canvas.drawBitmap(
            kaaba, (px - kaaba.width / 2).toFloat(), (py - radius - kaaba.height / 2).toFloat(),
            qiblaPaint
        )
        canvas.restore()
    }

    fun setBearing(bearing: Float) {
        this.bearing = bearing
        postInvalidate()
    }

    // 0=North, 90=East, 180=South, 270=West
    val isOnDirectionAction: Unit
        get() {
            val context = context ?: return

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
            if (isLongLatAvailable && qiblaInfo != null) {
                if (isNearToDegree(bearing, qiblaInfo.getHeading() as Float)) {
                    if (!isCurrentlyQibla) {
                        a11yAnnounceAndClick(this, R.string.qibla)
                        isCurrentlyQibla = true
                    }
                } else {
                    isCurrentlyQibla = false
                }
            }
        }

    fun setLatitude(latitude: Double) {
        this.latitude = latitude
    }

    fun setLongitude(longitude: Double) {
        this.longitude = longitude
    }

    fun setScreenResolution(widthPixels: Int, heightPixels: Int) {
        px = widthPixels / 2
        py = heightPixels / 2
    }

    companion object {
        fun isNearToDegree(angle: Float, compareTo: Float): Boolean {
            val difference = Math.abs(angle - compareTo)
            return if (difference > 180) 360 - difference < 3f else difference < 3f
        }
    }
}
