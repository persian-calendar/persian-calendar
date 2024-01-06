package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import androidx.core.animation.doOnEnd
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.theme.AppDaySelectionColor
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SelectionIndicator(
    viewModel: CalendarViewModel,
    monthStartJdn: Jdn,
    monthRange: IntRange,
    size: DpSize,
    startingDayOfWeek: Int,
    widthPixels: Float,
    cellWidthPx: Float,
    cellHeightPx: Float,
    oneDpInPx: Float,
) {
    val isHighlighted by viewModel.isHighlighted.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    var lastSelectedDay by remember { mutableStateOf(selectedDay) }
    // Why the moving circle feels faster this way
    val invalidationFlow = remember { MutableStateFlow(0) }
    val invalidationToken by invalidationFlow.collectAsState()
    val indicatorColor = AppDaySelectionColor()
    // New indicator for every launch, needed when a day is selected and we are
    // coming back from other screens
    var launchId by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { ++launchId }
    val painter = remember(indicatorColor, launchId) {
        SelectionIndicatorPainter(400, 200, indicatorColor) {
            ++invalidationFlow.value
        }
    }
    if (isHighlighted) lastSelectedDay = selectedDay else painter.clearSelection()
    if (isHighlighted && selectedDay - monthStartJdn in monthRange) {
        painter.startSelection()
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(Modifier.size(size)) {
        invalidationToken.run {}
        val index = lastSelectedDay - monthStartJdn
        if (index !in monthRange) return@Canvas
        val cellIndex = index + startingDayOfWeek
        val row = cellIndex / 7 + 1 // +1 for weekday names initials row
        val column = cellIndex % 7 + if (isShowWeekOfYearEnabled) 1 else 0
        drawIntoCanvas {
            painter.draw(
                canvas = it,
                left = if (isRtl) widthPixels - (column + 1) * cellWidthPx else column * cellWidthPx,
                top = row * cellHeightPx,
                width = cellWidthPx,
                height = cellHeightPx,
                halfDp = oneDpInPx / 2,
            )
        }
    }
}

private class SelectionIndicatorPainter(
    transitionAnimTime: Int,
    hideAnimTime: Int,
    color: Color,
    invalidate: () -> Unit,
) {
    private var isCurrentlySelected = false
    private var currentX = 0f
    private var currentY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var lastRadius = 0f
    private var isReveal = false
    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = transitionAnimTime.toLong()
        it.interpolator = LinearInterpolator()
        it.addUpdateListener { invalidate() }
        it.doOnEnd { isReveal = false }
    }
    private val hideAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = hideAnimTime.toLong()
        it.addUpdateListener { invalidate() }
    }
    private val paint = Paint().also {
        it.style = PaintingStyle.Fill
        it.color = color
    }
    private val transitionInterpolators = listOf(1f, 1.25f).map(::OvershootInterpolator)
    private val revealInterpolator = OvershootInterpolator(1.5f)

    fun clearSelection() {
        if (isCurrentlySelected) {
            isReveal = false
            isCurrentlySelected = false
            hideAnimator.start()
        }
    }

    fun startSelection() {
        isReveal = !isCurrentlySelected
        isCurrentlySelected = true
        currentX = lastX
        currentY = lastY
        transitionAnimator.start()
    }

    fun draw(canvas: Canvas, left: Float, top: Float, width: Float, height: Float, halfDp: Float) {
        if (hideAnimator.isRunning) canvas.drawCircle(
            Offset(left + width / 2f, top + height / 2f),
            lastRadius * (1 - hideAnimator.animatedFraction),
            paint
        ) else if (isReveal) {
            val fraction = revealInterpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = left
            lastY = top
            lastRadius = (DayPainter.radius(width, height) - halfDp) * fraction
            canvas.drawCircle(
                Offset(lastX + width / 2f, lastY + height / 2f),
                (DayPainter.radius(width, height) - halfDp) * fraction,
                paint
            )
        } else if (isCurrentlySelected) transitionInterpolators.forEach { interpolator ->
            val fraction = interpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = lerp(currentX, left, fraction)
            lastY = lerp(currentY, top, fraction)
            lastRadius = (DayPainter.radius(width, height) - halfDp)
            canvas.drawCircle(
                Offset(lastX + width / 2f, lastY + height / 2f), lastRadius, paint
            )
        }
    }
}
