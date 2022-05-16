package com.byagowi.persiancalendar.ui.level

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showFlingDemoDialog(activity: FragmentActivity) {
    val x = FloatValueHolder()
    val horizontalFling = FlingAnimation(x)
    val y = FloatValueHolder()
    val verticalFling = FlingAnimation(y)
    val view = object : View(activity) {
        private var r = 0f
        private var previousX = 0f
        private var previousY = 0f

        private var storedVelocityX = 0f
        private var storedVelocityY = 0f

        init {
            horizontalFling.addUpdateListener { _, _, velocity ->
                storedVelocityX = velocity
                invalidate()
            }
            verticalFling.addUpdateListener { _, _, velocity ->
                storedVelocityY = velocity
                invalidate()
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            x.value = w / 2f
            y.value = h / 2f
            r = w / 20f
            path.rewind()
            path.moveTo(x.value, y.value)
        }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.GRAY }
        private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        private val path = Path()
        override fun onDraw(canvas: Canvas) {
            path.lineTo(x.value, y.value)
            canvas.drawPath(path, linesPaint)
            canvas.drawCircle(x.value, y.value, r, paint)
            if (x.value < r) {
                x.value = r
                horizontalFling.cancel()
                horizontalFling.setStartVelocity(-storedVelocityX).start()
            }
            if (x.value > width - r) {
                x.value = width - r
                horizontalFling.cancel()
                horizontalFling.setStartVelocity(-storedVelocityX).start()
            }
            if (y.value < r) {
                y.value = r
                verticalFling.cancel()
                verticalFling.setStartVelocity(-storedVelocityY).start()
            }
            if (y.value > height - r) {
                y.value = height - r
                verticalFling.cancel()
                verticalFling.setStartVelocity(-storedVelocityY).start()
            }
        }

        private var velocityTracker: VelocityTracker? = null
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    velocityTracker = VelocityTracker.obtain()
                    horizontalFling.cancel()
                    verticalFling.cancel()
                    previousX = event.x
                    previousY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker?.addMovement(event)
                    x.value += event.x - previousX
                    y.value += event.y - previousY
                    previousX = event.x
                    previousY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    velocityTracker?.computeCurrentVelocity(1000)
                    horizontalFling.setStartVelocity(velocityTracker?.xVelocity ?: 0f).start()
                    verticalFling.setStartVelocity(velocityTracker?.yVelocity ?: 0f).start()
                    velocityTracker?.recycle()
                    velocityTracker = null
                }
            }
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}
