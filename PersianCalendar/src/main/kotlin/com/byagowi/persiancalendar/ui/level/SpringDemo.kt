package com.byagowi.persiancalendar.ui.level

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showSpringDemoDialog(activity: FragmentActivity) {
    val x = FloatValueHolder()
    val horizontalSpring = SpringAnimation(x)
    horizontalSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val y = FloatValueHolder()
    val verticalSpring = SpringAnimation(y)
    verticalSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val view = object : View(activity) {
        private var r = 0f
        private var previousX = 0f
        private var previousY = 0f

        init {
            horizontalSpring.addUpdateListener { _, _, _ -> invalidate() }
            verticalSpring.addUpdateListener { _, _, _ -> invalidate() }
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
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    horizontalSpring.cancel()
                    verticalSpring.cancel()
                    previousX = event.x
                    previousY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    x.value += event.x - previousX
                    y.value += event.y - previousY
                    previousX = event.x
                    previousY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    horizontalSpring.animateToFinalPosition(width / 2f)
                    verticalSpring.animateToFinalPosition(height / 2f)
                }
            }
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}
