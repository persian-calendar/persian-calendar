package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CalendarPager(
    viewModel: CalendarViewModel,
    pagerState: PagerState,
    addEvent: (AddEventData) -> Unit,
    today: Jdn,
    suggestedPagerSize: DpSize,
    navigateToDays: (Jdn, Boolean) -> Unit,
    animatedContentScope: AnimatedContentScope,
) {
    val selectedMonthOffsetCommand by viewModel.selectedMonthOffsetCommand.collectAsState()
    LaunchedEffect(key1 = selectedMonthOffsetCommand) {
        val offset = selectedMonthOffsetCommand ?: return@LaunchedEffect
        val page = applyOffset(-offset)
        if (viewModel.daysScreenSelectedDay.value == null) {
            pagerState.animateScrollToPage(page)
        } else {
            // Apply immediately if we're just coming back from days screen
            pagerState.scrollToPage(page)
            viewModel.changeDaysScreenSelectedDay(null)
        }
        viewModel.changeSelectedMonthOffsetCommand(null)
    }

    val language by language.collectAsState()
    val monthColors = appMonthColors()

    viewModel.notifySelectedMonthOffset(-applyOffset(pagerState.currentPage))

    val coroutineScope = rememberCoroutineScope()
    val isHighlighted by viewModel.isHighlighted.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val refreshToken by viewModel.refreshToken.collectAsState()
    val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
    val isVazirEnabled by isVazirEnabled.collectAsState()
    val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
    val context = LocalContext.current

    HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
        val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(today, -applyOffset(page))
        val monthStartJdn = Jdn(monthStartDate)
        val monthDeviceEvents = remember(refreshToken, isShowDeviceCalendarEvents) {
            if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
            else EventsStore.empty()
        }
        DaysTable(
            monthStartDate = monthStartDate,
            monthStartJdn = monthStartJdn,
            suggestedPagerSize = suggestedPagerSize,
            addEvent = addEvent,
            monthColors = monthColors,
            animatedContentScope = animatedContentScope,
            today = today,
            isHighlighted = isHighlighted,
            refreshToken = refreshToken,
            selectedDay = selectedDay,
            setSelectedDay = { viewModel.changeSelectedDay(it) },
            onWeekClick = navigateToDays,
            language = language,
            coroutineScope = coroutineScope,
            pagerState = pagerState,
            page = page,
            deviceEvents = monthDeviceEvents,
            isVazirEnabled = isVazirEnabled,
            isShowWeekOfYearEnabled = isShowWeekOfYearEnabled,
        )
    }
}

fun calendarPagerSize(
    isLandscape: Boolean,
    maxWidth: Dp,
    maxHeight: Dp,
    bottomPadding: Dp,
    twoRows: Boolean = false
): DpSize {
    return if (isLandscape) {
        val width = (maxWidth * 45 / 100).coerceAtMost(400.dp)
        val height = 400.dp.coerceAtMost(maxHeight - bottomPadding)
        DpSize(width, height)
    } else DpSize(
        maxWidth,
        (maxHeight / 2f).coerceIn(280.dp, 440.dp).let { if (twoRows) it / 7 * 2 else it }
    )
}

@Composable
fun calendarPagerState(): PagerState =
    rememberPagerState(initialPage = applyOffset(0), pageCount = ::monthsLimit)

private const val monthsLimit = 5000 // this should be an even number

private fun applyOffset(position: Int) = monthsLimit / 2 - position
