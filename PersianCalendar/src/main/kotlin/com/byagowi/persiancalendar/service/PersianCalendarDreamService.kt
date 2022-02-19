package com.byagowi.persiancalendar.service

import android.animation.ValueAnimator
import android.service.dreams.DreamService
import android.view.View
import android.view.animation.LinearInterpolator
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.athan.PatternDrawable

class PersianCalendarDreamService : DreamService() {

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).also {
        it.duration = 360000L
        it.interpolator = LinearInterpolator()
        it.repeatMode = ValueAnimator.RESTART
        it.repeatCount = ValueAnimator.INFINITE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        setContentView(View(this).also {
            val isNightMode = Theme.isNightMode(this)
            val accentColor = if (Theme.isDynamicColorAvailable()) getColor(
                if (isNightMode) listOf(
                    android.R.color.system_accent1_200,
                    android.R.color.system_accent2_200,
                    android.R.color.system_accent3_200
                ).random() else listOf(
                    android.R.color.system_accent1_400,
                    android.R.color.system_accent2_400,
                    android.R.color.system_accent3_400
                ).random()
            ) else null
            val pattern = PatternDrawable(
                preferredTintColor = accentColor,
                darkBaseColor = Theme.isNightMode(this)
            )
            it.background = pattern
            valueAnimator.addUpdateListener {
                pattern.rotationDegree = valueAnimator.animatedValue as? Float ?: 0f
                pattern.invalidateSelf()
            }
        })
        listOf(valueAnimator::start, valueAnimator::reverse).random()()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        valueAnimator.removeAllUpdateListeners()
        valueAnimator.cancel()
    }
}
