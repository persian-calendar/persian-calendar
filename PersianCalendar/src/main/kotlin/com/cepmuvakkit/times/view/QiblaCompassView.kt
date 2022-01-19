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
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import com.cepmuvakkit.times.posAlgo.EarthPosition
import net.androgames.level.AngleDisplay
import java.util.*
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

class QiblaCompassView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var angle = 0f
        set(value) {
            if (value != field) {
                field = value
                postInvalidate()
            }
        }

    private val northwardShapePath = Path()
    private val northArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
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
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = Color.LTGRAY
        it.strokeWidth = 1.dp
    }
    private val moonShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x808080FF.toInt()
        it.style = Paint.Style.STROKE
        it.strokeWidth = 9.dp
        it.strokeCap = Paint.Cap.ROUND
    }
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1.dp
    }
    private val sunShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x80808080.toInt()
        it.style = Paint.Style.STROKE
        it.strokeWidth = 9.dp
        it.strokeCap = Paint.Cap.ROUND
    }
    private val qiblaPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF009000.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = 1.dp
        it.pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }
    private val kaaba = BitmapFactory.decodeResource(resources, R.drawable.kaaba)

    private var cx = 0f
    private var cy = 0f // Center of Compass (cx, cy)
    private var radius = 0f // radius of Compass dial
    private var r = 0f // radius of Sun and Moon

    private var sunMoonPosition = GregorianCalendar().calculateSunMoonPosition(coordinates)

    private val fullDay = Clock(24, 0).toMinutes().toFloat()
    private var sunProgress = Clock(Calendar.getInstance()).toMinutes() / fullDay

    private val enableShade = false

    fun setTime(time: GregorianCalendar) {
        sunMoonPosition = time.calculateSunMoonPosition(coordinates)
        sunProgress = Clock(time).toMinutes() / fullDay
        postInvalidate()
    }

    val qiblaHeading = coordinates?.run {
        EarthPosition(latitude, longitude).toEarthHeading(EarthPosition(21.422522, 39.826181))
    }
    var isShowQibla = true
        set(value) {
            field = value
            postInvalidate()
        }
    private val textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = ContextCompat.getColor(context, R.color.qibla_color)
        it.textSize = 12.sp
        it.textAlign = Paint.Align.CENTER
    }
    private val textStrokePaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = context.resolveColor(R.attr.colorCard)
        it.strokeWidth = 5.dp
        it.style = Paint.Style.STROKE
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
            it.rewind()
            it.moveTo(cx, cy - radius)
            it.lineTo(cx - r, cy)
            it.arcTo(RectF(cx - r, cy - r, cx + r, cy + r), 180f, -180f)
            it.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        angleDisplay.draw(canvas, (round(angle) + 360f) % 360f)
        canvas.withRotation(-angle, cx, cy) {
            drawDial()
            drawPath(northwardShapePath, northArrowPaint)
            if (coordinates != null) {
                drawQibla()
                drawMoon()
                drawSun()
            }
        }
    }

    private fun Canvas.drawDial() {
        // Draw the background
        drawCircle(cx, cy, radius, circlePaint)
        drawCircle(cx, cy, radius * .975f, circlePaint)
        // Rotate our perspective so that the "top" is
        // facing the current bearing.
        val cardinalX = cx
        val cardinalY = cy - radius * .85f

        // Draw the marker every 15 degrees and text every 45.
        (0..23).forEach {
            withRotation(15f * it, cx, cy) {
                drawLine(cx, cy - radius, cx, cy - radius * .975f, markerPaint)
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
                    drawText((it * 15).toString(), cardinalX, cardinalY, textPaint)
                }
            }
        }
    }

    private val solarDraw = SolarDraw(context)

    private val shadeFactor = 1.5f

    private fun Canvas.drawSun() {
        if (sunMoonPosition.isNight == true) return
        val sunPosition = sunMoonPosition.sunPosition ?: return
        val rotation = sunPosition.azimuth.toFloat() - 360
        withRotation(rotation, cx, cy) {
            val sunHeight = (sunPosition.altitude.toFloat() / 90 - 1) * radius
            val sunColor = solarDraw.sunColor(sunProgress)
            sunPaint.color = sunColor
            drawLine(cx, cy - radius, cx, cy + radius, sunPaint)
            if (enableShade) drawLine(cx, cy, cx, cy - sunHeight / shadeFactor, sunShadePaint)
            solarDraw.sun(this, cx, cy + sunHeight, r, sunColor)
        }
    }

    private fun Canvas.drawMoon() {
        val moonPosition = sunMoonPosition.moonPosition ?: return
        if (moonPosition.altitude <= -5) return
        withRotation(moonPosition.azimuth.toFloat() - 360, cx, cy) {
            val moonHeight = (moonPosition.altitude.toFloat() / 90 - 1) * radius
            drawLine(cx, cy - radius, cx, cy + radius, moonPaint)
            if (enableShade) drawLine(cx, cy, cx, cy - moonHeight / shadeFactor, moonShadePaint)
            solarDraw.moon(this, sunMoonPosition, cx, cy + moonHeight, r * .8f)
        }
    }

    private fun Canvas.drawQibla() {
        if (!isShowQibla) return
        val qiblaHeading = qiblaHeading ?: return
        withRotation(qiblaHeading.heading.toFloat() - 360, cx, cy) {
            drawLine(cx, cy - radius, cx, cy + radius, qiblaPaint)
            drawBitmap(kaaba, cx - kaaba.width / 2, cy - radius - kaaba.height / 2, null)
            val textCenter = cy - radius / 2
            withRotation(90f, cx, textCenter) {
                val distance =
                    "%,d km".format(Locale.ENGLISH, (qiblaHeading.metres / 1000f).roundToInt())
                drawText(distance, cx, textCenter + 4.dp, textStrokePaint)
                drawText(distance, cx, textCenter + 4.dp, textPaint)
            }
        }
    }
}
