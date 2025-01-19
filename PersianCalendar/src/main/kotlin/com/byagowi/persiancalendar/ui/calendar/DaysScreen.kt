package com.byagowi.persiancalendar.ui.calendar

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.lruCache
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_DAYS_SCREEN_ICON
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.preferredSwipeUpAction
import com.byagowi.persiancalendar.ui.calendar.calendarpager.DaysTable
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.pagerArrowSizeAndPadding
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appFabElevation
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.theme.noTransitionSpec
import com.byagowi.persiancalendar.ui.utils.AnimatableFloatSaver
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.JdnSaver
import com.byagowi.persiancalendar.ui.utils.SmallShapeCornerSize
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import com.byagowi.persiancalendar.utils.readWeekDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DaysScreen(
    calendarViewModel: CalendarViewModel,
    initiallySelectedDay: Jdn,
    appAnimatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
    isInitiallyWeek: Boolean,
) {
    var selectedDay by rememberSaveable(saver = JdnSaver) { mutableStateOf(initiallySelectedDay) }
    val today by calendarViewModel.today.collectAsState()
    var isHighlighted by rememberSaveable { mutableStateOf(selectedDay != today) }
    val date = selectedDay.inCalendar(mainCalendar)
    calendarViewModel.changeDaysScreenSelectedDay(selectedDay)
    val coroutineScope = rememberCoroutineScope()
    val weekInitialPage = remember(today) { weekPageFromJdn(initiallySelectedDay, today) }
    val weekPagerState = rememberPagerState(initialPage = weekInitialPage) { weeksLimit }
    val dayInitialPage = remember(today) { dayPageFromJdn(selectedDay, today) }
    val dayPagerState = rememberPagerState(initialPage = dayInitialPage) { daysLimit }
    var isWeekView by rememberSaveable { mutableStateOf(isInitiallyWeek) }
    val setSelectedDayInWeekPager = { jdn: Jdn ->
        selectedDay = jdn
        isHighlighted = true
        if (!isWeekView) coroutineScope.launch {
            val destination = dayPageFromJdn(jdn, today)
            if (abs(destination - dayPagerState.currentPage) > 1) {
                dayPagerState.scrollToPage(destination)
            } else dayPagerState.animateScrollToPage(destination)
        }
    }

    val addEvent = addEvent(calendarViewModel)
    val hasWeeksPager = LocalConfiguration.current.screenHeightDp > 600
    val language by language.collectAsState()
    var isAddEventBoxEnabled by remember { mutableStateOf(false) }

    val todayButtonAction = {
        isAddEventBoxEnabled = false
        selectedDay = today
        isHighlighted = false

        coroutineScope.launch {
            if (abs(weekPagerState.currentPage - weeksLimit / 2) == 1) {
                weekPagerState.animateScrollToPage(weeksLimit / 2)
            } else weekPagerState.scrollToPage(weeksLimit / 2)
        }

        if (!isWeekView) coroutineScope.launch {
            if (abs(dayPagerState.currentPage - daysLimit / 2) == 1) {
                dayPagerState.animateScrollToPage(daysLimit / 2)
            } else dayPagerState.scrollToPage(daysLimit / 2)
        }
    }

    var isFirstTime by remember { mutableStateOf(true) }
    LaunchedEffect(today) {
        if (isFirstTime) isFirstTime = false else if (selectedDay == today - 1) todayButtonAction()
    }

    var addAction by remember { mutableStateOf({}) }

    val snackbarHostState = remember { SnackbarHostState() }

    val preferredSwipeUpAction by preferredSwipeUpAction.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val swipeDownModifier = Modifier.detectSwipe {
        { isUp ->
            if (!isLandscape && !isUp) when (preferredSwipeUpAction) {
                SwipeUpAction.WeekView, SwipeUpAction.DayView -> navigateUp()
                else -> {}
            }
        }
    }

    AnimatedContent(
        isWeekView,
        label = "is week view",
        transitionSpec = noTransitionSpec,
    ) { isWeekViewState ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = addAction,
                    elevation = appFabElevation(),
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay()
                        .padding(end = 8.dp),
                ) { Icon(Icons.Default.Add, stringResource(R.string.add_event)) }
            },
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                    modifier = swipeDownModifier,
                    title = {
                        Column(Modifier.clickable(
                            interactionSource = null,
                            indication = ripple(bounded = false),
                            onClickLabel = stringResource(
                                if (!isWeekView) R.string.week_view else R.string.calendar
                            )
                        ) { if (!isWeekView) isWeekView = true else navigateUp() }) {
                            Crossfade(
                                if (hasWeeksPager) date.monthName
                                else language.dm.format(
                                    formatNumber(date.dayOfMonth), date.monthName
                                ),
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
                        TodayActionButton(isHighlighted || isAddEventBoxEnabled) {
                            todayButtonAction()
                        }
                        IconButton(
                            onClick = {
                                isWeekView = !isWeekView
                                if (!isWeekView) coroutineScope.launch {
                                    dayPagerState.scrollToPage(dayPageFromJdn(selectedDay, today))
                                }
                            },
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_DAYS_SCREEN_ICON),
                                animatedVisibilityScope = this@AnimatedContent,
                            ),
                        ) {
                            val title = if (isWeekView) stringResource(R.string.day_view)
                            else stringResource(R.string.week_view)
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = { PlainTooltip { Text(title) } },
                                state = rememberTooltipState()
                            ) {
                                if (isWeekViewState) Icon(Icons.Default.CalendarViewDay, title)
                                else Icon(Icons.Default.CalendarViewWeek, title)
                            }
                        }
                    },
                )
            },
        ) { paddingValues ->
            val monthColors = appMonthColors()
            val bottomPadding = paddingValues.calculateBottomPadding().coerceAtLeast(16.dp)
            BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
                val screenWidth = this.maxWidth
                val pagerSize =
                    calendarPagerSize(false, this.maxWidth, this.maxHeight, bottomPadding, true)
                // Don't show weeks pager if there isn't enough space
                Column {
                    val now by calendarViewModel.now.collectAsState()
                    val refreshToken by calendarViewModel.refreshToken.collectAsState()
                    val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
                    val isVazirEnabled by isVazirEnabled.collectAsState()
                    val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
                    HorizontalPager(
                        state = weekPagerState,
                        verticalAlignment = Alignment.Top,
                        pageSpacing = 2.dp,
                    ) { page ->
                        Column {
                            val offset = page - weeksLimit / 2
                            val sampleDay = today + 7 * offset
                            val startOfYearJdn = Jdn(mainCalendar, date.year, 1, 1)
                            val week = sampleDay.getWeekOfYear(startOfYearJdn)

                            val isCurrentPage = weekPagerState.currentPage == page
                            LaunchedEffect(isCurrentPage) {
                                if (isCurrentPage &&
                                    selectedDay.getWeekOfYear(startOfYearJdn) != week
                                ) {
                                    val pageDay = sampleDay + (selectedDay.weekDay - today.weekDay)
                                    setSelectedDayInWeekPager(pageDay)
                                    isHighlighted = today != pageDay
                                }
                            }

                            val context = LocalContext.current
                            val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(
                                today, mainCalendar.getMonthsDistance(today, selectedDay)
                            )
                            val monthStartJdn = Jdn(monthStartDate)
                            val weekStart = (today + (page - weeksLimit / 2) * 7).let {
                                it - applyWeekStartOffsetToWeekDay(it.weekDay)
                            }
                            val weekDeviceEvents = remember(
                                refreshToken, isShowDeviceCalendarEvents, weekStart
                            ) {
                                if (isShowDeviceCalendarEvents) {
                                    context.readWeekDeviceEvents(weekStart)
                                } else EventsStore.empty()
                            }

                            if (hasWeeksPager) DaysTable(
                                modifier = swipeDownModifier,
                                monthStartDate = monthStartDate,
                                monthStartJdn = monthStartJdn,
                                suggestedPagerSize = pagerSize,
                                addEvent = addEvent,
                                monthColors = monthColors,
                                animatedContentScope = appAnimatedContentScope,
                                onlyWeek = week,
                                today = today,
                                isHighlighted = isHighlighted,
                                selectedDay = selectedDay,
                                refreshToken = refreshToken,
                                setSelectedDay = setSelectedDayInWeekPager,
                                language = language,
                                pagerState = weekPagerState,
                                page = page,
                                coroutineScope = coroutineScope,
                                deviceEvents = weekDeviceEvents,
                                isVazirEnabled = isVazirEnabled,
                                isShowWeekOfYearEnabled = isShowWeekOfYearEnabled,
                            )

                            if (isWeekViewState) ScreenSurface(
                                animatedContentScope = appAnimatedContentScope,
                                disableSharedContent = initiallySelectedDay - weekStart !in 0..<7,
                            ) {
                                DaysView(
                                    modifier = if (weekPagerState.currentPage == page) Modifier.sharedBounds(
                                        rememberSharedContentState(key = SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT),
                                        animatedVisibilityScope = this@AnimatedContent,
                                    ) else Modifier,
                                    bottomPadding = bottomPadding,
                                    setAddAction = {
                                        if (weekPagerState.currentPage == page) addAction = it
                                    },
                                    hasWeekPager = hasWeeksPager,
                                    startingDay = weekStart,
                                    selectedDay = selectedDay,
                                    setSelectedDay = setSelectedDayInWeekPager,
                                    addEvent = addEvent,
                                    refreshCalendar = calendarViewModel::refreshCalendar,
                                    days = 7,
                                    deviceEvents = weekDeviceEvents,
                                    now = now,
                                    isAddEventBoxEnabled = isAddEventBoxEnabled,
                                    setAddEventBoxEnabled = { isAddEventBoxEnabled = true },
                                    snackbarHostState = snackbarHostState,
                                    calendarViewModel = calendarViewModel,
                                    screenWidth = screenWidth,
                                )
                            }
                        }
                    }

                    if (!isWeekViewState) ScreenSurface(appAnimatedContentScope) {
                        HorizontalPager(
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT),
                                animatedVisibilityScope = this@AnimatedContent,
                            ),
                            state = dayPagerState,
                            verticalAlignment = Alignment.Top,
                        ) { page ->
                            val isCurrentPage = dayPagerState.currentPage == page
                            val pageDay = today + (page - daysLimit / 2)

                            LaunchedEffect(isCurrentPage) {
                                if (isCurrentPage) {
                                    selectedDay = pageDay
                                    if (today != pageDay) isHighlighted = true
                                    val destination = weekPageFromJdn(pageDay, today)
                                    if (abs(destination - weekPagerState.currentPage) > 1) {
                                        weekPagerState.scrollToPage(destination)
                                    } else weekPagerState.animateScrollToPage(destination)
                                }
                            }

                            val context = LocalContext.current
                            val dayDeviceEvents = remember(
                                refreshToken, isShowDeviceCalendarEvents, pageDay
                            ) {
                                if (isShowDeviceCalendarEvents) {
                                    context.readDayDeviceEvents(pageDay)
                                } else EventsStore.empty()
                            }

                            DaysView(
                                bottomPadding = bottomPadding,
                                setAddAction = {
                                    if (dayPagerState.currentPage == page) addAction = it
                                },
                                startingDay = pageDay,
                                selectedDay = selectedDay,
                                setSelectedDay = { selectedDay = it; isHighlighted = true },
                                addEvent = addEvent,
                                refreshCalendar = calendarViewModel::refreshCalendar,
                                days = 1,
                                now = now,
                                isAddEventBoxEnabled = isAddEventBoxEnabled,
                                setAddEventBoxEnabled = { isAddEventBoxEnabled = true },
                                snackbarHostState = snackbarHostState,
                                calendarViewModel = calendarViewModel,
                                hasWeekPager = hasWeeksPager,
                                deviceEvents = dayDeviceEvents,
                                screenWidth = screenWidth,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun weekPageFromJdn(day: Jdn, today: Jdn): Int {
    val daysStart = day - applyWeekStartOffsetToWeekDay(day.weekDay)
    val todaysStart = today - applyWeekStartOffsetToWeekDay(today.weekDay)
    return (daysStart - todaysStart) / 7 + weeksLimit / 2
}

private fun dayPageFromJdn(day: Jdn, today: Jdn): Int = day - today + daysLimit / 2

private fun hoursFractionOfDay(date: GregorianCalendar): Float =
    date[Calendar.HOUR_OF_DAY] + date[Calendar.MINUTE] / 60f

private data class EventDivision(
    val event: CalendarEvent.DeviceCalendarEvent, val column: Int, val columnsCount: Int
)

private fun addDivisions(events: List<CalendarEvent.DeviceCalendarEvent>): List<EventDivision> {
    val graph = Graph(events.size)
    events.indices.forEach { i ->
        events.indices.forEach inner@{ j ->
            if (i == j) return@inner
            val a = events[i]
            val b = events[j]
            if (a.start.timeInMillis in (b.start.timeInMillis..<b.end.timeInMillis)) {
                graph.addEdge(i, j)
            }
        }
    }
    val columnsCount = MutableList(events.size) { 0 }
    val colors = graph.coloring()
    graph.connectedComponents().forEach { group ->
        val max = group.maxOf { colors[it] } + 1
        group.forEach { columnsCount[it] = max }
    }
    return events.indices.map { EventDivision(events[it], colors[it], columnsCount[it]) }
}

@Composable
private fun DaysView(
    setAddAction: (() -> Unit) -> Unit,
    startingDay: Jdn,
    selectedDay: Jdn,
    setSelectedDay: (Jdn) -> Unit,
    bottomPadding: Dp,
    addEvent: (AddEventData) -> Unit,
    refreshCalendar: () -> Unit,
    now: Long,
    days: Int,
    isAddEventBoxEnabled: Boolean,
    setAddEventBoxEnabled: () -> Unit,
    snackbarHostState: SnackbarHostState,
    calendarViewModel: CalendarViewModel,
    hasWeekPager: Boolean,
    deviceEvents: DeviceCalendarEventsStore,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val scale = rememberSaveable(saver = AnimatableFloatSaver) { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    var interaction by remember { mutableStateOf<Interaction?>(null) }
    Column(modifier.detectZoom(onZoom = {
        if (interaction == null) interaction = Interaction.Zoom
        if (interaction == Interaction.Zoom) coroutineScope.launch {
            val value = scale.value * it
            scale.snapTo(value.coerceIn(.5f, 2f))
        }
    }, onRelease = { if (interaction == Interaction.Zoom) interaction = null })) {
        val cellHeight by remember(scale.value) { mutableStateOf((64 * scale.value).dp) }
        val density = LocalDensity.current
        val initialScroll = with(density) { (cellHeight * 7 * scale.value - 16.dp).roundToPx() }
        val scrollState = rememberScrollState(initialScroll)
        val events = (startingDay..<startingDay + days).toList().map { jdn ->
            readEvents(jdn, calendarViewModel, deviceEvents)
        }
        val eventsWithTime = events.map { dayEvents ->
            addDivisions(dayEvents.filterIsInstance<CalendarEvent.DeviceCalendarEvent>()
                .filter { it.time != null }.sortedWith { x, y ->
                    x.start.timeInMillis.compareTo(y.end.timeInMillis).let {
                        if (it != 0) return@sortedWith it
                    }
                    // If both start at the same time, put bigger events first, better for interval graphs
                    y.start.timeInMillis.compareTo(x.end.timeInMillis)
                })
        }
        val eventsWithoutTime = events.map { dayEvents ->
            dayEvents.filter { it !is CalendarEvent.DeviceCalendarEvent || it.time == null }
        }
        val maxDayAllDayEvents = eventsWithoutTime.maxOf { it.size }
        val hasHeader by remember(events) {
            val needsHeader =
                eventsWithTime.all { it.isEmpty() } || maxDayAllDayEvents != 0 || (days != 1 && !hasWeekPager)
            derivedStateOf {
                needsHeader && !scrollState.lastScrolledForward && scrollState.value <= initialScroll * scale.value
            }
        }
        val launcher = rememberLauncherForActivityResult(ViewEventContract()) {
            refreshCalendar()
        }
        val context = LocalContext.current
        val defaultWidthReduction = 2.dp
        val defaultWidthReductionPx = with(density) { defaultWidthReduction.toPx() }
        val tableWidth = screenWidth - when (days) {
            1 -> pagerArrowSizeAndPadding.dp + 24.dp - defaultWidthReduction
            else -> pagerArrowSizeAndPadding.dp * 2
        }
        val cellWidth = tableWidth / days

        // Header
        AnimatedVisibility(hasHeader) {
            var isExpanded by rememberSaveable { mutableStateOf(false) }
            val clickToExpandModifier = Modifier.clickable(
                onClickLabel = stringResource(R.string.more),
                interactionSource = null,
                indication = null,
            ) { isExpanded = !isExpanded }
            if (days == 1) Column(
                (if (eventsWithoutTime[0].size > 3) {
                    clickToExpandModifier
                } else Modifier).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                if (events[0].isEmpty()) Text(
                    stringResource(R.string.no_event),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Column(Modifier.animateContentSize()) {
                    DayEvents(eventsWithoutTime[0].let { if (isExpanded) it else it.take(3) }) {
                        refreshCalendar()
                    }
                }
                if (eventsWithoutTime[0].size > 3) {
                    Spacer(Modifier.height(4.dp))
                    ExpandArrow(isExpanded = isExpanded)
                    Spacer(Modifier.height(8.dp))
                } else Spacer(Modifier.height(12.dp))
            } else Row(
                verticalAlignment = Alignment.Bottom,
                modifier = if (maxDayAllDayEvents > 3) clickToExpandModifier else Modifier
            ) {
                Box(
                    Modifier
                        .width(pagerArrowSizeAndPadding.dp)
                        .padding(bottom = 2.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) { if (maxDayAllDayEvents > 3) ExpandArrow(isExpanded = isExpanded) }
                Row(
                    Modifier
                        .padding(end = pagerArrowSizeAndPadding.dp)
                        .animateContentSize(),
                ) {
                    val headerTextStyle = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 24.sp
                    )
                    eventsWithoutTime.forEachIndexed { i, dayEvents ->
                        Column(Modifier.weight(1f)) {
                            if (!hasWeekPager) {
                                val weekDayPosition = revertWeekStartOffsetFromWeekDay(i)
                                val weekDayName = getWeekDayName(weekDayPosition)
                                val isLandscape =
                                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                                Text(
                                    text = if (isLandscape) weekDayName else {
                                        getInitialOfWeekDay(weekDayPosition)
                                    },
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .semantics { this.contentDescription = weekDayName },
                                )
                            }
                            dayEvents.forEachIndexed { i, event ->
                                if (isExpanded || i < 2 || (i == 2 && dayEvents.size == 3)) {
                                    val color = eventColor(event)
                                    Text(
                                        " " + event.title,
                                        maxLines = 1,
                                        style = headerTextStyle,
                                        color = eventTextColor(color),
                                        modifier = Modifier
                                            .requiredWidth(cellWidth - defaultWidthReduction)
                                            .padding(
                                                top = if (i == 0) 2.dp else 0.dp,
                                                bottom = 2.dp,
                                            )
                                            .clip(MaterialTheme.shapes.small)
                                            .background(eventColor(event))
                                            .clickable {
                                                if (event is CalendarEvent.DeviceCalendarEvent) {
                                                    launcher.viewEvent(event, context)
                                                } else coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(event.title)
                                                }
                                            },
                                    )
                                }
                                if (i == 2 && dayEvents.size > 3 && !isExpanded) Text(
                                    " +" + formatNumber(dayEvents.size - 3),
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    maxLines = 1,
                                    style = headerTextStyle,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Time cells, table and indicators
        Box {
            Box(Modifier.verticalScroll(scrollState)) {
                val firstColumnPx = with(density) { pagerArrowSizeAndPadding.dp.toPx() }
                val oneDayTableWidthPx = with(density) { (tableWidth + 24.dp).toPx() }
                val tableWidthPx = with(density) { tableWidth.toPx() }
                val cellWidthPx = tableWidthPx / days
                val cellHeightPx = with(density) { cellHeight.toPx() }
                var offset by remember(tableWidthPx) { mutableStateOf<Offset?>(null) }
                var duration by remember { mutableFloatStateOf(cellHeightPx) }
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val directionSign = if (isRtl) -1 else 1
                val heightSizeReduction = 3.dp
                val heightSizeReductionPx = with(density) { heightSizeReduction.toPx() }
                val clockCache = remember {
                    lruCache(1024, create = { minutes: Int ->
                        Clock(minutes / 60.0).toBasicFormatString()
                    })
                }

                // Time cells and table
                val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                Row(
                    Modifier.drawBehind {
                        val topLineY = 2.dp.toPx()
                        val paintCellWidthPx = if (days == 1) oneDayTableWidthPx else cellWidthPx
                        (0..days).forEach { i ->
                            val x = (firstColumnPx + paintCellWidthPx * i).let {
                                if (isRtl) this.size.width - it else it
                            }
                            val y = this.size.height
                            drawLine(outlineVariant, Offset(x, topLineY), Offset(x, y))
                        }
                        val x1 = firstColumnPx.let { if (isRtl) this.size.width - it else it }
                        val x2 = (firstColumnPx + paintCellWidthPx * days).let {
                            if (isRtl) this.size.width - it else it
                        }
                        val extraLineWidth = 8.dp.toPx() * directionSign
                        (0..23).forEach {
                            val x = x1 - if (it != 0) extraLineWidth else 0f
                            val y = if (it != 0) cellHeightPx * it else topLineY
                            drawLine(outlineVariant, Offset(x, y), Offset(x2, y))
                        }
                    },
                ) {
                    repeat(9) { column ->
                        Column {
                            if (column == 0) Spacer(Modifier.height(cellHeight / 2))
                            repeat(24) { row ->
                                Box(
                                    when (column) {
                                        0, days + 1 -> Modifier
                                        else -> Modifier
                                            .clickable(
                                                indication = null,
                                                interactionSource = null,
                                            ) {
                                                offset = Offset(
                                                    cellWidthPx * (column - 1),
                                                    cellHeightPx * row / scale.value
                                                )
                                                setAddEventBoxEnabled()
                                                duration = cellHeightPx / scale.value
                                                setSelectedDay(startingDay + column - 1)
                                            }
                                            .then(if (isTalkBackEnabled) Modifier.semantics {
                                                this.contentDescription =
                                                    (startingDay + (column - 1)).weekDayName + " " + clockCache[row * 60] + " " + clockCache[(row + 1) * 60]
                                            } else Modifier)
                                    }.size(
                                        if (column == 0 || column == days + 1) pagerArrowSizeAndPadding.dp
                                        else cellWidth,
                                        when {
                                            row != 23 -> cellHeight
                                            column != 0 -> cellHeight + bottomPadding
                                            else -> 0.dp
                                        },
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (column == 0 && row != 23) Text(
                                        clockCache[(row + 1) * 60],
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }

                // Already available events boxes
                eventsWithTime.mapIndexed { i, it ->
                    it.map { (event, column, columnsCount) ->
                        val start = hoursFractionOfDay(event.start)
                        val end = hoursFractionOfDay(event.end)
                        val color = eventColor(event)
                        Text(
                            " " + event.title,
                            color = eventTextColor(color),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (firstColumnPx + cellWidthPx * i + cellWidthPx / columnsCount * column).roundToInt(),
                                        (start * cellHeightPx).roundToInt()
                                    )
                                }
                                .requiredSize(
                                    with(density) { (cellWidthPx / columnsCount - defaultWidthReductionPx).toDp() },
                                    with(density) { ((end - start) * cellHeightPx).toDp() - heightSizeReduction },
                                )
                                .clickable { launcher.viewEvent(event, context) }
                                .background(color, MaterialTheme.shapes.small)
                        )
                    }
                }

                // Time indicator
                val radius = with(density) { 4.dp.toPx() }
                run {
                    val time = GregorianCalendar().also { it.timeInMillis = now }
                    val offsetDay = Jdn(time.toCivilDate()) - startingDay
                    val primary = MaterialTheme.colorScheme.primary
                    if (offsetDay in 0..<days) Canvas(Modifier
                        .offset {
                            IntOffset(
                                (cellWidthPx * offsetDay + firstColumnPx).roundToInt(),
                                (hoursFractionOfDay(time) * cellHeightPx).roundToInt()
                            )
                        }
                        .size(1.dp)
                    ) {
                        drawCircle(primary, radius)
                        drawLine(
                            color = primary,
                            start = Offset(if (isRtl) this.size.width else 0f, 0f),
                            end = Offset(
                                directionSign * if (days == 1) oneDayTableWidthPx else cellWidthPx,
                                0f
                            ),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Enabled times indicator
                getEnabledAlarms(context).takeIf { it.isNotEmpty() }?.let { enabledTimes ->
                    val strokeWidth = with(density) { 1.dp.toPx() }
                    val size = 12
                    val centerOffset = with(density) {
                        IntOffset((size / 2).dp.roundToPx(), (size / 2).dp.roundToPx())
                    }
                    val coordinates = coordinates.collectAsState().value ?: return@let
                    val circleRadius = with(density) { (size / 4).dp.toPx() }
                    val pathEffect = with(density) {
                        val dashSize = 4.dp.toPx()
                        PathEffect.dashPathEffect(floatArrayOf(dashSize, dashSize / 2))
                    }
                    (0..<days).map { offsetDay ->
                        val date = (startingDay + offsetDay).toGregorianCalendar()
                        val prayTimes = coordinates.calculatePrayTimes(date)
                        enabledTimes.forEach { prayTime ->
                            val tint = prayTime.tint.copy(alpha = AppBlendAlpha)
                            val position = IntOffset(
                                (cellWidthPx * offsetDay + firstColumnPx).roundToInt(),
                                (prayTimes[prayTime].value * cellHeightPx).roundToInt()
                            )
                            Canvas(
                                Modifier
                                    .offset { position }
                                    .size(1.dp),
                            ) {
                                if (offsetDay != 0) drawCircle(tint, circleRadius)
                                drawLine(
                                    color = tint,
                                    start = Offset(if (isRtl) this.size.width else 0f, 0f),
                                    end = Offset(
                                        directionSign * if (days == 1) oneDayTableWidthPx else cellWidthPx,
                                        0f
                                    ),
                                    pathEffect = pathEffect,
                                    strokeWidth = strokeWidth,
                                )
                            }
                            if (offsetDay == 0) Icon(
                                prayTime.imageVector,
                                null,
                                Modifier
                                    .offset { position - centerOffset }
                                    .size(size.dp),
                                tint,
                            )
                        }
                    }
                }

                // Add event box
                val x = offset?.let { (it.x / cellWidthPx).roundToInt() } ?: 0
                LaunchedEffect(selectedDay) {
                    val selectedDayIndex = selectedDay - startingDay
                    offset?.let {
                        if (selectedDayIndex != x)
                            offset = it.copy(x = selectedDayIndex * cellWidthPx)
                    }
                }
                val ySteps = (cellHeightPx / 4).roundToInt()
                val y = offset?.let { (it.y * scale.value / ySteps).roundToInt() } ?: 0
                val animatedOffset = if (offset == null) Offset.Zero
                else animateOffsetAsState(
                    Offset(x * cellWidthPx, y * ySteps.toFloat()),
                    animationSpec = if (interaction == Interaction.Zoom) snap() else {
                        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
                    },
                    label = "offset"
                ).value
                val dy = (duration / (cellHeightPx / 4) * scale.value).roundToInt()
                val animatedDuration by animateFloatAsState(
                    targetValue = dy * (cellHeightPx / 4),
                    animationSpec = if (interaction == Interaction.Zoom) snap() else spring(),
                    label = "duration"
                )
                val lifecycleOwner = LocalLifecycleOwner.current
                val widthReduction = remember { Animatable(defaultWidthReductionPx) }
                var resetOnNextRefresh by remember { mutableStateOf(false) }
                val addAction = {
                    if (offset == null) {
                        offset = Offset(
                            cellWidthPx * (selectedDay - startingDay),
                            ceil(scrollState.value / cellHeightPx) * cellHeightPx / scale.value
                        )
                        setAddEventBoxEnabled()
                    } else {
                        val time = selectedDay.toGregorianCalendar()
                        run {
                            val minutes = y * 15
                            time[GregorianCalendar.HOUR_OF_DAY] = minutes / 60
                            time[GregorianCalendar.MINUTE] = minutes % 60
                        }
                        time[GregorianCalendar.SECOND] = 0
                        time[GregorianCalendar.MILLISECOND] = 0
                        val beginTime = time.time
                        run {
                            val minutes = (y + dy) * 15
                            time[GregorianCalendar.HOUR_OF_DAY] = minutes / 60
                            time[GregorianCalendar.MINUTE] = minutes % 60
                        }
                        val endTime = time.time
                        addEvent(
                            AddEventData(
                                beginTime = beginTime,
                                endTime = endTime,
                                allDay = false,
                                description = dayTitleSummary(
                                    selectedDay,
                                    selectedDay.inCalendar(mainCalendar),
                                ),
                            )
                        )
                        resetOnNextRefresh = true
                    }
                }
                setAddAction(addAction)
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME && resetOnNextRefresh) {
                            duration = cellHeightPx / 4 * 4f
                            offset = null
                            resetOnNextRefresh = false
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }
                LaunchedEffect(isAddEventBoxEnabled) {
                    if (!isAddEventBoxEnabled && offset != null) offset = null
                }
                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                (animatedOffset.x + firstColumnPx).roundToInt(),
                                (animatedOffset.y + if (interaction == Interaction.ExtendUp) {
                                    duration * scale.value - animatedDuration
                                } else 0f).roundToInt(),
                            )
                        }
                        .size(
                            with(density) { cellWidthPx.toDp() - 1.dp },
                            with(density) {
                                (animatedDuration + if (interaction == Interaction.ExtendUp) {
                                    (offset?.y ?: 0f) * scale.value - animatedOffset.y
                                } else 0f).toDp()
                            },
                        )
                        .clickable(
                            indication = null,
                            interactionSource = null,
                            onClickLabel = stringResource(R.string.add_event)
                        ) { addAction() }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val id = awaitFirstDown().id
                                coroutineScope.launch { widthReduction.animateTo(0f) }
                                drag(id) {
                                    val position = offset ?: return@drag
                                    val delta = it.positionChange()
                                    if (interaction == null) interaction = when {
                                        abs(it.position.y - duration * scale.value) < cellHeightPx * scale.value * .2f -> Interaction.ExtendDown

                                        abs(it.position.y) < cellHeightPx * scale.value * .2f -> Interaction.ExtendUp
                                        else -> Interaction.Move
                                    }
                                    when (interaction) {
                                        Interaction.ExtendDown -> duration =
                                            (duration + delta.y / scale.value).coerceIn(
                                                minimumValue = ySteps * 1f,
                                                maximumValue = (ySteps * 24 * 4) - position.y
                                            )

                                        Interaction.ExtendUp -> {
                                            val newValueY = position.y + delta.y / scale.value
                                            offset = position.copy(
                                                y = newValueY.coerceIn(0f, cellHeightPx * 23)
                                            )
                                            duration =
                                                (duration - delta.y / scale.value).coerceAtLeast(
                                                    ySteps * 1f
                                                )
                                        }

                                        Interaction.Move -> {
                                            val newValueX = position.x + directionSign * delta.x
                                            val newValueY = position.y + delta.y / scale.value
                                            offset = Offset(
                                                newValueX.coerceIn(0f, tableWidthPx - cellWidthPx),
                                                newValueY.coerceIn(0f, cellHeightPx * 24 - duration)
                                            )

                                            val effectiveColumn =
                                                (position.x / cellWidthPx).roundToInt()
                                            setSelectedDay(startingDay + effectiveColumn)
                                        }

                                        Interaction.Zoom -> {}

                                        else -> null.debugAssertNotNull
                                    }
                                    it.consume()
                                }
                                interaction = null
                                coroutineScope.launch {
                                    widthReduction.animateTo(defaultWidthReductionPx)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) addEventBox@{
                    val alpha by animateFloatAsState(
                        if (offset == null) 0f else 1f,
                        animationSpec = spring(
                            Spring.DampingRatioNoBouncy, Spring.StiffnessLow
                        ), label = "alpha"
                    )
                    if (offset == null) return@addEventBox
                    val circleBorder = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
                    val background = MaterialTheme.colorScheme.surface.copy(alpha = AppBlendAlpha)
                    val primaryWithAlpha = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    val contentColor = LocalContentColor.current.copy(alpha = alpha)
                    Canvas(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxSize(),
                    ) {
                        val rectTopLeft = Offset(
                            x = if (isRtl) widthReduction.value else 0f,
                            y = if (animatedOffset.y < radius) 1.dp.toPx() else 0f,
                        )
                        val rectSize = Size(
                            width = this.size.width - widthReduction.value,
                            height = this.size.height - heightSizeReductionPx
                        )
                        drawRoundRect(
                            background,
                            size = rectSize,
                            topLeft = rectTopLeft,
                            cornerRadius = CornerRadius(SmallShapeCornerSize.dp.toPx()),
                        )
                        drawRoundRect(
                            primaryWithAlpha,
                            topLeft = rectTopLeft,
                            size = rectSize,
                            style = Stroke(1.dp.toPx()),
                            cornerRadius = CornerRadius(SmallShapeCornerSize.dp.toPx()),
                        )
                        val circleOffset = this.size.width * .05f
                        val offset1 = Offset(
                            x = this.center.x - (this.size.width / 2 - radius - circleOffset) * directionSign,
                            y = if (animatedOffset.y < radius) radius - animatedOffset.y else 0f
                        )
                        val circleBorderSize = 2.dp.toPx()
                        drawCircle(circleBorder, radius + circleBorderSize, offset1)
                        drawCircle(primaryWithAlpha, radius, offset1)
                        val offset2 = Offset(
                            x = this.center.x + (this.size.width / 2 - widthReduction.value - radius - circleOffset) * directionSign,
                            y = this.size.height - heightSizeReduction.toPx()
                        )
                        drawCircle(circleBorder, radius + circleBorderSize, offset2)
                        drawCircle(primaryWithAlpha, radius, offset2)
                    }
                    val compact = dy < 3 / scale.value
                    Text(
                        text = clockCache[y * 15] + when {
                            !compact -> "\n"
                            days == 7 -> " "
                            else -> " $EN_DASH "
                        } + clockCache[(y + dy) * 15],
                        color = contentColor,
                        style = if (days == 7 && compact) MaterialTheme.typography.bodySmall
                        else LocalTextStyle.current,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Visible,
                    )
                }
            }
            ScrollShadow(scrollState, top = true)
            ScrollShadow(scrollState, top = false)
        }
    }
}

private enum class Interaction { ExtendUp, ExtendDown, Move, Zoom }

private const val weeksLimit = 25000 // this should be an even number
private const val daysLimit = 175000 // this should be an even number
