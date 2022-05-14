package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.byagowi.persiancalendar.ui.common.BaseSlider
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showGlobeDialog(activity: FragmentActivity, image: Bitmap) {
    val frame = FrameLayout(activity)
    frame.post {
        val glView = GLSurfaceView(activity)
        glView.setOnClickListener { glView.requestRender() }
        glView.setEGLContextClientVersion(2)
        val renderer = GLRenderer(onSurfaceCreated = { it.loadTexture(image) })
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        renderer.fragmentShader = globeFragmentShader
        frame.addView(glView)
        frame.addView(BaseSlider(activity).also {
            it.enableVerticalSlider = true
            it.onScrollListener = { dx: Float, dy: Float ->
                if (dx != 0f && renderer.overriddenTime == 0f)
                    renderer.overriddenTime = System.nanoTime() / 1e9f
                renderer.overriddenTime += dx / 200
                renderer.overriddenY += dy / 200
            }
        })
    }
    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(frame)
        .show()
    // Just close the dialog when activity is paused so we don't get ANR after app switch and etc.
    activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) dialog.cancel()
    })
}
