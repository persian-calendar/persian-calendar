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
    widthPx: Float,
    heightPx: Float,
    halfDpInPx: Float,
    isRtl: Boolean,
) {
    var lastHighlightedDay by remember { mutableStateOf(selectedDay) }
    if (isHighlighted) lastHighlightedDay = selectedDay

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

    val cellWidthPx = widthPx / columnsCount
    val cellHeightPx = heightPx / rowsCount

    val offset by animateOffsetAsState(
        targetValue = Offset(
            x = (cellIndex % 7 + if (isShowWeekOfYearEnabled) 1 else 0).let {
                if (isRtl) widthPx - (it + 1) * cellWidthPx else it * cellWidthPx
            } + cellWidthPx / 2f,
            // +1 for weekday names initials row
            y = (cellIndex / 7 + 1) * cellHeightPx + cellHeightPx / 2f,
        ),
        animationSpec = if (isHideOrReveal) moveImmediately else moveSpec,
        label = "offset",
    )
    isHideOrReveal = !isHighlighted

    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            color = indicatorColor,
            center = offset,
            radius = (min(cellWidthPx, cellHeightPx) / 2 - halfDpInPx) * radiusFraction,
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
