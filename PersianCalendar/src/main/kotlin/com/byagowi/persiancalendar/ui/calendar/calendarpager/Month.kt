package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
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
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Month(
    viewModel: CalendarViewModel,
    offset: Int,
    width: Dp,
    height: Dp,
    addEvent: () -> Unit,
    monthColors: MonthColors,
) {
    val today by viewModel.today.collectAsState()
    val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(today, offset)
    val monthStartJdn = Jdn(monthStartDate)

    val isShowWeekOfYearEnabled = isShowWeekOfYearEnabled

    val startingDayOfWeek = applyWeekStartOffsetToWeekDay(monthStartJdn.dayOfWeek)
    val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
    val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
    val weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)

    val isHighlighted by viewModel.isHighlighted.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    SelectionIndicator(
        isHighlighted = isHighlighted,
        selectedDay = selectedDay,
        monthStartJdn = monthStartJdn,
        monthLength = monthLength,
        startingDayOfWeek = startingDayOfWeek,
        isShowWeekOfYearEnabled = isShowWeekOfYearEnabled,
        indicatorColor = monthColors.indicator,
    )

    val widthPx = with(LocalDensity.current) { width.toPx() }
    val heightPx = with(LocalDensity.current) { height.toPx() }
    val columnsCount = if (isShowWeekOfYearEnabled) 8 else 7
    val rowsCount = 7
    val cellWidthPx = widthPx / columnsCount
    val cellHeightPx = heightPx / rowsCount

    val refreshToken by viewModel.refreshToken.collectAsState()
    val context = LocalContext.current
    val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
    val monthDeviceEvents = remember(refreshToken, isShowDeviceCalendarEvents) {
        if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
        else EventsStore.empty()
    }

    val diameter = min(width / columnsCount, height / rowsCount)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val dayPainter = remember(width, height, refreshToken, monthColors) {
        DayPainter(
            resources = context.resources,
            width = cellWidthPx,
            height = cellHeightPx,
            isRtl = isRtl,
            colors = monthColors
        )
    }
    val textMeasurer = rememberTextMeasurer()
    val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
    val daysTextSize = diameter * (if (mainCalendarDigitsIsArabic) 18 else 25) / 40
    val daysStyle = LocalTextStyle.current.copy(
        fontSize = with(LocalDensity.current) { daysTextSize.toSp() },
    )
    val contentColor = LocalContentColor.current

    // Slight fix for the particular font we use for native digits in Persian and so
    val dayOffsetY = if (mainCalendarDigits === Language.ARABIC_DIGITS) 0f else min(
        cellWidthPx, cellHeightPx
    ) * 1 / 40

    FixedSizeHorizontalGrid(columnsCount, rowsCount) {
        if (isShowWeekOfYearEnabled) Spacer(Modifier)
        (0..<7).forEach { column ->
            Box(contentAlignment = Alignment.Center) {
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
        val daysInteractionSource = remember { MutableInteractionSource() }
        repeat(monthLength) { dayOffset ->
            if (isShowWeekOfYearEnabled && (dayOffset == 0 || (dayOffset + startingDayOfWeek) % 7 == 0)) {
                Box(contentAlignment = Alignment.Center) {
                    val weekNumber =
                        formatNumber(weekOfYearStart + (dayOffset + startingDayOfWeek) / 7)
                    val description = stringResource(R.string.nth_week_of_year, weekNumber)
                    Text(
                        weekNumber,
                        fontSize = with(LocalDensity.current) { (daysTextSize * .625f).toSp() },
                        modifier = Modifier
                            .alpha(AppBlendAlpha)
                            .semantics { this.contentDescription = description },
                    )
                }
            }
            if (dayOffset == 0) repeat(startingDayOfWeek) { Spacer(Modifier) }
            val day = monthStartJdn + dayOffset
            val isToday = day == today
            Canvas(
                Modifier.combinedClickable(
                    indication = null,
                    interactionSource = daysInteractionSource,
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
                )
            ) {
                val events = eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()
                val hasEvents = events.any { it !is CalendarEvent.DeviceCalendarEvent }
                val hasAppointments = events.any { it is CalendarEvent.DeviceCalendarEvent }
                val shiftWorkTitle = getShiftWorkTitle(day, true)
                val isSelected = isHighlighted && selectedDay == day
                dayPainter.setDayOfMonthItem(
                    isToday = false,
                    isSelected = isSelected,
                    hasEvent = hasEvents,
                    hasAppointment = hasAppointments,
                    isHoliday = false,
                    jdn = day,
                    dayOfMonth = "",
                    header = shiftWorkTitle,
                )
                drawIntoCanvas { dayPainter.drawDay(it.nativeCanvas) }
                if (isToday) drawCircle(
                    monthColors.currentDay,
                    radius = this.size.minDimension / 2 - .5.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
                val textLayoutResult = textMeasurer.measure(
                    text = formatNumber(dayOffset + 1, mainCalendarDigits),
                    style = daysStyle,
                )
                val isHoliday = events.any { it.isHoliday }
                drawText(
                    textLayoutResult,
                    color = when {
                        isSelected -> monthColors.textDaySelected
                        isHoliday || day.isWeekEnd() -> monthColors.holidays
                        else -> contentColor
                    },
                    topLeft = Offset(
                        x = center.x - textLayoutResult.size.width / 2,
                        y = center.y - textLayoutResult.size.height / 2 + dayOffsetY,
                    ),
                )
            }
        }
    }
}
