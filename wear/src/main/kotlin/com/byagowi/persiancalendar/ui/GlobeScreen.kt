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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
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
import androidx.compose.ui.util.fastAny
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.globeRuntimeShader
import kotlinx.coroutines.launch
import kotlin.math.PI

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
                val scale = this.drawContext.size.height / 2f / (180 * 16)
                drawRect(colorScheme.primaryContainer)
                scale(scaleX = scale, scaleY = scale * 2, pivot = Offset.Zero) {
                    drawPath(path, colorScheme.primary)
                }
            }
            val width = this.maxWidth
            val height = this.maxHeight
            val density = LocalDensity.current
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
                                    x.floatValue += panChange.x / with(density) { width.toPx() } * 4 / zoom
                                    y =
                                        (y + panChange.y / with(density) { height.toPx() } * -4 / zoom).coerceIn(
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
                    .fillMaxSize(.7f),
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview
@Composable
internal fun GlobeScreenPreview() = GlobeScreen()
