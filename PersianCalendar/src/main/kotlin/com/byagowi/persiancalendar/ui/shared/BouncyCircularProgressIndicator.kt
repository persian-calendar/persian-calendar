package com.byagowi.persiancalendar.ui.shared

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator

class BouncyCircularProgressIndicator(context: Context, attrs: AttributeSet? = null) :
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
            if (!enableAnimation) {
                progressIndicator.progress = value * accuracyFactor
                return
            }
            ValueAnimator.ofInt(progressIndicator.progress / accuracyFactor, value).also {
                animator = it
                it.duration = animationDuration
                it.interpolator = BounceInterpolator()
                it.addUpdateListener(this)
            }.start()
        }

    override fun onAnimationUpdate(animator: ValueAnimator?) {
        progressIndicator.progress = ((animator?.animatedValue as? Int) ?: 0) * accuracyFactor
    }

    companion object {
        private const val animationDuration = 1000L
        private const val accuracyFactor = 100
    }
}
