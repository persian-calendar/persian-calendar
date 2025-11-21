package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarNumeral
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.resolveFontFile
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min

// For performance reasons it has two phases, a initiation phase that should be called outside
// the pager which creates a callback that should be invoked inside the pager.
@Composable
fun daysTable(
    suggestedPagerSize: DpSize,
    addEvent: (AddEventData) -> Unit,
    today: Jdn,
    refreshToken: Int,
    setSelectedDay: (Jdn) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    secondaryCalendar: Calendar? = null,
    onWeekClick: ((Jdn, Boolean) -> Unit)? = null,
    isWeekMode: Boolean = false,
): @Composable (
    page: Int, monthStartDate: AbstractDate, monthStartJdn: Jdn,
    deviceEvents: DeviceCalendarEventsStore, onlyWeek: Int?,
    isHighlighted: Boolean, selectedDay: Jdn,
) -> Unit {
    val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
    val density = LocalDensity.current
    val (width, suggestedHeight) = suggestedPagerSize
    val cellWidth = (width - (pagerArrowSizeAndPadding * 2).dp) / 7
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val cellHeight = suggestedHeight / if (isWeekMode) 2 else 7
    val cellHeightPx = with(density) { cellHeight.toPx() }
    val cellRadius = min(cellWidthPx, cellHeightPx) / 2 - with(density) { .5f.dp.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val pagerArrowSizeAndPaddingPx = with(density) { pagerArrowSizeAndPadding.dp.toPx() }
    val fontFile = resolveFontFile()
    val isBoldFont by isBoldFont.collectAsState()
    val language by language.collectAsState()
    val monthColors = appMonthColors()
    val coroutineScope = rememberCoroutineScope()

    val resources = LocalResources.current
    val diameter = min(cellWidth, cellHeight)
    val context = LocalContext.current
    val dayPainter = remember(
        cellWidthPx, suggestedHeight, refreshToken, monthColors, resources, fontFile, isBoldFont,
    ) {
        DayPainter(
            context = context,
            resources = resources,
            width = cellWidthPx,
            height = cellHeightPx,
            isRtl = isRtl,
            colors = monthColors,
            fontFile = fontFile,
            isBoldFont = isBoldFont,
        )
    }
    val daysTextSize = diameter * when {
        mainCalendarNumeral.isTamil -> 16
        mainCalendarNumeral.isArabicIndicVariants && fontFile == null -> 25
        else -> 18
    } / 40
    val daysStyle = LocalTextStyle.current.copy(
        fontSize = with(density) { daysTextSize.toSp() },
    )
    val contentColor = LocalContentColor.current
    val cellsSizeModifier = Modifier.size(cellWidth, cellHeight)
    val isTalkBackEnabled by isTalkBackEnabled.collectAsState()
    val isHighTextContrastEnabled by isHighTextContrastEnabled.collectAsState()
    val todayIndicatorStroke = with(density) {
        Stroke(width = (if (isHighTextContrastEnabled || isBoldFont) 4 else 2).dp.toPx())
    }
    val numeral by numeral.collectAsState()
    var focusedDay by remember { mutableStateOf<Jdn?>(null) }
    val focusColor = LocalContentColor.current.copy(.1f)
    val weekStart by weekStart.collectAsState()
    val weekEnds by weekEnds.collectAsState()
    val eventsRepository by eventsRepository.collectAsState()

    return { page, monthStartDate, monthStartJdn, deviceEvents, onlyWeek, isHighlighted, selectedDay ->
        val previousMonthLength =
            if (onlyWeek == null) null else ((monthStartJdn - 1) on mainCalendar).dayOfMonth

        val startingWeekDay = monthStartJdn.weekDay - weekStart
        val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
        val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
        val monthStartWeekOfYear = monthStartJdn.getWeekOfYear(startOfYearJdn, weekStart)
        val daysRowsCount = ceil((monthLength + startingWeekDay) / 7f).toInt()

        Box(
            modifier
                .height(
                    if (onlyWeek != null) suggestedHeight + 8.dp
                    else (cellHeight * (daysRowsCount + 1) + 12.dp)
                )
                .semantics { this.isTraversalGroup = true },
        ) {
            val highlightedDayOfMonth = selectedDay - monthStartJdn
            val indicatorCenter = if (isHighlighted && highlightedDayOfMonth in 0..<monthLength) {
                val cellIndex = selectedDay - monthStartJdn + startingWeekDay
                Offset(
                    x = cellWidthPx * (cellIndex % 7).let {
                        .5f + if (isRtl) 6 - it else it
                    } + pagerArrowSizeAndPaddingPx,
                    // +1 for weekday names initials row, .5f for center of the circle
                    y = cellHeightPx * (1.5f + if (onlyWeek == null) cellIndex / 7 else 0),
                )
            } else null

            val animatedCenter = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
            val animatedRadius = remember { Animatable(if (indicatorCenter == null) 0f else 1f) }

            // Handles circle radius change animation, initial selection reveal and hide
            LaunchedEffect(key1 = indicatorCenter != null) {
                if (indicatorCenter != null) animatedCenter.snapTo(indicatorCenter)
                val target = if (indicatorCenter != null) 1f else 0f
                if (animatedRadius.value != target || animatedRadius.isRunning) animatedRadius.animateTo(
                    targetValue = target,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
                )
            }

            // Handles circle moves animation, change of the selected day
            LaunchedEffect(key1 = indicatorCenter) {
                if (indicatorCenter != null) animatedCenter.animateTo(
                    targetValue = indicatorCenter,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
                )
            }

            val arrowOffsetY =
                (cellHeight + (if (language.isArabicScript) 4 else 0).dp - MaterialIconDimension.dp) / 2
            PagerArrow(arrowOffsetY, coroutineScope, pagerState, page, width, true, onlyWeek)

            repeat(7) { column ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = cellsSizeModifier.offset(
                        x = pagerArrowSizeAndPadding.dp + cellWidth * column
                    ),
                ) {
                    val weekDay = weekStart + column
                    val description = stringResource(
                        R.string.week_days_name_column,
                        weekDay.title,
                    )
                    Text(
                        weekDay.shortTitle,
                        fontSize = with(density) { (diameter * .5f).toSp() },
                        modifier = Modifier
                            .alpha(AppBlendAlpha)
                            .semantics { this.contentDescription = description },
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                // Invalidate the indicator state on table size changes
                key(width, suggestedHeight) {
                    Canvas(Modifier.fillMaxSize()) {
                        val radiusFraction = animatedRadius.value
                        if (radiusFraction > 0f) drawCircle(
                            color = monthColors.indicator,
                            center = animatedCenter.value,
                            radius = cellRadius * radiusFraction,
                        )
                    }
                }

                val holidaysPositions = remember { DayTablePositions() }
                repeat(daysRowsCount * 7) { dayOffset ->
                    if (onlyWeek != null && monthStartWeekOfYear + dayOffset / 7 != onlyWeek) return@repeat
                    val row = if (onlyWeek == null) dayOffset / 7 else 0
                    val day = monthStartJdn + dayOffset - startingWeekDay
                    val isToday = day == today
                    val isBeforeMonth = dayOffset < startingWeekDay
                    val isAfterMonth = dayOffset + 1 > startingWeekDay + monthLength
                    val column = dayOffset % 7
                    Box(
                        Modifier
                            .offset(y = cellHeight * (row + 1))
                            .semantics { this.isTraversalGroup = true },
                    ) {
                        if (column == 0) AnimatedVisibility(
                            isShowWeekOfYearEnabled,
                            modifier = Modifier
                                .offset(x = (16 - 4).dp)
                                .size((24 + 8).dp, cellHeight),
                            label = "week number",
                        ) {
                            val weekNumber = onlyWeek ?: (monthStartWeekOfYear + row)
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (onWeekClick != null) Modifier.clickable(
                                            onClickLabel = stringResource(R.string.week_view),
                                            indication = ripple(bounded = false),
                                            interactionSource = null,
                                        ) {
                                            onWeekClick(
                                                when {
                                                    selectedDay - day in 0..<7 -> selectedDay
                                                    onlyWeek != null -> day
                                                    row == 0 -> monthStartJdn
                                                    // Select first non weekend day of the week
                                                    else -> day + ((0..6).firstOrNull {
                                                        (day + it).weekDay !in weekEnds
                                                    } ?: 0)
                                                },
                                                true,
                                            )
                                        } else Modifier,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                val formattedWeekNumber = numeral.format(weekNumber)
                                val description =
                                    stringResource(R.string.nth_week_of_year, formattedWeekNumber)
                                Text(
                                    formattedWeekNumber,
                                    fontSize = with(density) { (daysTextSize * .625f).toSp() },
                                    modifier = Modifier
                                        .alpha(AppBlendAlpha)
                                        .semantics { this.contentDescription = description },
                                )
                            }
                        }
                        if (previousMonthLength != null || (!isBeforeMonth && !isAfterMonth)) Box(
                            contentAlignment = Alignment.Center,
                            modifier = cellsSizeModifier
                                .offset(x = pagerArrowSizeAndPadding.dp + cellWidth * column)
                                .onFocusChanged {
                                    if (it.isFocused) focusedDay = day
                                    else if (!it.hasFocus && focusedDay == day) focusedDay = null
                                }
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
                                .then(
                                    if (isBeforeMonth || isAfterMonth) Modifier.alpha(.5f)
                                    else Modifier
                                ),
                        ) {
                            val isSelected = isHighlighted && selectedDay == day
                            val events = eventsRepository.getEvents(day, deviceEvents)
                            val isHoliday = events.any { it.isHoliday } || day.weekDay in weekEnds
                            if (isHoliday) holidaysPositions.add(row = row, column = column)
                            Canvas(cellsSizeModifier) {
                                val hasEvents =
                                    events.any { it !is CalendarEvent.DeviceCalendarEvent }
                                val hasAppointments =
                                    events.any { it is CalendarEvent.DeviceCalendarEvent }
                                val shiftWorkTitle = getShiftWorkTitle(day, true)
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
                                    style = todayIndicatorStroke,
                                )
                                if (day == focusedDay) drawCircle(
                                    focusColor,
                                    radius = cellRadius * 1.5f
                                )
                            }
                            Text(
                                text = mainCalendarNumeral.format(
                                    if (previousMonthLength != null && isBeforeMonth) {
                                        previousMonthLength - (startingWeekDay - dayOffset) + 1
                                    } else if (onlyWeek != null && isAfterMonth) {
                                        dayOffset + 1 - monthLength - startingWeekDay
                                    } else dayOffset + 1 - startingWeekDay
                                ),
                                color = when {
                                    isHoliday -> monthColors.holidays
                                    isSelected -> monthColors.textDaySelected
                                    else -> contentColor
                                },
                                style = daysStyle,
                                modifier = Modifier
                                    .padding(top = cellHeight / 15)
                                    .semantics {
                                        if (isTalkBackEnabled) this.contentDescription =
                                            getA11yDaySummary(
                                                resources = resources,
                                                jdn = day,
                                                isToday = isToday,
                                                deviceCalendarEvents = EventsStore.empty(),
                                                withZodiac = isToday,
                                                withOtherCalendars = false,
                                                withTitle = true,
                                                withWeekOfYear = false,
                                            )
                                    },
                            )
                        }
                    }
                }

                Canvas(
                    Modifier
                        .fillMaxSize()
                        .zIndex(-1f)
                ) {
                    holidaysPositions.forEach { row, column ->
                        val center = Offset(
                            x = (.5f + if (isRtl) 6 - column else column) * cellWidthPx + pagerArrowSizeAndPaddingPx,
                            // +1 for weekday names initials row, .5f for center of the circle
                            y = cellHeightPx * (1.5f + if (onlyWeek == null) row else 0),
                        )
                        drawCircle(monthColors.holidaysCircle, center = center, radius = cellRadius)
                    }
                }
            }

            PagerArrow(arrowOffsetY, coroutineScope, pagerState, page, width, false, onlyWeek)
        }
    }
}

// A bitset useful for a 7x7 table, seven days of seven weeks
@VisibleForTesting
internal class DayTablePositions {
    private var bits: Long = 0L
    fun add(row: Int, column: Int) {
        val index = row * 7 + column
        if (BuildConfig.DEVELOPMENT) assert(row < 7 && column < 7 && index in 0..63)
        bits = bits or (1L shl index)
    }

    inline fun forEach(crossinline action: (row: Int, column: Int) -> Unit) {
        var remaining = bits
        while (remaining != 0L) {
            val index = java.lang.Long.numberOfTrailingZeros(remaining)
            action(index / 7, index % 7)
            remaining = remaining and (remaining - 1)
        }
    }
}

private const val pagerArrowSize = MaterialIconDimension + 8 * 2
const val pagerArrowSizeAndPadding = pagerArrowSize + 4

@Composable
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
                x = if (isPrevious) 16.dp else (screenWidth - pagerArrowSize.dp),
                y = arrowOffsetY,
            )
            .then(
                if (week == null) Modifier.combinedClickable(
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
                },
            )
            .alpha(.9f),
    )
}
