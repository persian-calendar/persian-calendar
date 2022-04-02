package com.byagowi.persiancalendar.ui.about

import android.opengl.GLSurfaceView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.databinding.ShaderSandboxBinding
import com.byagowi.persiancalendar.ui.map.GLRenderer
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private val defaultShader = """
precision mediump float;
uniform float time;
uniform vec2 resolution;
void main() {
    // https://twitter.com/notargs/status/1250468645030858753
    vec3 d = .5 - vec3(gl_FragCoord.xy, 1) / resolution.y, p, o;
    for (int i = 0; i < 32; ++i) {
        o = p;
        o.z -= time * 9.;
        float a = o.z * .1;
        o.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        p += (.1 - length(cos(o.xy) + sin(o.yz))) * d;
    }
    gl_FragColor = vec4((sin(p) + vec3(2, 5, 9)) / length(p) * vec3(1), 1);
}
""".trim()

fun showShaderSandboxDialog(activity: FragmentActivity) {
    val binding = ShaderSandboxBinding.inflate(activity.layoutInflater)
    binding.glView.setEGLContextClientVersion(2)
    val renderer = GLRenderer {
        activity.runOnUiThread { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
    }
    binding.glView.setRenderer(renderer)
    binding.glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    binding.inputText.doAfterTextChanged {
        renderer.fragmentShader = binding.inputText.text?.toString() ?: ""
        binding.glView.queueEvent { renderer.compileProgram(); binding.glView.requestRender() }
    }
    binding.inputText.setText(defaultShader)
    MaterialAlertDialogBuilder(activity)
        .setView(binding.root)
        .show()
}
