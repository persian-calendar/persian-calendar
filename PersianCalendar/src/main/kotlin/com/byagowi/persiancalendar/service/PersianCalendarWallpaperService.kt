package com.byagowi.persiancalendar.service

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.utils.logException

class PersianCalendarWallpaperService : WallpaperService() {
    override fun onCreateEngine() = object : Engine() {
        private var patternDrawable = PatternDrawable()
        private val drawRunner = Runnable { draw() }
        private val handler = Handler(Looper.getMainLooper()).also { it.post(drawRunner) }
        private var visible = true
        private val bounds = Rect()
        override fun onVisibilityChanged(visible: Boolean) {
            patternDrawable = PatternDrawable()
            this.visible = visible
            if (visible) handler.post(drawRunner)
            else handler.removeCallbacks(drawRunner)
        }

        private var rotationDegree = 0f
        private fun draw() {
            val surfaceHolder = surfaceHolder
            rotationDegree += .1f
            val canvas = runCatching {
                val result = surfaceHolder.lockCanvas()
                result.getClipBounds(bounds)
                patternDrawable.bounds = bounds
                patternDrawable.rotationDegree = rotationDegree
                patternDrawable.draw(result)
                result
            }.onFailure(logException).getOrNull()
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.removeCallbacks(drawRunner)
            if (visible) handler.postDelayed(drawRunner, 1000 / 15)
        }
    }
}
