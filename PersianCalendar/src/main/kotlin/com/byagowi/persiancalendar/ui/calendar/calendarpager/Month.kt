package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.AddEvent
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.lerp
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Month(
    viewModel: CalendarViewModel,
    offset: Int,
    isCurrentSelection: Boolean,
    width: Dp,
    height: Dp,
) {
    val todayJdn = remember { Jdn.today() }
    val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(todayJdn, offset)
    val monthStartJdn = Jdn(monthStartDate)

    // Why the moving circle feels faster this way
    val invalidationFlow = remember { MutableStateFlow(0) }
    if (isCurrentSelection) ++invalidationFlow.value
    val invalidationToken by invalidationFlow.collectAsState()
    val context = LocalContext.current
    val selectionIndicator = remember {
        SelectionIndicator(context) { ++invalidationFlow.value }
    }

    val columnsCount = if (isShowWeekOfYearEnabled) 8 else 7
    val rowsCount = 7

    val startingDayOfWeek = monthStartJdn.dayOfWeek
    val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
    val monthRange = 0..<monthLength
    val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
    val weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)
    val weeksCount =
        (monthStartJdn + monthLength - 1).getWeekOfYear(startOfYearJdn) - weekOfYearStart + 1

    val refreshToken by viewModel.refreshToken.collectAsState()
    val monthDeviceEvents = remember(refreshToken) {
        if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
        else EventsStore.empty()
    }
    val dayPositions = remember(monthStartJdn) { mutableMapOf<Int, Pair<Int, Int>>() }

    val selectedDay by viewModel.selectedDay.collectAsState()
    var lastSelectedDay by remember { mutableStateOf(selectedDay) }
    val isHighlighted by viewModel.isHighlighted.collectAsState()
    if (isHighlighted) lastSelectedDay = selectedDay else selectionIndicator.clearSelection()
    if (isHighlighted && selectedDay - monthStartJdn in monthRange && isCurrentSelection) {
        selectionIndicator.startSelection()
    }

    val addEvent = AddEvent(viewModel)

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val cellSize = DpSize(width / columnsCount, height / rowsCount)
    val widthPixels = with(LocalDensity.current) { width.toPx() }
    val heightPixels = with(LocalDensity.current) { height.toPx() }
    val cellPixelsWidth = widthPixels / columnsCount
    val cellPixelsHeight = heightPixels / rowsCount
    val dayPainter = remember(height, width, refreshToken) {
        DayPainter(context, cellPixelsWidth, cellPixelsHeight, isRtl)
    }
    val oneDp = with(LocalDensity.current) { 1.dp.toPx() }

    Column(
        Modifier.drawWithCache {
            onDrawBehind {
                drawIntoCanvas {
                    invalidationToken.run {}
                    val index = lastSelectedDay - monthStartJdn
                    if (index !in monthRange) return@drawIntoCanvas
                    val (column, row) = dayPositions[index] ?: return@drawIntoCanvas
                    selectionIndicator.draw(
                        canvas = it,
                        left = if (isRtl) widthPixels - (column + 1) * cellPixelsWidth
                        else column * cellPixelsWidth,
                        top = row * cellPixelsHeight,
                        width = cellPixelsWidth,
                        height = cellPixelsHeight,
                        oneDp = oneDp,
                    )
                }
            }
        },
    ) {
        Row(Modifier.height(cellSize.height)) {
            if (isShowWeekOfYearEnabled) Spacer(Modifier.width(cellSize.width))
            (0..<7).forEach { column ->
                val weekDayPosition = revertWeekStartOffsetFromWeekDay(column)
                val description = stringResource(
                    R.string.week_days_name_column, getWeekDayName(weekDayPosition)
                )
                Cell(Modifier
                    .semantics { this.contentDescription = description }
                    .size(cellSize),
                    dayPainter) { it.setInitialOfWeekDay(getInitialOfWeekDay(weekDayPosition)) }
            }
        }
        (0..<6).forEach { row ->
            Row(Modifier.height(cellSize.height)) {
                if (isShowWeekOfYearEnabled && row < weeksCount) {
                    val weekNumber = formatNumber(weekOfYearStart + row - 1)
                    val description = stringResource(R.string.nth_week_of_year, weekNumber)
                    Cell(
                        Modifier
                            .semantics { this.contentDescription = description }
                            .size(cellSize),
                        dayPainter,
                    ) { it.setWeekNumber(weekNumber) }
                }
                (0..<7).forEach RowForEach@{ column ->
                    val dayOffset =
                        (column + row * 7) - applyWeekStartOffsetToWeekDay(startingDayOfWeek)
                    val day = monthStartJdn + dayOffset
                    if (dayOffset !in monthRange) return@RowForEach Spacer(Modifier.width(cellSize.width))
                    val isToday = day == todayJdn
                    Cell(
                        Modifier
                            .size(cellSize)
                            .combinedClickable(indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { viewModel.changeSelectedDay(day) },
                                onClickLabel = if (isTalkBackEnabled) getA11yDaySummary(
                                    context,
                                    day,
                                    isToday,
                                    EventsStore.empty(),
                                    withZodiac = isToday,
                                    withOtherCalendars = false,
                                    withTitle = true
                                ) else (dayOffset + 1).toString(),
                                onLongClickLabel = stringResource(R.string.add_event),
                                onLongClick = {
                                    viewModel.changeSelectedDay(day)
                                    addEvent()
                                }),
                        dayPainter,
                    ) {
                        dayPositions[dayOffset] =
                            (column + if (isShowWeekOfYearEnabled) 1 else 0) to row + 1
                        val events =
                            eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()
                        it.setDayOfMonthItem(
                            isToday,
                            isHighlighted && selectedDay == day,
                            events.any { it !is CalendarEvent.DeviceCalendarEvent },
                            events.any { it is CalendarEvent.DeviceCalendarEvent },
                            events.any { it.isHoliday },
                            day,
                            dayOffset + 1,
                            getShiftWorkTitle(day, true)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(
    modifier: Modifier = Modifier,
    dayPainter: DayPainter,
    update: (DayPainter) -> Unit,
) {
    Box(
        Modifier
            .drawWithCache {
                onDrawWithContent {
                    drawIntoCanvas {
                        update(dayPainter)
                        dayPainter.drawDay(it.nativeCanvas)
                    }
                }
            }
            .then(modifier),
    )
}

private class SelectionIndicator(context: Context, invalidate: () -> Unit) {
    private var isCurrentlySelected = false
    private var currentX = 0f
    private var currentY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var lastRadius = 0f
    private var isReveal = false
    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = LinearInterpolator()
        it.addUpdateListener { invalidate() }
        it.doOnEnd { isReveal = false }
    }
    private val hideAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        it.addUpdateListener { invalidate() }
    }
    private val paint = Paint().also {
        it.style = PaintingStyle.Fill
        it.color = Color(context.resolveColor(R.attr.colorSelectedDay))
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

    fun draw(canvas: Canvas, left: Float, top: Float, width: Float, height: Float, oneDp: Float) {
        if (hideAnimator.isRunning) canvas.drawCircle(
            Offset(left + width / 2f, top + height / 2f),
            lastRadius * (1 - hideAnimator.animatedFraction),
            paint
        ) else if (isReveal) {
            val fraction = revealInterpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = left
            lastY = top
            lastRadius = (DayPainter.radius(width, height) - oneDp) * fraction
            canvas.drawCircle(
                Offset(lastX + width / 2f, lastY + height / 2f),
                (DayPainter.radius(width, height) -oneDp) * fraction,
                paint
            )
        } else if (isCurrentlySelected) transitionInterpolators.forEach { interpolator ->
            val fraction = interpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = lerp(currentX, left, fraction)
            lastY = lerp(currentY, top, fraction)
            lastRadius = (DayPainter.radius(width, height) - oneDp)
            canvas.drawCircle(
                Offset(lastX + width / 2f, lastY + height / 2f), lastRadius, paint
            )
        }
    }
}
