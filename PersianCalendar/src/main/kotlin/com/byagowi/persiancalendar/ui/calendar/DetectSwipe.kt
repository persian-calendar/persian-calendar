package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun Modifier.detectSwipe(
    threshold: Dp = 80.dp,
    detector: () -> ((isUp: Boolean) -> Boolean)
) = this then Modifier.pointerInput(Unit) {
    val thresholdPx = threshold.toPx()
    awaitEachGesture {
        // Don't inline id into verticalDrag, detector should be instanced after the first down
        val id = awaitFirstDown(requireUnconsumed = false).id
        val detectorInstance = detector()
        var successful = false
        var yAccumulation = 0f
        verticalDrag(id) {
            yAccumulation += it.positionChange().y
            if (!successful && abs(yAccumulation) > thresholdPx) {
                successful = detectorInstance(yAccumulation < 0f)
            }
        }
    }
}

fun Modifier.detectSwipeDown(onSwipeDown: () -> Unit) = this then Modifier.detectSwipe {
    { isUp ->
        if (!isUp) {
            onSwipeDown()
            true
        } else false
    }
}
