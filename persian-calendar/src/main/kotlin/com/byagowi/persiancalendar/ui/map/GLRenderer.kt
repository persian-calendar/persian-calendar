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
    private var xHandle = 0
    private var yHandle = 0
    private var zoomHandle = 0
    private var arrayBufferHandle = 0
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

        val bufferHandles = IntArray(1)
        GLES20.glGenBuffers(1, bufferHandles, 0)
        arrayBufferHandle = bufferHandles[0]
        val buffer = ByteBuffer
            .allocateDirect(rectangleVertices.size * floatSize).order(ByteOrder.nativeOrder())
        val floatBuffer = buffer.asFloatBuffer().put(rectangleVertices).position(0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBufferHandle)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, buffer.limit(), floatBuffer, GLES20.GL_STATIC_DRAW
        )
    }

    private val rectangleVertices = floatArrayOf(
        -1f, -1f, .999f,
        3f, -1f, .999f,
        -1f, 3f, .999f,
    )
    private val floatSize = 4
    private val perVertex = 3
    private val vertexStride = perVertex * floatSize
    private val vertexCount = 3

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
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, arrayBufferHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, perVertex, GLES20.GL_FLOAT, false, vertexStride, 0
        )
        GLES20.glUniform2f(resolutionHandle, width, height)
        val time = System.nanoTime() / 1e9f
        GLES20.glUniform1f(timeHandle, time)
        GLES20.glUniform1f(xHandle, if (overriddenTime == 0f) time else overriddenTime)
        GLES20.glUniform1f(yHandle, overriddenY)
        GLES20.glUniform1f(zoomHandle, overriddenZoom)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
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
        xHandle = GLES20.glGetUniformLocation(program, "u_x")
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
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        if (textureHandle[0] == 0) onError("Failed to load texture")
        this.textureHandle = textureHandle[0]
    }
}
