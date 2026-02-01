package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.byagowi.persiancalendar.ui.calendar.detectZoom
import kotlinx.coroutines.launch
import kotlin.math.PI

@Composable
fun GlobeView(bitmap: Bitmap) {
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
    AndroidView(
        factory = { context ->
            val glView = GLSurfaceView(context)
            glView.setOnClickListener { glView.requestRender() }
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
            .detectZoom(onlyMultitouch = true) { factor ->
                renderer.overriddenZoom = (renderer.overriddenZoom * factor).coerceIn(.25f, 6f)
            }
            .fillMaxSize(),
    )
}
