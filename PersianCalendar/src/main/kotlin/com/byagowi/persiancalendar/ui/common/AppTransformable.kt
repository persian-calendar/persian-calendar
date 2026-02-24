package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

/**
 * Similar to [androidx.compose.foundation.gestures.transformable]. That had¬
 * some shortcomings on a quick look but the decision can be reconsidered.
 */
@Composable
fun Modifier.appTransformable(
    scale: Animatable<Float, AnimationVector1D>,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    disableHorizontalLimit: Boolean = false,
    disableVerticalLimit: Boolean = false,
    disablePan: Boolean = false,
    contentSize: ((canvasSize: Size) -> Size)? = null,
    onClick: ((x: Float, y: Float, canvasSize: Size) -> Unit)? = null,
    scaleRange: ClosedFloatingPointRange<Float> = 1f..Float.MAX_VALUE,
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    return this.pointerInput(disablePan) {
        val size = this@pointerInput.size.toSize()
        fun updateBounds(scale: Float) {
            if (disableVerticalLimit && disableHorizontalLimit) return
            val contentSize = contentSize?.invoke(size) ?: size
            if (!disableHorizontalLimit) {
                val bound = ((contentSize.width * scale - size.width) / 2f).coerceAtLeast(0f)
                offsetX.updateBounds(-bound, bound)
            }
            if (!disableVerticalLimit) {
                val bound = ((contentSize.height * scale - size.height) / 2f).coerceAtLeast(0f)
                offsetY.updateBounds(-bound, bound)
            }
        }

        awaitEachGesture {
            val tracker = if (disablePan) null else VelocityTracker()
            val down = awaitFirstDown(requireUnconsumed = false)
            val downPosition = down.position
            val downTime = down.uptimeMillis
            var hasMoved = false
            var lastEvent: PointerEvent
            var newScale = scale.value
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
                        val oldScale = scale.value
                        newScale = (scale.value * zoomChange).coerceIn(scaleRange)
                        coroutineScope.launch { scale.snapTo(newScale) }
                        val focusX = centroid.x - size.width / 2f
                        val focusY = centroid.y - size.height / 2f
                        targetX =
                            (offsetX.value + focusX) * (newScale / oldScale) - focusX + panChange.x
                        targetY =
                            (offsetY.value + focusY) * (newScale / oldScale) - focusY + panChange.y
                    } else if (panChange != Offset.Zero) {
                        hasMoved = true
                        targetX = offsetX.value + panChange.x
                        targetY = offsetY.value + panChange.y
                    }

                    if (targetX != 0f || targetY != 0f) {
                        updateBounds(newScale)
                        coroutineScope.launch {
                            offsetX.snapTo(targetX)
                            offsetY.snapTo(targetY)
                        }
                    }

                    event.changes.forEach {
                        if (it.positionChanged()) {
                            tracker?.addPosition(it.uptimeMillis, it.position)
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
                val translatedX = upPosition.x - centerX - offsetX.value
                val translatedY = upPosition.y - centerY - offsetY.value
                val x = translatedX / newScale + centerX
                val y = translatedY / newScale + centerY
                onClick(x, y, size)
            }

            if (hasMoved && !disablePan) {
                val velocity = tracker?.calculateVelocity()
                updateBounds(newScale)
                coroutineScope.launch {
                    offsetX.animateDecay(
                        initialVelocity = velocity?.x ?: 0f,
                        animationSpec = exponentialDecay(frictionMultiplier = 3f),
                    )
                }
                coroutineScope.launch {
                    offsetY.animateDecay(
                        initialVelocity = velocity?.y ?: 0f,
                        animationSpec = exponentialDecay(frictionMultiplier = 3f),
                    )
                }
            }
            tracker?.resetTracking()
        }
    }
}
