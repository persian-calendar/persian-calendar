package com.byagowi.persiancalendar.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.util.fastAny
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.wear.compose.foundation.requestFocusOnHierarchyActive
import androidx.wear.compose.foundation.rotary.RotaryScrollableBehavior
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.globeRuntimeShader
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.rotationEqdHor
import io.github.cosinekitty.astronomy.rotationEqjEqd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.time.Duration.Companion.seconds

@Composable
fun GlobeScreen(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val runtimeShader = remember { RuntimeShader(globeRuntimeShader) }
    val resources = LocalResources.current
    val path = remember {
        addPathNodes(
            resources.openRawResource(R.raw.worldmap).readBytes().decodeToString(),
        ).toPath()
    }

    var zoom by remember { mutableFloatStateOf(1f) }
    val x = remember { mutableFloatStateOf(Float.NaN) }
    var y by remember { mutableFloatStateOf(0f) }
    val rotation by if (!x.floatValue.isNaN()) x else rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360_000, easing = LinearEasing),
        ),
    )
    val colorScheme = MaterialTheme.colorScheme
    ScreenScaffold(modifier) {
        BoxWithConstraints(Modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
            var timeInMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(key1 = Unit) {
                while (true) {
                    delay(30.seconds)
                    timeInMillis = System.currentTimeMillis()
                }
            }
            val dayNightMask = remember(timeInMillis) { dayNightMask(timeInMillis) }
            val width = this.maxWidth
            val height = this.maxHeight
            val density = LocalDensity.current
            Canvas(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        runtimeShader.setFloatUniform(
                            "u_resolution", this.size.width, this.size.height,
                        )
                        runtimeShader.setFloatUniform("u_zoom", zoom)
                        runtimeShader.setFloatUniform("u_x", rotation)
                        runtimeShader.setFloatUniform("u_y", y)
                        this.renderEffect = RenderEffect.createRuntimeShaderEffect(
                            runtimeShader, "content",
                        ).asComposeRenderEffect()
                    },
            ) {
                val size = this.drawContext.size
                val scale = size.height / 2f / (180 * 16)
                drawRect(colorScheme.primaryContainer)
                scale(scaleX = scale, scaleY = scale * 2, pivot = Offset.Zero) {
                    drawPath(path, colorScheme.primary)
                }
                if (dayNightMask != null) drawImage(dayNightMask, dstSize = size.toIntSize())
            }
            val coroutineScope = rememberCoroutineScope()
            Box(
                Modifier
                    .pointerInput(Unit) {
                        var lastPointerId: PointerId? = null
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            lastPointerId = down.id
                            val tracker = VelocityTracker()
                            do {
                                val event = awaitPointerEvent()
                                val zoomChange = event.calculateZoom()
                                if (zoomChange != 1f) {
                                    zoom = (zoom * zoomChange).coerceIn(.5f, 3f)
                                } else {
                                    event.changes.forEach {
                                        tracker.addPointerInputChange(it)
                                        it.consume()
                                    }
                                    val panChange = event.calculatePan()
                                    if (x.floatValue.isNaN()) x.floatValue = rotation
                                    val widthPx = with(density) { width.toPx() }
                                    x.floatValue += panChange.x / widthPx * 4 / zoom
                                    val heightPx = with(density) { height.toPx() }
                                    y = (y + panChange.y / heightPx * -4 / zoom).coerceIn(
                                        -PI.toFloat() / 3,
                                        PI.toFloat() / 3,
                                    )
                                }
                            } while (event.changes.fastAny { it.pressed })
                            val velocity = tracker.calculateVelocity().x
                            tracker.resetTracking()
                            coroutineScope.launch {
                                animateDecay(
                                    initialValue = 0f,
                                    initialVelocity = velocity,
                                    animationSpec = SplineBasedFloatDecayAnimationSpec(density),
                                ) { _, velocity ->
                                    if (down.id == lastPointerId && velocity.isFinite()) {
                                        x.floatValue += velocity / 10 / with(density) { width.toPx() } / zoom
                                    }
                                }
                            }
                        }
                    }
                    .requestFocusOnHierarchyActive()
                    .rotaryScrollable(
                        behavior = remember {
                            object : RotaryScrollableBehavior {
                                override suspend fun CoroutineScope.performScroll(
                                    timestampMillis: Long,
                                    delta: Float,
                                    inputDeviceId: Int,
                                    orientation: Orientation,
                                ) {
                                    zoom = (zoom + delta / 1000).coerceIn(.5f, 3f)
                                }
                            }
                        },
                        focusRequester = remember { FocusRequester() },
                    )
                    .fillMaxSize(.7f),
            )
        }
    }
}

private fun dayNightMask(timeInMillis: Long): ImageBitmap? {
    val time = Time.fromMillisecondsSince1970(timeInMillis)
    val geoSunEqj = geoVector(Body.Sun, time, Aberration.Corrected)
    val rot = rotationEqjEqd(time)
    val geoSunEqd = rot.rotate(geoSunEqj)
    // https://github.com/cosinekitty/astronomy/blob/edcf9248/demo/c/worldmap.cpp
    val scaleDegree = 3
    val result = runCatching {
        createBitmap(360 / scaleDegree, 180 / scaleDegree)
    }.getOrNull() ?: return null
    (0..<360 step scaleDegree).forEach { x ->
        (0..<180 step scaleDegree).forEach { y ->
            val latitude = 180 / 2.0 - y
            val longitude = x - 360 / 2.0
            val observer = Observer(latitude, longitude, .0)
            val observerVec = observer.toVector(time, EquatorEpoch.OfDate)
            val observerRot = rotationEqdHor(time, observer)
            val sunAltitude = observerRot.rotate(geoSunEqd - observerVec).let { it.z / it.length() }
            if (sunAltitude < 0) {
                val value = (-sunAltitude * 90 * 7).toInt().coerceAtMost(120)
                // This moves the value to alpha channel so ARGB 0x0000007F becomes 0x7F000000
                result[x / scaleDegree, y / scaleDegree] = value shl 24
            }
        }
    }
    return result.asImageBitmap()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview
@Composable
internal fun GlobeScreenPreview() = GlobeScreen()
