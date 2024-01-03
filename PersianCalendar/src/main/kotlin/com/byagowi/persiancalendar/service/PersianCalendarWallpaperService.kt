package com.byagowi.persiancalendar.service

import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import androidx.annotation.CallSuper
import androidx.core.content.getSystemService
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.global.wallpaperDark
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.isSystemInDarkTheme
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.logException
import kotlinx.coroutines.launch

class PersianCalendarWallpaperService : WallpaperService(), LifecycleOwner {
    /**
     * The best practice is to derive from [androidx.lifecycle.LifecycleService] instead
     * but we need a WallpaperService so we have to mimic that ourselves this way
     * */
    private val dispatcher = ServiceLifecycleDispatcher(this)

    @CallSuper
    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                wallpaperDark.collect { wallpaperDark ->
                    val isNightMode = isSystemInDarkTheme(resources.configuration)
                    val accentColor =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) resources.getColor(
                            if (isNightMode) android.R.color.system_accent1_500
                            else android.R.color.system_accent1_300,
                            null
                        ) else null
                    patternDrawable = PatternDrawable(
                        preferredTintColor = accentColor,
                        darkBaseColor = wallpaperDark,
                        dp = resources.dp
                    )
                }
            }
        }
    }

    @CallSuper
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
        private val bounds = Rect()
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

        private var sensorRotation = 0f
        private val sensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                if (event.values.size != 3) return
                sensorRotation = (event.values[0] + event.values[1] + event.values[2]) / 10
            }
        }

        private var rotationDegree = 0f
        private val direction = listOf(1, -1).random()
        private fun draw() {
            val surfaceHolder = surfaceHolder
            val fasterUpdate = fasterUpdateTimestamp != 0L && fasterUpdateTimestamp + TWO_SECONDS_IN_MILLIS > System.currentTimeMillis()
            if (!fasterUpdate) rotationDegree += .05f * direction
            handler.removeCallbacks(drawRunner)
            runCatching {
                val canvas = surfaceHolder.lockCanvas() ?: return@runCatching
                canvas.getClipBounds(bounds)
                val centerX = bounds.exactCenterX()
                val centerY = bounds.exactCenterY()
                if (touchX != 0f && touchY != 0f) {
                    xOffset += (centerX - touchX) / 400f
                    yOffset += (centerY - touchY) / 400f
                }
                patternDrawable?.also { patternDrawable ->
                    patternDrawable.setSize(bounds.width(), bounds.height())
                    patternDrawable.rotationDegree = rotationDegree + slideRotation + sensorRotation
                    canvas.withScale(scale, scale, centerX, centerY) {
                        canvas.withTranslation(xOffset, yOffset, patternDrawable::draw)
                    }
                }
                surfaceHolder.unlockCanvasAndPost(canvas)
            }.onFailure(logException)
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
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    touchX = event.x
                    touchY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    touchX = 0f
                    touchY = 0f
                }
            }
            fasterUpdateTimestamp = System.currentTimeMillis()
        }
    }
}
