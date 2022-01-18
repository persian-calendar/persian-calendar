package com.byagowi.persiancalendar.ui.astronomy

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.entities.Zodiac
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.material.math.MathUtils
import java.util.*
import kotlin.math.min

class SolarView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var currentTime = System.currentTimeMillis() - DAY_IN_MILLIS // Initial animation
    private var sunMoonPosition: SunMoonPosition? = null
    private var animator: ValueAnimator? = null

    fun setTime(time: GregorianCalendar, immediate: Boolean, update: (SunMoonPosition) -> Unit) {
        animator?.removeAllUpdateListeners()
        if (immediate) {
            currentTime = time.timeInMillis
            sunMoonPosition = time.calculateSunMoonPosition(null).also(update)
            postInvalidate()
            return
        }
        ValueAnimator.ofFloat(currentTime.toFloat(), time.timeInMillis.toFloat()).also {
            animator = it
            it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            it.interpolator = AccelerateDecelerateInterpolator()
            val date = GregorianCalendar()
            it.addUpdateListener { _ ->
                currentTime = ((it.animatedValue as? Float) ?: 0f).toLong()
                date.timeInMillis = currentTime
                sunMoonPosition = date.calculateSunMoonPosition(null).also(update)
                postInvalidate()
            }
        }.start()
    }

    var isTropicalDegree = false
        set(value) {
            ValueAnimator.ofFloat(if (value) 0f else 1f, if (field) 0f else 1f).also { animator ->
                animator.duration =
                    resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.addUpdateListener { _ ->
                    val fraction = ((animator.animatedValue as? Float) ?: 0f)
                    ranges.indices.forEach {
                        ranges[it][0] = MathUtils.lerp(
                            iauRanges[it][0], tropicalRanges[it][0], fraction
                        )
                        ranges[it][1] = MathUtils.lerp(
                            iauRanges[it][1], tropicalRanges[it][1], fraction
                        )
                    }
                    postInvalidate()
                }
            }.start()
            field = value
        }
    private val tropicalRanges = Zodiac.values().map { it.tropicalRange.map(Double::toFloat) }
    private val iauRanges = Zodiac.values().map { it.iauRange.map(Double::toFloat) }
    private val ranges = iauRanges.map { it.toFloatArray() }

    private val labels = Zodiac.values().map { it.format(context, false, short = true) }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        val sunMoonPosition = sunMoonPosition ?: return
        val radius = min(width, height) / 2f
        canvas.withScale(x = scaleFactor, y = scaleFactor, pivotX = radius, pivotY = radius) {
            arcRect.set(0f, 0f, 2 * radius, 2 * radius)
            val circleInset = radius * .05f
            arcRect.inset(circleInset, circleInset)
            canvas.drawArc(arcRect, 0f, 360f, true, zodiacBackgroundPaint)
            ranges.forEachIndexed { index, (start, end) ->
                canvas.withRotation(-end + 90f, radius, radius) {
                    if (index % 2 == 0) canvas.drawArc(
                        arcRect, -90f, end - start, true, zodiacForegroundPaint
                    )
                    drawLine(radius, circleInset, radius, radius, zodiacSeparatorPaint)
                }
                canvas.withRotation(-(start + end) / 2 + 90f, radius, radius) {
                    drawText(labels[index], radius, radius * .12f, zodiacPaint)
                }
            }
            val cr = radius / 8f
            solarDraw.earth(canvas, radius, radius, cr / 1.5f)
            val sunDegree = sunMoonPosition.sunEcliptic.λ.toFloat()
            canvas.withRotation(-sunDegree + 90f, radius, radius) {
                solarDraw.sun(this, radius, radius / 3.5f, cr)
                canvas.withTranslation(x = radius, y = 0f) {
                    canvas.drawPath(trianglePath, sunIndicatorPaint)
                }
            }
            val moonDegree = sunMoonPosition.moonEcliptic.λ.toFloat()
            canvas.drawCircle(radius, radius, radius * .3f, moonOrbitPaint)
            canvas.withRotation(-moonDegree + 90f, radius, radius) {
                val moonDistance = sunMoonPosition.moonEcliptic.Δ / SunMoonPosition.LUNAR_DISTANCE
                solarDraw.moon(
                    this, sunMoonPosition, radius,
                    radius * moonDistance.toFloat() * .7f, cr / 1.9f
                )
                canvas.withTranslation(x = radius, y = 0f) {
                    canvas.drawPath(trianglePath, moonIndicatorPaint)
                }
            }
        }
    }

    private val trianglePath = Path().also {
        it.moveTo(0f, 6.dp)
        it.lineTo((-5).dp, .5.dp)
        it.lineTo(5.dp, .5.dp)
        it.close()
    }
    private val arcRect = RectF()

    private val moonIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x78808080
        it.style = Paint.Style.FILL
    }
    private val sunIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFFEEBB22.toInt()
        it.style = Paint.Style.FILL
    }
    private val zodiacBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x08808080
        it.style = Paint.Style.FILL
    }
    private val zodiacForegroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x18808080
        it.style = Paint.Style.FILL
    }
    private val zodiacSeparatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(com.google.android.material.R.attr.colorSurface)
        it.strokeWidth = .5.dp
        it.style = Paint.Style.STROKE
    }

    private val zodiacPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF808080.toInt()
        it.strokeWidth = 1.dp
        it.textSize = 10.dp
        it.textAlign = Paint.Align.CENTER
    }
    private val moonOrbitPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1.dp
        it.color = 0x40808080
    }

    private var scaleFactor = 1f
    private val scaleGestureDetector = ScaleGestureDetector(
        context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                scaleFactor = (scaleFactor + (detector?.scaleFactor ?: 1f)).coerceIn(.9f, 1.1f)
                postInvalidate()
                return true
            }
        }
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private val solarDraw = SolarDraw(context)
}
