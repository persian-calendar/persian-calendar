package com.byagowi.persiancalendar.ui.about

import android.opengl.GLSurfaceView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.databinding.ShaderSandboxBinding
import com.byagowi.persiancalendar.generated.sandboxFragmentShader
import com.byagowi.persiancalendar.ui.map.GLRenderer
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showShaderSandboxDialog(activity: FragmentActivity) {
    val binding = ShaderSandboxBinding.inflate(activity.layoutInflater)
    binding.glView.setEGLContextClientVersion(2)
    val renderer = GLRenderer(onError = {
        activity.runOnUiThread { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
    })
    binding.glView.setRenderer(renderer)
    binding.glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    binding.inputText.doAfterTextChanged {
        renderer.fragmentShader = binding.inputText.text?.toString() ?: ""
        binding.glView.queueEvent { renderer.compileProgram(); binding.glView.requestRender() }
    }
    binding.inputText.setText(sandboxFragmentShader)
    MaterialAlertDialogBuilder(activity)
        .setView(binding.root)
        .show()
}
