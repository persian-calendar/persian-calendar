package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastAny

fun Modifier.detectZoom(
    onRelease: () -> Unit = {},
    onZoom: (Float) -> Unit,
) = this then Modifier.pointerInput(Unit) {
    /** This is reduced from [androidx.compose.foundation.gestures.detectTransformGestures] */
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            onZoom(event.calculateZoom())
        } while (event.changes.fastAny { it.pressed })
        onRelease()
    }
}
