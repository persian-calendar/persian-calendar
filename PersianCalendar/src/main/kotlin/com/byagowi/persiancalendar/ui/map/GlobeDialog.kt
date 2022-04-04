package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.generated.globeFragmentShader
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    }
    MaterialAlertDialogBuilder(activity)
        .setView(frame)
        .show()
}
