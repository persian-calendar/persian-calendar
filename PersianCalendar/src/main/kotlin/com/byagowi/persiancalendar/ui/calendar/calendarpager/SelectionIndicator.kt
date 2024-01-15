package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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

    val columnsCount = if (isShowWeekOfYearEnabled) 8 else 7
    val rowsCount = 7

    val monthRange = 0..<monthLength
    val lastHighlightedDayOfMonth = lastHighlightedDay - monthStartJdn
    val isInRange = lastHighlightedDayOfMonth in monthRange
    val radiusFraction by animateFloatAsState(
        targetValue = if (isHighlighted && isInRange) 1f else 0f,
        animationSpec = if (isInRange) springSpec else applyImmediately,
        label = "radius",
    )
    if (!isInRange) return
    val cellIndex = lastHighlightedDayOfMonth + startingDayOfWeek
    var isHideOrReveal by remember { mutableStateOf(true) }
    val row by animateFloatAsState(
        targetValue = cellIndex / 7 + 1f, // +1 for weekday names initials row
        animationSpec = if (isHideOrReveal) applyImmediately else springSpec,
        label = "row",
    )
    val column by animateFloatAsState(
        targetValue = cellIndex % 7 + if (isShowWeekOfYearEnabled) 1f else 0f,
        animationSpec = if (isHideOrReveal) applyImmediately else springSpec,
        label = "column",
    )
    isHideOrReveal = !isHighlighted

    Canvas(Modifier.fillMaxSize()) {
        val cellWidthPx = size.width / columnsCount
        val cellHeightPx = size.height / rowsCount

        val left = if (isRtl) size.width - (column + 1) * cellWidthPx else column * cellWidthPx
        val top = row * cellHeightPx

        val radius = min(cellWidthPx, cellHeightPx) / 2 - .5.dp.toPx()
        drawCircle(
            color = indicatorColor,
            center = Offset(left + cellWidthPx / 2f, top + cellHeightPx / 2f),
            radius = radius * radiusFraction,
        )
    }
}

private val springSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
)
private val applyImmediately = snap<Float>(0)
