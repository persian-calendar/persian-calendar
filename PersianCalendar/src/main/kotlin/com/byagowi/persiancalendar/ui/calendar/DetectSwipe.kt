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
    // Detector has a two phase execution, right after the first down and after pass of the
    // threshold, this is needed for a screen with its own scrollable content and state.
    // In retrospect maybe this could be implemented based on Modifier.nestedScroll also.
    detector: () -> ((isUp: Boolean) -> Unit),
) = this then Modifier.pointerInput(Unit) {
    val thresholdPx = threshold.toPx()
    awaitEachGesture {
        var disable = false
        var yAccumulation = 0f
        // Don't inline the following line into verticalDrag, detector should be instanced
        // right after the first down
        val downId = awaitFirstDown(requireUnconsumed = false).id
        val detectorInstance = detector()
        verticalDrag(downId) {
            yAccumulation += it.positionChange().y
            if (!disable && abs(yAccumulation) > thresholdPx) {
                detectorInstance(yAccumulation < 0f)
                disable = true
            }
        }
    }
}
