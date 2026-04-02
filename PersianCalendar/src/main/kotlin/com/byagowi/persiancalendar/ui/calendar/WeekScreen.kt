package com.byagowi.persiancalendar.ui.calendar

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.lruCache
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_DAYS_SCREEN_ICON
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.preferredSwipeUpAction
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.daysTable
import com.byagowi.persiancalendar.ui.calendar.calendarpager.pagerArrowSizeAndPadding
import com.byagowi.persiancalendar.ui.common.AppFloatingActionButton
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.theme.noTransitionSpec
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.SmallShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import com.byagowi.persiancalendar.utils.readWeekDeviceEvents
import com.byagowi.persiancalendar.utils.toCivilDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun SharedTransitionScope.WeekScreen(
    refreshToken: Int,
    refreshCalendar: () -> Unit,
    commandBringDay: (Jdn) -> Unit,
    initiallySelectedDay: Jdn,
    navigateUp: () -> Unit,
    navigateToHolidaysSettings: (String?) -> Unit,
    today: Jdn,
    now: Long,
    modifier: Modifier = Modifier,
) {
    var selectedDay by rememberSaveable { mutableStateOf(initiallySelectedDay) }
    var isHighlighted by rememberSaveable { mutableStateOf(selectedDay != today) }
    val date = selectedDay on mainCalendar
    commandBringDay(selectedDay)
    val coroutineScope = rememberCoroutineScope()
    val weekInitialPage = remember(today) { weekPageFromJdn(initiallySelectedDay, today) }
    val weekPagerState = rememberPagerState(initialPage = weekInitialPage) { weeksLimit }
    val dayInitialPage = remember(today) { dayPageFromJdn(selectedDay, today) }
    val dayPagerState = rememberPagerState(initialPage = dayInitialPage) { daysLimit }
    var isWeekView by rememberSaveable { mutableStateOf(true) }
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

    val snackbarHostState = remember { SnackbarHostState() }
    val addEvent = addEvent(refreshCalendar, snackbarHostState)
    val density = LocalDensity.current
    val hasWeeksPager =
        LocalWindowInfo.current.containerSize.height > with(density) { 600.dp.toPx() }
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

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    fun onSwipeDown(isUp: Boolean) {
        if (!isLandscape && !isUp) when (preferredSwipeUpAction) {
            SwipeUpAction.WeekView -> navigateUp()
            else -> {}
        }
    }

    var fabPlaceholderHeight by remember { mutableStateOf<Dp?>(null) }
    val windowHeightPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        LocalActivity.current?.windowManager?.currentWindowMetrics?.bounds?.height()
    } else null

    AnimatedContent(
        isWeekView,
        transitionSpec = noTransitionSpec,
        modifier = modifier,
    ) { isWeekViewState ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                AppFloatingActionButton(
                    onClick = addAction,
                    modifier = Modifier
                        .onGloballyPositioned {
                            if (windowHeightPx != null) fabPlaceholderHeight = with(density) {
                                (windowHeightPx - it.positionInWindow().y).toDp()
                            } + 4.dp
                        }
                        .padding(end = 8.dp),
                ) { Icon(Icons.Default.Add, stringResource(R.string.add_event)) }
            },
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                    modifier = Modifier.detectSwipe { ::onSwipeDown },
                    title = {
                        Column(
                            Modifier.clickable(
                                interactionSource = null,
                                indication = ripple(bounded = false),
                                onClickLabel = stringResource(
                                    if (!isWeekView) R.string.week_view else R.string.calendar,
                                ),
                            ) { if (!isWeekView) isWeekView = true else navigateUp() },
                        ) {
                            val secondaryCalendar = secondaryCalendar
                            val title: String
                            val subtitle: String
                            if (secondaryCalendar == null) {
                                title = if (hasWeeksPager) date.monthName else language.dm.format(
                                    numeral.format(date.dayOfMonth), date.monthName,
                                )
                                subtitle = numeral.format(date.year)
                            } else {
                                title = if (hasWeeksPager) language.my.format(
                                    date.monthName,
                                    numeral.format(date.year),
                                ) else language.dmy.format(
                                    numeral.format(date.dayOfMonth),
                                    date.monthName,
                                    numeral.format(date.year),
                                )
                                val secondaryDate = selectedDay on secondaryCalendar
                                subtitle = if (hasWeeksPager) language.my.format(
                                    secondaryDate.monthName,
                                    numeral.format(secondaryDate.year),
                                ) else language.dmy.format(
                                    numeral.format(secondaryDate.dayOfMonth),
                                    secondaryDate.monthName,
                                    numeral.format(secondaryDate.year),
                                )
                            }

                            Crossfade(targetState = title) { state ->
                                Text(state, style = MaterialTheme.typography.titleLarge)
                            }
                            Crossfade(targetState = subtitle) { state ->
                                Text(state, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    },
                    colors = appTopAppBarColors(),
                    navigationIcon = {
                        if (isAddEventBoxEnabled) {
                            @SuppressLint("UseBackHandlerInsteadOfPredictiveBackHandler") BackHandler {
                                isAddEventBoxEnabled = false
                            }
                        }
                        NavigationNavigateUpIcon {
                            if (isAddEventBoxEnabled) isAddEventBoxEnabled = false else navigateUp()
                        }
                    },
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
                                boundsTransform = appBoundsTransform,
                            ),
                        ) {
                            val title = if (isWeekView) stringResource(R.string.day_view)
                            else stringResource(R.string.week_view)
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above,
                                ),
                                tooltip = { PlainTooltip { Text(title) } },
                                state = rememberTooltipState(),
                            ) {
                                if (isWeekViewState) Icon(Icons.Default.CalendarViewDay, title)
                                else Icon(Icons.Default.CalendarViewWeek, title)
                            }
                        }
                    },
                )
            },
        ) { paddingValues ->
            val bottomPadding = paddingValues.calculateBottomPadding().coerceAtLeast(16.dp)
            BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
                val screenWidth = this.maxWidth
                val pagerSize =
                    calendarPagerSize(false, this.maxWidth, this.maxHeight, bottomPadding, true)
                // Don't show weeks pager if there isn't enough space
                Column {
                    val daysTable = daysTable(
                        modifier = Modifier.detectSwipe { ::onSwipeDown },
                        suggestedPagerSize = pagerSize,
                        addEvent = addEvent,
                        today = today,
                        refreshToken = refreshToken,
                        setSelectedDay = setSelectedDayInWeekPager,
                        secondaryCalendar = secondaryCalendar,
                        arrowAction = { isPrevious, _ ->
                            coroutineScope.launch {
                                weekPagerState.animateScrollToPage(
                                    weekPagerState.currentPage + if (isPrevious) -1 else 1,
                                )
                            }
                        },
                        isWeekMode = true,
                    )

                    val scale = remember { mutableFloatStateOf(1f) }
                    val cellHeight = (defaultCellHeight * scale.floatValue).dp
                    val initialScroll =
                        with(density) { (cellHeight * initialHour * scale.floatValue - 16.dp).roundToPx() }
                    val scrollState = rememberScrollState(initialScroll)
                    val swipeDownOnScrollableModifier = Modifier.detectSwipe {
                        val wasAtTop = scrollState.value == 0
                        { isUp: Boolean -> if (wasAtTop) onSwipeDown(isUp) }
                    }
                    val weekStart = weekStart

                    HorizontalPager(
                        state = weekPagerState,
                        verticalAlignment = Alignment.Top,
                        pageSpacing = 2.dp,
                    ) { page ->
                        Column {
                            val offset = page - weeksLimit / 2
                            val sampleDay = today + 7 * offset
                            val startOfYearJdn = Jdn(mainCalendar, date.year, 1, 1)
                            val week = sampleDay.getWeekOfYear(startOfYearJdn, weekStart)

                            val isCurrentPage = weekPagerState.currentPage == page
                            LaunchedEffect(isCurrentPage) {
                                if (isCurrentPage && selectedDay.getWeekOfYear(
                                        startOfYearJdn,
                                        weekStart,
                                    ) != week
                                ) {
                                    val pageDay =
                                        sampleDay + (selectedDay.weekDay.ordinal - today.weekDay.ordinal)
                                    setSelectedDayInWeekPager(pageDay)
                                    isHighlighted = today != pageDay
                                }
                            }

                            val context = LocalContext.current
                            val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(
                                today, mainCalendar.getMonthsDistance(today, selectedDay),
                            )
                            val monthStartJdn = Jdn(monthStartDate)
                            val pageWeekStart = (today + (page - weeksLimit / 2) * 7).let {
                                it - (it.weekDay - weekStart)
                            }
                            val weekDeviceEvents = remember(
                                refreshToken,
                                isShowDeviceCalendarEvents,
                                pageWeekStart,
                            ) {
                                if (isShowDeviceCalendarEvents) {
                                    context.readWeekDeviceEvents(pageWeekStart)
                                } else EventsStore.empty()
                            }

                            if (hasWeeksPager) daysTable(
                                monthStartDate,
                                monthStartJdn,
                                weekDeviceEvents,
                                week,
                                isHighlighted,
                                selectedDay,
                            )

                            if (isWeekViewState) ScreenSurface(
                                mayNeedDragHandleToDivide = !isLandscape,
                                disableSharedContent = initiallySelectedDay - pageWeekStart !in 0..<7,
                            ) {
                                DaysView(
                                    modifier = if (weekPagerState.currentPage == page) Modifier.sharedBounds(
                                        rememberSharedContentState(key = SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        boundsTransform = appBoundsTransform,
                                    ) else Modifier,
                                    scrollableModifier = swipeDownOnScrollableModifier,
                                    bottomPadding = fabPlaceholderHeight ?: 0.dp,
                                    onAddActionChange = {
                                        if (weekPagerState.currentPage == page) addAction = it
                                    },
                                    hasWeekPager = hasWeeksPager,
                                    startingDay = pageWeekStart,
                                    selectedDay = selectedDay,
                                    onSelectedDayChange = setSelectedDayInWeekPager,
                                    addEvent = addEvent,
                                    refreshCalendar = refreshCalendar,
                                    days = 7,
                                    deviceEvents = weekDeviceEvents,
                                    now = now,
                                    isAddEventBoxEnabled = isAddEventBoxEnabled,
                                    onAddEventBoxEnabledChange = { isAddEventBoxEnabled = it },
                                    snackbarHostState = snackbarHostState,
                                    navigateToHolidaysSettings = navigateToHolidaysSettings,
                                    screenWidth = screenWidth,
                                    scrollState = scrollState,
                                    initialScroll = initialScroll,
                                    cellHeight = cellHeight,
                                    scale = scale,
                                    numeral = numeral,
                                )
                            }
                        }
                    }

                    if (!isWeekViewState) ScreenSurface(mayNeedDragHandleToDivide = !isLandscape) {
                        HorizontalPager(
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = appBoundsTransform,
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
                                refreshToken, isShowDeviceCalendarEvents, pageDay,
                            ) {
                                if (isShowDeviceCalendarEvents) {
                                    context.readDayDeviceEvents(pageDay)
                                } else EventsStore.empty()
                            }

                            DaysView(
                                bottomPadding = fabPlaceholderHeight ?: 0.dp,
                                onAddActionChange = {
                                    if (dayPagerState.currentPage == page) addAction = it
                                },
                                startingDay = pageDay,
                                selectedDay = selectedDay,
                                onSelectedDayChange = { selectedDay = it; isHighlighted = true },
                                addEvent = addEvent,
                                refreshCalendar = refreshCalendar,
                                days = 1,
                                now = now,
                                isAddEventBoxEnabled = isAddEventBoxEnabled,
                                onAddEventBoxEnabledChange = { isAddEventBoxEnabled = it },
                                snackbarHostState = snackbarHostState,
                                navigateToHolidaysSettings = navigateToHolidaysSettings,
                                hasWeekPager = hasWeeksPager,
                                deviceEvents = dayDeviceEvents,
                                screenWidth = screenWidth,
                                scrollState = scrollState,
                                scale = scale,
                                initialScroll = initialScroll,
                                cellHeight = cellHeight,
                                scrollableModifier = swipeDownOnScrollableModifier,
                                numeral = numeral,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun weekPageFromJdn(day: Jdn, today: Jdn): Int {
    val daysStart = day - (day.weekDay - weekStart)
    val todayStart = today - (today.weekDay - weekStart)
    return (daysStart - todayStart) / 7 + weeksLimit / 2
}

private fun dayPageFromJdn(day: Jdn, today: Jdn): Int = day - today + daysLimit / 2

private fun hoursFractionOfDay(date: GregorianCalendar): Float =
    date[GregorianCalendar.HOUR_OF_DAY] + date[GregorianCalendar.MINUTE] / 60f

private data class EventDivision(
    val event: CalendarEvent.DeviceCalendarEvent, val column: Int, val columnsCount: Int,
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

const val defaultCellHeight = 64
const val initialHour = 8

@SuppressLint("ComposeModifierWithoutDefault")
@Composable
fun DaysView(
    onAddActionChange: (() -> Unit) -> Unit,
    startingDay: Jdn,
    selectedDay: Jdn,
    onSelectedDayChange: (Jdn) -> Unit,
    bottomPadding: Dp,
    addEvent: (AddEventData) -> Unit,
    refreshCalendar: () -> Unit,
    now: Long,
    days: Int,
    isAddEventBoxEnabled: Boolean,
    onAddEventBoxEnabledChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    hasWeekPager: Boolean,
    deviceEvents: DeviceCalendarEventsStore,
    navigateToHolidaysSettings: (String?) -> Unit,
    screenWidth: Dp,
    scrollState: ScrollState,
    scale: MutableFloatState,
    initialScroll: Int,
    cellHeight: Dp,
    numeral: Numeral,
    @SuppressLint("ModifierParameter") scrollableModifier: Modifier,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.(ImmutableList<CalendarEvent<*>>, (Boolean) -> Unit) -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var interaction by remember { mutableStateOf<Interaction?>(null) }
    Column(
        modifier.detectZoom(
            onZoom = {
                if (interaction == null) interaction = Interaction.Zoom
                if (interaction == Interaction.Zoom) coroutineScope.launch {
                    scale.floatValue = (scale.floatValue * it).coerceIn(.5f, 2f)
                }
            },
            onRelease = { if (interaction == Interaction.Zoom) interaction = null },
        ),
    ) {
        val events = (0..<days).map { index ->
            readEventsWithEquinox(startingDay + index, now, deviceEvents)
        }
        val eventsWithTime = events.map { dayEvents ->
            addDivisions(
                dayEvents.filterIsInstance<CalendarEvent.DeviceCalendarEvent>()
                    .filter { it.time != null }.sortedWith { x, y ->
                        (x.start.timeInMillis compareTo y.end.timeInMillis).let {
                            if (it != 0) return@sortedWith it
                        }
                        // If both start at the same time, put bigger events first, better for interval graphs
                        y.start.timeInMillis compareTo x.end.timeInMillis
                    },
            )
        }
        val eventsWithoutTime = remember(events) {
            events.map { dayEvents ->
                dayEvents.filter { it !is CalendarEvent.DeviceCalendarEvent || it.time == null }
                    .toImmutableList()
            }.toImmutableList()
        }
        val maxDayAllDayEvents = eventsWithoutTime.maxOf { it.size }
        var hasContent by remember { mutableStateOf(true) }
        val hasHeader by remember(events, hasContent) {
            val needsHeader =
                maxDayAllDayEvents != 0 || (days != 1 && !hasWeekPager) || (content != null && hasContent)
            derivedStateOf { needsHeader && scrollState.value <= initialScroll }
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
        AnimatedVisibility(
            visible = hasHeader,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
            ),
        ) {
            if (days == 1) {
                var isExpanded by rememberSaveable { mutableStateOf(false) }
                val clickToExpandModifier = Modifier.clickable(
                    onClickLabel = stringResource(R.string.more),
                    interactionSource = null,
                    indication = null,
                ) { isExpanded = !isExpanded }
                Box(
                    modifier = if (eventsWithoutTime[0].size > 3) clickToExpandModifier else Modifier,
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        Modifier
                            .verticalScroll(scrollState)
                            .fillMaxWidth()
                            .then(
                                if (isAddEventBoxEnabled) Modifier.clickable(
                                    indication = null,
                                    interactionSource = null,
                                ) { onAddEventBoxEnabledChange(false) } else Modifier,
                            ),
                    ) {
                        Spacer(Modifier.height(12.dp))
                        val headerHasFilled =
                            scrollState.canScrollBackward || scrollState.canScrollForward
                        val displayedEvents =
                            (if (headerHasFilled || isExpanded || isTalkBackEnabled) events else eventsWithoutTime)[0].filter { content == null || it.source == null }
                        val dayEvents = displayedEvents.let { if (isExpanded) it else it.take(3) }
                            .toImmutableList()
                        DayEvents(
                            events = dayEvents,
                            navigateToHolidaysSettings = navigateToHolidaysSettings,
                            refreshCalendar = refreshCalendar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(appContentSizeAnimationSpec)
                                .padding(horizontal = 24.dp),
                        )
                        AnimatedVisibility(dayEvents.isNotEmpty()) {
                            Box(Modifier.height(12.dp))
                        }
                        val appointments =
                            eventsWithoutTime[0].filter { it.source != null }.toImmutableList()
                        if (displayedEvents.size > 3) {
                            Spacer(Modifier.height(4.dp))
                            ExpandArrow(
                                isExpanded = isExpanded,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                            if (content != null) {
                                content(appointments) { hasContent = it }
                            } else Spacer(Modifier.height(8.dp))
                            if (isExpanded && headerHasFilled) {
                                Spacer(Modifier.height(bottomPadding))
                            }
                        } else if (content != null) {
                            content(appointments) { hasContent = it }
                        } else Spacer(Modifier.height(12.dp))
                    }
                }
            } else if (maxDayAllDayEvents != 0) EventsRow(
                cellWidth = cellWidth,
                events = eventsWithoutTime,
                horizontalPadding = pagerArrowSizeAndPadding.dp,
                defaultWidthReduction = defaultWidthReduction,
                launcher = launcher,
                itemsTextStyle = MaterialTheme.typography.bodySmall.copy(
                    textDirection = TextDirection.Content,
                ),
                itemHeight = with(density) { 24.sp.toDp() },
                defaultItems = 3,
                shape = MaterialTheme.shapes.small,
                snackbarHostState = snackbarHostState,
            ) { dayIndex, content ->
                if (!hasWeekPager) {
                    val weekDay = weekStart + dayIndex
                    val weekDayTitle = weekDay.title
                    val isLandscape =
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    Text(
                        text = if (isLandscape) weekDayTitle else weekDay.shortTitle,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .width(cellWidth)
                            .semantics { this.contentDescription = weekDayTitle },
                    )
                }
                content()
            }
        }

        // Time cells, table and indicators
        Box {
            Box(
                Modifier
                    .windowInsetsPadding(
                        WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
                    )
                    .verticalScroll(scrollState)
                    .then(scrollableModifier),
            ) {
                val firstColumnPx = with(density) { pagerArrowSizeAndPadding.dp.toPx() }
                val oneDayTableWidthPx = with(density) { (tableWidth + 24.dp).toPx() }
                val tableWidthPx = with(density) { tableWidth.toPx() }
                val cellWidthPx = tableWidthPx / days
                val cellHeightPx = with(density) { cellHeight.toPx() }
                var offset by remember(tableWidthPx) { mutableStateOf(Offset.Zero) }
                var duration by remember { mutableFloatStateOf(cellHeightPx / scale.floatValue) }
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val directionSign = if (isRtl) -1 else 1
                val heightSizeReduction = 3.dp
                val heightSizeReductionPx = with(density) { heightSizeReduction.toPx() }
                val clockCache = remember {
                    lruCache(
                        1024,
                        create = { minutes: Int ->
                            Clock(minutes / 60.0).toBasicFormatString()
                        },
                    )
                }

                // Add event box
                val x = (offset.x / cellWidthPx).roundToInt()
                LaunchedEffect(selectedDay) {
                    val selectedDayIndex = selectedDay - startingDay
                    if (selectedDayIndex != x) {
                        offset = offset.copy(x = selectedDayIndex * cellWidthPx)
                    }
                }
                val ySteps = (cellHeightPx / 4).roundToInt()
                val y = (offset.y * scale.floatValue / ySteps).roundToInt()
                val animatedOffset by animateOffsetAsState(
                    targetValue = Offset(x * cellWidthPx, y * ySteps.toFloat()),
                    animationSpec = when (interaction) {
                        Interaction.Zoom, Interaction.AddBox -> snap()
                        else -> spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
                    },
                )
                if (interaction == Interaction.AddBox) interaction = null
                val dy = (duration / (cellHeightPx / 4) * scale.floatValue).roundToInt()
                val animatedDuration by animateFloatAsState(
                    targetValue = dy * (cellHeightPx / 4),
                    animationSpec = if (interaction == Interaction.Zoom) snap() else spring(),
                )
                val widthReduction = remember { Animatable(defaultWidthReductionPx) }
                var resetOnNextRefresh by remember { mutableStateOf(false) }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    if (resetOnNextRefresh) {
                        duration = cellHeightPx / 4 * 4f
                        offset = Offset.Zero
                        onAddEventBoxEnabledChange(false)
                        resetOnNextRefresh = false
                    }
                }
                val addAction = {
                    if (!isAddEventBoxEnabled && !isTalkBackEnabled) {
                        interaction = Interaction.AddBox
                        duration = cellHeightPx / scale.floatValue
                        offset = Offset(
                            cellWidthPx * (selectedDay - startingDay),
                            ceil(scrollState.value / cellHeightPx) * cellHeightPx / scale.floatValue,
                        )
                        onAddEventBoxEnabledChange(true)
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
                                    selectedDay on mainCalendar,
                                ),
                            ),
                        )
                        resetOnNextRefresh = true
                    }
                }
                onAddActionChange(addAction)

                // Time cells and table
                val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                Row(
                    Modifier.drawBehind {
                        val topLineY = 2.dp.toPx()
                        val paintCellWidthPx = if (days == 1) oneDayTableWidthPx else cellWidthPx
                        repeat(days + 1) { i ->
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
                        repeat(24) {
                            val x = x1 - if (it != 0) extraLineWidth else 0f
                            val y = if (it != 0) cellHeightPx * it else topLineY
                            drawLine(outlineVariant, Offset(x, y), Offset(x2, y))
                        }
                    },
                ) {
                    repeat(days + 2) { column ->
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
                                                onClickLabel = stringResource(R.string.add_event),
                                            ) {
                                                if (!isAddEventBoxEnabled) {
                                                    interaction = Interaction.AddBox
                                                    duration = cellHeightPx / scale.floatValue
                                                }
                                                offset = Offset(
                                                    cellWidthPx * (column - 1),
                                                    cellHeightPx * row / scale.floatValue,
                                                )
                                                onAddEventBoxEnabledChange(true)
                                                duration = cellHeightPx / scale.floatValue
                                                onSelectedDayChange(startingDay + column - 1)
                                                if (isTalkBackEnabled) addAction()
                                            }
                                            .semantics {
                                                if (isTalkBackEnabled) {
                                                    this.contentDescription = listOf(
                                                        (startingDay + (column - 1)).weekDay.title,
                                                        clockCache[row * 60],
                                                        clockCache[(row + 1) * 60],
                                                    ).joinToString(" ")
                                                }
                                            }
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
                eventsWithTime.forEachIndexed { i, it ->
                    it.forEach { (event, column, columnsCount) ->
                        val start = hoursFractionOfDay(event.start)
                        val end = hoursFractionOfDay(event.end)
                        val color = eventColor(event)
                        Text(
                            " " + event.title,
                            color = eventTextColor(color),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDirection = TextDirection.Content,
                            ),
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (firstColumnPx + cellWidthPx * i + cellWidthPx / columnsCount * column).roundToInt(),
                                        (start * cellHeightPx).roundToInt(),
                                    )
                                }
                                .requiredSize(
                                    with(density) { (cellWidthPx / columnsCount - defaultWidthReductionPx).toDp() },
                                    with(density) { ((end - start) * cellHeightPx).toDp() - heightSizeReduction },
                                )
                                .clickable { launcher.viewEvent(event, context) }
                                .background(color, MaterialTheme.shapes.small)
                                .padding(all = 4.dp),
                        )
                    }
                }

                // Time indicator
                val radius = with(density) { 4.dp.toPx() }
                run {
                    val time = GregorianCalendar().also { it.timeInMillis = now }
                    val offsetDay = Jdn(time.toCivilDate()) - startingDay
                    val primary = MaterialTheme.colorScheme.primary
                    if (offsetDay in 0..<days) Canvas(
                        Modifier
                            .offset {
                                IntOffset(
                                    (cellWidthPx * offsetDay + firstColumnPx).roundToInt(),
                                    (hoursFractionOfDay(time) * cellHeightPx).roundToInt(),
                                )
                            }
                            .size(1.dp),
                    ) {
                        drawCircle(primary, radius)
                        drawLine(
                            color = primary,
                            start = Offset(if (isRtl) this.size.width else 0f, 0f),
                            end = Offset(
                                directionSign * if (days == 1) oneDayTableWidthPx else cellWidthPx,
                                0f,
                            ),
                            strokeWidth = 1.dp.toPx(),
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
                    val coordinates = coordinates ?: return@let
                    val circleRadius = with(density) { (size / 4).dp.toPx() }
                    val pathEffect = with(density) {
                        val dashSize = 4.dp.toPx()
                        PathEffect.dashPathEffect(floatArrayOf(dashSize, dashSize / 2))
                    }
                    repeat(days) { offsetDay ->
                        val date = (startingDay + offsetDay).toGregorianCalendar()
                        val prayTimes = coordinates.calculatePrayTimes(date)
                        enabledTimes.forEach { prayTime ->
                            val tint = prayTime.tint.copy(alpha = AppBlendAlpha)
                            val position = IntOffset(
                                (cellWidthPx * offsetDay + firstColumnPx).roundToInt(),
                                ((prayTimes[prayTime].value.takeIf { !it.isNaN() }
                                    ?: .0) * cellHeightPx).roundToInt(),
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
                                        0f,
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

                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                (animatedOffset.x + firstColumnPx).roundToInt(),
                                (animatedOffset.y + if (interaction == Interaction.ExtendUp) {
                                    duration * scale.floatValue - animatedDuration
                                } else 0f).roundToInt(),
                            )
                        }
                        .size(
                            with(density) { cellWidthPx.toDp() - 1.dp },
                            with(density) {
                                (animatedDuration + if (interaction == Interaction.ExtendUp) {
                                    offset.y * scale.floatValue - animatedOffset.y
                                } else 0f).toDp()
                            },
                        )
                        .clickable(
                            indication = null,
                            interactionSource = null,
                            onClickLabel = stringResource(R.string.add_event),
                        ) { addAction() }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val id = awaitFirstDown().id
                                coroutineScope.launch { widthReduction.animateTo(0f) }
                                drag(id) {
                                    if (!isAddEventBoxEnabled) return@drag
                                    val position = offset
                                    val delta = it.positionChange()
                                    if (interaction == null) interaction = when {
                                        abs(it.position.y - duration * scale.floatValue) < cellHeightPx / scale.floatValue * .2f -> Interaction.ExtendDown

                                        abs(it.position.y) < cellHeightPx / scale.floatValue * .2f -> Interaction.ExtendUp
                                        else -> Interaction.Move
                                    }
                                    when (interaction) {
                                        Interaction.ExtendDown -> duration =
                                            (duration + delta.y / scale.floatValue).coerceIn(
                                                minimumValue = ySteps * 1f / scale.floatValue,
                                                maximumValue = (ySteps * 24 * 4 / scale.floatValue - position.y).coerceAtLeast(
                                                    ySteps * 1f,
                                                ),
                                            )

                                        Interaction.ExtendUp -> {
                                            val newValueY = position.y + delta.y / scale.floatValue
                                            offset = position.copy(
                                                y = newValueY.coerceIn(
                                                    0f,
                                                    cellHeightPx / scale.floatValue * 23,
                                                ),
                                            )
                                            duration =
                                                (duration - delta.y / scale.floatValue).coerceAtLeast(
                                                    ySteps * 1f,
                                                )
                                        }

                                        Interaction.Move -> {
                                            val newValueX = position.x + directionSign * delta.x
                                            val newValueY = position.y + delta.y / scale.floatValue
                                            offset = Offset(
                                                newValueX.coerceIn(0f, tableWidthPx - cellWidthPx),
                                                newValueY.coerceIn(
                                                    0f,
                                                    cellHeightPx / scale.floatValue * 24 - duration,
                                                ),
                                            )

                                            val effectiveColumn =
                                                (position.x / cellWidthPx).roundToInt()
                                            onSelectedDayChange(startingDay + effectiveColumn)
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
                        targetValue = if (isAddEventBoxEnabled) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                    if (alpha == 0f) return@addEventBox
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
                            height = this.size.height - heightSizeReductionPx,
                        )
                        drawRoundRect(
                            color = background,
                            size = rectSize,
                            topLeft = rectTopLeft,
                            cornerRadius = CornerRadius(SmallShapeCornerSize.dp.toPx()),
                        )
                        drawRoundRect(
                            color = primaryWithAlpha,
                            topLeft = rectTopLeft,
                            size = rectSize,
                            style = Stroke(1.dp.toPx()),
                            cornerRadius = CornerRadius(SmallShapeCornerSize.dp.toPx()),
                        )
                        val circleOffset = this.size.width * .05f
                        val offset1 = Offset(
                            x = this.center.x - (this.size.width / 2 - radius - circleOffset) * directionSign,
                            y = if (animatedOffset.y < radius) radius - animatedOffset.y else 0f,
                        )
                        val circleBorderSize = 2.dp.toPx()
                        drawCircle(circleBorder, radius + circleBorderSize, offset1)
                        drawCircle(primaryWithAlpha, radius, offset1)
                        val offset2 = Offset(
                            x = this.center.x + (this.size.width / 2 - widthReduction.value - radius - circleOffset) * directionSign,
                            y = this.size.height - heightSizeReduction.toPx(),
                        )
                        drawCircle(circleBorder, radius + circleBorderSize, offset2)
                        drawCircle(primaryWithAlpha, radius, offset2)
                    }
                    val compact = dy < 3 / scale.floatValue || run {
                        // This is an ugly hack as the lack of proper autosize here, for now
                        !numeral.isArabicIndicVariants
                    }
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
            ScrollShadow(scrollState)
        }
    }
}

@Composable
fun EventsRow(
    cellWidth: Dp,
    events: ImmutableList<ImmutableList<CalendarEvent<*>>>,
    defaultWidthReduction: Dp,
    launcher: ManagedActivityResultLauncher<Long, Void?>,
    snackbarHostState: SnackbarHostState,
    horizontalPadding: Dp,
    itemsTextStyle: TextStyle,
    defaultItems: Int,
    itemHeight: Dp,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(i: Int, content: @Composable () -> Unit) -> Unit = { _, content -> content() },
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val clickToExpandModifier = Modifier.clickable(
        onClickLabel = stringResource(R.string.more),
        interactionSource = null,
        indication = null,
    ) { isExpanded = !isExpanded }
    val maxEventsCount = events.maxOf { it.size }
    Box {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = modifier.then(
                if (maxEventsCount > defaultItems) clickToExpandModifier else Modifier,
            ),
        ) {
            Row(
                Modifier
                    .animateContentSize(appContentSizeAnimationSpec)
                    .padding(horizontal = horizontalPadding),
            ) {
                events.forEachIndexed { i, dayEvents ->
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        content(i) {
                            dayEvents.forEachIndexed { i, event ->
                                if (isExpanded || i < (defaultItems - 1) || (i == (defaultItems - 1) && dayEvents.size == defaultItems)) {
                                    Box(
                                        Modifier
                                            .requiredSize(
                                                width = cellWidth - defaultWidthReduction,
                                                height = itemHeight,
                                            )
                                            .padding(
                                                top = if (i == 0) 2.dp else 0.dp,
                                                bottom = 2.dp,
                                            )
                                            .clip(shape)
                                            .background(eventColor(event))
                                            .clickable {
                                                if (event is CalendarEvent.DeviceCalendarEvent) {
                                                    launcher.viewEvent(event, context)
                                                } else coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(event.title)
                                                }
                                            },
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        Text(
                                            " " + event.title,
                                            maxLines = 1,
                                            style = itemsTextStyle,
                                            color = eventTextColor(eventColor(event)),
                                        )
                                    }
                                }
                                if (i == defaultItems - 1 && dayEvents.size > defaultItems && !isExpanded) Text(
                                    " +" + numeral.format(dayEvents.size - defaultItems),
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    maxLines = 1,
                                    style = itemsTextStyle,
                                )
                            }
                        }
                    }
                }
            }
        }
        Box(
            Modifier
                .width(pagerArrowSizeAndPadding.dp)
                .padding(bottom = 2.dp)
                .align(Alignment.BottomStart),
            contentAlignment = Alignment.BottomCenter,
        ) { if (maxEventsCount > defaultItems) ExpandArrow(isExpanded = isExpanded) }
    }
}

private enum class Interaction { ExtendUp, ExtendDown, Move, Zoom, AddBox }

private const val weeksLimit = 25000 // this should be an even number
private const val daysLimit = 175000 // this should be an even number
