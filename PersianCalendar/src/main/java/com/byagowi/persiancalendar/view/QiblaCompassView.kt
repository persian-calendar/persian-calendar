// copyedited from https://code.google.com/p/android-salat-times/source/browse/src/com/cepmuvakkit/times/view/QiblaCompassView.java
// licensed under GPLv3
package com.byagowi.persiancalendar.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.byagowi.persiancalendar.R
import com.cepmuvakkit.times.posAlgo.AstroLib
import com.cepmuvakkit.times.posAlgo.EarthHeading
import com.cepmuvakkit.times.posAlgo.Horizontal
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import java.util.*

class QiblaCompassView : View {
  private lateinit var dashedPaint: Paint
  private var px: Int = 0
  private var py: Int = 0 // Center of Compass (px,py)
  private var Radius: Int = 0 // Radius of Compass dial
  private var r: Int = 0 // Radius of Sun and Moon
  private var northString: String? = null
  private var eastString: String? = null
  private var southString: String? = null
  private var westString: String? = null
  private var dashPath: DashPathEffect? = null
  private var bearing: Float = 0.toFloat()
  private var sunPosition: Horizontal? = null
  private var moonPosition: Horizontal? = null
  private var qiblaInfo: EarthHeading? = null
  private lateinit var sunMoonPosition: SunMoonPosition
  private var longitude = 0.0
  private var latitude = 0.0

  private var textPaint: Paint? = null

  val isLongLatAvailable: Boolean
    get() = longitude != 0.0 && latitude != 0.0

  internal var mPath = Path()
  internal var trueNorthArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

  internal var markerPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
  internal var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

  internal var sunPaint = Paint(Paint.ANTI_ALIAS_FLAG)

  internal var moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  internal var moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG)
  internal var moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG)
  internal var moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG)
  internal var moonRect = RectF()
  internal var moonOval = RectF()

  internal var qiblaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  internal var kaaba = BitmapFactory.decodeResource(resources, R.drawable.kaaba)

  constructor(context: Context) : super(context) {
    initCompassView()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    initCompassView()
  }

  constructor(context: Context, ats: AttributeSet, defaultStyle: Int) : super(context, ats, defaultStyle) {
    initCompassView()
  }

  private fun initAstronomicParameters() {
    val c = GregorianCalendar()
    val jd = AstroLib.calculateJulianDay(c)

    val ΔT = 0.0
    val altitude = 0.0
    sunMoonPosition = SunMoonPosition(jd, latitude, longitude, altitude, ΔT)
    sunPosition = sunMoonPosition.sunPosition
    moonPosition = sunMoonPosition.moonPosition
  }

  fun initCompassView() {
    isFocusable = true
    initAstronomicParameters()
    northString = "N"
    eastString = "E"
    southString = "S"
    westString = "W"

    dashPath = DashPathEffect(floatArrayOf(2f, 5f), 1f)
    dashedPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
    dashedPaint.pathEffect = dashPath
    dashedPaint.strokeWidth = 2f
    dashedPaint.pathEffect = dashPath
    dashedPaint.color = ContextCompat.getColor(context, R.color.qibla_color)

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
    val specMode = View.MeasureSpec.getMode(measureSpec)
    val specSize = View.MeasureSpec.getSize(measureSpec)

    if (specMode == View.MeasureSpec.UNSPECIFIED) {
      // Return a default size of 200 if no bounds are specified.
      result = 600
    } else {
      // As you want to fill the available space
      // always return the full available bounds.
      result = specSize
    }
    return result
  }

  override fun onDraw(canvas: Canvas) {
    this.Radius = Math.min(px, py)
    this.r = Radius / 10 // Sun Moon radius;
    // over here
    qiblaInfo = sunMoonPosition.destinationHeading
    textPaint!!.textAlign = Paint.Align.LEFT
    textPaint!!.color = ContextCompat.getColor(context, R.color.qibla_color)
    canvas.rotate(-bearing, px.toFloat(), py.toFloat())// Attach and Detach capability lies
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

  fun drawTrueNorthArrow(canvas: Canvas, drawnAngle: Float) {
    trueNorthArrowPaint.reset()
    trueNorthArrowPaint.color = Color.RED
    trueNorthArrowPaint.style = Paint.Style.FILL
    trueNorthArrowPaint.alpha = 100
    val r = Radius / 12
    // Construct a wedge-shaped path
    mPath.reset()
    mPath.moveTo(px.toFloat(), (py - Radius).toFloat())
    mPath.lineTo((px - r).toFloat(), py.toFloat())
    mPath.lineTo(px.toFloat(), (py + r).toFloat())
    mPath.lineTo((px + r).toFloat(), py.toFloat())
    mPath.addCircle(px.toFloat(), py.toFloat(), r.toFloat(), Path.Direction.CCW)
    mPath.close()
    canvas.drawPath(mPath, trueNorthArrowPaint)
    dashedPaint.color = Color.RED
    canvas.drawLine(px.toFloat(), (py - Radius).toFloat(), px.toFloat(), (py + Radius).toFloat(), dashedPaint!!)
    canvas.drawCircle(px.toFloat(), py.toFloat(), 5f, dashedPaint)
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
    canvas.drawCircle(px.toFloat(), py.toFloat(), Radius.toFloat(), circlePaint)
    canvas.drawCircle(px.toFloat(), py.toFloat(), (Radius - 20).toFloat(), circlePaint)
    // Rotate our perspective so that the "top" is
    // facing the current bearing.

    val textWidth = textPaint!!.measureText("W").toInt()
    val cardinalX = px - textWidth / 2
    val cardinalY = py - Radius + textHeight

    // Draw the marker every 15 degrees and text every 45.
    for (i in 0..23) {
      // Draw a marker.
      canvas.drawLine(px.toFloat(), (py - Radius).toFloat(), px.toFloat(), (py - Radius + 10).toFloat(), markerPaint)
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
        val angleTextY = py - Radius + textHeight
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

    if (sunPosition!!.elevation > -10) {
      canvas.rotate(sunPosition!!.azimuth.toFloat() - 360, px.toFloat(), py.toFloat())
      sunPaint.pathEffect = dashPath

      val ry = ((90 - sunPosition!!.elevation) / 90 * Radius).toInt()
      canvas.drawCircle(px.toFloat(), (py - ry).toFloat(), r.toFloat(), sunPaint)
      dashedPaint!!.color = Color.YELLOW
      canvas.drawLine(px.toFloat(), (py - Radius).toFloat(), px.toFloat(), (py + Radius).toFloat(), dashedPaint)
      sunPaint.pathEffect = null
      canvas.restore()
    }

  }

  fun drawMoon(canvas: Canvas) {
    moonPaint.reset()
    moonPaint.color = Color.WHITE
    moonPaint.style = Paint.Style.FILL_AND_STROKE
    moonPaintB.reset()// moon Paint Black
    moonPaintB.color = Color.BLACK
    moonPaintB.style = Paint.Style.FILL_AND_STROKE
    moonPaintO.reset()// moon Paint for Oval
    moonPaintO.color = Color.WHITE
    moonPaintO.style = Paint.Style.FILL_AND_STROKE
    moonPaintD.reset()// moon Paint for Diameter
    // draw
    moonPaintD.color = Color.GRAY
    moonPaintD.style = Paint.Style.STROKE
    val moonPhase = sunMoonPosition!!.moonPhase
    if (moonPosition!!.elevation > -5) {
      canvas.rotate(moonPosition!!.azimuth.toFloat() - 360, px.toFloat(), py.toFloat())
      val eOffset = (moonPosition!!.elevation / 90 * Radius).toInt()
      // elevation Offset 0 for 0 degree; r for 90 degree
      moonRect.set((px - r).toFloat(), (py + eOffset - Radius - r).toFloat(), (px + r).toFloat(), (py + eOffset - Radius + r).toFloat())
      canvas.drawArc(moonRect, 90f, 180f, false, moonPaint)
      canvas.drawArc(moonRect, 270f, 180f, false, moonPaintB)
      val arcWidth = ((moonPhase - 0.5) * (4 * r)).toInt()
      moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
      moonOval.set((px - Math.abs(arcWidth) / 2).toFloat(), (py + eOffset - Radius - r).toFloat(),
          (px + Math.abs(arcWidth) / 2).toFloat(), (py + eOffset - Radius + r).toFloat())
      canvas.drawArc(moonOval, 0f, 360f, false, moonPaintO)
      canvas.drawArc(moonRect, 0f, 360f, false, moonPaintD)
      moonPaintD.pathEffect = dashPath
      canvas.drawLine(px.toFloat(), (py - Radius).toFloat(), px.toFloat(), (py + Radius).toFloat(), moonPaintD)
      moonPaintD.pathEffect = null
      canvas.restore()

    }

  }

  fun drawQibla(canvas: Canvas) {

    canvas.rotate(qiblaInfo!!.heading.toFloat() - 360, px.toFloat(), py.toFloat())
    qiblaPaint.reset()
    qiblaPaint.color = Color.GREEN
    qiblaPaint.style = Paint.Style.FILL_AND_STROKE
    qiblaPaint.pathEffect = dashPath
    qiblaPaint.strokeWidth = 5.5f

    canvas.drawLine(px.toFloat(), (py - Radius).toFloat(), px.toFloat(), (py + Radius).toFloat(), qiblaPaint)
    qiblaPaint.pathEffect = null
    canvas.drawBitmap(kaaba, (px - kaaba.width / 2).toFloat(), (py - Radius - kaaba.height / 2).toFloat(),
        qiblaPaint)
    canvas.restore()

  }

  fun setBearing(_bearing: Float) {
    bearing = _bearing
  }

  fun setLatitude(latitude: Double) {
    this.latitude = latitude
  }

  fun setLongitude(longitude: Double) {
    this.longitude = longitude
  }

  fun setScreenResolution(widthPixels: Int, heightPixels: Int) {
    this.px = widthPixels / 2
    this.py = heightPixels / 2
  }
}