package com.byagowi.persiancalendar.service

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.color.DynamicColors

class PersianCalendarWallpaperService : WallpaperService() {
    override fun onCreateEngine() = object : Engine() {
        private var patternDrawable = PatternDrawable(dp = resources.dp)
        private val drawRunner = Runnable { draw() }
        private val handler = Handler(Looper.getMainLooper()).also { it.post(drawRunner) }
        private var visible = true
        private val bounds = Rect()
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
            if (visible) handler.post(drawRunner)
            else handler.removeCallbacks(drawRunner)
        }

        private var rotationDegree = 0f
        private val direction = listOf(1, -1).random()
        private fun draw(skipRotation: Boolean = false) {
            val surfaceHolder = surfaceHolder
            if (!skipRotation) rotationDegree += .05f * direction
            runCatching {
                val canvas = surfaceHolder.lockCanvas() ?: return@runCatching
                canvas.getClipBounds(bounds)
                patternDrawable.bounds = bounds
                patternDrawable.rotationDegree = rotationDegree + addedRotation
                canvas.withScale(scale, scale, bounds.exactCenterX(), bounds.exactCenterY()) {
                    canvas.withTranslation(xOffset, yOffset, patternDrawable::draw)
                }
                surfaceHolder.unlockCanvasAndPost(canvas)
            }.onFailure(logException)
            handler.removeCallbacks(drawRunner)
            if (visible) handler.postDelayed(drawRunner, 1000 / 10)
        }

        private var rotateOnOffsetChange = true
        private var xOffset = 0f
        private var yOffset = 0f
        private var addedRotation = 0f
        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            if (rotateOnOffsetChange) {
                this.addedRotation = (xPixelOffset + yPixelOffset) / 1000f
            } else {
                this.xOffset = xPixelOffset / 10f
                this.yOffset = yPixelOffset / 10f
            }
            draw(skipRotation = true)
        }

        private var scale = 1f
        override fun onZoomChanged(
            zoom: Float // [0-1], indicating fully zoomed in to fully zoomed out
        ) {
            this.scale = 1 - zoom / 3
            draw(skipRotation = true)
        }
    }
}
