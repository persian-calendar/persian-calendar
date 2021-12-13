package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class SunView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @ColorInt textColor: Int? = null
) : View(context, attrs), ValueAnimator.AnimatorUpdateListener {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dayPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL_AND_STROKE }
    private val horizonColor = textColor ?: context.resolveColor(R.attr.SunViewHorizonColor)
    private val timelineColor = textColor ?: context.resolveColor(R.attr.SunViewTimelineColor)
    private val taggingColor = textColor ?: context.resolveColor(R.attr.SunViewTaglineColor)
    private val nightColor = ContextCompat.getColor(context, R.color.sViewNightColor)
    private val dayColor = ContextCompat.getColor(context, R.color.sViewDayColor)
    private val daySecondColor = ContextCompat.getColor(context, R.color.sViewDaySecondColor)
    private val sunriseTextColor = textColor ?: context.resolveColor(R.attr.SunViewSunriseTextColor)
    private val middayTextColor = textColor ?: context.resolveColor(R.attr.SunViewMiddayTextColor)
    private val sunsetTextColor = textColor ?: context.resolveColor(R.attr.SunViewSunsetTextColor)
    private val colorTextSecond = textColor ?: context.resolveColor(R.attr.colorTextSecond)

    internal var width: Int = 0
    internal var height: Int = 0
    private val curvePath = Path()
    private val nightPath = Path()
    private var current = 0f
    private var dayLengthString = ""
    private var remainingString = ""
    private val sunriseString = context.getString(R.string.sunriseSunView)
    private val middayString = context.getString(R.string.middaySunView)
    private val sunsetString = context.getString(R.string.sunsetSunView)
    private var segmentByPixel = .0
    private var prayTimes: PrayTimes? = null
    private var moonPhase = 1.0
    private val fontSize = if (language.isArabicScript) 14.dp else 11.5.dp

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        width = w
        height = h - 18

        dayPaint.shader = LinearGradient(
            width * .17f, 0f, width / 2f, 0f, dayColor, daySecondColor,
            Shader.TileMode.MIRROR
        )

        if (width != 0) segmentByPixel = 2 * PI / width

        curvePath.also {
            it.reset()
            it.moveTo(0f, height.toFloat())
            (0..width).forEach { x ->
                it.lineTo(x.toFloat(), getY(x, segmentByPixel, (height * .9f).toInt()))
            }
        }

        nightPath.also {
            it.reset()
            it.addPath(curvePath)
            it.setLastPoint(width.toFloat(), height.toFloat())
            it.lineTo(width.toFloat(), 0f)
            it.lineTo(0f, 0f)
            it.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (clippingPath.isEmpty) mainDraw(canvas) // no change if there is path is empty
        else canvas.withClip(clippingPath) { mainDraw(canvas) }
    }

    // A home-screen widget with background has some roundness that is taken care by a passed path
    val clippingPath = Path()

    private fun mainDraw(canvas: Canvas) {
        val width = width
        val height = height
        val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        canvas.withScale(x = if (isRtl) -1f else 1f, pivotX = width / 2f) {
            // draw fill of night
            withClip(0f, height * .75f, width * current, height.toFloat()) {
                paint.also {
                    it.style = Paint.Style.FILL
                    it.color = nightColor
                }
                drawPath(nightPath, paint)
            }

            // draw fill of day
            withClip(0f, 0f, width * current, height * .75f) {
                drawPath(curvePath, dayPaint)
            }

            // draw time curve
            paint.also {
                it.strokeWidth = 3f
                it.style = Paint.Style.STROKE
                it.color = timelineColor
            }
            drawPath(curvePath, paint)

            // draw horizon line
            paint.color = horizonColor
            drawLine(0f, height * .75f, width.toFloat(), height * .75f, paint)

            // draw sunset and sunrise tag line indicator
            paint.also {
                it.color = taggingColor
                it.strokeWidth = 2f
            }
            drawLine(width * .17f, height * .3f, width * .17f, height * .7f, paint)
            drawLine(width * .83f, height * .3f, width * .83f, height * .7f, paint)
            drawLine(width / 2f, height * .7f, width / 2f, height * .8f, paint)

            // draw sun
            val radius = sqrt(width * height * .002f)
            val cx = width * current
            val cy = getY((width * current).toInt(), segmentByPixel, (height * .9f).toInt())
            if (current in .17f..0.83f) {
                solarDraw.sun(canvas, cx, cy, radius, current)
            } else canvas.withRotation(if (isRtl) 0f else 180f, cx, cy) { // cancel parent flip
                solarDraw.moon(canvas, moonPhase, cx, cy, radius)
            }
        }

        // draw text
        paint.also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = fontSize
            it.strokeWidth = 0f
            it.style = Paint.Style.FILL
            it.color = sunriseTextColor
        }
        canvas.drawText(
            sunriseString, width * if (isRtl) .83f else .17f, height * .2f, paint
        )
        paint.color = middayTextColor
        canvas.drawText(middayString, width / 2f, height * .94f, paint)
        paint.color = sunsetTextColor
        canvas.drawText(
            sunsetString, width * if (isRtl) .17f else .83f, height * .2f, paint
        )

        // draw remaining time
        paint.also {
            it.textAlign = Paint.Align.CENTER
            it.strokeWidth = 0f
            it.style = Paint.Style.FILL
            it.color = colorTextSecond
        }
        canvas.drawText(
            dayLengthString, width * if (isRtl) .70f else .30f, height * .94f, paint
        )
        canvas.drawText(
            remainingString, width * if (isRtl) .30f else .70f, height * .94f, paint
        )
    }

    private val solarDraw = SolarDraw()

    private fun getY(x: Int, segment: Double, height: Int): Float =
        height - height * ((cos(-PI + x * segment) + 1f) / 2f).toFloat() + height * .1f

    fun setPrayTimesAndMoonPhase(prayTimes: PrayTimes, moonPhase: Double) {
        this.prayTimes = prayTimes
        this.moonPhase = moonPhase
        postInvalidate()
    }

    fun initiate() {
        val prayTimes = prayTimes ?: return

        val sunset = Clock.fromHoursFraction(prayTimes.sunset).toMinutes().toFloat()
        val sunrise = Clock.fromHoursFraction(prayTimes.sunrise).toMinutes().toFloat()
        val now = Clock(Calendar.getInstance(Locale.getDefault())).toMinutes().toFloat()

        fun Float.safeDiv(other: Float) = if (other == 0f) 0f else this / other
        current = when {
            now <= sunrise -> now.safeDiv(sunrise) * .17f
            now <= sunset -> (now - sunrise).safeDiv(sunset - sunrise) * .66f + .17f
            else -> (now - sunset).safeDiv(fullDay - sunset) * .17f + .17f + .66f
        }

        val dayLength = Clock.fromMinutesCount((sunset - sunrise).toInt())
        val remaining = Clock.fromMinutesCount(
            if (now > sunset || now < sunrise) 0 else (sunset - now).toInt()
        )
        dayLengthString = context.getString(R.string.length_of_day) + spacedColon +
                dayLength.asRemainingTime(resources, short = true)
        remainingString = if (remaining.toMinutes() == 0) "" else
            context.getString(R.string.remaining_daylight) + spacedColon +
                    remaining.asRemainingTime(resources, short = true)
        // a11y
        contentDescription = context.getString(R.string.length_of_day) + spacedColon +
                dayLength.asRemainingTime(resources) + if (remaining.toMinutes() == 0) "" else
            ("\n\n" + context.getString(R.string.remaining_daylight) + spacedColon +
                    remaining.asRemainingTime(resources))
    }

    fun startAnimate() {
        initiate()
        // "current" has the final value after #initiate() call, let's animate from zero to it.
        ValueAnimator.ofFloat(0F, current).also {
            it.duration = 1500L
            it.interpolator = DecelerateInterpolator()
            it.addUpdateListener(this)
        }.start()
    }

    fun clear() {
        current = 0f
        postInvalidate()
    }

    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        current = valueAnimator.animatedValue as Float
        postInvalidate()
    }

    companion object {
        private val fullDay = Clock(24, 0).toMinutes().toFloat()
    }
}
