package com.byagowi.persiancalendar.ui.about

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar

class IndeterminateProgressBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : ProgressBar(context, attrs) {
    init {
        isIndeterminate = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            ValueAnimator.ofArgb(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE).apply {
                duration = 3000
                interpolator = LinearInterpolator()
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    indeterminateDrawable?.setColorFilter(
                            it.animatedValue as Int,
                            PorterDuff.Mode.SRC_ATOP
                    )
                }
            }.start()
    }
}
