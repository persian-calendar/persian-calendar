package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_JDN
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_WEEK_NUMBER
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import kotlin.math.ceil
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.Month(
    offset: Int,
    width: Dp,
    height: Dp,
    addEvent: (AddEventData) -> Unit,
    monthColors: MonthColors,
    animatedContentScope: AnimatedContentScope,
    today: Jdn,
    isHighlighted: Boolean,
    selectedDay: Jdn,
    refreshToken: Int,
    setSelectedDay: (Jdn) -> Unit,
    onWeekClick: ((Jdn, Boolean) -> Unit)? = null,
    onlyWeek: Int? = null,
) {
    val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(today, offset)
    val monthStartJdn = Jdn(monthStartDate)
    val previousMonthLength =
        if (onlyWeek == null) null else (monthStartJdn - 1).inCalendar(mainCalendar).dayOfMonth

    val startingWeekDay = applyWeekStartOffsetToWeekDay(monthStartJdn.weekDay)
    val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
    val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
    val monthStartWeekOfYear = monthStartJdn.getWeekOfYear(startOfYearJdn)

    val columnsCount = 7
    val rowsCount = if (onlyWeek != null) 2 else 7

    val widthPx = with(LocalDensity.current) { width.toPx() }
    val heightPx = with(LocalDensity.current) { height.toPx() }
    val cellWidthPx = widthPx / columnsCount
    val cellHeight = height / rowsCount
    val cellHeightPx = heightPx / rowsCount
    val cellRadius =
        min(cellWidthPx, cellHeightPx) / 2 - with(LocalDensity.current) { .5.dp.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    run {
        val cellIndex = selectedDay - monthStartJdn + startingWeekDay
        val highlightedDayOfMonth = selectedDay - monthStartJdn
        val center = if (isHighlighted && highlightedDayOfMonth in 0..<monthLength) Offset(
            x = (cellIndex % 7).let {
                if (isRtl) widthPx - (it + 1) * cellWidthPx else it * cellWidthPx
            } + cellWidthPx / 2f,
            // +1 for weekday names initials row
            y = ((if (onlyWeek != null) 0 else (cellIndex / 7)) + 1.5f) * cellHeightPx,
        ) else null
        // Invalidate the indicator state on table size changes
        key(width, height) {
            SelectionIndicator(color = monthColors.indicator, radius = cellRadius, center = center)
        }
    }

    val context = LocalContext.current
    val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
    val monthDeviceEvents = remember(refreshToken, isShowDeviceCalendarEvents) {
        if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
        else EventsStore.empty()
    }

    val diameter = min(width / columnsCount, height / rowsCount)
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
    val isVazirEnabled by isVazirEnabled.collectAsState()
    val daysTextSize =
        diameter * (if (mainCalendarDigitsIsArabic || isVazirEnabled) 18 else 25) / 40
    val daysStyle = LocalTextStyle.current.copy(
        fontSize = with(LocalDensity.current) { daysTextSize.toSp() },
    )
    val contentColor = LocalContentColor.current

    // Slight fix for the particular font we use for native digits in Persian and so
    val dayOffsetY = if (mainCalendarDigits === Language.ARABIC_DIGITS) 0f else min(
        cellWidthPx, cellHeightPx
    ) * 1 / 40

    val daysRowsCount = ceil((monthLength + startingWeekDay) / 7f).toInt()

    val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
    (0..<daysRowsCount).forEach { row ->
        val weekNumber = monthStartWeekOfYear + row
        AnimatedVisibility(
            isShowWeekOfYearEnabled && (onlyWeek == null || onlyWeek == weekNumber),
            modifier = Modifier
                .offset(
                    -pagerArrowSizeAndPadding.dp * .625f,
                    cellHeight * (1 + if (onlyWeek == null) row else 0)
                )
                .size(DpSize(pagerArrowSizeAndPadding.dp * .625f, cellHeight)),
            label = "week number",
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .sharedBounds(
                        rememberSharedContentState(
                            "$SHARED_CONTENT_KEY_WEEK_NUMBER$monthStartJdn-$weekNumber"
                        ),
                        animatedVisibilityScope = animatedContentScope,
                    )
                    .then(if (onWeekClick != null) Modifier.clickable(
                        onClickLabel = stringResource(R.string.week_view),
                        indication = ripple(bounded = false),
                        interactionSource = null,
                    ) {
                        val day = if (row == 0) monthStartJdn
                        else monthStartJdn - applyWeekStartOffsetToWeekDay(monthStartJdn.weekDay) + row * 7
                        onWeekClick(when {
                            selectedDay - day in 0..<7 -> selectedDay
                            row == 0 -> day
                            // Select first non weekend day of the week
                            else -> day + ((0..6).firstOrNull { !(day + it).isWeekEnd } ?: 0)
                        }, true)
                    } else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                val formattedWeekNumber = formatNumber(weekNumber)
                val description = stringResource(R.string.nth_week_of_year, formattedWeekNumber)
                Text(
                    formattedWeekNumber,
                    fontSize = with(LocalDensity.current) { (daysTextSize * .625f).toSp() },
                    modifier = Modifier
                        .alpha(AppBlendAlpha)
                        .semantics { this.contentDescription = description },
                )
            }
        }
    }

    FixedSizeHorizontalGrid(
        columnsCount = columnsCount,
        rowsCount = rowsCount,
        cellHeight = cellHeightPx,
        modifier = Modifier.height(height / 7 * if (onlyWeek != null) 2 else (daysRowsCount + 1)),
    ) {
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
        repeat(daysRowsCount * 7) { dayOffset ->
            if (onlyWeek != null && monthStartWeekOfYear + dayOffset / 7 != onlyWeek) return@repeat
            val day = monthStartJdn + dayOffset - startingWeekDay
            val isToday = day == today
            val isBeforeMonth = dayOffset < startingWeekDay
            val isAfterMonth = dayOffset + 1 > startingWeekDay + monthLength
            if (previousMonthLength == null && (isBeforeMonth || isAfterMonth)) {
                Spacer(Modifier)
            } else Canvas(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(SHARED_CONTENT_KEY_JDN + day.value),
                        animatedVisibilityScope = animatedContentScope,
                    )
                    .combinedClickable(
                        indication = null,
                        interactionSource = null,
                        onClick = { setSelectedDay(day) },
                        onClickLabel = stringResource(R.string.select_day),
                        onLongClickLabel = stringResource(R.string.add_event),
                        onLongClick = {
                            setSelectedDay(day)
                            addEvent(AddEventData.fromJdn(day))
                        },
                    )
                    .semantics {
                        this.contentDescription = if (isTalkBackEnabled) getA11yDaySummary(
                            context.resources,
                            day,
                            isToday,
                            EventsStore.empty(),
                            withZodiac = isToday,
                            withOtherCalendars = false,
                            withTitle = true,
                            withWeekOfYear = false,
                        ) else (dayOffset + 1 - startingWeekDay).toString()
                    }
                    .then(if (isBeforeMonth || isAfterMonth) Modifier.alpha(.5f) else Modifier),
            ) {
                val events = eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()
                val hasEvents = events.any { it !is CalendarEvent.DeviceCalendarEvent }
                val hasAppointments = events.any { it is CalendarEvent.DeviceCalendarEvent }
                val shiftWorkTitle = getShiftWorkTitle(day, true)
                val isSelected = isHighlighted && selectedDay == day
                val isHoliday = events.any { it.isHoliday }
                dayPainter.setDayOfMonthItem(
                    isToday = false,
                    isSelected = isSelected,
                    hasEvent = hasEvents,
                    hasAppointment = hasAppointments,
                    isHoliday = isHoliday,
                    jdn = day,
                    dayOfMonth = "",
                    header = shiftWorkTitle,
                )
                drawIntoCanvas { dayPainter.drawDay(it.nativeCanvas) }
                if (isToday) drawCircle(
                    monthColors.currentDay,
                    radius = cellRadius,
                    style = Stroke(width = 1.dp.toPx()),
                )
                val textLayoutResult = textMeasurer.measure(
                    text = formatNumber(
                        if (previousMonthLength != null && isBeforeMonth) {
                            previousMonthLength - (startingWeekDay - dayOffset) + 1
                        } else if (onlyWeek != null && isAfterMonth) {
                            dayOffset + 1 - monthLength - startingWeekDay
                        } else dayOffset + 1 - startingWeekDay,
                        mainCalendarDigits,
                    ),
                    style = daysStyle,
                )
                drawText(
                    textLayoutResult,
                    color = when {
                        isSelected -> monthColors.textDaySelected
                        isHoliday || day.isWeekEnd -> monthColors.holidays
                        else -> contentColor
                    },
                    topLeft = Offset(
                        x = this.center.x - textLayoutResult.size.width / 2,
                        y = this.center.y - textLayoutResult.size.height / 2 + dayOffsetY,
                    ),
                )
            }
        }
    }
}
