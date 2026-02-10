// copyedited from https://code.google.com/p/android-salat-times/source/browse/src/com/cepmuvakkit/times/view/QiblaCompassView.java
// licensed under GPLv3
package com.byagowi.persiancalendar.ui.compass

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.EarthPosition
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.showQibla
import com.byagowi.persiancalendar.global.showTrueNorth
import com.byagowi.persiancalendar.ui.calendar.detectZoom
import com.byagowi.persiancalendar.ui.common.AngleDisplay
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.resolveAndroidCustomTypeface
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.toObserver
import java.util.GregorianCalendar
import kotlin.math.cbrt
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun Compass(
    qiblaHeading: EarthPosition.EarthHeading?,
    time: GregorianCalendar,
    angle: FloatState,
    modifier: Modifier,
) {
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    val resources = LocalResources.current
    val context = LocalContext.current
    var updateToken by remember { mutableIntStateOf(0) }
    val angleDisplay = remember { AngleDisplay(context, "0", "888") }
    val compassView = remember { CompassView(resources) { ++updateToken } }
    compassView.isShowQibla = showQibla
    compassView.isTrueNorth = showTrueNorth
    val density = LocalDensity.current
    with(LocalDensity.current) {
        val textSizePx = (12 * zoom).sp.toPx()
        val strokeWidthPx = (1 * zoom).dp.toPx()
        compassView.setScale(textSizePx, strokeWidthPx, zoom)
    }
    compassView.qiblaHeading = qiblaHeading
    compassView.setFont(resolveAndroidCustomTypeface())
    compassView.setSurfaceColor(animateColor(MaterialTheme.colorScheme.surface).value.toArgb())
    compassView.setTime(time)
    BoxWithConstraints {
        val width = this.maxWidth
        val height = this.maxHeight
        with(density) {
            val width = width.roundToPx()
            val height = height.roundToPx()
            angleDisplay.updatePlacement(width / 2, height)
            val cx = width / 2f
            val cy = height / 2f - angleDisplay.lcdHeight
            compassView.updateSize(cx, cy)
        }
        Canvas(
            modifier
                .fillMaxSize()
                .detectZoom { zoom = (zoom * it).coerceIn(1f, 2f) },
        ) {
            updateToken.let {}
            compassView.draw(this.drawContext.canvas.nativeCanvas, angle.floatValue)
        }
        Canvas(Modifier.fillMaxSize()) {
            val value = (round(compassView.trueNorth(angle.floatValue)) + 360f) % 360f
            angleDisplay.draw(this.drawContext.canvas.nativeCanvas, value)
        }
    }
}

private class CompassView(
    private val resources: Resources,
    private val invalidate: () -> Unit,
) {
    var isTrueNorth: Boolean = true
    fun trueNorth(angle: Float) =
        angle + if (isTrueNorth) astronomyState?.declination ?: 0f else 0f

    var qiblaHeading: EarthPosition.EarthHeading? = null
    var isShowQibla: Boolean = false

    // This applies true north correction if it isn't enabled by user but does nothing if is already on
    private fun fixForTrueNorth(angle: Float) =
        angle - (if (!isTrueNorth) astronomyState?.declination ?: 0f else 0f)

    private val northwardShapePath = Path()
    private val northArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.RED
        it.style = Paint.Style.FILL
    }
    private val markerPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = Color.GRAY
    }
    private val dp = resources.dp
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GRAY
        it.strokeWidth = .5f * dp
        it.style = Paint.Style.STROKE // Sadece Cember ciziyor.
    }
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = Color.LTGRAY
    }
    private val moonShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x808080FF.toInt()
        it.style = Paint.Style.STROKE
        it.strokeWidth = 9 * dp
        it.strokeCap = Paint.Cap.ROUND
    }
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.STROKE }
    private val sunShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x80808080.toInt()
        it.style = Paint.Style.STROKE
        it.strokeWidth = 9 * dp
        it.strokeCap = Paint.Cap.ROUND
    }
    private val qiblaPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF009000.toInt()
        it.style = Paint.Style.FILL_AND_STROKE
        val dashSize = 4 * dp
        it.pathEffect = DashPathEffect(floatArrayOf(dashSize, dashSize / 2), 0f)
    }
    private val kaaba = resources.getDrawable(R.drawable.kaaba, null)
        .toBitmap((32 * dp).toInt(), (32 * dp).toInt())

    private var cx = 0f
    private var cy = 0f // Center of Compass (cx, cy)
    private var radius = 0f // radius of Compass dial
    private var r = 0f // radius of Sun and Moon

    private val observer = coordinates?.toObserver()
    private var astronomyState = observer?.let { AstronomyState(it, GregorianCalendar()) }

    private var sunProgress = (Clock(GregorianCalendar()).value / 24).toFloat()

    private val enableShade = false

    fun setTime(time: GregorianCalendar) {
        astronomyState = observer?.let { AstronomyState(it, time) }
        sunProgress = (Clock(time).value / 24).toFloat()
        invalidate()
    }

    private val planetsPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = Color.GRAY
        it.textAlign = Paint.Align.CENTER
    }
    private val textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = Color.GRAY
        it.textAlign = Paint.Align.CENTER
    }
    private val textSecondPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = Color.GRAY
        it.alpha = 120
        it.textAlign = Paint.Align.CENTER
    }
    private val textStrokePaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.strokeWidth = 5 * dp
        it.style = Paint.Style.STROKE
        it.textAlign = Paint.Align.CENTER
    }

    fun setFont(typeface: Typeface?) {
        textPaint.typeface = typeface
        textStrokePaint.typeface = typeface
        planetsPaint.typeface = typeface
    }

    fun setSurfaceColor(@ColorInt color: Int) {
        textStrokePaint.color = color
    }

    fun updateSize(cx: Float, cy: Float) {
        this.cx = cx
        this.cy = cy
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
        invalidate()
    }

    fun setScale(textSize: Float, strokeWidth: Float, scale: Float) {
        planetsPaint.textSize = textSize
        textPaint.textSize = textSize
        textSecondPaint.textSize = textSize
        textStrokePaint.textSize = textSize
        northArrowPaint.alpha = (100 * cbrt(scale)).roundToInt()
        qiblaPaint.strokeWidth = strokeWidth
        moonPaint.strokeWidth = strokeWidth
        sunPaint.strokeWidth = strokeWidth
        invalidate()
    }

    fun draw(canvas: Canvas, angle: Float) {
        val trueNorth = trueNorth(angle)
        canvas.withRotation(-trueNorth, cx, cy) {
            drawDial()
            drawPath(northwardShapePath, northArrowPaint)
            if (coordinates != null) {
                drawMoon(trueNorth)
                drawSun()
                drawQibla()
                drawPlanets(trueNorth)
            }
        }
    }

    private fun cardinalDirection(value: Int): String {
        return when (value) {
            0 -> "N"
            6 -> "E"
            12 -> "S"
            18 -> "W"
            else -> ""
        }
    }

    private val directions = (0..<24).map {
        when {
            it % 6 == 0 -> if (language.isArabicScript) resources.getString(
                when (it) {
                    0 -> R.string.north
                    6 -> R.string.east
                    12 -> R.string.south
                    18 -> R.string.west
                    else -> R.string.empty
                },
            ) else cardinalDirection(it)

            it % 3 == 0 -> numeral.format(it * 15) + "°" // Draw the text every alternate 45deg
            else -> ""
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
        val cardinalSecondY = cy - radius * .70f

        // Draw the marker every 15 degrees and text every 45.
        repeat(24) {
            withRotation(15f * it, cx, cy) {
                drawLine(cx, cy - radius, cx, cy - radius * .975f, markerPaint)
                // Draw the cardinal points
                drawText(directions[it], cardinalX, cardinalY, textPaint)
                if (language.isArabicScript && (it == 0 || it == 6 || it == 12 || it == 18)) {
                    val label = cardinalDirection(it)
                    drawText(label, cardinalX, cardinalSecondY, textSecondPaint)
                }
            }
        }
    }

    private val solarDraw = SolarDraw(resources)

    private val shadeFactor = 1.5f

    private fun Canvas.drawSun() {
        val astronomyState = astronomyState ?: return
        if (astronomyState.isNight) return
        val rotation = fixForTrueNorth(astronomyState.sunHorizon.azimuth.toFloat())
        withRotation(rotation, cx, cy) {
            val sunHeight = (astronomyState.sunHorizon.altitude.toFloat() / 90 - 1) * radius
            val sunColor = solarDraw.sunColor(sunProgress)
            sunPaint.color = sunColor
            drawLine(cx, cy - radius, cx, cy + radius, sunPaint)
            if (enableShade) drawLine(cx, cy, cx, cy - sunHeight / shadeFactor, sunShadePaint)
            solarDraw.sun(this, cx, cy + sunHeight, r, sunColor)
        }
    }

    private fun Canvas.drawMoon(trueNorth: Float) {
        val astronomyState = astronomyState ?: return
        if (astronomyState.isMoonGone) return
        val azimuth = fixForTrueNorth(astronomyState.moonHorizon.azimuth.toFloat())
        withRotation(azimuth, cx, cy) {
            val moonHeight = (astronomyState.moonHorizon.altitude.toFloat() / 90 - 1) * radius
            drawLine(cx, cy - radius, cx, cy + radius, moonPaint)
            if (enableShade) drawLine(cx, cy, cx, cy - moonHeight / shadeFactor, moonShadePaint)
            withRotation(-azimuth + trueNorth, cx, cy + moonHeight) {
                solarDraw.moon(
                    this, astronomyState.sun, astronomyState.moon, cx, cy + moonHeight,
                    r * .8f, astronomyState.moonTiltAngle,
                )
            }
        }
    }

    private fun Canvas.drawPlanets(trueNorth: Float) {
        val astronomyState = astronomyState ?: return
        planetsPaint.alpha = (127 - astronomyState.sunHorizon.altitude.toInt() * 3).coerceIn(0, 255)
        astronomyState.planets.forEach { (title, planetHorizon) ->
            val azimuth = fixForTrueNorth(planetHorizon.azimuth.toFloat())
            val planetHeight = (planetHorizon.altitude.toFloat() / 90 - 1) * radius
            withRotation(azimuth, cx, cy) {
                drawCircle(cx, cy + planetHeight, radius / 120, planetsPaint)
                withRotation(-azimuth + trueNorth, cx, cy + planetHeight) {
                    drawText(
                        resources.getString(title), cx, cy + planetHeight - radius / 40,
                        planetsPaint,
                    )
                }
            }
        }
    }

    private fun Canvas.drawQibla() {
        if (!isShowQibla) return
        val qiblaHeading = qiblaHeading ?: return
        withRotation(fixForTrueNorth(qiblaHeading.heading), cx, cy) {
            drawLine(cx, cy - radius, cx, cy + radius, qiblaPaint)
            drawBitmap(kaaba, cx - kaaba.width / 2, cy - radius - kaaba.height / 2, null)
            val textCenter = cy - radius / 2
            withRotation(90f, cx, textCenter) {
                val distance = qiblaHeading.km
                drawText(distance, cx, textCenter + 4 * dp, textStrokePaint)
                drawText(distance, cx, textCenter + 4 * dp, textPaint)
            }
        }
    }
}
