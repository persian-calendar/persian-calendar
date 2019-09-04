package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.praytimes.Clock
import com.byagowi.persiancalendar.praytimes.PrayTimes
import com.byagowi.persiancalendar.utils.TypefaceUtils
import com.byagowi.persiancalendar.utils.Utils
import java.util.*

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

class SunView : View, ValueAnimator.AnimatorUpdateListener {

    private val FULL_DAY = Clock(24, 0).toInt().toFloat()
    private val HALF_DAY = Clock(12, 0).toInt().toFloat()
    lateinit var mPaint: Paint
    lateinit var mSunPaint: Paint
    lateinit var mSunRaisePaint: Paint
    lateinit var mDayPaint: Paint
    @ColorInt
    internal var horizonColor: Int = 0
    @ColorInt
    internal var timelineColor: Int = 0
    @ColorInt
    internal var taggingColor: Int = 0
    @ColorInt
    internal var nightColor: Int = 0
    @ColorInt
    internal var dayColor: Int = 0
    @ColorInt
    internal var daySecondColor: Int = 0
    @ColorInt
    internal var sunColor: Int = 0
    @ColorInt
    internal var sunBeforeMiddayColor: Int = 0
    @ColorInt
    internal var sunAfterMiddayColor: Int = 0
    @ColorInt
    internal var sunEveningColor: Int = 0
    @ColorInt
    internal var sunriseTextColor: Int = 0
    @ColorInt
    internal var middayTextColor: Int = 0
    @ColorInt
    internal var sunsetTextColor: Int = 0
    @ColorInt
    internal var colorTextNormal: Int = 0
    @ColorInt
    internal var colorTextSecond: Int = 0
    internal var width: Int = 0
    internal var height: Int = 0
    lateinit var curvePath: Path
    lateinit var nightPath: Path
    internal var current = 0f
    internal var linearGradient = LinearGradient(0f, 0f, 1f, 0f, 0, 0, Shader.TileMode.MIRROR)
    internal var moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var moonPaintB = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var moonPaintO = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var moonPaintD = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var moonRect = RectF()
    internal var moonOval = RectF()
    internal var dayLengthString = ""
    internal var remainingString = ""
    internal var sunriseString = ""
    internal var middayString = ""
    internal var sunsetString = ""
    internal var isRTL = false
    private var segmentByPixel: Double = 0.toDouble()
    private var argbEvaluator = ArgbEvaluator()
    private var prayTimes: PrayTimes? = null
    //    private Horizontal moonPosition;
    private var moonPhase = 1.0
    private var fontSize: Int = 0

    constructor(context: Context) : super(context) {

        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {

        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SunView)
            val typedValue = TypedValue()
            val theme = context.theme

            try {
                theme.resolveAttribute(R.attr.SunViewHorizonColor, typedValue, true)
                val HorizonColor = ContextCompat.getColor(context, typedValue.resourceId)
                horizonColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, HorizonColor)
                theme.resolveAttribute(R.attr.SunViewTimelineColor, typedValue, true)
                val TimelineColor = ContextCompat.getColor(context, typedValue.resourceId)
                timelineColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, TimelineColor)
                theme.resolveAttribute(R.attr.SunViewTaglineColor, typedValue, true)
                val taglineColor = ContextCompat.getColor(context, typedValue.resourceId)
                taggingColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, taglineColor)
                nightColor = typedArray.getColor(R.styleable.SunView_SunViewNightColor, ContextCompat.getColor(context, R.color.sViewNightColor))
                dayColor = typedArray.getColor(R.styleable.SunView_SunViewDayColor, ContextCompat.getColor(context, R.color.sViewDayColor))
                daySecondColor = typedArray.getColor(R.styleable.SunView_SunViewDaySecondColor, ContextCompat.getColor(context, R.color.sViewDaySecondColor))
                sunColor = typedArray.getColor(R.styleable.SunView_SunViewSunColor, ContextCompat.getColor(context, R.color.sViewSunColor))
                sunBeforeMiddayColor = typedArray.getColor(R.styleable.SunView_SunViewBeforeMiddayColor, ContextCompat.getColor(context, R.color.sViewSunBeforeMiddayColor))
                sunAfterMiddayColor = typedArray.getColor(R.styleable.SunView_SunViewAfterMiddayColor, ContextCompat.getColor(context, R.color.sViewSunAfterMiddayColor))
                sunEveningColor = typedArray.getColor(R.styleable.SunView_SunViewEveningColor, ContextCompat.getColor(context, R.color.sViewSunEveningColor))
                theme.resolveAttribute(R.attr.SunViewSunriseTextColor, typedValue, true)
                val SunriseTextColor = ContextCompat.getColor(context, typedValue.resourceId)
                sunriseTextColor = typedArray.getColor(R.styleable.SunView_SunViewSunriseTextColor, SunriseTextColor)
                theme.resolveAttribute(R.attr.SunViewMiddayTextColor, typedValue, true)
                val MiddayTextColor = ContextCompat.getColor(context, typedValue.resourceId)
                middayTextColor = typedArray.getColor(R.styleable.SunView_SunViewMiddayTextColor, MiddayTextColor)
                theme.resolveAttribute(R.attr.SunViewSunsetTextColor, typedValue, true)
                val SunsetTextColor = ContextCompat.getColor(context, typedValue.resourceId)
                sunsetTextColor = typedArray.getColor(R.styleable.SunView_SunViewSunsetTextColor, SunsetTextColor)

                theme.resolveAttribute(R.attr.colorTextNormal, typedValue, true)
                colorTextNormal = ContextCompat.getColor(context, typedValue.resourceId)
                theme.resolveAttribute(R.attr.colorTextSecond, typedValue, true)
                colorTextSecond = ContextCompat.getColor(context, typedValue.resourceId)

                fontSize = dpToPx(14)
            } finally {
                typedArray.recycle()
            }
        }

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.typeface = TypefaceUtils.getAppFont(context)

        mSunPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mSunPaint.color = sunColor
        mSunPaint.style = Paint.Style.FILL

        mSunRaisePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mSunRaisePaint.color = sunColor
        mSunRaisePaint.style = Paint.Style.STROKE
        mSunRaisePaint.strokeWidth = 7f
        val sunRaysEffects = DashPathEffect(floatArrayOf(3f, 7f), 0f)
        mSunRaisePaint.pathEffect = sunRaysEffects

        mDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mDayPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        width = w
        height = h - 18

        curvePath = Path()
        curvePath.moveTo(0f, height.toFloat())

        if (width != 0) {
            segmentByPixel = 2 * Math.PI / width
        }

        for (x in 0..width) {
            curvePath.lineTo(x.toFloat(), getY(x, segmentByPixel, (height * 0.9f).toInt()))
        }

        nightPath = Path(curvePath)
        nightPath.setLastPoint(width.toFloat(), height.toFloat())
        nightPath.lineTo(width.toFloat(), 0f)
        nightPath.lineTo(0f, 0f)
        nightPath.close()
    }

    // https://stackoverflow.com/a/34763668
    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()

        // draw fill of night
        mPaint.style = Paint.Style.FILL
        mPaint.color = nightColor
        canvas.clipRect(0f, height * 0.75f, width * current, height.toFloat())
        canvas.drawPath(nightPath, mPaint)

        canvas.restore()

        canvas.save()

        // draw fill of day
        canvas.clipRect(0, 0, width, height)
        canvas.clipRect(0f, 0f, width * current, height * 0.75f)
        mDayPaint.shader = linearGradient
        canvas.drawPath(curvePath, mDayPaint)

        canvas.restore()

        canvas.save()

        // draw time curve
        canvas.clipRect(0, 0, width, height)
        mPaint.strokeWidth = 3f
        mPaint.style = Paint.Style.STROKE
        mPaint.color = timelineColor
        canvas.drawPath(curvePath, mPaint)

        canvas.restore()

        // draw horizon line
        mPaint.color = horizonColor
        canvas.drawLine(0f, height * 0.75f, width.toFloat(), height * 0.75f, mPaint)

        // draw sunset and sunrise tag line indicator
        mPaint.color = taggingColor
        mPaint.strokeWidth = 2f
        canvas.drawLine(width * 0.17f, height * 0.3f, width * 0.17f, height * 0.7f, mPaint)
        canvas.drawLine(width * 0.83f, height * 0.3f, width * 0.83f, height * 0.7f, mPaint)
        canvas.drawLine(getWidth() / 2f, height * 0.7f, getWidth() / 2f, height * 0.8f, mPaint)

        // draw text
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.textSize = fontSize.toFloat()
        mPaint.strokeWidth = 0f
        mPaint.style = Paint.Style.FILL
        mPaint.color = sunriseTextColor
        canvas.drawText(sunriseString, width * 0.17f, height * .2f, mPaint)
        mPaint.color = middayTextColor
        canvas.drawText(middayString, width / 2f, height * .94f, mPaint)
        mPaint.color = sunsetTextColor
        canvas.drawText(sunsetString, width * 0.83f, height * .2f, mPaint)

        // draw remaining time
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.strokeWidth = 0f
        mPaint.style = Paint.Style.FILL
        mPaint.color = colorTextSecond
        canvas.drawText(dayLengthString, width * if (isRTL) 0.70f else 0.30f, height * .94f, mPaint)
        if (!TextUtils.isEmpty(remainingString)) {
            canvas.drawText(remainingString, width * if (isRTL) 0.30f else 0.70f, height * .94f, mPaint)
        }

        // draw sun
        if (current >= 0.17f && current <= 0.83f) {

            @ColorInt
            val color = argbEvaluator.evaluate(current,
                    sunBeforeMiddayColor, sunAfterMiddayColor) as Int

            mSunPaint.color = color
            //mSunRaisePaint.setColor(color);
            //mPaint.setShadowLayer(1.0f, 1.0f, 2.0f, 0x33000000);
            canvas.drawCircle(width * current, getY((width * current).toInt(), segmentByPixel, (height * 0.9f).toInt()), height * 0.09f, mSunPaint)
            //mPaint.clearShadowLayer();
            //canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * 0.9f)), (height * 0.09f) - 5, mSunRaisePaint);
        } else {
            drawMoon(canvas)
        }
    }

    fun drawMoon(canvas: Canvas) {
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
        moonOval.set(px - Math.abs(arcWidth) / 2f, py + eOffset - radius - r,
                px + Math.abs(arcWidth) / 2f, py + eOffset - radius + r)
        canvas.drawArc(moonOval, 0f, 360f, false, moonPaintO)
        canvas.drawArc(moonRect, 0f, 360f, false, moonPaintD)
        canvas.drawLine(px, py - radius, px, py + radius, moonPaintD)
        moonPaintD.pathEffect = null
    }

    private fun getY(x: Int, segment: Double, height: Int): Float {
        val cos = (Math.cos(-Math.PI + x * segment) + 1) / 2
        return height - height * cos.toFloat() + height * 0.1f
    }

    fun setSunriseSunsetMoonPhase(prayTimes: PrayTimes, moonPhase: Double) {
        this.prayTimes = prayTimes
        this.moonPhase = moonPhase
        postInvalidate()
    }

    fun startAnimate(immediate: Boolean) {
        val context = context
        if (prayTimes == null || context == null)
            return

        isRTL = Utils.isRTL(context)
        sunriseString = context.getString(R.string.sunriseSunView)
        middayString = context.getString(R.string.middaySunView)
        sunsetString = context.getString(R.string.sunsetSunView)

        val sunset = prayTimes!!.sunsetClock.toInt().toFloat()
        val sunrise = prayTimes!!.sunriseClock.toInt().toFloat()
        var midnight = prayTimes!!.midnightClock.toInt().toFloat()

        if (midnight > HALF_DAY) midnight = midnight - FULL_DAY
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
        val remaining = Clock.fromInt(if (now > sunset || now < sunrise) 0 else (sunset - now).toInt())
        dayLengthString = String.format(context.getString(R.string.length_of_day),
                Utils.formatNumber(dayLength.hour),
                Utils.formatNumber(dayLength.minute))
        if (remaining.toInt() == 0) {
            remainingString = ""
        } else {
            remainingString = String.format(context.getString(R.string.remaining_daylight),
                    Utils.formatNumber(remaining.hour),
                    Utils.formatNumber(remaining.minute))
        }

        argbEvaluator = ArgbEvaluator()

        linearGradient = LinearGradient(getWidth() * 0.17f, 0f, getWidth() * 0.5f, 0f,
                dayColor, daySecondColor, Shader.TileMode.MIRROR)

        if (immediate) {
            current = c
            postInvalidate()
        } else {
            val animator = ValueAnimator.ofFloat(0F, c)
            animator.duration = 1500L
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener(this)
            animator.start()
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
