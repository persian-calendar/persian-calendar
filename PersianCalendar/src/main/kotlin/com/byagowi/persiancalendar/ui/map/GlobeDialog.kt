package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.byagowi.persiancalendar.ui.calendar.detectZoom
import kotlinx.coroutines.launch
import kotlin.math.PI

@Composable
fun GlobeDialog(bitmap: Bitmap, onDismissRequest: () -> Unit) {
    @OptIn(ExperimentalMaterial3Api::class) Dialog(onDismissRequest = onDismissRequest) {
        var renderer by remember { mutableStateOf<GLRenderer?>(null) }
        val startTime = remember { System.nanoTime() }
        fun onDelta(dx: Float, dy: Float) {
            renderer?.also { renderer ->
                if (dx != 0f && renderer.overriddenTime == 0f)
                    renderer.overriddenTime = (System.nanoTime() - startTime) / 1e9f
                renderer.overriddenTime += dx / renderer.overriddenZoom / 200
                renderer.overriddenY =
                    (renderer.overriddenY + dy / renderer.overriddenZoom / 200)
                        .coerceIn(-PI.toFloat() / 3, PI.toFloat() / 3)
            }
        }

        val density = LocalDensity.current
        val coroutineScope = rememberCoroutineScope()
        AndroidView(
            factory = { context ->
                val glView = GLSurfaceView(context)
                glView.setOnClickListener { glView.requestRender() }
                glView.setEGLContextClientVersion(2)
                val renderer = GLRenderer(onSurfaceCreated = { it.loadTexture(bitmap) })
                    .also { renderer = it }
                glView.setRenderer(renderer)
                glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                renderer.fragmentShader = globeFragmentShader
                glView
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
                    renderer?.also { renderer ->
                        renderer.overriddenZoom =
                            (renderer.overriddenZoom * factor).coerceIn(.25f, 6f)
                    }
                }
                .fillMaxSize(),
        )
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) onDismissRequest()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}
