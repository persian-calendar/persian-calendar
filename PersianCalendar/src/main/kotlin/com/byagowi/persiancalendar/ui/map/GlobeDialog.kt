package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.byagowi.persiancalendar.ui.astronomy.SliderView
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
        frame.addView(SliderView(activity).also {
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dx != 0 && renderer.overriddenTime == 0f)
                        renderer.overriddenTime = System.nanoTime() / 1e9f
                    renderer.overriddenTime -= dx / 200f
                }
            })
            it.hiddenBars = true
        })
    }
    MaterialAlertDialogBuilder(activity)
        .setView(frame)
        .show()
}
