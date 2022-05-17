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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

fun showRotationalSpringDemoDialog(activity: FragmentActivity) {
    val radius = FloatValueHolder()
    val radiusSpring = SpringAnimation(radius)
    radiusSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val theta = FloatValueHolder()
    val rotationalSpring = SpringAnimation(theta)
    rotationalSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val view = object : View(activity) {
        private var circleRadius = 0f
        private var centerX = 0f
        private var centerY = 0f

        init {
            radiusSpring.addUpdateListener { _, _, _ -> invalidate() }
            rotationalSpring.addUpdateListener { _, _, _ -> invalidate() }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            radius.value = 0f
            theta.value = 0f
            circleRadius = w / 20f
            centerX = w / 2f
            centerY = h / 2f
            path.rewind()
            path.moveTo(centerX, centerY)
        }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.GRAY }
        private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        private val path = Path()
        override fun onDraw(canvas: Canvas) {
            val x = radius.value * cos(theta.value / 180) + centerX
            val y = radius.value * sin(theta.value / 180) + centerY
            path.lineTo(x, y)
            canvas.drawPath(path, linesPaint)
            canvas.drawCircle(x, y, circleRadius, paint)
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    radiusSpring.cancel()
                    rotationalSpring.cancel()
                }
                MotionEvent.ACTION_MOVE -> {
                    radius.value = hypot(event.x - centerX, event.y - centerY)
                    theta.value = atan2(event.y - centerY, event.x - centerX) * 180
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    radiusSpring.animateToFinalPosition(0f)
                    rotationalSpring.animateToFinalPosition(0f)
                }
            }
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}
