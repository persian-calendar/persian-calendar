package com.byagowi.persiancalendar.service

import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import androidx.core.content.getSystemService
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.color.DynamicColors

class PersianCalendarWallpaperService : WallpaperService() {
    override fun onCreateEngine() = object : Engine() {
        private var patternDrawable = PatternDrawable(dp = resources.dp)
        private val drawRunner = Runnable { draw() }
        private val handler = Handler(Looper.getMainLooper()).also { it.post(drawRunner) }
        private var visible = true
        private val bounds = Rect()
        private val sensorManager = getSystemService<SensorManager>()
        private val sensor = sensorManager?.getSensorList(Sensor.TYPE_ACCELEROMETER)?.getOrNull(0)
        override fun onVisibilityChanged(visible: Boolean) {
            val context = this@PersianCalendarWallpaperService
            val isNightMode = Theme.isNightMode(context)
            val accentColor = if (DynamicColors.isDynamicColorAvailable()) context.getColor(
                if (isNightMode) android.R.color.system_accent1_500
                else android.R.color.system_accent1_300
            ) else null
            patternDrawable = PatternDrawable(
                preferredTintColor = accentColor,
                darkBaseColor = true, // launcher always has white text so let's make it always dark, for now
                dp = resources.dp
            )
            this.visible = visible
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)

            if (sensor != null) {
                if (visible) sensorManager?.registerListener(
                    sensorListener, sensor, SensorManager.SENSOR_DELAY_UI
                ) else sensorManager?.unregisterListener(sensorListener)
            }
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
            val fasterUpdate = fasterUpdateTimestamp != 0L &&
                    fasterUpdateTimestamp + TWO_SECONDS_IN_MILLIS > System.currentTimeMillis()
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
                patternDrawable.bounds = bounds
                patternDrawable.rotationDegree = rotationDegree + slideRotation + sensorRotation
                canvas.withScale(scale, scale, centerX, centerY) {
                    canvas.withTranslation(xOffset, yOffset, patternDrawable::draw)
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
