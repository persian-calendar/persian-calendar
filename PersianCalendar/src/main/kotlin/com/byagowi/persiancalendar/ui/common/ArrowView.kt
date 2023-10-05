package com.byagowi.persiancalendar.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ArrowView(context: Context, attr: AttributeSet? = null) : AppCompatImageView(context, attr) {

    init {
        setImageResource(androidx.preference.R.drawable.ic_arrow_down_24dp)
    }

    private var animator = ValueAnimator().also {
        it.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        it.addUpdateListener { v -> applyRotation(v.animatedValue as? Float ?: 0f) }
    }
    private var isRtl = false
    private fun applyRotation(degree: Float) {
        rotation = if (isRtl) -degree else degree
    }

    fun rotateTo(direction: Direction) = applyRotation(direction.degree)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        applyRotation(animator.animatedValue as? Float ?: 0f)
    }

    fun animateTo(direction: Direction) {
        animator.setFloatValues(animator.animatedValue as? Float ?: 0f, direction.degree)
        animator.start()
    }

    enum class Direction(val degree: Float) { START(90f), END(-90f), UP(180f), DOWN(0f) }
}
