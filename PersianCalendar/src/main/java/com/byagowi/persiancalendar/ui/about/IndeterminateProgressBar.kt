package com.byagowi.persiancalendar.ui.about

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar

class IndeterminateProgressBar : ProgressBar {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        isIndeterminate = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            ValueAnimator.ofArgb(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE).apply {
                duration = 3000
                interpolator = LinearInterpolator()
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                val indeterminateDrawable = indeterminateDrawable
                addUpdateListener { indeterminateDrawable.setColorFilter(it.animatedValue as Int, PorterDuff.Mode.SRC_ATOP) }
                start()
            }
    }
}
