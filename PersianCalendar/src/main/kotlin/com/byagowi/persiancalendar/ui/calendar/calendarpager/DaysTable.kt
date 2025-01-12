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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Fill
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
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DaysTable(
    suggestedPagerSize: DpSize,
    addEvent: (AddEventData) -> Unit,
    monthColors: MonthColors,
    animatedContentScope: AnimatedContentScope,
    today: Jdn,
    isHighlighted: Boolean,
    selectedDay: Jdn,
    refreshToken: Int,
    setSelectedDay: (Jdn) -> Unit,
    language: Language,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int,
    isVazirEnabled: Boolean,
    isShowWeekOfYearEnabled: Boolean,
    deviceEvents: DeviceCalendarEventsStore,
    monthStartDate: AbstractDate,
    monthStartJdn: Jdn,
    modifier: Modifier = Modifier,
    onWeekClick: ((Jdn, Boolean) -> Unit)? = null,
    onlyWeek: Int? = null,
) {
    val previousMonthLength =
        if (onlyWeek == null) null else (monthStartJdn - 1).inCalendar(mainCalendar).dayOfMonth

    val startingWeekDay = applyWeekStartOffsetToWeekDay(monthStartJdn.weekDay)
    val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
    val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
    val monthStartWeekOfYear = monthStartJdn.getWeekOfYear(startOfYearJdn)

    val density = LocalDensity.current
    val (width, suggestedHeight) = suggestedPagerSize
    val cellWidth = (width - (pagerArrowSizeAndPadding * 2).dp) / 7
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val cellHeight = suggestedHeight / if (onlyWeek != null) 2 else 7
    val cellHeightPx = with(density) { cellHeight.toPx() }
    val cellRadius = min(cellWidthPx, cellHeightPx) / 2 - with(density) { .5f.dp.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val pagerArrowSizeAndPaddingPx = with(density) { pagerArrowSizeAndPadding.dp.toPx() }

    val daysRowsCount = ceil((monthLength + startingWeekDay) / 7f).toInt()

    Box(
        modifier.height(
            if (onlyWeek != null) suggestedHeight + 8.dp
            else (cellHeight * (daysRowsCount + 1) + 12.dp)
        )
    ) {
        run {
            val highlightedDayOfMonth = selectedDay - monthStartJdn
            val center = if (isHighlighted && highlightedDayOfMonth in 0..<monthLength) {
                val cellIndex = selectedDay - monthStartJdn + startingWeekDay
                Offset(
                    x = cellWidthPx * (cellIndex % 7).let {
                        .5f + if (isRtl) 6 - it else it
                    } + pagerArrowSizeAndPaddingPx,
                    // +1 for weekday names initials row, .5f for center of the circle
                    y = cellHeightPx * (1.5f + if (onlyWeek == null) cellIndex / 7 else 0),
                )
            } else null
            // Invalidate the indicator state on table size changes
            key(width, suggestedHeight) {
                SelectionIndicator(monthColors.indicator, cellRadius, center)
            }
        }

        val context = LocalContext.current
        val diameter = min(cellWidth, cellHeight)
        val dayPainter = remember(cellWidthPx, suggestedHeight, refreshToken, monthColors) {
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
        val daysTextSize =
            diameter * (if (mainCalendarDigitsIsArabic || isVazirEnabled) 18 else 25) / 40
        val daysStyle = LocalTextStyle.current.copy(
            fontSize = with(density) { daysTextSize.toSp() },
        )
        val contentColor = LocalContentColor.current

        // Slight fix for the particular font we use for native digits in Persian and so
        val dayOffsetY = if (mainCalendarDigits === Language.ARABIC_DIGITS) 0f else min(
            cellWidthPx, cellHeightPx
        ) * 1 / 40

        val cellsSizeModifier = Modifier.size(cellWidth, cellHeight)

        val arrowOffsetY =
            (cellHeight + (if (language.isArabicScript) 4 else 0).dp - MaterialIconDimension.dp) / 2
        PagerArrow(arrowOffsetY, coroutineScope, pagerState, page, width, true, onlyWeek)

        repeat(7) { column ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = cellsSizeModifier.offset(
                    pagerArrowSizeAndPadding.dp + cellWidth * column,
                    0.dp
                )
            ) {
                val weekDayPosition = revertWeekStartOffsetFromWeekDay(column)
                val description = stringResource(
                    R.string.week_days_name_column, getWeekDayName(weekDayPosition)
                )
                Text(
                    getInitialOfWeekDay(weekDayPosition),
                    fontSize = with(density) { (diameter * .5f).toSp() },
                    modifier = Modifier
                        .alpha(AppBlendAlpha)
                        .semantics { this.contentDescription = description },
                )
            }
        }

        repeat(daysRowsCount * 7) { dayOffset ->
            if (onlyWeek != null && monthStartWeekOfYear + dayOffset / 7 != onlyWeek) return@repeat
            val row = if (onlyWeek == null) dayOffset / 7 else 0
            val day = monthStartJdn + dayOffset - startingWeekDay
            val isToday = day == today
            val isBeforeMonth = dayOffset < startingWeekDay
            val isAfterMonth = dayOffset + 1 > startingWeekDay + monthLength
            val column = dayOffset % 7
            if (column == 0) AnimatedVisibility(
                isShowWeekOfYearEnabled,
                modifier = Modifier
                    .offset(
                        (16 - 4).dp, cellHeight * (1 + if (onlyWeek == null) row else 0)
                    )
                    .size((24 + 8).dp, cellHeight),
                label = "week number",
            ) {
                val weekNumber = onlyWeek ?: (monthStartWeekOfYear + row)
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
                            onWeekClick(when {
                                selectedDay - day in 0..<7 -> selectedDay
                                onlyWeek != null -> day
                                row == 0 -> monthStartJdn
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
                        fontSize = with(density) { (daysTextSize * .625f).toSp() },
                        modifier = Modifier
                            .alpha(AppBlendAlpha)
                            .semantics { this.contentDescription = description },
                    )
                }
            }
            if (previousMonthLength != null || (!isBeforeMonth && !isAfterMonth)) Canvas(
                modifier = cellsSizeModifier
                    .offset(
                        pagerArrowSizeAndPadding.dp + cellWidth * column, cellHeight * (row + 1)
                    )
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
                val events = eventsRepository?.getEvents(day, deviceEvents) ?: emptyList()
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
                    secondaryCalendar = secondaryCalendar,
                )
                drawIntoCanvas { dayPainter.drawDay(it.nativeCanvas) }
                if (isToday) drawCircle(
                    monthColors.currentDay,
                    radius = cellRadius,
                    style = Stroke(width = (if (isHighTextContrastEnabled) 4 else 2).dp.toPx()),
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
                if (isHighTextContrastEnabled && (isHoliday || day.isWeekEnd)) drawCircle(
                    monthColors.holidays.copy(alpha = .25f), radius = cellRadius, style = Fill,
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

        PagerArrow(arrowOffsetY, coroutineScope, pagerState, page, width, false, onlyWeek)
    }
}

private const val pagerArrowSize = MaterialIconDimension + 8 * 2
const val pagerArrowSizeAndPadding = pagerArrowSize + 4

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PagerArrow(
    arrowOffsetY: Dp,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int,
    screenWidth: Dp,
    isPrevious: Boolean,
    week: Int?,
) {
    val stringId = if (isPrevious) R.string.previous_x else R.string.next_x
    Icon(
        if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = if (week == null) {
            stringResource(stringId, stringResource(R.string.month))
        } else stringResource(R.string.nth_week_of_year, week + if (isPrevious) -1 else 1),
        modifier = Modifier
            .offset(
                if (isPrevious) 16.dp else (screenWidth - pagerArrowSize.dp), arrowOffsetY
            )
            .then(if (week == null) Modifier.combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = null,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page + 1 * if (isPrevious) -1 else 1)
                    }
                },
                onClickLabel = stringResource(R.string.select_month),
                onLongClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(page + 12 * if (isPrevious) -1 else 1)
                    }
                },
                onLongClickLabel = stringResource(stringId, stringResource(R.string.year)),
            ) else Modifier.clickable(
                indication = ripple(bounded = false),
                interactionSource = null,
            ) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page + 1 * if (isPrevious) -1 else 1)
                }
            })
            .alpha(.9f),
    )
}
