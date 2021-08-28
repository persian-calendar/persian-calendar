package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getAppFont
import com.byagowi.persiancalendar.utils.language
import com.google.android.material.animation.ArgbEvaluatorCompat
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class SunView(context: Context, attrs: AttributeSet? = null) : View(context, attrs),
    ValueAnimator.AnimatorUpdateListener {

    private val fullDay = Clock(24, 0).toInt().toFloat()
    private val halfDay = Clock(12, 0).toInt().toFloat()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.typeface = getAppFont(context) }
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL }
    private val dayPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL_AND_STROKE }
    private val horizonColor = context.resolveColor(R.attr.SunViewHorizonColor)
    private val timelineColor = context.resolveColor(R.attr.SunViewTimelineColor)
    private val taggingColor = context.resolveColor(R.attr.SunViewTaglineColor)
    private val nightColor = ContextCompat.getColor(context, R.color.sViewNightColor)
    private val dayColor = ContextCompat.getColor(context, R.color.sViewDayColor)
    private val daySecondColor = ContextCompat.getColor(context, R.color.sViewDaySecondColor)
    private val sunBeforeMiddayColor =
        ContextCompat.getColor(context, R.color.sViewSunBeforeMiddayColor)
    private val sunAfterMiddayColor =
        ContextCompat.getColor(context, R.color.sViewSunAfterMiddayColor)
    private val sunriseTextColor = context.resolveColor(R.attr.SunViewSunriseTextColor)
    private val middayTextColor = context.resolveColor(R.attr.SunViewMiddayTextColor)
    private val sunsetTextColor = context.resolveColor(R.attr.SunViewSunsetTextColor)
    private val colorTextSecond = context.resolveColor(R.attr.colorTextSecond)

    internal var width: Int = 0
    internal var height: Int = 0
    private val curvePath = Path()
    private val nightPath = Path()
    private var current = 0f
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.style = Paint.Style.FILL_AND_STROKE
    }

    // moon Paint Black
    private val moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.BLACK
        it.style = Paint.Style.FILL_AND_STROKE
    }

    // moon Paint for Oval
    private val moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.style = Paint.Style.FILL_AND_STROKE
    }

    // moon Paint for Diameter
    private val moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GRAY
        it.style = Paint.Style.STROKE
    }
    private val moonRect = RectF()
    private val moonOval = RectF()
    private var dayLengthString = ""
    private var remainingString = ""
    private val sunriseString = context.getString(R.string.sunriseSunView)
    private val middayString = context.getString(R.string.middaySunView)
    private val sunsetString = context.getString(R.string.sunsetSunView)
    private var segmentByPixel = .0
    private var prayTimes: PrayTimes? = null
    private var moonPhase = 1.0
    private val fontSize = if (language.isArabicScript) 14.dp else 12.dp

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
            val sunMoonRadius = height * .09f
            if (current in .17f..0.83f) {
                sunPaint.color = ArgbEvaluatorCompat.getInstance().evaluate(
                    current, sunBeforeMiddayColor, sunAfterMiddayColor
                )
                drawCircle(
                    width * current,
                    getY((width * current).toInt(), segmentByPixel, (height * .9f).toInt()),
                    sunMoonRadius, sunPaint
                )
            } else drawMoon(canvas, sunMoonRadius)
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

    private fun drawMoon(canvas: Canvas, r: Float) {
        // This is brought from QiblaCompassView with some modifications
        val radius = 1f
        val px = width * current
        val py = getY((width * current).toInt(), segmentByPixel, (height * .9f).toInt())
        canvas.withRotation(180f, px, py) {
            val eOffset = 0
            val arcWidth = ((moonPhase - .5) * (4 * r)).toInt()
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect.set(px - r, py + eOffset - radius - r, px + r, py + eOffset - radius + r)
            canvas.drawArc(moonRect, 90f, 180f, false, moonPaint)
            canvas.drawArc(moonRect, 270f, 180f, false, moonPaintB)
            moonOval.set(
                px - abs(arcWidth) / 2f, py + eOffset - radius - r,
                px + abs(arcWidth) / 2f, py + eOffset - radius + r
            )
            moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
            canvas.drawArc(moonOval, 0f, 360f, false, moonPaintO)
            canvas.drawArc(moonRect, 0f, 360f, false, moonPaintD)
        }
    }

    private fun getY(x: Int, segment: Double, height: Int): Float =
        height - height * ((cos(-PI + x * segment) + 1f) / 2f).toFloat() + height * .1f

    fun setSunriseSunsetMoonPhase(prayTimes: PrayTimes, moonPhase: Double) {
        this.prayTimes = prayTimes
        this.moonPhase = moonPhase
        postInvalidate()
    }

    fun startAnimate() {
        val context = context ?: return
        val prayTimes = prayTimes ?: return

        val sunset = prayTimes.sunsetClock.toInt().toFloat()
        val sunrise = prayTimes.sunriseClock.toInt().toFloat()
        var midnight = prayTimes.midnightClock.toInt().toFloat()

        if (midnight > halfDay) midnight -= fullDay
        val now = Clock(Calendar.getInstance(Locale.getDefault())).toInt().toFloat()

        fun Float.safeDiv(other: Float) = if (other == 0f) 0f else this / other
        val c = when {
            now <= sunrise -> (now - midnight).safeDiv(sunrise) * .17f
            now <= sunset -> (now - sunrise).safeDiv(sunset - sunrise) * .66f + .17f
            else -> (now - sunset).safeDiv(fullDay + midnight - sunset) * .17f + .17f + .66f
        }

        val dayLength = Clock.fromInt((sunset - sunrise).toInt())
        val remaining = Clock.fromInt(
            if (now > sunset || now < sunrise) 0 else (sunset - now).toInt()
        )
        dayLengthString = context.getString(
            R.string.length_of_day, formatNumber(dayLength.hour), formatNumber(dayLength.minute)
        )
        remainingString = when {
            remaining.toInt() == 0 -> ""
            else -> context.getString(
                R.string.remaining_daylight,
                formatNumber(remaining.hour), formatNumber(remaining.minute)
            )
        }

        ValueAnimator.ofFloat(0F, c).also {
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
}
