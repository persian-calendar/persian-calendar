package com.byagowi.persiancalendar.ui.shared

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.google.android.material.progressindicator.CircularProgressIndicator

class ExtendedCircularProgressIndicator(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), ValueAnimator.AnimatorUpdateListener {

    private val progressIndicator = CircularProgressIndicator(context, attrs).also(::addView)

    var enableAnimation = true

    var max = 0
        set(value) {
            progressIndicator.max = value * accuracyFactor
            field = value
        }

    private var animator: ValueAnimator? = null

    var progress = 0
        set(value) {
            animator?.removeAllUpdateListeners()
            field = value
            if (!enableAnimation || isTalkBackEnabled) {
                progressIndicator.progress = value * accuracyFactor
                return
            }
            val current = progressIndicator.progress
            val dest = value * accuracyFactor
            ValueAnimator.ofInt(if (current == dest) 0 else current, dest).also {
                animator = it
                it.duration = resources.getInteger(android.R.integer.config_longAnimTime) * 2L
                it.interpolator = OvershootInterpolator(2f)
                it.addUpdateListener(this)
            }.start()
        }

    override fun onAnimationUpdate(animator: ValueAnimator?) {
        progressIndicator.progress = (animator?.animatedValue as? Int) ?: 0
    }

    private val accuracyFactor = if (isTalkBackEnabled) 1 else 100
}
