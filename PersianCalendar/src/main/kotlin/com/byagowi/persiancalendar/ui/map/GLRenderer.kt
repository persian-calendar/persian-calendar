package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import androidx.core.graphics.scale
import com.byagowi.persiancalendar.variants.debugLog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(
    val onError: (String) -> Unit = { debugLog(it) },
    val onSurfaceCreated: (GLRenderer) -> Unit = {}
) : GLSurfaceView.Renderer {
    private val vertexShaderCode =
        "attribute vec4 position; void main() { gl_Position = position; }"

    private var program: Int = 0
    private var resolutionHandle: Int = 0
    private var timeHandle: Int = 0
    private var textureHandle: Int = 0
    private var isSurfaceCreated = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        isSurfaceCreated = true
        compileProgram()
        onSurfaceCreated(this)
    }

    var fragmentShader = ""

    private var width = 1f
    private var height = 1f
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (program == 0) return
        GLES20.glUseProgram(program)
        if (textureHandle != 0) {
            val textureUniformHandle = GLES20.glGetUniformLocation(program, "tex0")
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            GLES20.glUniform1i(textureUniformHandle, 0);
        }
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

    fun compileProgram() {
        if (!isSurfaceCreated) return
        if (program != 0) GLES20.glDeleteProgram(program)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val message = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            onError(message)
        }
        resolutionHandle = GLES20.glGetUniformLocation(program, "resolution")
        timeHandle = GLES20.glGetUniformLocation(program, "time")
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
                onError(message)
            }
        }
    }

    fun loadTexture(bitmap: Bitmap) {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST
            )
            val minified = bitmap.scale(500, 500)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, minified, 0)
            minified.recycle()
        }
        if (textureHandle[0] == 0)
            onError("Failed to load texture")
        this.textureHandle = textureHandle[0]
    }

}
