package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import androidx.core.view.WindowInsetsControllerCompat
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.logException
import kotlinx.coroutines.launch
import kotlin.math.PI

@Composable
fun GlobeView(bitmap: Bitmap, onDismissRequest: () -> Unit) {
    val renderer by remember {
        val renderer = GLRenderer(onSurfaceCreated = { it.loadTexture(bitmap) })
        renderer.fragmentShader = globeFragmentShader
        mutableStateOf(renderer)
    }
    val startTime = remember { System.nanoTime() }
    fun onDelta(dx: Float, dy: Float) {
        if (dx != 0f && renderer.overriddenTime == 0f)
            renderer.overriddenTime = (System.nanoTime() - startTime) / 1e9f
        renderer.overriddenTime += dx / renderer.overriddenZoom / 200
        renderer.overriddenY =
            (renderer.overriddenY + dy / renderer.overriddenZoom / 200)
                .coerceIn(-PI.toFloat() / 3, PI.toFloat() / 3)
    }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    LaunchedEffect(Unit) {
        if (zoom == 1f) animate(
            initialValue = .25f,
            targetValue = 1f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        ) { value, _ -> zoom = value }
    }
    renderer.overriddenZoom = zoom
    AndroidView(
        factory = { context ->
            val glView = GLSurfaceView(context)
            glView.setEGLContextClientVersion(2)
            glView.setRenderer(renderer)
            glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            glView
        },
        update = {
            bitmap.let {}
            if (renderer.isSurfaceCreated) it.queueEvent { renderer.loadTexture(bitmap) }
        },
        modifier = Modifier
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
                            zoom = (zoom * zoomChange).coerceIn(.25f, 6f)
                        } else {
                            event.changes.forEach {
                                tracker.addPointerInputChange(it)
                                it.consume()
                            }
                            val panChange = event.calculatePan()
                            onDelta(panChange.x, panChange.y)
                        }
                    } while (event.changes.fastAny { it.pressed })
                    val velocity = tracker.calculateVelocity().x
                    tracker.resetTracking()
                    coroutineScope.launch {
                        animateDecay(
                            initialValue = 0f,
                            initialVelocity = velocity,
                            animationSpec = SplineBasedFloatDecayAnimationSpec(density),
                        ) { _, dx ->
                            if (down.id == lastPointerId && dx.isFinite()) onDelta(dx / 20, 0f)
                        }
                    }
                }
            }
            .fillMaxSize(),
    )

    PredictiveBackHandler { progress ->
        runCatching {
            val original = renderer.overriddenZoom
            progress.collect {
                renderer.overriddenZoom = (original - it.progress).coerceAtLeast(.05f)
            }
        }.onFailure(logException).onSuccess { onDismissRequest() }
    }

    LocalActivity.current?.window?.also { window ->
        val view = LocalView.current
        val colorScheme = MaterialTheme.colorScheme
        DisposableEffect(key1 = Unit) {
            val controller = WindowInsetsControllerCompat(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            onDispose {
                controller.isAppearanceLightStatusBars = colorScheme.background.isLight
                controller.isAppearanceLightNavigationBars = colorScheme.surface.isLight
            }
        }
    }
}

@Preview
@Composable
internal fun GlobeViewPreview() {
    val globeTextureSize = 2048
    val bitmap = createBitmap(globeTextureSize, globeTextureSize)
    val resources = LocalResources.current
    val mapDraw = remember(resources) { MapDraw(resources) }
    bitmap.applyCanvas {
        val scale = globeTextureSize / 2f / mapDraw.mapHeight
        withScale(x = scale, y = scale * 2) {
            mapDraw.draw(
                canvas = this,
                scale = scale,
                displayLocation = false,
                coordinates = null,
                directPathDestination = null,
                displayGrid = true,
            )
        }
    }
    GlobeView(bitmap) {}
}
