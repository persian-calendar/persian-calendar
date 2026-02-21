package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import kotlinx.coroutines.launch

@Composable
fun CalendarPager(
    selectedDay: Jdn,
    isHighlighted: Boolean,
    refreshToken: Int,
    changeSelectedDay: (Jdn) -> Unit,
    calendarPagerState: CalendarPagerState,
    yearViewCalendar: Calendar?,
    addEvent: (AddEventData) -> Unit,
    today: Jdn,
    suggestedPagerSize: DpSize,
    navigateToDays: (Jdn, Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val daysTable = daysTable(
        suggestedPagerSize = suggestedPagerSize,
        addEvent = addEvent,
        today = today,
        refreshToken = refreshToken,
        setSelectedDay = changeSelectedDay,
        onWeekClick = navigateToDays,
        arrowAction = { isPrevious, isLongClick ->
            coroutineScope.launch {
                val page = clampPageNumber(
                    calendarPagerState.currentPage + (if (isLongClick) 12 else 1) * if (isPrevious) -1 else 1,
                )
                if (isLongClick) calendarPagerState.scrollToPage(page)
                else calendarPagerState.animateScrollToPage(page)
            }
        },
        secondaryCalendar = yearViewCalendar.takeIf { it != mainCalendar } ?: secondaryCalendar,
    )

    val context = LocalContext.current
    HorizontalPager(state = calendarPagerState, verticalAlignment = Alignment.Top) { page ->
        val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(today, -applyOffset(page))
        val monthStartJdn = Jdn(monthStartDate)
        val monthDeviceEvents = remember(refreshToken, isShowDeviceCalendarEvents) {
            if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
            else EventsStore.empty()
        }
        daysTable(
            monthStartDate,
            monthStartJdn,
            monthDeviceEvents,
            null,
            isHighlighted,
            selectedDay,
        )
    }
}

fun calendarPagerSize(
    isLandscape: Boolean,
    maxWidth: Dp,
    maxHeight: Dp,
    bottomPadding: Dp,
    twoRows: Boolean = false,
): DpSize {
    return if (isLandscape) {
        val width = (maxWidth * 45 / 100).coerceAtMost(400.dp)
        val height = 400.dp.coerceAtMost(maxHeight - bottomPadding).coerceAtLeast(10.dp)
        DpSize(width, height)
    } else DpSize(
        maxWidth,
        (maxHeight / 2f).coerceIn(280.dp, 440.dp).let { if (twoRows) it / 7 * 2 else it },
    )
}

typealias CalendarPagerState = PagerState

@Composable
fun calendarPagerState(initialMonthsDistance: Int): CalendarPagerState =
    rememberPagerState(initialPage = applyOffset(initialMonthsDistance), pageCount = ::monthsLimit)

private const val monthsLimit = 5000 // this should be an even number

fun clampPageNumber(pageNumber: Int) = pageNumber.coerceIn(0..<monthsLimit)

fun applyOffset(monthsDistance: Int) = monthsLimit / 2 - monthsDistance
