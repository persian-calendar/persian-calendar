package com.byagowi.persiancalendar.service

import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.getSystemService
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.global.wallpaperAlternative
import com.byagowi.persiancalendar.global.wallpaperAutomatic
import com.byagowi.persiancalendar.global.wallpaperDark
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.isSystemInDarkTheme
import com.byagowi.persiancalendar.utils.logException
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PersianCalendarWallpaperService : WallpaperService(), LifecycleOwner {
    private val dispatcher = ServiceLifecycleDispatcher(this)

    private var configurationChangeCounter by mutableIntStateOf(0)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ++configurationChangeCounter
    }

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                snapshotFlow {
                    Triple(wallpaperDark, wallpaperAutomatic, configurationChangeCounter)
                }.collect { (wallpaperDark, wallpaperAutomatic, _) ->
                    val isNightMode = isSystemInDarkTheme(resources.configuration)
                    val accentColor =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) resources.getColor(
                            if (isNightMode) android.R.color.system_accent1_500
                            else android.R.color.system_accent1_300, null
                        ) else null
                    patternDrawable = PatternDrawable(
                        preferredTintColor = accentColor,
                        darkBaseColor = if (wallpaperAutomatic) isNightMode else wallpaperDark,
                        dp = resources.dp
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    private var patternDrawable: PatternDrawable? = null
    override val lifecycle: Lifecycle get() = dispatcher.lifecycle

    override fun onCreateEngine() = object : Engine() {
        private val drawRunner = Runnable { draw() }
        private val handler = Handler(Looper.getMainLooper()).also { it.post(drawRunner) }
        private var visible = true
        private val sensorManager = getSystemService<SensorManager>()
        private val sensor = sensorManager?.getSensorList(Sensor.TYPE_ACCELEROMETER)?.getOrNull(0)
        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)

            if (sensor != null) {
                if (visible) sensorManager?.registerListener(
                    sensorListener, sensor, SensorManager.SENSOR_DELAY_UI
                ) else sensorManager?.unregisterListener(sensorListener)
            }

            if (visible) dispatcher.onServicePreSuperOnStart()
        }

        private val shader =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RuntimeShader(
                """
uniform float iTime;
uniform float3 iAccelerometer;
uniform float2 iResolution;
uniform float2 iTouch0;
uniform float2 iTouch1;
//half4 main(float2 fragCoord) {
//    float2 uv = fragCoord / iResolution;
//    float a = iTouch0.x == 0 ? 0 : distance(fragCoord, iTouch0) / min(iResolution.x, iResolution.y);
//    float b = iTouch1.x == 0 ? 0 : distance(fragCoord, iTouch1) / min(iResolution.x, iResolution.y);
//    return half4(mod(uv.x + time / 10, 1), uv.y, .5 + a + b, 1) + half4(normalize(iAccelerometer), 0);
//}
// Source: @notargs https://twitter.com/notargs/status/1250468645030858753
half4 main(vec2 fragCoord) {
    vec3 d = .5 - fragCoord.xy1 / iResolution.y, p = vec3(0), o;
    for (int i = 0; i < 32; ++i) {
        o = p;
        o.z -= iTime;
        float a = o.z * .1;
        o.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        p += (.1 - length(cos(o.xy) + sin(o.yz))) * d;
    }
    return ((sin(p) + vec3(2, 5, 12)) / length(p)).xyz1;
}
"""
            ) else null
        private val shaderPaint = Paint().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) it.shader = shader
        }

        private var sensorRotation = 0f
        private val sensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                if (event.values.size != 3) return
                sensorRotation = (event.values[0] + event.values[1] + event.values[2]) / 10
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    shader?.setFloatUniform("iAccelerometer", event.values)
                }
            }
        }

        var width = 0
        var height = 0
        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int,
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                shader?.setFloatUniform("iResolution", width + 0f, height + 0f)
            }
        }

        private var time = 0f
        private val direction = listOf(1, -1).random()
        private fun draw() {
            val surfaceHolder = surfaceHolder
            val fasterUpdate =
                fasterUpdateTimestamp != 0L && fasterUpdateTimestamp.milliseconds + 2.seconds > System.currentTimeMillis().milliseconds
            if (!fasterUpdate) time += .05f * direction
            handler.removeCallbacks(drawRunner)
            val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                surfaceHolder.lockHardwareCanvas()
            } else {
                surfaceHolder.lockCanvas()
            }
            if (canvas != null) runCatching {
                if (wallpaperAlternative.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    shader?.setFloatUniform(
                        "iTime",
                        System.currentTimeMillis() % 100_000L / 1000f,
                    )
                    canvas.drawPaint(shaderPaint)
                } else {
                    val centerX = width / 2f
                    val centerY = height / 2f
                    if (touchX != 0f && touchY != 0f) {
                        xOffset += (centerX - touchX) / 400f
                        yOffset += (centerY - touchY) / 400f
                    }
                    patternDrawable?.also { patternDrawable ->
                        patternDrawable.setSize(width, height)
                        patternDrawable.rotationDegree = time + slideRotation + sensorRotation
                        canvas.withScale(scale, scale, centerX, centerY) {
                            canvas.withTranslation(xOffset, yOffset, patternDrawable::draw)
                        }
                    }
                }
            }.onFailure(logException)
            if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
            if (visible) {
                val nextFrameDelay = if (fasterUpdate) {
                    this.fasterUpdateTimestamp = 0L
                    1000L / 20
                } else 1000L / 10

                handler.postDelayed(drawRunner, nextFrameDelay)
            }
        }

        private var fasterUpdateTimestamp = 0L

        private var xOffset = 0f
        private var yOffset = 0f
        private var slideRotation = 0f
        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            this.slideRotation = (xPixelOffset + yPixelOffset) / 2000f
            fasterUpdateTimestamp = System.currentTimeMillis()
        }

        private var scale = 1f
        override fun onZoomChanged(
            zoom: Float // [0-1], indicating fully zoomed in to fully zoomed out
        ) {
            this.scale = 1 - zoom / 5
            fasterUpdateTimestamp = System.currentTimeMillis()
        }

        private var touchX = 0f
        private var touchY = 0f
        override fun onTouchEvent(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                    touchX = event.x
                    touchY = event.y
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val id = event.getPointerId(event.actionIndex).coerceIn(0, 1)
                        shader?.setFloatUniform("iTouch$id", event.x, event.y)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    touchX = 0f
                    touchY = 0f
                }
            }
            fasterUpdateTimestamp = System.currentTimeMillis()
        }
    }
}
