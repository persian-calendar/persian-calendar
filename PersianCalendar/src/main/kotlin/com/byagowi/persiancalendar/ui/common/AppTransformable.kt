package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

/**
 * Similar to [androidx.compose.foundation.gestures.transformable]. That had¬
 * some shortcomings on a quick look but the decision can be reconsidered.
 */
@Composable
fun Modifier.appTransformable(
    scale: MutableFloatState,
    offsetX: MutableFloatState,
    offsetY: MutableFloatState,
    disableHorizontalLimit: Boolean = false,
    disableVerticalLimit: Boolean = false,
    disablePan: Boolean = false,
    contentSize: ((canvasSize: Size) -> Size)? = null,
    onClick: ((x: Float, y: Float, canvasSize: Size) -> Unit)? = null,
    scaleRange: ClosedFloatingPointRange<Float> = 1f..Float.MAX_VALUE,
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    return this.pointerInput(disablePan) {
        val size = this@pointerInput.size.toSize()
        var lastPointerId: PointerId? = null
        awaitEachGesture {
            var xBound = Float.POSITIVE_INFINITY
            var yBound = Float.POSITIVE_INFINITY
            fun updateBounds(scale: Float) {
                if (disableVerticalLimit && disableHorizontalLimit) return
                val contentSize = contentSize?.invoke(size) ?: size
                if (!disableHorizontalLimit) {
                    xBound = ((contentSize.width * scale - size.width) / 2f).coerceAtLeast(0f)
                }
                if (!disableVerticalLimit) {
                    yBound = ((contentSize.height * scale - size.height) / 2f).coerceAtLeast(0f)
                }
            }
            val tracker = if (disablePan) null else VelocityTracker()
            val down = awaitFirstDown(requireUnconsumed = false)
            lastPointerId = down.id
            val downPosition = down.position
            val downTime = down.uptimeMillis
            var hasMoved = false
            var lastEvent: PointerEvent
            var newScale = scale.floatValue
            do {
                val event = awaitPointerEvent()
                lastEvent = event
                val canceled = event.changes.any { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val panChange = if (disablePan) Offset.Zero else event.calculatePan()
                    val centroid = event.calculateCentroid(useCurrent = false)
                    var targetX = 0f
                    var targetY = 0f
                    if (zoomChange != 1f) {
                        hasMoved = true
                        val oldScale = scale.floatValue
                        newScale = (scale.floatValue * zoomChange).coerceIn(scaleRange)
                        scale.floatValue = newScale
                        val focusX = centroid.x - size.width / 2f
                        val focusY = centroid.y - size.height / 2f
                        targetX =
                            (offsetX.floatValue + focusX) * (newScale / oldScale) - focusX + panChange.x
                        targetY =
                            (offsetY.floatValue + focusY) * (newScale / oldScale) - focusY + panChange.y
                    } else if (panChange != Offset.Zero) {
                        event.changes.forEach { tracker?.addPointerInputChange(event = it) }
                        hasMoved = true
                        targetX = offsetX.floatValue + panChange.x
                        targetY = offsetY.floatValue + panChange.y
                    }

                    if (targetX != 0f || targetY != 0f) {
                        updateBounds(newScale)
                        offsetX.floatValue = targetX.coerceIn(-xBound, xBound)
                        offsetY.floatValue = targetY.coerceIn(-yBound, yBound)
                    }

                    event.changes.forEach {
                        if (it.positionChanged()) {
                            // Check if movement exceeds click threshold
                            if ((it.position - downPosition).getDistance() > viewConfiguration.touchSlop) {
                                hasMoved = true
                            }
                        }
                    }
                }
            } while (event.changes.any { it.pressed })

            val upPosition = lastEvent.changes.firstOrNull()?.position ?: downPosition
            val upTime = lastEvent.changes.firstOrNull()?.uptimeMillis ?: downTime
            if (onClick != null && !hasMoved && run {
                    (upPosition - downPosition).getDistance() <= viewConfiguration.touchSlop
                } && upTime - downTime < viewConfiguration.longPressTimeoutMillis) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val translatedX = upPosition.x - centerX - offsetX.floatValue
                val translatedY = upPosition.y - centerY - offsetY.floatValue
                val x = translatedX / newScale + centerX
                val y = translatedY / newScale + centerY
                onClick(x, y, size)
            }

            if (hasMoved && !disablePan) {
                val velocity = tracker?.calculateVelocity()
                updateBounds(newScale)
                coroutineScope.launch {
                    animateDecay(
                        initialVelocity = velocity?.x ?: 0f,
                        initialValue = offsetX.floatValue,
                        animationSpec = FloatExponentialDecaySpec(frictionMultiplier = 3f),
                    ) { value, _ ->
                        if (down.id == lastPointerId) {
                            offsetX.floatValue = value.coerceIn(-xBound, xBound)
                        }
                    }
                }
                coroutineScope.launch {
                    animateDecay(
                        initialVelocity = velocity?.y ?: 0f,
                        initialValue = offsetY.floatValue,
                        animationSpec = FloatExponentialDecaySpec(frictionMultiplier = 3f),
                    ) { value, _ ->
                        if (down.id == lastPointerId) {
                            offsetY.floatValue = value.coerceIn(-yBound, yBound)
                        }
                    }
                }
            }
            tracker?.resetTracking()
        }
    }
}
