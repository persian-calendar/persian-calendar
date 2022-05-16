package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import com.byagowi.persiancalendar.generated.commonVertexShader
import com.byagowi.persiancalendar.variants.debugLog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(
    val onError: (String) -> Unit = { debugLog(it) },
    val onSurfaceCreated: (GLRenderer) -> Unit = {}
) : GLSurfaceView.Renderer {
    var overriddenTime = 0f
    var overriddenY = 0f
    var overriddenZoom = 1f

    private var program = 0
    private var positionHandle = 0
    private var resolutionHandle = 0
    private var timeHandle = 0
    private var yHandle = 0
    private var zoomHandle = 0
    private var verticesHandle = 0
    private var textureHandle = 0
    private var textureUniformHandle = 0
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

        val vertexBuffer = ByteBuffer
            .allocateDirect(rectangleVertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(rectangleVertices).position(0)
        val handle = IntArray(1)
        GLES20.glGenBuffers(1, handle, 0)
        verticesHandle = handle[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesHandle)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, vertexBuffer.limit() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW
        )
    }

    private val rectangleVertices = floatArrayOf(
        -1f, -1f, 0f,
        1f, -1f, 0f,
        -1f, 1f, 0f,
        1f, -1f, 0f,
        1f, 1f, 0f,
        -1f, 1f, 0f
    )
    private val perVertex = 3
    private val vertexStride = perVertex * 4
    private val vertexCount = rectangleVertices.size / perVertex

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (program == 0) return
        GLES20.glUseProgram(program)
        if (textureHandle != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            GLES20.glUniform1i(textureUniformHandle, 0)
        }
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, perVertex, GLES20.GL_FLOAT, false, vertexStride, 0
        )
        GLES20.glUniform2f(resolutionHandle, width, height)
        GLES20.glUniform1f(
            timeHandle, if (overriddenTime == 0f) System.nanoTime() / 1e9f else overriddenTime
        )
        GLES20.glUniform1f(yHandle, overriddenY)
        GLES20.glUniform1f(zoomHandle, overriddenZoom)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    fun compileProgram() {
        if (!isSurfaceCreated) return
        if (program != 0) GLES20.glDeleteProgram(program)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, commonVertexShader)
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
        positionHandle = GLES20.glGetAttribLocation(program, "position")
        resolutionHandle = GLES20.glGetUniformLocation(program, "u_resolution")
        timeHandle = GLES20.glGetUniformLocation(program, "u_time")
        yHandle = GLES20.glGetUniformLocation(program, "u_y")
        zoomHandle = GLES20.glGetUniformLocation(program, "u_zoom")
        textureUniformHandle = GLES20.glGetUniformLocation(program, "u_tex0")
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
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        if (textureHandle[0] == 0) onError("Failed to load texture")
        this.textureHandle = textureHandle[0]
    }
}
