package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.byagowi.persiancalendar.ui.calendar.detectZoom
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
    var isInZooming by remember { mutableStateOf(false) }
    val zoom = remember { Animatable(.25f) }
    LaunchedEffect(Unit) {
        zoom.animateTo(
            targetValue = 1f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        )
    }
    renderer.overriddenZoom = zoom.value
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
            .draggable2D(
                enabled = !isInZooming,
                state = rememberDraggable2DState { (dx, dy) -> onDelta(dx, dy) },
                onDragStopped = { (dx, _) ->
                    coroutineScope.launch {
                        animateDecay(
                            initialValue = 0f,
                            initialVelocity = dx,
                            animationSpec = SplineBasedFloatDecayAnimationSpec(density),
                        ) { _, dx -> if (dx.isFinite()) onDelta(dx / 20, 0f) }
                    }
                },
            )
            .detectZoom(
                onRelease = { isInZooming = false },
                // Otherwise it intercepts pointer inputs of draggable2d
                onlyMultitouch = true,
            ) { factor ->
                isInZooming = true
                coroutineScope.launch { zoom.snapTo((zoom.value * factor).coerceIn(.25f, 6f)) }
            }
            .fillMaxSize(),
    )

    PredictiveBackHandler { progress ->
        runCatching {
            progress.collect { renderer.overriddenZoom = (1 - it.progress).coerceAtLeast(.05f) }
        }.onFailure(logException).onSuccess { onDismissRequest() }
    }

    run {
        val view = LocalView.current
        val window = LocalActivity.current?.window ?: return@run
        val controller = remember(view, window) {
            WindowInsetsControllerCompat(window, view)
        }
        DisposableEffect(controller) {
            if (controller.isAppearanceLightStatusBars) {
                controller.isAppearanceLightStatusBars = false
                onDispose { controller.isAppearanceLightStatusBars = true }
            } else onDispose {}
        }
        DisposableEffect(controller) {
            if (controller.isAppearanceLightNavigationBars) {
                controller.isAppearanceLightNavigationBars = false
                onDispose { controller.isAppearanceLightNavigationBars = true }
            } else onDispose {}
        }
    }
}
