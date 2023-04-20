package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.byagowi.persiancalendar.ui.utils.createFlingDetector

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

    private var scrollStartX = 0f
    private var scrollStartY = 0f
    private var previousX = 0f
    private var previousY = 0f

    private val flingDetector = createFlingDetector(context) { velocityX, velocityY ->
        flingAnimationX.setStartVelocity(velocityX / 2).start()
        if (enableVerticalSlider) flingAnimationY.setStartVelocity(velocityY / 2).start()
        true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        flingDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scrollStartX = positionX.value
                flingAnimationX.cancel()
                if (enableVerticalSlider) {
                    scrollStartY = positionY.value
                    flingAnimationY.cancel()
                }
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                positionX.value = event.x - scrollStartX
                if (enableVerticalSlider) positionY.value = event.y - scrollStartY
                onScrollListener(event.x - previousX, event.y - previousY)
                previousX = event.x
                previousY = event.y
                invalidate()
            }
        }
        return true
    }

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
