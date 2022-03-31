package com.byagowi.persiancalendar.ui.map

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.BuildConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

fun showGLDemoDialog(activity: FragmentActivity) {
    val glView = GLSurfaceView(activity)
    glView.setEGLContextClientVersion(2)
    glView.setRenderer(
        Renderer(
            """
precision mediump float;
// https://twitter.com/notargs/status/1250468645030858753
uniform float time;
uniform vec2 resolution;
void main()
{
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
"""
        )
    )
    glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    MaterialAlertDialogBuilder(activity)
        .setView(glView)
        .show()
}

private class Renderer(private val fragmentShaderCode: String) : GLSurfaceView.Renderer {
    private val vertexShaderCode =
        "attribute vec4 position; void main() { gl_Position = position; }"

    private var program: Int = 0
    private var resolutionHandle: Int = 0
    private var timeHandle: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                val message = GLES20.glGetProgramInfoLog(it)
                GLES20.glDeleteProgram(it)
                if (BuildConfig.DEVELOPMENT) error(message)
            }
            resolutionHandle = GLES20.glGetUniformLocation(it, "resolution")
            timeHandle = GLES20.glGetUniformLocation(it, "time")
        }
    }

    private var width = 1f
    private var height = 1f
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        val positionHandle = GLES20.glGetAttribLocation(program, "position")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, perVertex, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer
        )
        GLES20.glUniform2f(resolutionHandle, width, height)
        GLES20.glUniform1f(timeHandle, System.nanoTime() / 1e9f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private val perVertex = 3
    private val triangles = floatArrayOf(
        -1f, -1f, 0f,
        1f, -1f, 0f,
        -1f, 1f, 0f,
        1f, -1f, 0f,
        1f, 1f, 0f,
        -1f, 1f, 0f
    )
    private val vertexCount: Int = triangles.size / perVertex
    private val vertexStride: Int = perVertex * 4
    private var vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(triangles.size * 4).let { byteBuffer ->
            byteBuffer.order(ByteOrder.nativeOrder())
            byteBuffer.asFloatBuffer().also {
                it.put(triangles)
                it.position(0)
            }
        }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val message = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                if (BuildConfig.DEVELOPMENT) error(message)
            }
        }
    }
}
