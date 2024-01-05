package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.lerp
import androidx.core.animation.doOnEnd
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.ui.calendar.AddEvent
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.theme.AppDayPainterColors
import com.byagowi.persiancalendar.ui.theme.AppDaySelectionColor
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Month(
    viewModel: CalendarViewModel,
    offset: Int,
    isCurrentSelection: Boolean,
    width: Dp,
    height: Dp,
) {
    val today by viewModel.today.collectAsState()
    val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(today, offset)
    val monthStartJdn = Jdn(monthStartDate)

    // Why the moving circle feels faster this way
    val invalidationFlow = remember { MutableStateFlow(0) }
    if (isCurrentSelection) ++invalidationFlow.value
    val invalidationToken by invalidationFlow.collectAsState()
    val indicatorColor = AppDaySelectionColor()
    // New indicator for every launch, needed when a day is selected and we are
    // coming back from other screens
    var launchId by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { ++launchId }
    val selectionIndicator = remember(indicatorColor, launchId) {
        SelectionIndicator(400, 200, indicatorColor) {
            ++invalidationFlow.value
        }
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
    val context = LocalContext.current
    val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
    val monthDeviceEvents = remember(refreshToken, isShowDeviceCalendarEvents) {
        if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
        else EventsStore.empty()
    }

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
    val diameter = min(cellSize.height, cellSize.width)
    val dayPainterColors = AppDayPainterColors()
    val dayPainter = remember(height, width, refreshToken, dayPainterColors) {
        DayPainter(context.resources, cellPixelsWidth, cellPixelsHeight, isRtl, dayPainterColors)
    }
    val oneDpInPx = with(LocalDensity.current) { 1.dp.toPx() }
    val textMeasurer = rememberTextMeasurer()
    val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
    val daysTextSize = diameter * (if (mainCalendarDigitsIsArabic) 18 else 25) / 40
    val daysStyle = LocalTextStyle.current.copy(
        fontSize = with(LocalDensity.current) { daysTextSize.toSp() },
    )
    val contentColor = LocalContentColor.current

    // Slight fix for the particular font we use for native digits in Persian and so
    val dayOffsetY = if (mainCalendarDigits === Language.ARABIC_DIGITS) 0f else min(
        cellPixelsWidth, cellPixelsHeight
    ) * 1 / 40

    Column(
        Modifier.drawWithCache {
            onDrawBehind {
                drawIntoCanvas {
                    invalidationToken.run {}
                    val index = lastSelectedDay - monthStartJdn
                    if (index !in monthRange) return@drawIntoCanvas
                    val cellIndex = index + applyWeekStartOffsetToWeekDay(startingDayOfWeek)
                    val row = cellIndex / 7 + 1 // +1 for weekday names initials row
                    val column = cellIndex % 7 + if (isShowWeekOfYearEnabled) 1 else 0
                    selectionIndicator.draw(
                        canvas = it,
                        left = if (isRtl) widthPixels - (column + 1) * cellPixelsWidth
                        else column * cellPixelsWidth,
                        top = row * cellPixelsHeight,
                        width = cellPixelsWidth,
                        height = cellPixelsHeight,
                        halfDp = oneDpInPx / 2,
                    )
                }
            }
        },
    ) {
        Row(Modifier.height(cellSize.height)) {
            if (isShowWeekOfYearEnabled) Spacer(Modifier.width(cellSize.width))
            (0..<7).forEach { column ->
                Box(Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                    val weekDayPosition = revertWeekStartOffsetFromWeekDay(column)
                    val description = stringResource(
                        R.string.week_days_name_column, getWeekDayName(weekDayPosition)
                    )
                    Text(
                        getInitialOfWeekDay(weekDayPosition),
                        fontSize = with(LocalDensity.current) { (diameter * .5f).toSp() },
                        modifier = Modifier
                            .alpha(AppBlendAlpha)
                            .semantics { this.contentDescription = description },
                    )
                }
            }
        }
        (0..<6).forEach { row ->
            Row(Modifier.height(cellSize.height)) {
                if (row >= weeksCount) return@Row
                if (isShowWeekOfYearEnabled) {
                    Box(Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                        val weekNumber = formatNumber(weekOfYearStart + row)
                        val description = stringResource(R.string.nth_week_of_year, weekNumber)
                        Text(
                            weekNumber,
                            fontSize = with(LocalDensity.current) { (diameter * .35f).toSp() },
                            modifier = Modifier
                                .alpha(AppBlendAlpha)
                                .semantics { this.contentDescription = description },
                        )
                    }
                }
                (0..<7).forEach RowForEach@{ column ->
                    val dayOffset =
                        (column + row * 7) - applyWeekStartOffsetToWeekDay(startingDayOfWeek)
                    val day = monthStartJdn + dayOffset
                    if (dayOffset !in monthRange) return@RowForEach Spacer(Modifier.width(cellSize.width))
                    val isToday = day == today
                    Canvas(
                        modifier = Modifier
                            .size(cellSize)
                            .combinedClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { viewModel.changeSelectedDay(day) },
                                onClickLabel = if (isTalkBackEnabled) getA11yDaySummary(
                                    context.resources,
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
                                },
                            ),
                    ) {
                        val events =
                            eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()
                        val hasEvents = events.any { it !is CalendarEvent.DeviceCalendarEvent }
                        val hasAppointments = events.any { it is CalendarEvent.DeviceCalendarEvent }
                        val shiftWorkTitle = getShiftWorkTitle(day, true)
                        val isSelected = isHighlighted && selectedDay == day
                        dayPainter.setDayOfMonthItem(
                            false,
                            isSelected,
                            hasEvents,
                            hasAppointments,
                            false,
                            day,
                            "",
                            shiftWorkTitle,
                        )
                        drawIntoCanvas {
                            if (isToday) drawCircle(
                                Color(dayPainterColors.colorCurrentDay),
                                radius = size.minDimension / 2 - oneDpInPx / 2,
                                style = Stroke(width = oneDpInPx)
                            )
                            val textLayoutResult = textMeasurer.measure(
                                text = formatNumber(dayOffset + 1, mainCalendarDigits),
                                style = daysStyle,
                            )
                            val isHoliday = events.any { it.isHoliday }
                            drawText(
                                textLayoutResult,
                                color = when {
                                    isSelected -> Color(dayPainterColors.colorTextDaySelected)
                                    isHoliday || day.isWeekEnd() -> Color(dayPainterColors.colorHolidays)
                                    else -> contentColor
                                },
                                topLeft = Offset(
                                    x = center.x - textLayoutResult.size.width / 2,
                                    y = center.y - textLayoutResult.size.height / 2 + dayOffsetY,
                                ),
                            )
                            dayPainter.drawDay(it.nativeCanvas)
                        }
                    }
                }
            }
        }
    }
}

private class SelectionIndicator(
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
