package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
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
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.WeekScreen(
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
    val initialPage = remember {
        val initialSelectedDayWeekStart =
            initialSelectedDay - applyWeekStartOffsetToWeekDay(initialSelectedDay.weekDay)
        val todayWeekStart = today - applyWeekStartOffsetToWeekDay(today.weekDay)
        (initialSelectedDayWeekStart - todayWeekStart) / 7 + weeksLimit / 2
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { weeksLimit }

    DisposableEffect(Unit) {
        onDispose {
            if (isClickedOnce) {
                bringDate(calendarViewModel, selectedDay, context, highlight = selectedDay != today)
            }
        }
    }
    val addEvent = addEvent(calendarViewModel)
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    Column {
                        Crossfade(date.monthName, label = "title") { state ->
                            Text(state, style = MaterialTheme.typography.titleLarge)
                        }
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
                        coroutineScope.launch { pagerState.scrollToPage(weeksLimit / 2) }
                    }
                }
            )
        },
    ) { paddingValues ->
        val monthColors = appMonthColors()
        BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val pagerSize = calendarPagerSize(false, this.maxWidth, this.maxHeight, true)
            Column {
                val language by language.collectAsState()
                val refreshToken by calendarViewModel.refreshToken.collectAsState()
                HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
                    Box(modifier = Modifier.height(pagerSize.height)) {
                        WeekPage(
                            pagerSize = pagerSize,
                            addEvent = addEvent,
                            monthColors = monthColors,
                            selectedDay = selectedDay,
                            selectedDayDate = date,
                            setSelectedDay = { jdn -> selectedDay = jdn },
                            setClickedOnce = { isClickedOnce = true },
                            animatedContentScope = animatedContentScope,
                            language = language,
                            coroutineScope = coroutineScope,
                            pagerState = pagerState,
                            page = page,
                            today = today,
                            refreshToken = refreshToken,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                ScreenSurface(animatedContentScope) {
                    Box(Modifier.fillMaxSize()) {
                        val events = readEvents(selectedDay, refreshToken)
//                        val eventsWithTime = events
//                            .filterIsInstance<CalendarEvent.DeviceCalendarEvent>()
//                            .filter { it.time != null }
//                        val eventsWithoutTime = events - eventsWithTime.toSet()
                        val calendarPageJdn = remember { calendarViewModel.selectedDay.value }
                        Column(
                            if (calendarPageJdn == selectedDay) Modifier.sharedBounds(
                                rememberSharedContentState(SHARED_CONTENT_KEY_EVENTS),
                                animatedVisibilityScope = animatedContentScope,
                            ) else Modifier,
                        ) {
                            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
                                DayEvents(events) {}
                            }
//                            eventsWithTime.forEach {
//                                Spacer(Modifier.height(16.dp))
//                                Text(
//                                    "${it.time?.replace(" ", "")}:",
//                                    style = MaterialTheme.typography.titleLarge,
//                                    modifier = Modifier.padding(start = 16.dp),
//                                    maxLines = 1,
//                                )
//                                Text(
//                                    it.title,
//                                    style = MaterialTheme.typography.headlineLarge,
//                                    modifier = Modifier.padding(start = 8.dp),
//                                )
//                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.WeekPage(
    pagerSize: DpSize,
    addEvent: () -> Unit,
    monthColors: MonthColors,
    selectedDay: Jdn,
    selectedDayDate: AbstractDate,
    setSelectedDay: (Jdn) -> Unit,
    setClickedOnce: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    language: Language,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int,
    today: Jdn,
    refreshToken: Int,
) {
    Box {
        val offset = page - weeksLimit / 2
        val initialDay = today + 7 * offset
        val startOfYearJdn = Jdn(mainCalendar, selectedDayDate.year, 1, 1)
        val week = initialDay.getWeekOfYear(startOfYearJdn)

        val isCurrentPage = pagerState.currentPage == page
        LaunchedEffect(isCurrentPage) {
            if (isCurrentPage && selectedDay.getWeekOfYear(startOfYearJdn) != week) {
                setSelectedDay(initialDay + (selectedDay.weekDay - today.weekDay))
            }
        }

        val height = pagerSize.height
        val arrowWidth = pagerSize.width / 12
        val arrowHeight = height / 2 + (if (language.isArabicScript) 4 else 0).dp
        PagerArrow(
            arrowWidth = arrowWidth,
            arrowHeight = arrowHeight,
            scope = coroutineScope,
            pagerState = pagerState,
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
                navigateToWeek = null,
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
            pagerState = pagerState,
            index = page,
            week = week,
            isPrevious = false
        )
    }
}

private const val weeksLimit = 25000 // this should be an even number
