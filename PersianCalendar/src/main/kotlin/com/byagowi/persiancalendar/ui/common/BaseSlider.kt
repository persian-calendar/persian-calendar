package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder

open class BaseSlider(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var enableVerticalSlider = false
    protected var positionX = FloatValueHolder()
    private val flingAnimationX = FlingAnimation(positionX)
        .addUpdateListener { _, _, velocity ->
            onScrollListener(velocity / 10, 0f)
            invalidate()
        }
    protected var positionY = FloatValueHolder()
    private val flingAnimationY = FlingAnimation(positionY)
        .addUpdateListener { _, _, velocity ->
            onScrollListener(0f, velocity / 10)
            invalidate()
        }
    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        var scrollStartX = 0f
        var scrollStartY = 0f

        override fun onDown(e: MotionEvent): Boolean {
            scrollStartX = positionX.value
            flingAnimationX.cancel()
            if (enableVerticalSlider) {
                scrollStartY = positionY.value
                flingAnimationY.cancel()
            }
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            positionX.value = e2.x - scrollStartX
            if (enableVerticalSlider) positionY.value = e2.y - scrollStartY
            onScrollListener(-distanceX, -distanceY)
            invalidate()
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            flingAnimationX.setStartVelocity(velocityX).start()
            if (enableVerticalSlider) flingAnimationY.setStartVelocity(velocityY).start()
            return true
        }
    })

    override fun dispatchTouchEvent(event: MotionEvent) = gestureDetector.onTouchEvent(event)

    fun smoothScrollBy(velocityX: Float, velocityY: Float) {
        flingAnimationX.setStartVelocity(velocityX).start()
        if (enableVerticalSlider) flingAnimationY.setStartVelocity(velocityY).start()
    }

    fun manualScrollBy(x: Float, y: Float) {
        positionX.value += x
        positionY.value += y
        invalidate()
    }

    var onScrollListener = { _: Float, _: Float -> } // dx, dy
}
