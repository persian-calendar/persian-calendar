package com.byagowi.persiancalendar.ui.astronomy

import android.graphics.Paint
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
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
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.resolveAndroidCustomTypeface
import com.byagowi.persiancalendar.utils.handleAngleWrapping
import com.byagowi.persiancalendar.utils.symbol
import com.byagowi.persiancalendar.utils.titleStringId
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sign
import kotlin.time.Duration.Companion.minutes

@Composable
fun EarthView(
    isTropical: Boolean,
    state: AstronomyState,
    isScaled: Boolean,
    timeInMillis: MutableLongState,
    modifier: Modifier = Modifier,
) {
    val surfaceColor by animateColor(MaterialTheme.colorScheme.surface)
    val contentColor = LocalContentColor.current
    val typeface = resolveAndroidCustomTypeface()
    val textPath = remember { Path() }
    val geocentricPlanetsTitles = geocentricPlanetsList.map {
        stringResource(it.titleStringId) + " " + it.symbol
    }
    val density = LocalDensity.current
    val trianglePath = remember(density) { Path() }
    val zodiacForegroundColor = Color(0x18808080)
    val colorTextPaint = remember(typeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.textAlign = Paint.Align.CENTER
            it.typeface = typeface
        }
    }
    colorTextPaint.color = contentColor.toArgb()
    val zodiacPaint = remember(typeface) {
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = 0xFF808080.toInt()
            it.strokeWidth = with(density) { 1.dp.toPx() }
            it.textSize = with(density) { 10.dp.toPx() }
            it.textAlign = Paint.Align.CENTER
            it.typeface = typeface
        }
    }
    val context = LocalContext.current
    val zodiacSymbolPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = 0x38808080
            it.strokeWidth = with(density) { 1.dp.toPx() }
            it.textSize = with(density) { 20.dp.toPx() }
            it.textAlign = Paint.Align.CENTER
            it.typeface =
                ResourcesCompat.getFont(context, R.font.notosanssymbolsregularzodiacsubset)
            if (isBoldFont) it.isFakeBoldText = true
        }
    }
    val moonOrbitStroke = remember { Stroke(with(density) { 1.dp.toPx() }) }
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
    val labels = remember(resources) { Zodiac.entries.map { it.shortTitle(resources) } }
    val symbols = remember { Zodiac.entries.map { it.symbol } }
    val coroutineScope = rememberCoroutineScope()
    val pointerModifier = Modifier.pointerInput(isScaled) {
        var lastPointerId: PointerId? = null
        val velocityTracker = VelocityTracker()
        if (!isScaled) awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            lastPointerId = down.id
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            var previousAngle = atan2(down.position.y - centerY, down.position.x - centerX)

            // Determine rotation speed based on touch position (outer = sun orbit, inner = moon orbit)
            val touchDistance = hypot(down.position.x - centerX, down.position.y - centerY)
            val isSunRotation = touchDistance > size.width / 4f
            val rotationSpeed = if (isSunRotation) {
                525949 // minutes in solar year
            } else {
                39341 // 27.32 days in minutes, https://en.wikipedia.org/wiki/Orbit_of_the_Moon
            }

            var rotationDirection = 0

            do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull() ?: break

                if (change.pressed) {
                    val currentAngle =
                        atan2(change.position.y - centerY, change.position.x - centerX)
                    val angleChange = handleAngleWrapping(currentAngle - previousAngle)
                    val minutesChange = -angleChange * rotationSpeed / PI.toFloat() / 2
                    rotationDirection = minutesChange.sign.toInt()

                    timeInMillis.longValue += (minutesChange * oneMinute).toLong()
                    velocityTracker.addPointerInputChange(change)

                    previousAngle = currentAngle
                }
            } while (event.changes.any { it.pressed })

            val velocity = velocityTracker.calculateVelocity()
            velocityTracker.resetTracking()
            val velocityMagnitude = hypot(velocity.x, velocity.y) * rotationSpeed

            if (velocityMagnitude > 0) coroutineScope.launch {
                animateDecay(
                    initialValue = 0f,
                    initialVelocity = rotationDirection * velocityMagnitude / if (isSunRotation) 1_500_000 else 600_000,
                    animationSpec = SplineBasedFloatDecayAnimationSpec(density),
                ) { _, velocity ->
                    if (velocity.isFinite() && down.id == lastPointerId) {
                        timeInMillis.longValue += (velocity * 1.5f * oneMinute).toLong()
                    }
                }
            }
        }
    }
    Canvas(modifier = pointerModifier.then(modifier)) {
        val radius = this.center.x
        val unit = radius / 140
        run {
            trianglePath.rewind()
            trianglePath.moveTo(0f, 6 * unit)
            trianglePath.lineTo(-5 * unit, .5f * unit)
            trianglePath.lineTo(5 * unit, .5f * unit)
            trianglePath.close()
        }

        repeat(12) {
            rotate(degrees = it * 30f) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(size.width - unit / 2, radius),
                    end = Offset(size.width - 6 * unit, radius),
                    strokeWidth = (if (it == 0) 2 else 1) * unit,
                    alpha = if (it == 0) .5f else (tropicalFraction / 2),
                )
            }
        }
        val circleInsetStart = radius * .05f
        val circleInsetEnd = radius * 1.95f
        drawCircle(Color(0x08808080), radius = circleInsetStart)
        tropicalFraction.let {}
        run {
            val rectSize = radius * .88f
            textPath.rewind()
            textPath.asAndroidPath().addArc(
                radius - rectSize, radius - rectSize, radius + rectSize, radius + rectSize,
                180f, 180f,
            )
        }
        val nativeCanvas = this.drawContext.canvas.nativeCanvas
        run {
            val arcTopLeft = Offset(circleInsetStart, circleInsetStart)
            val arcSize = Size(circleInsetEnd - circleInsetStart, circleInsetEnd - circleInsetStart)
            val lineStart = Offset(radius, circleInsetStart)
            val lineEnd = Offset(radius, radius)
            repeat(12) { index ->
                val start = zodiacRanges[index * 2]
                val end = zodiacRanges[index * 2 + 1]
                rotate(degrees = -end + 90) {
                    if (index % 2 == 0) drawArc(
                        topLeft = arcTopLeft,
                        size = arcSize,
                        startAngle = -90f,
                        sweepAngle = end - start,
                        useCenter = true,
                        color = zodiacForegroundColor,
                    )
                    drawLine(surfaceColor, lineStart, lineEnd, strokeWidth = unit)
                }
                rotate(degrees = -(start + end) / 2 + 90) {
                    nativeCanvas.drawTextOnPath(
                        labels[index], textPath.asAndroidPath(), 0f, 0f, zodiacPaint,
                    )
                    nativeCanvas.drawText(symbols[index], radius, radius * .25f, zodiacSymbolPaint)
                }
            }
        }
        val cr = radius / 8f
        solarDraw.earth(nativeCanvas, radius, radius, cr / 1.5f, state.sun)
        val sunDegree = state.sun.elon.toFloat()
        rotate(degrees = -sunDegree + 90) {
            solarDraw.sun(nativeCanvas, radius, radius / 2.5f, cr)
            translate(left = radius) { drawPath(trianglePath, Color(0xFFEEBB22)) }
        }
        val moonDegree = state.moon.lon.toFloat()
        drawCircle(Color(0x40808080), radius * .25f, style = moonOrbitStroke)
        rotate(degrees = -moonDegree + 90) {
            val moonDistance = state.moon.dist / 0.002569 // Lunar distance in AU
            solarDraw.moon(
                nativeCanvas, state.sun, state.moon, radius,
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
                textPath.asAndroidPath().addArc(
                    radius - rectSize, radius - rectSize,
                    radius + rectSize, radius + rectSize,
                    0f, 180f,
                )
                nativeCanvas.drawTextOnPath(
                    geocentricPlanetsTitles[i],
                    textPath.asAndroidPath(), 0f, 0f, colorTextPaint,
                )
            }
        }
    }
}

private val oneMinute = 1.minutes.inWholeMilliseconds
