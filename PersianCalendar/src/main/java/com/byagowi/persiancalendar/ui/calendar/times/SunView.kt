package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getAppFont
import com.byagowi.persiancalendar.utils.isRTL
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

class SunView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        View(context, attrs), ValueAnimator.AnimatorUpdateListener {

    private val FULL_DAY = Clock(24, 0).toInt().toFloat()
    private val HALF_DAY = Clock(12, 0).toInt().toFloat()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = getAppFont(context) }
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var sunRaisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 7f
        pathEffect = DashPathEffect(floatArrayOf(3f, 7f), 0f) /* Sun rays effect */
    }
    private val dayPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL_AND_STROKE }

    @ColorInt
    private var horizonColor: Int = 0

    @ColorInt
    private var timelineColor: Int = 0

    @ColorInt
    private var taggingColor: Int = 0

    @ColorInt
    private var nightColor: Int = 0

    @ColorInt
    private var dayColor: Int = 0

    @ColorInt
    private var daySecondColor: Int = 0

    @ColorInt
    private var sunColor: Int = 0

    @ColorInt
    private var sunBeforeMiddayColor: Int = 0

    @ColorInt
    private var sunAfterMiddayColor: Int = 0

    @ColorInt
    private var sunEveningColor: Int = 0

    @ColorInt
    private var sunriseTextColor: Int = 0

    @ColorInt
    private var middayTextColor: Int = 0

    @ColorInt
    private var sunsetTextColor: Int = 0

    @ColorInt
    private var colorTextNormal: Int = 0

    @ColorInt
    private var colorTextSecond: Int = 0
    internal var width: Int = 0
    internal var height: Int = 0
    lateinit var curvePath: Path
    lateinit var nightPath: Path
    private var current = 0f
    private var linearGradient = LinearGradient(0f, 0f, 1f, 0f, 0, 0, Shader.TileMode.MIRROR)
    private var moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG)
    private var moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG)
    private var moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG)
    private var moonRect = RectF()
    private var moonOval = RectF()
    private var dayLengthString = ""
    private var remainingString = ""
    private var sunriseString = ""
    private var middayString = ""
    private var sunsetString = ""
    private var isRTL = false
    private var segmentByPixel: Double = 0.toDouble()
    private var argbEvaluator = ArgbEvaluator()
    private var prayTimes: PrayTimes? = null

    //    private Horizontal moonPosition;
    private var moonPhase = 1.0
    private var fontSize: Int = 0

    private val Number.dp: Int
        get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()

    init {
        val tempTypedValue = TypedValue()

        @ColorInt
        fun resolveColor(attr: Int) = tempTypedValue.let {
            context.theme.resolveAttribute(attr, it, true)
            ContextCompat.getColor(context, it.resourceId)
        }

        colorTextNormal = resolveColor(R.attr.colorTextNormal)
        colorTextSecond = resolveColor(R.attr.colorTextSecond)

        horizonColor = resolveColor(R.attr.SunViewHorizonColor)
        timelineColor = resolveColor(R.attr.SunViewTimelineColor)
        taggingColor = resolveColor(R.attr.SunViewTaglineColor)
        sunriseTextColor = resolveColor(R.attr.SunViewSunriseTextColor)
        middayTextColor = resolveColor(R.attr.SunViewMiddayTextColor)
        sunsetTextColor = resolveColor(R.attr.SunViewSunsetTextColor)

        // resolveColor(R.attr.SunViewNightColor)
        nightColor = ContextCompat.getColor(context, R.color.sViewNightColor)
        // resolveColor(R.attr.SunViewDayColor)
        dayColor = ContextCompat.getColor(context, R.color.sViewDayColor)
        // resolveColor(R.attr.SunViewDaySecondColor)
        daySecondColor = ContextCompat.getColor(context, R.color.sViewDaySecondColor)
        // resolveColor(R.attr.SunViewSunColor)
        sunColor = ContextCompat.getColor(context, R.color.sViewSunColor)
        // resolveColor(R.attr.SunViewBeforeMiddayColor)
        sunBeforeMiddayColor = ContextCompat.getColor(context, R.color.sViewSunBeforeMiddayColor)
        // resolveColor(R.attr.SunViewAfterMiddayColor)
        sunAfterMiddayColor = ContextCompat.getColor(context, R.color.sViewSunAfterMiddayColor)
        // resolveColor(R.attr.SunViewEveningColor)
        sunEveningColor = ContextCompat.getColor(context, R.color.sViewSunEveningColor)

        sunPaint.color = sunColor
        sunRaisePaint.color = sunColor

        fontSize = 14.dp
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        width = w
        height = h - 18

        curvePath = Path()
        curvePath.moveTo(0f, height.toFloat())

        if (width != 0) {
            segmentByPixel = 2 * PI / width
        }

        (0..width).forEach {
            curvePath.lineTo(it.toFloat(), getY(it, segmentByPixel, (height * 0.9f).toInt()))
        }

        nightPath = Path(curvePath)
        nightPath.setLastPoint(width.toFloat(), height.toFloat())
        nightPath.lineTo(width.toFloat(), 0f)
        nightPath.lineTo(0f, 0f)
        nightPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()

        // draw fill of night
        paint.style = Paint.Style.FILL
        paint.color = nightColor
        canvas.clipRect(0f, height * 0.75f, width * current, height.toFloat())
        canvas.drawPath(nightPath, paint)

        canvas.restore()

        canvas.save()

        // draw fill of day
        canvas.clipRect(0, 0, width, height)
        canvas.clipRect(0f, 0f, width * current, height * 0.75f)
        dayPaint.shader = linearGradient
        canvas.drawPath(curvePath, dayPaint)

        canvas.restore()

        canvas.save()

        // draw time curve
        canvas.clipRect(0, 0, width, height)
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        paint.color = timelineColor
        canvas.drawPath(curvePath, paint)

        canvas.restore()

        // draw horizon line
        paint.color = horizonColor
        canvas.drawLine(0f, height * 0.75f, width.toFloat(), height * 0.75f, paint)

        // draw sunset and sunrise tag line indicator
        paint.color = taggingColor
        paint.strokeWidth = 2f
        canvas.drawLine(width * 0.17f, height * 0.3f, width * 0.17f, height * 0.7f, paint)
        canvas.drawLine(width * 0.83f, height * 0.3f, width * 0.83f, height * 0.7f, paint)
        canvas.drawLine(getWidth() / 2f, height * 0.7f, getWidth() / 2f, height * 0.8f, paint)

        // draw text
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = fontSize.toFloat()
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
        paint.color = sunriseTextColor
        canvas.drawText(sunriseString, width * 0.17f, height * .2f, paint)
        paint.color = middayTextColor
        canvas.drawText(middayString, width / 2f, height * .94f, paint)
        paint.color = sunsetTextColor
        canvas.drawText(sunsetString, width * 0.83f, height * .2f, paint)

        // draw remaining time
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
        paint.color = colorTextSecond
        canvas.drawText(dayLengthString, width * if (isRTL) 0.70f else 0.30f, height * .94f, paint)
        if (remainingString.isNotEmpty()) {
            canvas.drawText(
                    remainingString,
                    width * if (isRTL) 0.30f else 0.70f,
                    height * .94f,
                    paint
            )
        }

        // draw sun
        if (current in 0.17f..0.83f) {

            @ColorInt
            val color = argbEvaluator.evaluate(
                    current,
                    sunBeforeMiddayColor, sunAfterMiddayColor
            ) as Int

            sunPaint.color = color
            //mSunRaisePaint.setColor(color);
            //mPaint.setShadowLayer(1.0f, 1.0f, 2.0f, 0x33000000);
            canvas.drawCircle(
                    width * current,
                    getY((width * current).toInt(), segmentByPixel, (height * 0.9f).toInt()),
                    height * 0.09f,
                    sunPaint
            )
            //mPaint.clearShadowLayer();
            //canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * 0.9f)), (height * 0.09f) - 5, mSunRaisePaint);
        } else {
            drawMoon(canvas)
        }
    }

    private fun drawMoon(canvas: Canvas) {
        // This is brought from QiblaCompassView with some modifications
        val r = height * 0.08f
        val radius = 1f
        val px = width * current
        val py = getY((width * current).toInt(), segmentByPixel, (height * 0.9f).toInt())
        moonPaint.reset()
        moonPaint.flags = Paint.ANTI_ALIAS_FLAG
        moonPaint.color = Color.WHITE
        moonPaint.style = Paint.Style.FILL_AND_STROKE
        moonPaintB.reset()// moon Paint Black
        moonPaintB.flags = Paint.ANTI_ALIAS_FLAG
        moonPaintB.color = Color.BLACK
        moonPaintB.style = Paint.Style.FILL_AND_STROKE
        moonPaintO.reset()// moon Paint for Oval
        moonPaintO.flags = Paint.ANTI_ALIAS_FLAG
        moonPaintO.color = Color.WHITE
        moonPaintO.style = Paint.Style.FILL_AND_STROKE
        moonPaintD.reset()// moon Paint for Diameter
        // draw
        moonPaintD.color = Color.GRAY
        moonPaintD.style = Paint.Style.STROKE
        moonPaintD.flags = Paint.ANTI_ALIAS_FLAG
        canvas.rotate(180f, px, py)
        val eOffset = 0
        // elevation Offset 0 for 0 degree; r for 90 degree
        moonRect.set(px - r, py + eOffset - radius - r, px + r, py + eOffset - radius + r)
        canvas.drawArc(moonRect, 90f, 180f, false, moonPaint)
        canvas.drawArc(moonRect, 270f, 180f, false, moonPaintB)
        val arcWidth = ((moonPhase - 0.5) * (4 * r)).toInt()
        moonPaintO.color = if (arcWidth < 0) Color.BLACK else Color.WHITE
        moonOval.set(
                px - abs(arcWidth) / 2f, py + eOffset - radius - r,
                px + abs(arcWidth) / 2f, py + eOffset - radius + r
        )
        canvas.drawArc(moonOval, 0f, 360f, false, moonPaintO)
        canvas.drawArc(moonRect, 0f, 360f, false, moonPaintD)
        canvas.drawLine(px, py - radius, px, py + radius, moonPaintD)
        moonPaintD.pathEffect = null
    }

    private fun getY(x: Int, segment: Double, height: Int): Float {
        val cos = (cos(-PI + x * segment) + 1) / 2
        return height - height * cos.toFloat() + height * 0.1f
    }

    fun setSunriseSunsetMoonPhase(prayTimes: PrayTimes, moonPhase: Double) {
        this.prayTimes = prayTimes
        this.moonPhase = moonPhase
        postInvalidate()
    }

    fun startAnimate(immediate: Boolean = false) {
        val context = context ?: return
        val prayTimes = prayTimes ?: return

        isRTL = isRTL(context)
        sunriseString = context.getString(R.string.sunriseSunView)
        middayString = context.getString(R.string.middaySunView)
        sunsetString = context.getString(R.string.sunsetSunView)

        val sunset = prayTimes.sunsetClock.toInt().toFloat()
        val sunrise = prayTimes.sunriseClock.toInt().toFloat()
        var midnight = prayTimes.midnightClock.toInt().toFloat()

        if (midnight > HALF_DAY) midnight -= FULL_DAY
        val now = Clock(Calendar.getInstance(Locale.getDefault())).toInt().toFloat()

        var c = 0f
        if (now <= sunrise) {
            if (sunrise != 0f) {
                c = (now - midnight) / sunrise * 0.17f
            }
        } else if (now <= sunset) {
            if (sunset - sunrise != 0f) {
                c = (now - sunrise) / (sunset - sunrise) * 0.66f + 0.17f
            }
        } else {
            if (FULL_DAY + midnight - sunset != 0f) {
                c = (now - sunset) / (FULL_DAY + midnight - sunset) * 0.17f + 0.17f + 0.66f
            }
        }

        val dayLength = Clock.fromInt((sunset - sunrise).toInt())
        val remaining =
                Clock.fromInt(if (now > sunset || now < sunrise) 0 else (sunset - now).toInt())
        dayLengthString = context.getString(R.string.length_of_day).format(
                formatNumber(dayLength.hour), formatNumber(dayLength.minute)
        )
        remainingString = if (remaining.toInt() == 0) "" else context.getString(
                R.string.remaining_daylight
        ).format(formatNumber(remaining.hour), formatNumber(remaining.minute))

        argbEvaluator = ArgbEvaluator()

        linearGradient = LinearGradient(
                getWidth() * 0.17f, 0f, getWidth() * 0.5f, 0f,
                dayColor, daySecondColor, Shader.TileMode.MIRROR
        )

        if (immediate) {
            current = c
            postInvalidate()
        } else {
            ValueAnimator.ofFloat(0F, c).apply {
                duration = 1500L
                interpolator = DecelerateInterpolator()
                addUpdateListener(this@SunView)
                start()
            }
        }
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
