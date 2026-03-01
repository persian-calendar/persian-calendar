package com.byagowi.persiancalendar.ui.calendar.times

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.GregorianCalendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

@Immutable
data class SunViewColors(
    val nightColor: Color,
    val dayColor: Color,
    val middayColor: Color,
    val sunriseTextColor: Color,
    val middayTextColor: Color,
    val sunsetTextColor: Color,
    val textColorSecondary: Color,
    val linesColor: Color,
)

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class SunView(
    resources: Resources,
    prayTimes: PrayTimes?,
    colors: SunViewColors,
    private val width: Int,
    private val height: Int,
    timeInMillis: Long,
    typeface: Typeface?,
    private val isRtl: Boolean,
) {
    private val sunset = prayTimes?.sunset
    private val sunrise = prayTimes?.sunrise
    private val now = Clock(GregorianCalendar().also { it.timeInMillis = timeInMillis }).value

    fun Double.safeDiv(other: Double): Float = if (other == .0) 0f else (this / other).toFloat()

    private val current = when {
        sunset == null || sunrise == null -> 0f
        now <= sunrise -> now.safeDiv(sunrise) * .17f
        now <= sunset -> (now - sunrise).safeDiv(sunset - sunrise) * .66f + .17f
        else -> (now - sunset).safeDiv(24 - sunset) * .17f + .17f + .66f
    }

    private val dayLength = Clock(if (sunset == null || sunrise == null) .0 else sunset - sunrise)
    private val dayLengthString = if (sunset == null || sunrise == null) "" else {
        resources.getString(R.string.length_of_day) + spacedColon + run {
            dayLength.asRemainingTime(resources, short = true)
        }
    }

    private val remaining = if (sunset == null || sunrise == null) null
    else if (now !in sunrise..sunset) null else Clock(sunset - now)
    private val remainingString = if (remaining == null) "" else {
        resources.getString(R.string.remaining_daylight) + spacedColon + run {
            remaining?.asRemainingTime(resources, short = true)
        }
    }

    val contentDescription = if (sunset == null || sunrise == null) "" else {
        resources.getString(R.string.length_of_day) + spacedColon + run {
            dayLength.asRemainingTime(resources)
        } + if (remaining == null) "" else {
            "\n\n" + resources.getString(R.string.remaining_daylight) + spacedColon + run {
                remaining?.asRemainingTime(resources)
            }
        }
    }

    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL_AND_STROKE
        it.shader = LinearGradient(
            width * .17f, 0f, width / 2f, 0f, colors.dayColor.toArgb(), colors.middayColor.toArgb(),
            Shader.TileMode.MIRROR,
        )
    }

    private val curvePath = Path().also {
        it.moveTo(0f, height.toFloat())
        repeat(width + 1) { x -> it.lineTo(x.toFloat(), getY(x.toFloat())) }
    }
    private val nightPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = colors.nightColor.toArgb()
    }

    private val nightPath = Path().also {
        it.addPath(curvePath)
        it.setLastPoint(width.toFloat(), height.toFloat())
        it.lineTo(width.toFloat(), 0f)
        it.lineTo(0f, 0f)
        it.close()
    }
    private val sunriseString = resources.getString(R.string.sunrise_sun_view)
    private val middayString = resources.getString(R.string.midday_sun_view)
    private val sunsetString = resources.getString(R.string.sunset_sun_view)

    private val time = Time.fromMillisecondsSince1970(timeInMillis)
    private var sun = sunPosition(time)
    private var moon = eclipticGeoMoon(time)
    private val dp = resources.dp
    private val fontSize = when {
        language.isArabicScript -> 14f
        language.isTamil -> 11f
        else -> 11.5f
    } * dp
    private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = 1 * dp
        it.style = Paint.Style.STROKE
        it.color = colors.linesColor.toArgb()
    }
    private val verticalLinesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = .75f * dp
        it.style = Paint.Style.STROKE
        it.color = colors.linesColor.toArgb()
    }
    private val sunriseTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.typeface = typeface
        it.textAlign = Paint.Align.CENTER
        it.textSize = fontSize
        it.style = Paint.Style.FILL
        it.color = colors.sunriseTextColor.toArgb()
    }
    private val middayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.typeface = typeface
        it.textAlign = Paint.Align.CENTER
        it.textSize = fontSize
        it.style = Paint.Style.FILL
        it.color = colors.middayTextColor.toArgb()
    }
    private val sunsetTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.typeface = typeface
        it.textAlign = Paint.Align.CENTER
        it.textSize = fontSize
        it.style = Paint.Style.FILL
        it.color = colors.sunsetTextColor.toArgb()
    }
    private val belowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.typeface = typeface
        it.textAlign = Paint.Align.CENTER
        it.textSize = fontSize
        it.style = Paint.Style.FILL
        it.color = colors.textColorSecondary.toArgb()
    }

    private val solarDraw = SolarDraw(resources)

    fun draw(canvas: Canvas, fraction: Float) {
        val value = fraction * current
        canvas.withScale(x = if (isRtl) -1f else 1f, pivotX = width / 2f) {
            val width = this@SunView.width
            val height = this@SunView.height
            // draw fill of night
            withClip(0f, height * .75f, width * value, height.toFloat()) {
                drawPath(nightPath, nightPaint)
            }

            // draw fill of day
            withClip(0f, 0f, width * value, height * .75f) {
                drawPath(curvePath, dayPaint)
            }

            // draw time curve
            drawPath(curvePath, linesPaint)
            // draw horizon line
            drawLine(0f, height * .75f, width.toFloat(), height * .75f, linesPaint)
            // draw sunset and sunrise tag line indicator
            drawLine(width * .17f, height * .3f, width * .17f, height * .7f, verticalLinesPaint)
            drawLine(width * .83f, height * .3f, width * .83f, height * .7f, verticalLinesPaint)
            drawLine(width / 2f, height * .7f, width / 2f, height * .8f, verticalLinesPaint)

            // draw sun
            val radius = sqrt(width * height * .002f)
            val cx = width * value
            val cy = getY(cx)
            if (value in .17f..0.83f) withRotation(fraction * 900f, cx, cy) {
                solarDraw.sun(canvas, cx, cy, radius, solarDraw.sunColor(value))
            } else canvas.withScale(x = if (isRtl) -1f else 1f, pivotX = cx) {
                // cancel parent flip
                solarDraw.moon(canvas, sun, moon, cx, cy, radius)
            }
        }

        // draw text
        canvas.drawText(
            sunriseString, width * if (isRtl) .83f else .17f, height * .2f, sunriseTextPaint,
        )
        canvas.drawText(middayString, width / 2f, height * .94f, middayTextPaint)
        canvas.drawText(
            sunsetString, width * if (isRtl) .17f else .83f, height * .2f, sunsetTextPaint,
        )

        // draw remaining time
        if (language.isTamil) {
            val (a, b) = dayLengthString.split(spacedColon).takeIf { it.size == 2 }
                ?: listOf("", "")
            val (c, d) = remainingString.split(spacedColon).takeIf { it.size == 2 }
                ?: listOf("", "")
            val lineHeight = belowTextPaint.descent() - belowTextPaint.ascent()
            canvas.drawSideText(isRtl, width, height * .94f - lineHeight / 2, a, c)
            canvas.drawSideText(isRtl, width, height * .94f + lineHeight / 2, b, d)
        } else canvas.drawSideText(isRtl, width, height * .94f, dayLengthString, remainingString)
    }

    private fun Canvas.drawSideText(isRtl: Boolean, w: Int, y: Float, a: String, b: String) {
        drawText(a, w * if (isRtl) .70f else .30f, y, belowTextPaint)
        drawText(b, w * if (isRtl) .30f else .70f, y, belowTextPaint)
    }

    private fun getY(x: Float): Float = height * (.55f + cos(x / width * 2 * PI.toFloat()) * .45f)
}
