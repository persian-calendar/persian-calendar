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
    // Detector has a two step execution, right after the drag start and after pass of the
    // threshold, this is needed for a with its own scrollable content and state.
    // Maybe this could be implemented based on Modifier.nestedScroll also.
    detector: () -> ((isUp: Boolean) -> Unit),
) = this then Modifier.pointerInput(Unit) {
    val thresholdPx = threshold.toPx()
    awaitEachGesture {
        // Don't inline id into verticalDrag, detector should be instanced after the first down
        val id = awaitFirstDown(requireUnconsumed = false).id
        val detectorInstance = detector()
        var disable = false
        var yAccumulation = 0f
        verticalDrag(id) {
            yAccumulation += it.positionChange().y
            if (!disable && abs(yAccumulation) > thresholdPx) {
                detectorInstance(yAccumulation < 0f)
                disable = true
            }
        }
    }
}
