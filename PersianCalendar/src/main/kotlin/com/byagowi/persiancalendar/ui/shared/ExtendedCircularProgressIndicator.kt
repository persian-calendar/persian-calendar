package com.byagowi.persiancalendar.ui.shared

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator

class ExtendedCircularProgressIndicator(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), ValueAnimator.AnimatorUpdateListener {

    private val progressIndicator = CircularProgressIndicator(context, attrs).also(::addView)

    var enableAnimation = true

    var max = 0
        set(value) {
            progressIndicator.max = value
            field = value
        }

    private var animator: ValueAnimator? = null

    var progress = 0
        set(value) {
            animator?.removeAllUpdateListeners()
            field = value
            if (!enableAnimation) {
                progressIndicator.progress = value
                return
            }
            ValueAnimator.ofInt(progressIndicator.progress, value).also {
                animator = it
                it.duration = resources.getInteger(android.R.integer.config_longAnimTime) * 2L
                it.interpolator = OvershootInterpolator(2f)
                it.addUpdateListener(this)
            }.start()
        }

    override fun onAnimationUpdate(animator: ValueAnimator?) {
        progressIndicator.progress = ((animator?.animatedValue as? Int) ?: 0)
    }
}
