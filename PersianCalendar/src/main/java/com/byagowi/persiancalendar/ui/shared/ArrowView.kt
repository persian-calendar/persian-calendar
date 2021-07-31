package com.byagowi.persiancalendar.ui.shared

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.byagowi.persiancalendar.R

class ArrowView(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs), ValueAnimator.AnimatorUpdateListener {

    init {
        setImageResource(R.drawable.ic_keyboard_arrow_down)
    }

    private var lastDegree = 0f
    private var isRtl = false
    private fun changeTo(degree: Float) {
        lastDegree = degree
        rotation = if (isRtl) -degree else degree
    }

    fun changeTo(direction: Direction) = changeTo(direction.degree)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        changeTo(lastDegree)
    }

    fun animateTo(direction: Direction) {
        ValueAnimator.ofFloat(lastDegree, direction.degree).also { valueAnimator ->
            valueAnimator.duration = arrowRotationAnimationDuration
            valueAnimator.addUpdateListener(this)
        }.start()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) =
        changeTo((animation?.animatedValue as? Float) ?: 0f)

    enum class Direction(val degree: Float) {
        START(90f), END(-90f), UP(180f), DOWN(0f)
    }

    private val arrowRotationAnimationDuration =
        resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
}
