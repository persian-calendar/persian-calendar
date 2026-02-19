/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2026 Android Persian Calendar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This is a revival of nostalgic good old
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:appcompat/appcompat/src/main/java/androidx/appcompat/graphics/drawable/DrawerArrowDrawable.java
// but using Compose.
// It's tweaked to match Icons.AutoMirrored.Default.ArrowBack and Icons.Default.Menu
package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun DrawerArrowDrawable(
    progress: State<Float>,
    // Whether bars should spin or not during progress
    spin: Boolean = false,
    // Whether we should mirror animation when animation is reversed, only meaningful
    // when spin is enabled
    verticalMirror: Boolean = false,
    // Quirks to match with Icons.AutoMirrored.Default.ArrowBack and Icons.Default.Menu
    quirks: Boolean = true,
) {
    val density = LocalDensity.current
    val barThickness = with(density) { 2.dp.toPx() }
    val strokeStyle = remember { Stroke(barThickness) }
    // the amount that overlaps w/ bar size when rotation is max
    val maxCutForBarSize = barThickness / 2 * ARROW_HEAD_ANGLE_COSINE
    // Use Path instead of canvas operations so that if color has transparency, overlapping sections
    // won't look different
    val path = remember { Path() }
    val contentColor = LocalContentColor.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(
        Modifier
            .padding(16.dp)
            .size(16.dp),
    ) {
        val progress = progress.value
        run {
            // The length of middle bar
            val barLength = if (quirks) {
                // Otherwise it won't match Icons.Default.Menu
                18.dp.toPx()
            } else 16.dp.toPx()
            // The space between bars when they are parallel
            val barGap = 3.dp.toPx()
            // The length of top and bottom bars when they merge into an arrow
            val arrowHeadLength = 8.dp.toPx()
            // Interpolated widths of arrow bars
            val arrowHeadBarLength = run {
                val finalValue = sqrt(arrowHeadLength * arrowHeadLength * 2)
                lerp(barLength, finalValue, progress)
            }
            // The length of the middle bar when arrow is shaped
            val arrowShaftLength = run {
                val finalValue = 16.dp.toPx()
                lerp(barLength, finalValue, progress)
            }
            // Interpolated size of middle bar
            val arrowShaftCut = lerp(0f, maxCutForBarSize, progress)
            // The rotation of the top and bottom bars (that make the arrow head)
            val rotation = lerp(0f, ARROW_HEAD_ANGLE, progress)
            val arrowWidth = arrowHeadBarLength * cos(rotation)
            val arrowHeight = arrowHeadBarLength * sin(rotation)
            val topBottomBarOffset = lerp(
                start = barGap + barThickness,
                stop = -maxCutForBarSize,
                fraction = progress,
            )
            val arrowEdge = -arrowShaftLength / 2
            path.rewind()
            // draw middle bar
            path.moveTo(arrowEdge + arrowShaftCut, 0f)
            path.relativeLineTo(arrowShaftLength - arrowShaftCut * 2, 0f)
            // bottom bar
            path.moveTo(arrowEdge, topBottomBarOffset)
            path.relativeLineTo(arrowWidth, arrowHeight)
            // top bar
            path.moveTo(arrowEdge, -topBottomBarOffset)
            path.relativeLineTo(arrowWidth, -arrowHeight)
            path.close()
        }
        // Rotate the whole canvas if spinning, if not, rotate it 180 to get
        // the arrow pointing the other way for RTL.
        translate(
            left = this.size.width / 2 + if (quirks) {
                // otherwise it won't match Icons.AutoMirrored.Default.ArrowBack
                progress * (if (isRtl) -1 else 1).dp.toPx()
            } else 0f,
            top = this.size.height / 2,
        ) {
            rotate(
                degrees = if (spin) {
                    // The whole canvas rotates as the transition happens
                    lerp(
                        start = if (isRtl) 0f else -180f,
                        stop = if (isRtl) 180f else 0f,
                        fraction = progress,
                    ) * if (verticalMirror xor isRtl) -1 else 1
                } else {
                    if (isRtl) 180f else 0f
                },
                pivot = Offset.Zero,
            ) { drawPath(path, contentColor, style = strokeStyle) }
        }
    }
}

private val ARROW_HEAD_ANGLE = Math.toRadians(45.0).toFloat()
private val ARROW_HEAD_ANGLE_COSINE = cos(ARROW_HEAD_ANGLE)
