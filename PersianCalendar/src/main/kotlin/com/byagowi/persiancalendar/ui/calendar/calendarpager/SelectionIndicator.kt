package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Jdn
import kotlin.math.min

@Composable
fun SelectionIndicator(
    columnsCount: Int,
    rowsCount: Int,
    monthStartJdn: Jdn,
    monthLength: Int,
    startingDayOfWeek: Int,
    isShowWeekOfYearEnabled: Boolean,
    isHighlighted: Boolean,
    selectedDay: Jdn,
    indicatorColor: Color,
) {
    var lastHighlightedDay by remember { mutableStateOf(selectedDay) }
    if (isHighlighted) lastHighlightedDay = selectedDay

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val lastHighlightedDayOfMonth = lastHighlightedDay - monthStartJdn
    val isInRange = lastHighlightedDayOfMonth in 0..<monthLength
    val radiusFraction by animateFloatAsState(
        targetValue = if (isHighlighted && isInRange) 1f else 0f,
        animationSpec = if (isInRange) revealOrHideSpec else revealOrHideImmediately,
        label = "radius",
    )
    if (!isInRange) return
    val cellIndex = lastHighlightedDayOfMonth + startingDayOfWeek
    var isHideOrReveal by remember { mutableStateOf(true) }
    val offset by animateOffsetAsState(
        targetValue = Offset(
            cellIndex % 7 + if (isShowWeekOfYearEnabled) 1f else 0f,
            cellIndex / 7 + 1f, // +1 for weekday names initials row
        ),
        animationSpec = if (isHideOrReveal) moveImmediately else moveSpec,
        label = "offset",
    )
    isHideOrReveal = !isHighlighted

    Canvas(Modifier.fillMaxSize()) {
        val cellWidthPx = size.width / columnsCount
        val cellHeightPx = size.height / rowsCount

        val left = if (isRtl) size.width - (offset.x + 1) * cellWidthPx else offset.x * cellWidthPx
        val top = offset.y * cellHeightPx

        val radius = min(cellWidthPx, cellHeightPx) / 2 - .5.dp.toPx()
        drawCircle(
            color = indicatorColor,
            center = Offset(left + cellWidthPx / 2f, top + cellHeightPx / 2f),
            radius = radius * radiusFraction,
        )
    }
}

private val revealOrHideSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
)
private val revealOrHideImmediately = snap<Float>()
private val moveSpec = spring<Offset>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
)
private val moveImmediately = snap<Offset>()
