package com.byagowi.persiancalendar.ui.common

import androidx.collection.FloatFloatPair
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

@Composable
fun ZoomableCanvas(
    modifier: Modifier,
    onClick: (position: Offset, canvasSize: Size) -> Unit,
    scaleRange: ClosedFloatingPointRange<Float> = 1f..Float.MAX_VALUE,
    contentSize: ((size: Size) -> Size)? = null,
    onDraw: DrawScope.(offsetX: Float, offsetY: Float, scale: Float) -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    Canvas(
        modifier
            .pointerInput(Unit) {
                val size = this@pointerInput.size.toSize()
                fun calculateMaxOffsets(scale: Float): FloatFloatPair {
                    val contentSize = contentSize?.invoke(size) ?: size
                    return FloatFloatPair(
                        ((contentSize.width * scale - size.width) / 2f).coerceAtLeast(0f),
                        ((contentSize.height * scale - size.height) / 2f).coerceAtLeast(0f),
                    )
                }

                awaitEachGesture {
                    val tracker = VelocityTracker()
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downPosition = down.position
                    val downTime = down.uptimeMillis
                    var hasMoved = false
                    var lastEvent: PointerEvent
                    do {
                        val event = awaitPointerEvent()
                        lastEvent = event
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val centroid = event.calculateCentroid(useCurrent = false)
                            var targetX = 0f
                            var targetY = 0f
                            if (zoomChange != 1f) {
                                hasMoved = true
                                val oldScale = scale
                                scale = (scale * zoomChange).coerceIn(scaleRange)
                                val focusX = centroid.x - size.width / 2f
                                val focusY = centroid.y - size.height / 2f
                                targetX =
                                    (offsetX.value + focusX) * (scale / oldScale) - focusX + panChange.x
                                targetY =
                                    (offsetY.value + focusY) * (scale / oldScale) - focusY + panChange.y
                            } else if (panChange != Offset.Zero) {
                                hasMoved = true
                                targetX = offsetX.value + panChange.x
                                targetY = offsetY.value + panChange.y
                            }

                            if (targetX != 0f || targetY != 0f) coroutineScope.launch {
                                val (maxOffsetX, maxOffsetY) = calculateMaxOffsets(scale)
                                offsetX.snapTo(targetX.coerceIn(-maxOffsetX, maxOffsetX))
                                offsetY.snapTo(targetY.coerceIn(-maxOffsetY, maxOffsetY))
                            }

                            event.changes.forEach {
                                if (it.positionChanged()) {
                                    tracker.addPosition(it.uptimeMillis, it.position)
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
                    if (!hasMoved && run {
                            (upPosition - downPosition).getDistance() <= viewConfiguration.touchSlop
                        } && upTime - downTime < viewConfiguration.longPressTimeoutMillis) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val translatedX = upPosition.x - centerX - offsetX.value
                        val translatedY = upPosition.y - centerY - offsetY.value
                        val canvasX = translatedX / scale + centerX
                        val canvasY = translatedY / scale + centerY
                        onClick(Offset(canvasX, canvasY), size)
                    }

                    if (hasMoved) {
                        val velocity = tracker.calculateVelocity()
                        coroutineScope.launch {
                            val (maxOffsetX, maxOffsetY) = calculateMaxOffsets(scale)
                            launch {
                                offsetX.updateBounds(-maxOffsetX, maxOffsetX)
                                offsetX.animateDecay(
                                    initialVelocity = velocity.x,
                                    animationSpec = exponentialDecay(frictionMultiplier = 3f),
                                )
                            }
                            launch {
                                offsetY.updateBounds(-maxOffsetY, maxOffsetY)
                                offsetY.animateDecay(
                                    initialVelocity = velocity.y,
                                    animationSpec = exponentialDecay(frictionMultiplier = 3f),
                                )
                            }
                        }
                    }
                }
            },
    ) { onDraw(offsetX.value, offsetY.value, scale) }
}
