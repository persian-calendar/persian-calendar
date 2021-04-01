package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.byagowi.persiancalendar.LANG_EN_IR
import com.byagowi.persiancalendar.LANG_EN_US
import com.byagowi.persiancalendar.LANG_JA
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.*
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

    private val fullDay = Clock(24, 0).toInt().toFloat()
    private val halfDay = Clock(12, 0).toInt().toFloat()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.typeface = getAppFont(context) }
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL }
    private var sunRaisePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 7f
        it.pathEffect = DashPathEffect(floatArrayOf(3f, 7f), 0f) /* Sun rays effect */
    }
    private val dayPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL_AND_STROKE }

    private var horizonColor: Int = 0
    private var timelineColor: Int = 0
    private var taggingColor: Int = 0
    private var nightColor = ContextCompat.getColor(context, R.color.sViewNightColor)
    private var dayColor = ContextCompat.getColor(context, R.color.sViewDayColor)
    private var daySecondColor = ContextCompat.getColor(context, R.color.sViewDaySecondColor)
    private var sunColor = ContextCompat.getColor(context, R.color.sViewSunColor)
    private var sunBeforeMiddayColor = ContextCompat.getColor(context, R.color.sViewSunBeforeMiddayColor)
    private var sunAfterMiddayColor = ContextCompat.getColor(context, R.color.sViewSunAfterMiddayColor)
    private var sunriseTextColor: Int = 0
    private var middayTextColor: Int = 0
    private var sunsetTextColor: Int = 0
    private var colorTextNormal: Int = 0
    private var colorTextSecond: Int = 0

    internal var width: Int = 0
    internal var height: Int = 0
    private lateinit var curvePath: Path
    private lateinit var nightPath: Path
    private var current = 0f
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
    private var isShaderInitiationNeeded = true
    private var segmentByPixel = .0
    private var argbEvaluator = ArgbEvaluator()
    private var prayTimes: PrayTimes? = null

    //    private Horizontal moonPosition;
    private var moonPhase = 1.0
    private var fontSize: Int = 0

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
        // resolveColor(R.attr.SunViewDayColor)
        // resolveColor(R.attr.SunViewDaySecondColor)
        // resolveColor(R.attr.SunViewSunColor)
        // resolveColor(R.attr.SunViewBeforeMiddayColor)
        // resolveColor(R.attr.SunViewAfterMiddayColor)
        // resolveColor(R.attr.SunViewEveningColor)

        sunPaint.color = sunColor
        sunRaisePaint.color = sunColor

        fontSize = when (language) {
            LANG_EN_IR, LANG_EN_US, LANG_JA -> {
                12.dp
            }
            else -> 14.dp
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        isShaderInitiationNeeded = true

        width = w
        height = h - 18

        curvePath = Path()
        curvePath.moveTo(0f, height.toFloat())

        when {
            width != 0 -> {
                segmentByPixel = 2 * PI / width
            }
        }

        (0..width).forEach {
            curvePath.lineTo(it.toFloat(), getY(it, segmentByPixel, (height * .9f).toInt()))
        }

        nightPath = Path(curvePath).also {
            it.setLastPoint(width.toFloat(), height.toFloat())
            it.lineTo(width.toFloat(), 0f)
            it.lineTo(0f, 0f)
            it.close()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val isRTL = isRTL

        when {
            isShaderInitiationNeeded -> {
                isShaderInitiationNeeded = false
                handler.postDelayed({
                    LinearGradient(
                            width * .17f,
                            0f,
                            width / 2f,
                            0f,
                            dayColor,
                            daySecondColor,
                            Shader.TileMode.MIRROR
                    ).also { dayPaint.shader = it }
                    postInvalidate()
                }, 80)
            }
        }

        canvas.also {
            it.save()
            when {
                isRTL -> it.scale(-1f, 1f, width / 2f, height / 2f)
            }
            it.save()
        }

        // draw fill of night
        paint.also {
            it.style = Paint.Style.FILL
            it.color = nightColor
        }
        canvas.also {
            it.clipRect(0f, height * .75f, width * current, height.toFloat())
            it.drawPath(nightPath, paint)
            it.restore()
            it.save()
        }

        // draw fill of day
        canvas.also {
            it.clipRect(0, 0, width, height)
            it.clipRect(0f, 0f, width * current, height * .75f)
            it.drawPath(curvePath, dayPaint)
            it.restore()
            it.save()
        }

        // draw time curve
        canvas.clipRect(0, 0, width, height)
        paint.also {
            it.strokeWidth = 3f
            it.style = Paint.Style.STROKE
            it.color = timelineColor
        }
        canvas.also {
            it.drawPath(curvePath, paint)
            it.restore()
        }

        // draw horizon line
        paint.color = horizonColor
        canvas.drawLine(0f, height * .75f, width.toFloat(), height * .75f, paint)

        // draw sunset and sunrise tag line indicator
        paint.also {
            it.color = taggingColor
            it.strokeWidth = 2f
        }
        canvas.also {
            it.drawLine(width * .17f, height * .3f, width * .17f, height * .7f, paint)
            it.drawLine(width * .83f, height * .3f, width * .83f, height * .7f, paint)
            it.drawLine(width / 2f, height * .7f, width / 2f, height * .8f, paint)
        }

        // draw sun
        when (current) {
            in .17f..0.83f -> {

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
                        getY((width * current).toInt(), segmentByPixel, (height * .9f).toInt()),
                        height * .09f,
                        sunPaint
                )
                //mPaint.clearShadowLayer();
                //canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * .9f)), (height * .09f) - 5, mSunRaisePaint);
            }
            else -> {
                drawMoon(canvas)
            }
        }

        canvas.restore()

        // draw text
        paint.also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = fontSize.toFloat()
            it.strokeWidth = 0f
            it.style = Paint.Style.FILL
            it.color = sunriseTextColor
        }
        canvas.drawText(
                sunriseString, width * when {
            isRTL -> .83f
            else -> .17f
        }, height * .2f, paint
        )
        paint.color = middayTextColor
        canvas.drawText(middayString, width / 2f, height * .94f, paint)
        paint.color = sunsetTextColor
        canvas.drawText(
                sunsetString, width * when {
            isRTL -> .17f
            else -> .83f
        }, height * .2f, paint
        )

        // draw remaining time
        paint.also {
            it.textAlign = Paint.Align.CENTER
            it.strokeWidth = 0f
            it.style = Paint.Style.FILL
            it.color = colorTextSecond
        }
        canvas.drawText(
                dayLengthString, width * when {
            isRTL -> .70f
            else -> .30f
        }, height * .94f, paint
        )
        when {
            remainingString.isNotEmpty() -> {
                canvas.drawText(
                        remainingString,
                        width * when {
                            isRTL -> .30f
                            else -> .70f
                        },
                        height * .94f,
                        paint
                )
            }
        }

    }

    private fun drawMoon(canvas: Canvas) {
        // This is brought from QiblaCompassView with some modifications
        val r = height * .08f
        val radius = 1f
        val px = width * current
        val py = getY((width * current).toInt(), segmentByPixel, (height * .9f).toInt())
        moonPaint.also {
            it.reset()
            it.flags = Paint.ANTI_ALIAS_FLAG
            it.color = Color.WHITE
            it.style = Paint.Style.FILL_AND_STROKE
        }
        moonPaintB.also {
            it.reset()// moon Paint Black
            it.flags = Paint.ANTI_ALIAS_FLAG
            it.color = Color.BLACK
            it.style = Paint.Style.FILL_AND_STROKE
        }
        moonPaintO.also {
            it.reset()// moon Paint for Oval
            it.flags = Paint.ANTI_ALIAS_FLAG
            it.color = Color.WHITE
            it.style = Paint.Style.FILL_AND_STROKE
        }
        moonPaintD.also {
            it.reset()// moon Paint for Diameter
            it.flags = Paint.ANTI_ALIAS_FLAG
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        canvas.rotate(180f, px, py)
        val eOffset = 0
        val arcWidth = ((moonPhase - .5) * (4 * r)).toInt()
        // elevation Offset 0 for 0 degree; r for 90 degree
        moonRect.set(px - r, py + eOffset - radius - r, px + r, py + eOffset - radius + r)
        canvas.also {
            it.drawArc(moonRect, 90f, 180f, false, moonPaint)
            it.drawArc(moonRect, 270f, 180f, false, moonPaintB)
        }
        moonPaintO.color = when {
            arcWidth < 0 -> Color.BLACK
            else -> Color.WHITE
        }
        moonOval.set(
                px - abs(arcWidth) / 2f, py + eOffset - radius - r,
                px + abs(arcWidth) / 2f, py + eOffset - radius + r
        )
        canvas.also {
            it.drawArc(moonOval, 0f, 360f, false, moonPaintO)
            it.drawArc(moonRect, 0f, 360f, false, moonPaintD)
            it.drawLine(px, py - radius, px, py + radius, moonPaintD)
        }
        moonPaintD.pathEffect = null
    }

    private fun getY(x: Int, segment: Double, height: Int): Float =
            height - height * ((cos(-PI + x * segment) + 1f) / 2f).toFloat() + height * .1f

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

        when {
            midnight > halfDay -> midnight -= fullDay
        }
        val now = Clock(Calendar.getInstance(Locale.getDefault())).toInt().toFloat()

        var c = 0f
        when {
            now <= sunrise -> {
                when {
                    sunrise != 0f -> {
                        c = (now - midnight) / sunrise * .17f
                    }
                }
            }
            now <= sunset -> {
                when {
                    sunset - sunrise != 0f -> {
                        c = (now - sunrise) / (sunset - sunrise) * .66f + .17f
                    }
                }
            }
            else -> {
                when {
                    fullDay + midnight - sunset != 0f -> {
                        c = (now - sunset) / (fullDay + midnight - sunset) * .17f + .17f + .66f
                    }
                }
            }
        }

        val dayLength = Clock.fromInt((sunset - sunrise).toInt())
        val remaining =
                Clock.fromInt(
                        when {
                            now > sunset || now < sunrise -> 0
                            else -> (sunset - now).toInt()
                        }
                )
        dayLengthString = context.getString(R.string.length_of_day).format(
                formatNumber(dayLength.hour), formatNumber(dayLength.minute)
        )
        remainingString = when {
            remaining.toInt() == 0 -> ""
            else -> context.getString(
                    R.string.remaining_daylight
            ).format(formatNumber(remaining.hour), formatNumber(remaining.minute))
        }

        argbEvaluator = ArgbEvaluator()

        when {
            immediate -> {
                current = c
                postInvalidate()
            }
            else -> {
                ValueAnimator.ofFloat(0F, c).apply {
                    duration = 1500L
                    interpolator = DecelerateInterpolator()
                    addUpdateListener(this@SunView)
                    start()
                }
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
