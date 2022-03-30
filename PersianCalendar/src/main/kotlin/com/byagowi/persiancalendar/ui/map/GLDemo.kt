package com.byagowi.persiancalendar.ui.map

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

fun showGLDemoDialog(activity: FragmentActivity) {
    val glView = GLSurfaceView(activity)
    glView.setEGLContextClientVersion(2)
    glView.setRenderer(Renderer())
    glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY // XXX: This is dangerous
    MaterialAlertDialogBuilder(activity)
        .setView(glView)
        .show()
}

class Renderer : GLSurfaceView.Renderer {
    private lateinit var triangle: Triangle

    override fun onSurfaceCreated(
        gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?
    ) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        triangle = Triangle()
    }

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        triangle.draw(vPMatrix)
    }
}

const val COORDS_PER_VERTEX = 3
val triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
)

class Triangle {
    private val vertexShaderCode = """
uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
void main() {
    gl_Position = uMVPMatrix * vPosition;
}"""

    private val fragmentShaderCode = """
precision mediump float;
//uniform vec4 vColor;
/*void main() {
  gl_FragColor = vColor;
}*/
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

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).let { byteBuffer ->
            // use the device hardware's native byte order
            byteBuffer.order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            byteBuffer.asFloatBuffer().also {
                // add the coordinates to the FloatBuffer
                it.put(triangleCoords)
                // set the buffer to read the first coordinate
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
                val message =
                    "Could not compile program: " + GLES20.glGetShaderInfoLog(shader) + " | " + shaderCode
                GLES20.glDeleteShader(shader)
                error(message)
            }
        }
    }

    private var program: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                val message = "Could not link program: " + GLES20.glGetProgramInfoLog(it)
                GLES20.glDeleteProgram(it)
                error(message)
            }
        }
    }

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)
        // get handle to vertex shader's vPosition member
        GLES20.glGetAttribLocation(program, "vPosition").also { positionHandle ->
            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(positionHandle)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer
            )

            // Use to access and set the view transformation
            // get handle to shape's transformation matrix
            GLES20.glGetUniformLocation(program, "uMVPMatrix").also { vPMatrixHandle ->
                // Pass the projection and view transformation to the shader
                GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
            }

            // get handle to fragment shader's vColor member
            GLES20.glGetUniformLocation(program, "vColor").also { colorHandle ->
                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            // get handle to fragment shader's vColor member
            GLES20.glGetUniformLocation(program, "resolution").also { colorHandle ->
                // Set color for drawing the triangle
                GLES20.glUniform2f(colorHandle, 600f, 800f)
            }

            // get handle to fragment shader's vColor member
            GLES20.glGetUniformLocation(program, "time").also { colorHandle ->
                // Set color for drawing the triangle
                GLES20.glUniform1f(colorHandle, System.nanoTime() / 1e9f)
            }

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(positionHandle)
        }
    }
}
