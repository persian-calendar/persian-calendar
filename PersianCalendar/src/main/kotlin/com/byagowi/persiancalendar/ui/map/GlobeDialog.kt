package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private val defaultShader = """
precision mediump float;
uniform vec2 resolution;
uniform float time;
uniform sampler2D tex0;

const float PI = 3.1415926;

void main() {
  float R = min(resolution.x, resolution.y) / 3.0;
  float x = resolution.x / 2.0 - gl_FragCoord.x;
  float y = resolution.y / 2.0 - gl_FragCoord.y;
  float l = x * x + y * y;
  float r = sqrt(l);
  if (r > R) discard;
  float z = sqrt(R * R - l);
  vec2 longLat = vec2((-atan(x, z) / PI + 1.0 + time / 10.0) * 0.5, (asin(y / R) / PI + .5));
  gl_FragColor = texture2D(tex0, longLat);
}
""".trim()

fun showGlobeDialog(activity: FragmentActivity, image: Bitmap) {
    val glView = GLSurfaceView(activity)
    glView.setEGLContextClientVersion(2)
    val renderer = GLRenderer()
    glView.setRenderer(renderer)
    glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    Handler(Looper.getMainLooper()).postDelayed({
        glView.queueEvent { renderer.loadTexture(image) }
    }, 100)
    renderer.fragmentShader = defaultShader
    MaterialAlertDialogBuilder(activity)
        .setView(glView)
        .show()
}
