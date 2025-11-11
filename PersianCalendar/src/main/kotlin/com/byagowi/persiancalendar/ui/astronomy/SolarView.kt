package com.byagowi.persiancalendar.ui.astronomy

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.compose.ui.util.lerp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.common.ZoomableView
import com.byagowi.persiancalendar.ui.utils.createFlingDetector
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.symbol
import com.byagowi.persiancalendar.utils.titleStringId
import java.util.GregorianCalendar
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

class SolarView(context: Context, attrs: AttributeSet? = null) : ZoomableView(context, attrs) {
    private var state = AstronomyState(GregorianCalendar())

    var mode: AstronomyMode = AstronomyMode.entries[0]
        set(value) {
            field = value
            invalidate()
        }

    fun setTime(astronomyState: AstronomyState) {
        this.state = astronomyState
        invalidate()
    }

    private val tropicalAnimator = ValueAnimator.ofFloat(0f, 1f).also { animator ->
        animator.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { _ ->
            val fraction = animator.animatedFraction
            monthsIndicator.color =
                ColorUtils.setAlphaComponent(0x808080, (0x78 * fraction).roundToInt())
            ranges.indices.forEach {
                ranges[it][0] = lerp(iauRanges[it][0], tropicalRanges[it][0], fraction)
                ranges[it][1] = lerp(iauRanges[it][1], tropicalRanges[it][1], fraction)
            }
            invalidate()
        }
    }

    var isTropicalDegree = false
        set(value) {
            if (value == field) return
            if (value) tropicalAnimator.start() else tropicalAnimator.reverse()
            field = value
        }
    private val tropicalRanges = Zodiac.entries.map { it.tropicalRange.map(Double::toFloat) }
    private val iauRanges = Zodiac.entries.map { it.iauRange.map(Double::toFloat) }
    private val ranges = iauRanges.map { it.toFloatArray() }

    private val labels = Zodiac.entries.map { it.shortTitle(resources) }
    private val symbols = Zodiac.entries.map { it.symbol }

    init {
        onDraw = { canvas, matrix ->
            canvas.withMatrix(matrix) {
                when (mode) {
                    AstronomyMode.MOON -> drawMoonOnlyView(this)
                    AstronomyMode.EARTH -> drawGeoCentricView(this)
                    AstronomyMode.SUN -> drawSolarSystemPlanetsView(this)
                }
            }
            if (mode == AstronomyMode.MOON) {
                val radius = min(width, height) / 2f
                canvas.drawText(
                    language.value.formatAuAsKm(state.moon.dist),
                    radius, radius * 1.7f, moonTextPaint
                )
            }
        }
    }

    var rotationalMinutesChange = { _: Int -> }
    private var previousAngle = 0f
    private var rotationSpeed = 0
    private val flingAnimation = FlingAnimation(FloatValueHolder())
        .addUpdateListener { _, _, velocity ->
            rotationalMinutesChange(velocity.toInt())
            invalidate()
        }
    private var rotationDirection = 0

    private val flingDetector = createFlingDetector(context) { velocityX, velocityY ->
        flingAnimation.setStartVelocity(rotationDirection * 2 * hypot(velocityX, velocityY))
        true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        super.dispatchTouchEvent(event)
        if (mode != AstronomyMode.EARTH || currentScale != 1f) return true
        val r = width / 2
        flingDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                flingAnimation.cancel()
                previousAngle = atan2(event.y - r, event.x - r)
                rotationSpeed = if (hypot(event.x - r, event.y - r) > r / 2)
                    525949 // minutes in solar year
                else 39341 // 27.32 days in minutes, https://en.wikipedia.org/wiki/Orbit_of_the_Moon
            }

            MotionEvent.ACTION_MOVE -> {
                val currentAngle = atan2(event.y - r, event.x - r)
                val rawAngleChange = currentAngle - previousAngle
                val angleChange =
                    if (rawAngleChange > PI) 2 * PI.toFloat() - rawAngleChange
                    else if (rawAngleChange < -PI) 2 * PI.toFloat() + rawAngleChange
                    else rawAngleChange
                val minutesChange = -(angleChange * rotationSpeed / PI.toFloat() / 2).toInt()
                rotationDirection = minutesChange.sign
                rotationalMinutesChange(minutesChange)
                previousAngle = currentAngle
            }

            MotionEvent.ACTION_UP -> {
                flingAnimation.start()
                previousAngle = 0f
            }
        }
        return true
    }

    private val textPath = Path()
    private val textPathRect = RectF()
    private fun drawSolarSystemPlanetsView(canvas: Canvas) {
        val radius = min(width, height) / 2f
        colorTextPaint.textSize = radius / 11
        colorTextPaint.alpha = 255
        circlesPaint.strokeWidth = radius / 9
        circlesPaint.style = Paint.Style.FILL_AND_STROKE
        (1..8).forEach {
            circlesPaint.color = ColorUtils.setAlphaComponent(0x808080, (9 - it) * 0x10)
            canvas.drawCircle(radius, radius, radius / 9 * it, circlesPaint)
            circlesPaint.style = Paint.Style.STROKE
        }
        canvas.drawCircle(radius, radius, radius / 35, sunIndicatorPaint)
        state.heliocentricPlanets.forEachIndexed { i, ecliptic ->
            canvas.withRotation(-ecliptic.elon.toFloat() + 90, radius, radius) {
                textPath.rewind()
                val rectSize = radius / 9 * (1 + i) * .95f
                textPathRect.set(
                    radius - rectSize, radius - rectSize, radius + rectSize, radius + rectSize
                )
                textPath.addArc(textPathRect, 0f, 180f)
                canvas.drawTextOnPath(
                    heliocentricPlanetsTitles[i], textPath, 0f, 0f, colorTextPaint
                )
            }
        }
    }

    private fun drawMoonOnlyView(canvas: Canvas) {
        val radius = min(width, height) / 2f
        solarDraw.moon(
            canvas, state.sun, state.moon, radius, radius, radius / 3, state.moonTilt,
            moonAltitude = state.moonAltitude
        )
        state.sunAltitude?.also { sunAltitude ->
            val alpha = ((127 + sunAltitude.toInt() * 3).coerceIn(0, 255) / 1.5).toInt()
            solarDraw.sun(canvas, radius, radius / 2, radius / 9, alpha = alpha)
        }
    }

    private val dp = resources.dp
    private val moonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.textAlign = Paint.Align.CENTER
        it.textSize = 14 * dp
        it.color = Color.GRAY
    }

    private val heliocentricPlanetsTitles = AstronomyState.heliocentricPlanetsList.map {
        resources.getString(it.titleStringId) + " " + it.symbol
    }
    private val geocentricPlanetsTitles = geocentricPlanetsList.map {
        resources.getString(it.titleStringId) + " " + it.symbol
    }

    private fun drawGeoCentricView(canvas: Canvas) {
        val radius = min(width, height) / 2f
        (0..12).forEach {
            canvas.withRotation(it * 30f, pivotX = radius, pivotY = radius) {
                val indicator = if (it == 0) yearIndicator else monthsIndicator
                canvas.drawLine(width - dp / 2, radius, width - 6 * dp, radius, indicator)
            }
        }
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
//                val rectSize = radius * .88f
//                textPath.rewind()
//                textPathRect.set(
//                    radius - rectSize, radius - rectSize, radius + rectSize, radius + rectSize
//                )
//                textPath.addArc(textPathRect, 0f, 180f)
//                drawTextOnPath(labels[(index + 6) % 12], textPath, 0f, 0f, zodiacPaint)
                drawText(labels[index], radius, radius * .12f, zodiacPaint)
                drawText(symbols[index], radius, radius * .25f, zodiacSymbolPaint)
            }
        }
        val cr = radius / 8f
        solarDraw.earth(canvas, radius, radius, cr / 1.5f, state.sun)
        val sunDegree = state.sun.elon.toFloat()
        canvas.withRotation(-sunDegree + 90, radius, radius) {
            solarDraw.sun(this, radius, radius / 2.5f, cr)
            canvas.withTranslation(x = radius, y = 0f) {
                canvas.drawPath(trianglePath, sunIndicatorPaint)
            }
        }
        val moonDegree = state.moon.lon.toFloat()
        canvas.drawCircle(radius, radius, radius * .25f, moonOrbitPaint)
        canvas.withRotation(-moonDegree + 90, radius, radius) {
            val moonDistance = state.moon.dist / 0.002569 // Lunar distance in AU
            solarDraw.moon(
                this, state.sun, state.moon, radius,
                radius * moonDistance.toFloat() * .75f, cr / 1.9f
            )
            canvas.withTranslation(x = radius, y = 0f) {
                canvas.drawPath(trianglePath, moonIndicatorPaint)
            }
        }
        colorTextPaint.textSize = radius / 15
        colorTextPaint.alpha = 120
        state.geocentricPlanets.forEachIndexed { i, ecliptic ->
            canvas.withRotation(-ecliptic.elon.toFloat() + 270, radius, radius) {
                val r = when (i) {
                    0 -> 2.5f
                    1 -> 3.25f
                    2 -> 5.25f
                    3 -> 6f
                    4 -> 6.75f
                    else -> 0f
                }
                val rectSize = radius / 9 * (1 + r) * .95f
                textPath.rewind()
                textPathRect.set(
                    radius - rectSize, radius - rectSize, radius + rectSize, radius + rectSize
                )
                textPath.addArc(textPathRect, 0f, 180f)
                canvas.drawTextOnPath(geocentricPlanetsTitles[i], textPath, 0f, 0f, colorTextPaint)
            }
        }
    }

    private val trianglePath = Path().also {
        it.moveTo(0f, 6 * dp)
        it.lineTo(-5 * dp, .5f * dp)
        it.lineTo(5 * dp, .5f * dp)
        it.close()
    }
    private val arcRect = RectF()

    private val monthsIndicator = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.TRANSPARENT
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = 1f * dp
    }
    private val yearIndicator = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x78808080
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = 2f * dp
    }
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
    private val circlesPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val zodiacSeparatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = .5f * dp
        it.style = Paint.Style.STROKE
    }

    fun setSurfaceColor(@ColorInt color: Int) {
        zodiacSeparatorPaint.color = color
    }

    private val colorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
    }

    fun setContentColor(@ColorInt color: Int) {
        colorTextPaint.color = color
    }

    private val zodiacPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF808080.toInt()
        it.strokeWidth = 1 * dp
        it.textSize = 10 * dp
        it.textAlign = Paint.Align.CENTER
    }

    fun setFont(typeface: Typeface?) {
        colorTextPaint.typeface = typeface
        zodiacPaint.typeface = typeface
        moonTextPaint.typeface = typeface
    }

    private val zodiacSymbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x38808080
        it.strokeWidth = 1 * dp
        it.textSize = 20 * dp
        it.textAlign = Paint.Align.CENTER
        it.typeface = ResourcesCompat.getFont(context, R.font.notosanssymbolsregularzodiacsubset)
        if (isBoldFont.value) it.isFakeBoldText = true
    }
    private val moonOrbitPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1 * dp
        it.color = 0x40808080
    }

    private val solarDraw = SolarDraw(resources)
}
