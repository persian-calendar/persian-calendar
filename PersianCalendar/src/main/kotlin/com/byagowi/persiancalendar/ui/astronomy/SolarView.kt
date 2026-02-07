package com.byagowi.persiancalendar.ui.astronomy

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.common.ZoomableCanvas
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.resolveAndroidCustomTypeface
import com.byagowi.persiancalendar.utils.symbol
import com.byagowi.persiancalendar.utils.titleStringId
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun SolarView(
    modifier: Modifier,
    isTropical: Boolean,
    scale: Animatable<Float, AnimationVector1D>,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    state: AstronomyState,
    mode: AstronomyMode,
    rotationalMinutesChange: (Int) -> Unit,
) {
    val surfaceColor by animateColor(MaterialTheme.colorScheme.surface)
    val contentColor = LocalContentColor.current
    val typeface = resolveAndroidCustomTypeface()

    val textPath = remember { Path() }
    val textPathRect = remember { RectF() }

    val heliocentricPlanetsTitles = AstronomyState.heliocentricPlanetsList.map {
        stringResource(it.titleStringId) + " " + it.symbol
    }
    val geocentricPlanetsTitles = geocentricPlanetsList.map {
        stringResource(it.titleStringId) + " " + it.symbol
    }

    val density = LocalDensity.current
    val trianglePath = remember(density) {
        Path().also {
            val dp = with(density) { 1.dp.toPx() }
            it.moveTo(0f, 6 * dp)
            it.lineTo(-5 * dp, .5f * dp)
            it.lineTo(5 * dp, .5f * dp)
            it.close()
        }
    }
    val arcRect = remember { RectF() }

    val monthsIndicator = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.Transparent.toArgb()
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = with(density) { 1.dp.toPx() }
    }
    val yearIndicator = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x78808080
        it.style = Paint.Style.FILL_AND_STROKE
        it.strokeWidth = with(density) { 2.dp.toPx() }
    }
    val moonIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x78808080
        it.style = Paint.Style.FILL
    }
    val sunIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFFEEBB22.toInt()
        it.style = Paint.Style.FILL
    }
    val zodiacBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x08808080
        it.style = Paint.Style.FILL
    }
    val zodiacForegroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x18808080
        it.style = Paint.Style.FILL
    }
    val circlesPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val zodiacSeparatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = with(density) { .5.dp.toPx() }
        it.style = Paint.Style.STROKE
        it.color = surfaceColor.toArgb()
    }

    val colorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.color = contentColor.toArgb()
    }

    val zodiacPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF808080.toInt()
        it.strokeWidth = with(density) { 1.dp.toPx() }
        it.textSize = with(density) { 10.dp.toPx() }
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
    }

    val zodiacSymbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x38808080
        it.strokeWidth = with(density) { 1.dp.toPx() }
        it.textSize = with(density) { 20.dp.toPx() }
        it.textAlign = Paint.Align.CENTER
        it.typeface =
            ResourcesCompat.getFont(LocalContext.current, R.font.notosanssymbolsregularzodiacsubset)
        if (isBoldFont) it.isFakeBoldText = true
    }
    val moonOrbitPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = with(density) { 1.dp.toPx() }
        it.color = 0x40808080
    }

    val zodiacRanges = run {
        val tropicalRanges = remember {
            FloatArray(24) { Zodiac.entries[it / 2].tropicalRange[it % 2].toFloat() }
        }
        val iauRanges = remember {
            FloatArray(24) { Zodiac.entries[it / 2].iauRange[it % 2].toFloat() }
        }
        val fraction by animateFloatAsState(
            targetValue = if (isTropical) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
        monthsIndicator.color =
            ColorUtils.setAlphaComponent(0x808080, (0x78 * fraction).roundToInt().coerceIn(0, 255))
        FloatArray(24) { lerp(iauRanges[it], tropicalRanges[it], fraction) }
    }
    val resources = LocalResources.current
    val solarDraw = remember(resources) { SolarDraw(resources) }
    val labels = Zodiac.entries.map { it.shortTitle(resources) }
    val symbols = Zodiac.entries.map { it.symbol }

    val coroutineScope = rememberCoroutineScope()
    val rotationVelocity = remember { Animatable(0f, Float.VectorConverter) }

    val isScaled by remember { derivedStateOf { scale.value != 1f } }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current
    ZoomableCanvas(
        scale = scale,
        offsetX = offsetX,
        offsetY = offsetY,
        disableHorizontalLimit = true,
        disableVerticalLimit = true,
        disablePan = !isScaled,
        modifier = modifier.pointerInput(mode, isScaled) {
            if (mode != AstronomyMode.EARTH || isScaled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                var previousAngle = atan2(down.position.y - centerY, down.position.x - centerX)

                // Determine rotation speed based on touch position (outer = sun orbit, inner = moon orbit)
                val touchDistance = hypot(down.position.x - centerX, down.position.y - centerY)
                val rotationSpeed = if (touchDistance > size.width / 4f) {
                    525949 // minutes in solar year
                } else {
                    39341 // 27.32 days in minutes, https://en.wikipedia.org/wiki/Orbit_of_the_Moon
                }

                val velocityTracker = VelocityTracker()
                var rotationDirection = 0

                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break

                    if (change.pressed) {
                        val currentAngle =
                            atan2(change.position.y - centerY, change.position.x - centerX)
                        val rawAngleChange = currentAngle - previousAngle

                        // Handle angle wrapping
                        val angleChange = when {
                            rawAngleChange > PI -> (2 * PI - rawAngleChange).toFloat()
                            rawAngleChange < -PI -> (2 * PI + rawAngleChange).toFloat()
                            else -> rawAngleChange
                        }

                        val minutesChange =
                            -(angleChange * rotationSpeed / PI.toFloat() / 2).toInt()
                        rotationDirection = minutesChange.sign

                        if (minutesChange != 0) {
                            rotationalMinutesChange(minutesChange)
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                        }

                        previousAngle = currentAngle
                    }
                } while (event.changes.any { it.pressed })

                // Handle fling animation
                val velocity = velocityTracker.calculateVelocity()
                val velocityMagnitude = hypot(velocity.x, velocity.y) * rotationSpeed / 2000

                if (velocityMagnitude > 0) coroutineScope.launch {
                    val startVelocity = rotationDirection * velocityMagnitude * 2
                    var lastValue = 0f
                    rotationVelocity.snapTo(0f)
                    rotationVelocity.animateDecay(
                        initialVelocity = startVelocity,
                        animationSpec = exponentialDecay(frictionMultiplier = 3f),
                    ) {
                        val minutesChange = (value - lastValue).toInt()
                        if (minutesChange != 0) {
                            rotationalMinutesChange(minutesChange)
                            lastValue = value
                        }
                    }
                }
            }
        },
    ) {
        val radius = size.minDimension / 2f
        val canvas = this.drawContext.canvas.nativeCanvas
        when (mode) {
            AstronomyMode.MOON -> {
                solarDraw.moon(
                    canvas, state.sun, state.moon, radius, radius, radius / 3, state.moonTilt,
                    moonAltitude = state.moonAltitude,
                )
                state.sunAltitude?.also { sunAltitude ->
                    val alpha = ((127 + sunAltitude.toInt() * 3).coerceIn(0, 255) / 1.5).toInt()
                    solarDraw.sun(canvas, radius, radius / 2, radius / 9, alpha = alpha)
                }
                val text = language.formatAuAsKm(state.moon.dist)
                val style = textStyle.copy(fontSize = 14.sp)
                textMeasurer.measure(text, style).let {
                    val topLeft =
                        Offset(radius - it.size.width / 2, radius * 1.7f - it.size.height / 2)
                    drawText(it, Color.Gray, topLeft)
                }
            }

            AstronomyMode.EARTH -> {
                val dp = 1.dp.toPx()
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
                repeat(12) { index ->
                    val start = zodiacRanges[index * 2]
                    val end = zodiacRanges[index * 2 + 1]
                    canvas.withRotation(-end + 90, radius, radius) {
                        if (index % 2 == 0) canvas.drawArc(
                            arcRect, -90f, end - start, true, zodiacForegroundPaint,
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
                        canvas.drawPath(trianglePath.asAndroidPath(), sunIndicatorPaint)
                    }
                }
                val moonDegree = state.moon.lon.toFloat()
                canvas.drawCircle(radius, radius, radius * .25f, moonOrbitPaint)
                canvas.withRotation(-moonDegree + 90, radius, radius) {
                    val moonDistance = state.moon.dist / 0.002569 // Lunar distance in AU
                    solarDraw.moon(
                        this, state.sun, state.moon, radius,
                        radius * moonDistance.toFloat() * .75f, cr / 1.9f,
                    )
                    canvas.withTranslation(x = radius, y = 0f) {
                        canvas.drawPath(trianglePath.asAndroidPath(), moonIndicatorPaint)
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
                            radius - rectSize,
                            radius - rectSize,
                            radius + rectSize,
                            radius + rectSize,
                        )
                        textPath.asAndroidPath().addArc(textPathRect, 0f, 180f)
                        canvas.drawTextOnPath(
                            geocentricPlanetsTitles[i],
                            textPath.asAndroidPath(), 0f, 0f, colorTextPaint,
                        )
                    }
                }
            }

            AstronomyMode.SUN -> {
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
                            radius - rectSize,
                            radius - rectSize,
                            radius + rectSize,
                            radius + rectSize,
                        )
                        textPath.asAndroidPath().addArc(textPathRect, 0f, 180f)
                        canvas.drawTextOnPath(
                            heliocentricPlanetsTitles[i],
                            textPath.asAndroidPath(), 0f, 0f, colorTextPaint,
                        )
                    }
                }
            }
        }
    }
}
