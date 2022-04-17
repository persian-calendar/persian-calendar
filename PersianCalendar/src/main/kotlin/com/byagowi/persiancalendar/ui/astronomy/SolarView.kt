package com.byagowi.persiancalendar.ui.astronomy

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.DAY_IN_MILLIS
import com.byagowi.persiancalendar.utils.sunlitSideMoonTiltAngle
import com.byagowi.persiancalendar.utils.toObserver
import com.google.android.material.math.MathUtils
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Ecliptic
import io.github.cosinekitty.astronomy.Spherical
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.helioVector
import io.github.cosinekitty.astronomy.sunPosition
import java.util.*
import kotlin.math.expm1
import kotlin.math.ln1p
import kotlin.math.min

class SolarView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var currentTime = System.currentTimeMillis() - DAY_IN_MILLIS // Initial animation
    private var animator: ValueAnimator? = null

    private class State(currentTime: Long) {
        private val time = Time.fromMillisecondsSince1970(currentTime)
        val sun = sunPosition(time)
        val moon = eclipticGeoMoon(time)
        val moonTilt by lazy(LazyThreadSafetyMode.NONE) {
            coordinates?.let { coordinates ->
                sunlitSideMoonTiltAngle(time, coordinates.toObserver()).toFloat()
            }
        }
        val planets by lazy(LazyThreadSafetyMode.NONE) {
            listOf(
                Body.Mercury, Body.Venus, Body.Earth, Body.Mars, Body.Jupiter,
                Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
            ).map { body ->
                body.name.substring(0, 3) to equatorialToEcliptic(helioVector(body, time))
            }
        }
    }

    private var state = State(currentTime)

    var mode: AstronomyViewModel.Mode = AstronomyViewModel.Mode.Earth
        set(value) {
            field = value
            invalidate()
        }

    fun setTime(
        time: GregorianCalendar,
        immediate: Boolean,
        update: (Ecliptic, Spherical) -> Unit
    ) {
        animator?.removeAllUpdateListeners()
        if (immediate) {
            currentTime = time.time.time
            state = State(currentTime)
            update(state.sun, state.moon)
            invalidate()
            return
        }
        ValueAnimator.ofFloat(currentTime.toFloat(), time.timeInMillis.toFloat()).also {
            animator = it
            it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            it.interpolator = AccelerateDecelerateInterpolator()
            it.addUpdateListener { _ ->
                currentTime = ((it.animatedValue as? Float) ?: 0f).toLong()
                state = State(currentTime)
                invalidate()
            }
        }.start()
    }

    var isTropicalDegree = false
        set(value) {
            if (value == field) return
            ValueAnimator.ofFloat(if (value) 0f else 1f, if (value) 1f else 0f).also { animator ->
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
                    invalidate()
                }
            }.start()
            field = value
        }
    private val tropicalRanges = Zodiac.values().map { it.tropicalRange.map(Double::toFloat) }
    private val iauRanges = Zodiac.values().map { it.iauRange.map(Double::toFloat) }
    private val ranges = iauRanges.map { it.toFloatArray() }

    private val labels = Zodiac.values().map { it.format(context, false, short = true) }

    override fun onDraw(canvas: Canvas) {
        when (mode) {
            AstronomyViewModel.Mode.Moon -> drawMoonOnlyView(canvas)
            AstronomyViewModel.Mode.Earth -> drawEarthCentricView(canvas)
            AstronomyViewModel.Mode.Sun -> drawSolarSystemPlanetsView(canvas)
        }
    }

    private val colorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = 5.dp
        it.color = context.resolveColor(R.attr.colorTextNormal)
    }
    private val indicatorLength = 5.dp / 2

    private fun drawSolarSystemPlanetsView(canvas: Canvas) {
        val radius = min(width, height) / 2f
        canvas.drawCircle(radius, radius, radius / 40, sunIndicatorPaint)
        state.planets.forEach { (label, ecliptic) ->
            canvas.withRotation(-ecliptic.elon.toFloat() + 90, radius, radius) {
                canvas.drawText(
                    label, radius, radius + ln1p(ecliptic.vec.length()).toFloat() * 80,
                    colorTextPaint
                )
            }
        }
        (3..5).forEach { // indicator to show it is in logarithmic scale
            val x = radius + expm1(it.toFloat())
            canvas.drawLine(
                x, radius - indicatorLength, x, radius + indicatorLength,
                colorTextPaint
            )
        }
    }

    private fun drawMoonOnlyView(canvas: Canvas) {
        val radius = min(width, height) / 2f
        solarDraw.moon(
            canvas, state.sun, state.moon, radius, radius, radius / 3, state.moonTilt
        )
    }

    private fun drawEarthCentricView(canvas: Canvas) {
        val radius = min(width, height) / 2f
        arcRect.set(0f, 0f, 2 * radius, 2 * radius)
        val circleInset = radius * .05f
        arcRect.inset(circleInset, circleInset)
        canvas.drawArc(arcRect, 0f, 360f, true, zodiacBackgroundPaint)
        ranges.forEachIndexed { index, (start, end) ->
            canvas.withRotation(-end + 90, radius, radius) {
                if (index % 2 == 0) canvas.drawArc(
                    arcRect, -90f, end - start, true, zodiacForegroundPaint
                )
                drawLine(radius, circleInset, radius, radius, zodiacSeparatorPaint)
            }
            canvas.withRotation(-(start + end) / 2 + 90, radius, radius) {
                drawText(labels[index], radius, radius * .12f, zodiacPaint)
            }
        }
        val cr = radius / 8f
        solarDraw.earth(canvas, radius, radius, cr / 1.5f, state.sun)
        val sunDegree = state.sun.elon.toFloat()
        canvas.withRotation(-sunDegree + 90, radius, radius) {
            solarDraw.sun(this, radius, radius / 3.5f, cr)
            canvas.withTranslation(x = radius, y = 0f) {
                canvas.drawPath(trianglePath, sunIndicatorPaint)
            }
        }
        val moonDegree = state.moon.lon.toFloat()
        canvas.drawCircle(radius, radius, radius * .3f, moonOrbitPaint)
        canvas.withRotation(-moonDegree + 90, radius, radius) {
            val moonDistance = state.moon.dist / 0.002569 // Lunar distance in AU
            solarDraw.moon(
                this, state.sun, state.moon, radius,
                radius * moonDistance.toFloat() * .7f, cr / 1.9f
            )
            canvas.withTranslation(x = radius, y = 0f) {
                canvas.drawPath(trianglePath, moonIndicatorPaint)
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

    private val solarDraw = SolarDraw(context)
}
