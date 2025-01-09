package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp

fun Modifier.detectSwipeDown(onSwipeDown: () -> Unit) = this then Modifier.pointerInput(Unit) {
    val thresholdPx = defaultSwipeThreshold.toPx()
    awaitEachGesture {
        val id = awaitFirstDown(requireUnconsumed = false).id
        var successful = false
        var yAccumulation = 0f
        verticalDrag(id) {
            yAccumulation += it.positionChange().y
            if (!successful && yAccumulation > thresholdPx) {
                onSwipeDown()
                successful = true
            }
        }
    }
}

val defaultSwipeThreshold = 80.dp
