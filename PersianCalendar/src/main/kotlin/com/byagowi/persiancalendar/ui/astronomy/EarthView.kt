package com.byagowi.persiancalendar.ui.astronomy

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.content.res.ResourcesCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isBoldFont
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
import kotlin.math.sign

@Composable
fun EarthView(
    isTropical: Boolean,
    scale: Animatable<Float, AnimationVector1D>,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    state: AstronomyState,
    rotationalMinutesChange: (Int) -> Unit,
) {
    val surfaceColor by animateColor(MaterialTheme.colorScheme.surface)
    val contentColor = LocalContentColor.current
    val typeface = resolveAndroidCustomTypeface()
    val textPath = remember { Path() }
    val textPathRect = remember { RectF() }
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
    val zodiacBackgroundPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }.also {
        it.color = 0x08808080
        it.style = Paint.Style.FILL
    }
    val zodiacForegroundPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }.also {
        it.color = 0x18808080
        it.style = Paint.Style.FILL
    }
    val colorTextPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }.also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.color = contentColor.toArgb()
    }
    val zodiacPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }.also {
        it.color = 0xFF808080.toInt()
        it.strokeWidth = with(density) { 1.dp.toPx() }
        it.textSize = with(density) { 10.dp.toPx() }
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
    }
    val zodiacSymbolPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }.also {
        it.color = 0x38808080
        it.strokeWidth = with(density) { 1.dp.toPx() }
        it.textSize = with(density) { 20.dp.toPx() }
        it.textAlign = Paint.Align.CENTER
        val context = LocalContext.current
        it.typeface = remember(context) {
            ResourcesCompat.getFont(context, R.font.notosanssymbolsregularzodiacsubset)
        }
        if (isBoldFont) it.isFakeBoldText = true
    }
    val moonOrbitStroke = Stroke(with(density) { 1.dp.toPx() })
    val tropicalFraction by animateFloatAsState(
        targetValue = if (isTropical) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
    )
    val zodiacRanges = remember { FloatArray(24) }.also { ranges ->
        val tropicalRanges = remember {
            FloatArray(24) { Zodiac.entries[it / 2].tropicalRange[it % 2].toFloat() }
        }
        val iauRanges = remember {
            FloatArray(24) { Zodiac.entries[it / 2].iauRange[it % 2].toFloat() }
        }
        repeat(24) { ranges[it] = lerp(iauRanges[it], tropicalRanges[it], tropicalFraction) }
    }
    val resources = LocalResources.current
    val solarDraw = remember(resources) { SolarDraw(resources) }
    val labels = Zodiac.entries.map { it.shortTitle(resources) }
    val symbols = Zodiac.entries.map { it.symbol }
    val coroutineScope = rememberCoroutineScope()
    val rotationVelocity = remember { Animatable(0f) }
    val isScaled by remember { derivedStateOf { scale.value != 1f } }
    val modifier = Modifier.aspectRatio(1f)
    ZoomableCanvas(
        scale = scale,
        offsetX = offsetX,
        offsetY = offsetY,
        disableHorizontalLimit = true,
        disableVerticalLimit = true,
        disablePan = !isScaled,
        modifier = modifier.pointerInput(isScaled) {
            if (!isScaled) awaitEachGesture {
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
        val radius = this.center.x
        val canvas = this.drawContext.canvas.nativeCanvas
        val dp = 1.dp.toPx()
        (0..12).forEach {
            rotate(degrees = it * 30f) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(size.width - dp / 2, radius),
                    end = Offset(size.width - 6 * dp, radius),
                    strokeWidth = (if (it == 0) 2 else 1) * dp,
                    alpha = if (it == 0) .5f else (tropicalFraction / 2),
                )
            }
        }
        arcRect.set(0f, 0f, 2 * radius, 2 * radius)
        val circleInset = radius * .05f
        arcRect.inset(circleInset, circleInset)
        canvas.drawArc(arcRect, 0f, 360f, true, zodiacBackgroundPaint)
        tropicalFraction.let {}
        repeat(12) { index ->
            val start = zodiacRanges[index * 2]
            val end = zodiacRanges[index * 2 + 1]
            rotate(degrees = -end + 90) {
                if (index % 2 == 0) canvas.drawArc(
                    arcRect, -90f, end - start, true, zodiacForegroundPaint,
                )
                val start = Offset(radius, circleInset)
                val end = Offset(radius, radius)
                drawLine(surfaceColor, start, end)
            }
            rotate(degrees = -(start + end) / 2 + 90) {
//                val rectSize = radius * .88f
//                textPath.rewind()
//                textPathRect.set(
//                    radius - rectSize, radius - rectSize, radius + rectSize, radius + rectSize
//                )
//                textPath.addArc(textPathRect, 0f, 180f)
//                drawTextOnPath(labels[(index + 6) % 12], textPath, 0f, 0f, zodiacPaint)
                canvas.drawText(labels[index], radius, radius * .12f, zodiacPaint)
                canvas.drawText(symbols[index], radius, radius * .25f, zodiacSymbolPaint)
            }
        }
        val cr = radius / 8f
        solarDraw.earth(canvas, radius, radius, cr / 1.5f, state.sun)
        val sunDegree = state.sun.elon.toFloat()
        rotate(degrees = -sunDegree + 90) {
            solarDraw.sun(canvas, radius, radius / 2.5f, cr)
            translate(left = radius) { drawPath(trianglePath, Color(0xFFEEBB22)) }
        }
        val moonDegree = state.moon.lon.toFloat()
        drawCircle(Color(0x40808080), radius * .25f, style = moonOrbitStroke)
        rotate(degrees = -moonDegree + 90) {
            val moonDistance = state.moon.dist / 0.002569 // Lunar distance in AU
            solarDraw.moon(
                canvas, state.sun, state.moon, radius,
                radius * moonDistance.toFloat() * .75f, cr / 1.9f,
            )
            translate(left = radius) { drawPath(trianglePath, Color(0x78808080)) }
        }
        colorTextPaint.textSize = radius / 15
        colorTextPaint.alpha = 120
        state.geocentricPlanets.forEachIndexed { i, ecliptic ->
            rotate(degrees = -ecliptic.elon.toFloat() + 270) {
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
}
