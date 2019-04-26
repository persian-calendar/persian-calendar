package com.byagowi.persiancalendar.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Build
import android.view.View
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget


// https://stackoverflow.com/a/52557989
fun <T> circularRevealFromMiddle(circularRevealWidget: T) where T : View, T : CircularRevealWidget {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        circularRevealWidget.post {
            val viewWidth = circularRevealWidget.width
            val viewHeight = circularRevealWidget.height

            val viewDiagonal = Math.sqrt((viewWidth * viewWidth + viewHeight * viewHeight).toDouble()).toInt()

            AnimatorSet().apply {
                playTogether(
                        CircularRevealCompat.createCircularReveal(circularRevealWidget,
                                (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(),
                                10f, (viewDiagonal / 2).toFloat()),
                        ObjectAnimator.ofArgb(circularRevealWidget,
                                CircularRevealWidget.CircularRevealScrimColorProperty
                                        .CIRCULAR_REVEAL_SCRIM_COLOR,
                                Color.GRAY, Color.TRANSPARENT))
                duration = 500
            }.start()
        }
    }
}