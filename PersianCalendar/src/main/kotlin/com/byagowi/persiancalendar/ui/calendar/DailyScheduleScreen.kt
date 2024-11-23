package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.calendarpager.Month
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthColors
import com.byagowi.persiancalendar.ui.calendar.calendarpager.PagerArrow
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.abs

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DailyScheduleScreen(
    calendarViewModel: CalendarViewModel,
    initialSelectedDay: Jdn,
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
) {
    var selectedDay by remember { mutableStateOf(initialSelectedDay) }
    val date = selectedDay.inCalendar(mainCalendar)
    val today by calendarViewModel.today.collectAsState()
    val context = LocalContext.current
    var isClickedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val weekInitialPage = remember { weekPageFromJdn(initialSelectedDay, today) }
    val weekPagerState = rememberPagerState(initialPage = weekInitialPage) { weeksLimit }
    val dayInitialPage = remember { dayPageFromJdn(selectedDay, today) }
    val dayPagerState = rememberPagerState(initialPage = dayInitialPage) { daysLimit }
    val setSelectedDayInWeekPager = { jdn: Jdn ->
        selectedDay = jdn
        coroutineScope.launch {
            val destination = dayPageFromJdn(jdn, today)
            if (abs(destination - dayPagerState.currentPage) > 1)
                dayPagerState.scrollToPage(destination)
            else dayPagerState.animateScrollToPage(destination)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isClickedOnce && calendarViewModel.selectedDay.value != selectedDay) {
                bringDate(calendarViewModel, selectedDay, context, highlight = selectedDay != today)
            }
        }
    }
    val addEvent = addEvent(calendarViewModel)
    val hasWeeksPager = LocalConfiguration.current.screenHeightDp > 600
    val language by language.collectAsState()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    Column {
                        Crossfade(
                            if (hasWeeksPager) date.monthName
                            else language.dm.format(formatNumber(date.dayOfMonth), date.monthName),
                            label = "title",
                        ) { state -> Text(state, style = MaterialTheme.typography.titleLarge) }
                        Crossfade(formatNumber(date.year), label = "subtitle") { state ->
                            Text(state, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
                actions = {
                    TodayActionButton(today != selectedDay) {
                        selectedDay = today
                        coroutineScope.launch {
                            weekPagerState.scrollToPage(weeksLimit / 2)
                            dayPagerState.scrollToPage(daysLimit / 2)
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        val monthColors = appMonthColors()
        val bottomPadding = paddingValues.calculateBottomPadding().coerceAtLeast(16.dp)
        BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val pagerSize = calendarPagerSize(false, this.maxWidth, this.maxHeight, true)
            // Don't show weeks pager if there isn't enough space
            Column {
                val refreshToken by calendarViewModel.refreshToken.collectAsState()
                if (hasWeeksPager) HorizontalPager(
                    state = weekPagerState,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 8.dp),
                ) { page ->
                    Box(modifier = Modifier.height(pagerSize.height)) {
                        WeekPage(
                            pagerSize = pagerSize,
                            addEvent = addEvent,
                            monthColors = monthColors,
                            selectedDay = selectedDay,
                            selectedDayDate = date,
                            setSelectedDay = { jdn -> setSelectedDayInWeekPager(jdn) },
                            setClickedOnce = { isClickedOnce = true },
                            animatedContentScope = animatedContentScope,
                            language = language,
                            coroutineScope = coroutineScope,
                            weekPagerState = weekPagerState,
                            page = page,
                            today = today,
                            refreshToken = refreshToken,
                        )
                    }
                }
                ScreenSurface(animatedContentScope) {
                    HorizontalPager(
                        state = dayPagerState,
                        verticalAlignment = Alignment.Top,
                    ) { page ->
                        val isCurrentPage = dayPagerState.currentPage == page
                        LaunchedEffect(isCurrentPage) {
                            if (isCurrentPage) {
                                selectedDay = today + (page - daysLimit / 2)
                                val destination = weekPageFromJdn(selectedDay, today)
                                if (abs(destination - weekPagerState.currentPage) > 1)
                                    weekPagerState.scrollToPage(destination)
                                else weekPagerState.animateScrollToPage(destination)
                            }
                        }

                        DaySchedule(
                            selectedDay = today + (page - daysLimit / 2),
                            refreshToken = refreshToken,
                            calendarViewModel = calendarViewModel,
                            animatedContentScope = animatedContentScope,
                            addEvent = addEvent,
                            bottomPadding = bottomPadding,
                            navigateUp = navigateUp,
                            setClickedOnce = { isClickedOnce = true },
                        )
                    }
                }
            }
        }
    }
}

private fun weekPageFromJdn(day: Jdn, today: Jdn): Int {
    val dayWeekStart = day - applyWeekStartOffsetToWeekDay(day.weekDay)
    val todayWeekStart = today - applyWeekStartOffsetToWeekDay(today.weekDay)
    return (dayWeekStart - todayWeekStart) / 7 + weeksLimit / 2
}

private fun dayPageFromJdn(day: Jdn, today: Jdn): Int = day - today + daysLimit / 2

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DaySchedule(
    selectedDay: Jdn,
    refreshToken: Int,
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    addEvent: (AddEventData) -> Unit,
    bottomPadding: Dp,
    navigateUp: () -> Unit,
    setClickedOnce: () -> Unit,
) {
    val events = readEvents(selectedDay, refreshToken)
    val eventsWithTime =
        events.filterIsInstance<CalendarEvent.DeviceCalendarEvent>().filter { it.time != null }
    val eventsWithoutTime = events - eventsWithTime.toSet()
    val calendarPageJdn = remember { calendarViewModel.selectedDay.value }

    Column {
        val state = rememberLazyListState(7, 0)
        val detectSwipeUpModifier = Modifier.pointerInput(Unit) {
            val threshold = 40.dp.toPx()
            awaitEachGesture {
                // Don't inline this
                val id = awaitFirstDown(requireUnconsumed = false).id
                val wasAtTop =
                    state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0
                verticalDrag(id) {
                    if (it.positionChange().y > threshold && wasAtTop) navigateUp()
                }
            }
        }

        val hasHeader by remember {
            val needsHeader = eventsWithTime.isEmpty() || eventsWithoutTime.isNotEmpty()
            derivedStateOf {
                needsHeader && !state.lastScrolledForward && state.firstVisibleItemIndex <= 7
            }
        }

        AnimatedVisibility(hasHeader) {
            Column(detectSwipeUpModifier) {
                Spacer(Modifier.height(24.dp))
                if (events.isEmpty()) Text(
                    stringResource(R.string.no_event),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Column(
                    Modifier
                        .padding(horizontal = 24.dp)
                        .then(
                            if (calendarPageJdn == selectedDay) Modifier.sharedBounds(
                                rememberSharedContentState(SHARED_CONTENT_KEY_EVENTS),
                                animatedVisibilityScope = animatedContentScope,
                            ) else Modifier
                        )
                ) {
                    DayEvents(eventsWithoutTime.take(3)) {
                        calendarViewModel.refreshCalendar()
                    }
                    if (eventsWithoutTime.size > 3) {
                        Spacer(Modifier.height(4.dp))
                        MoreButton(stringResource(R.string.more)) {
                            setClickedOnce()
                            navigateUp()
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        Box {
            LazyColumn(state = state) {
                items(24) { hour ->
                    Column(detectSwipeUpModifier) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                                .then(if (hour < 7) Modifier.alpha(.5f) else Modifier),
                        ) {
                            Text(formatNumber("${hour}:00"))
                            Spacer(Modifier.width(8.dp))
                            HorizontalDivider()
                        }
                        Column(Modifier.padding(horizontal = 24.dp)) {
                            val hourEvents = eventsWithTime.filter {
                                it.startTime.get(Calendar.HOUR_OF_DAY) == hour
                            }
                            if (hourEvents.isNotEmpty()) DayEvents(hourEvents) {
                                calendarViewModel.refreshCalendar()
                            } else AddHourEvent(addEvent, selectedDay, hour)
                        }
                    }
                }
                item { Spacer(Modifier.height(bottomPadding)) }
            }
            ScrollShadow(state, top = true)
            ScrollShadow(state, top = false)
        }
    }
}

@Composable
private fun AddHourEvent(addEvent: (AddEventData) -> Unit, jdn: Jdn, hour: Int) {
    Box(
        Modifier
            .padding(top = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f))
            .clickable {
                val time = jdn.toGregorianCalendar()
                time.set(GregorianCalendar.HOUR_OF_DAY, hour)
                time.set(GregorianCalendar.MINUTE, 0)
                time.set(GregorianCalendar.SECOND, 0)
                val beginTime = time.time
                time.set(GregorianCalendar.HOUR_OF_DAY, hour + 1)
                val endTime = time.time
                addEvent(
                    AddEventData(
                        beginTime = beginTime,
                        endTime = endTime,
                        allDay = false,
                        description = dayTitleSummary(jdn, jdn.inCalendar(mainCalendar)),
                    )
                )
            }
            .padding(all = 12.dp),
    ) {
        Icon(
            Icons.Default.Add,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
            contentDescription = stringResource(R.string.add_event),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.WeekPage(
    pagerSize: DpSize,
    addEvent: (AddEventData) -> Unit,
    monthColors: MonthColors,
    selectedDay: Jdn,
    selectedDayDate: AbstractDate,
    setSelectedDay: (Jdn) -> Unit,
    setClickedOnce: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    language: Language,
    coroutineScope: CoroutineScope,
    weekPagerState: PagerState,
    page: Int,
    today: Jdn,
    refreshToken: Int,
) {
    Box {
        val offset = page - weeksLimit / 2
        val sampleDay = today + 7 * offset
        val startOfYearJdn = Jdn(mainCalendar, selectedDayDate.year, 1, 1)
        val week = sampleDay.getWeekOfYear(startOfYearJdn)

        val isCurrentPage = weekPagerState.currentPage == page
        LaunchedEffect(isCurrentPage) {
            if (isCurrentPage && selectedDay.getWeekOfYear(startOfYearJdn) != week) {
                setSelectedDay(sampleDay + (selectedDay.weekDay - today.weekDay))
            }
        }

        val height = pagerSize.height
        val arrowWidth = pagerSize.width / 12
        val arrowHeight = height / 2 + (if (language.isArabicScript) 4 else 0).dp
        PagerArrow(
            arrowWidth = arrowWidth,
            arrowHeight = arrowHeight,
            scope = coroutineScope,
            pagerState = weekPagerState,
            week = week,
            index = page,
            isPrevious = true,
        )
        Box(
            modifier = Modifier
                .height(height)
                .padding(horizontal = arrowWidth)
        ) {
            val monthOffset = mainCalendar.getMonthsDistance(today, selectedDay)
            Month(
                offset = monthOffset,
                width = pagerSize.width - arrowWidth * 2,
                height = height,
                addEvent = addEvent,
                monthColors = monthColors,
                navigateToDailySchedule = null,
                animatedContentScope = animatedContentScope,
                onlyWeek = week,
                today = today,
                isHighlighted = true,
                selectedDay = selectedDay,
                refreshToken = refreshToken,
                setSelectedDay = {
                    setClickedOnce()
                    setSelectedDay(it)
                }
            )
        }
        PagerArrow(
            arrowWidth = arrowWidth,
            arrowHeight = arrowHeight,
            scope = coroutineScope,
            pagerState = weekPagerState,
            index = page,
            week = week,
            isPrevious = false
        )
    }
}

private const val weeksLimit = 25000 // this should be an even number
private const val daysLimit = 175000 // this should be an even number
