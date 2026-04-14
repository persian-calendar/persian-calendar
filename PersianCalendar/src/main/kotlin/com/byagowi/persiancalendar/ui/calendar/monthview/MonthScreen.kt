package com.byagowi.persiancalendar.ui.calendar.monthview

import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarNumeral
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.preferredSwipeDownAction
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.ui.calendar.EventsRow
import com.byagowi.persiancalendar.ui.calendar.SwipeDownAction
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.outOfMonthAlpha
import com.byagowi.persiancalendar.ui.calendar.calendarpager.pagerArrowSizeAndPadding
import com.byagowi.persiancalendar.ui.calendar.calendarpager.todayCircleWidth
import com.byagowi.persiancalendar.ui.calendar.detectSwipe
import com.byagowi.persiancalendar.ui.calendar.readEvents
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.isLandscape
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readWeekDeviceEvents
import com.byagowi.persiancalendar.utils.viewEvent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun SharedTransitionScope.MonthScreen(
    navigateUp: () -> Unit,
    initiallySelectedDay: Jdn,
    today: Jdn,
    refreshCalendar: () -> Unit,
    commandBringDay: (Jdn) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialItem = ITEMS_COUNT / 2
    val state = rememberLazyListState(initialItem, 0)
    val weekStartJdn = initiallySelectedDay - initiallySelectedDay.weekDay.ordinal
    val monthStart = Jdn(
        mainCalendar.getMonthStartFromMonthsDistance(initiallySelectedDay, 0),
    )
    val focusedJdn by remember {
        derivedStateOf {
            if (state.firstVisibleItemIndex == initialItem) monthStart
            else weekStartJdn + 3 + (state.firstVisibleItemIndex - initialItem - 1 + if (state.firstVisibleItemScrollOffset == 0) -1 else 0) * 7
        }
    }
    val focusedDate = focusedJdn on mainCalendar
    val viewEvent = viewEvent(refreshCalendar)
    val isLandscape = isLandscape()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier
            .detectSwipe {
                { isUp ->
                    if (!isLandscape && isUp) when (preferredSwipeDownAction) {
                        SwipeDownAction.MonthView -> navigateUp()
                        else -> {}
                    }
                }
            }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = .15f)),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    val date = focusedJdn on mainCalendar
                    val screenTitle = stringResource(R.string.schedule)
                    Column(Modifier.semantics { this.contentDescription = screenTitle }) {
                        Crossfade(targetState = date.monthName) { state ->
                            Text(state, style = MaterialTheme.typography.titleLarge)
                        }
                        Crossfade(targetState = numeral.format(date.year)) { state ->
                            Text(state, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
                actions = {
                    TodayActionButton(
                        visible = remember {
                            derivedStateOf { state.firstVisibleItemIndex != initialItem }
                        }.value,
                    ) { coroutineScope.launch { state.animateScrollToItem(index = initialItem) } }
                    AppIconButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        title = stringResource(R.string.next_x, stringResource(R.string.week)),
                    ) {
                        coroutineScope.launch {
                            state.animateScrollToItem(index = state.firstVisibleItemIndex + 1)
                        }
                    }
                    AppIconButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        title = stringResource(R.string.previous_x, stringResource(R.string.week)),
                    ) {
                        coroutineScope.launch {
                            state.animateScrollToItem(index = state.firstVisibleItemIndex - 1)
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val bottomPadding = paddingValues.calculateBottomPadding()
            val pagerSize = calendarPagerSize(isLandscape, maxWidth, maxHeight, bottomPadding)
            val (width, suggestedHeight) = pagerSize
            val cellWidth = (width - (pagerArrowSizeAndPadding * 2).dp) / 10
            val cellHeight = suggestedHeight / 7
            val density = LocalDensity.current
            val diameter = min(cellWidth, cellHeight)

            Column(
                modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            ) {
                val defaultWidthReduction = 2.dp
                val isShowWeekOfYearEnabled = isShowWeekOfYearEnabled
                val weekOfYearWeight = .625f
                Row {
                    if (isShowWeekOfYearEnabled) Spacer(Modifier.weight(weekOfYearWeight))
                    repeat(7) { column ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(diameter)
                                .weight(1f),
                        ) {
                            val weekDay = weekStart + column
                            val description = stringResource(
                                R.string.week_days_name_column,
                                weekDay.title,
                            )
                            Text(
                                weekDay.shortTitle,
                                fontSize = with(density) { (diameter * .6f).toSp() },
                                modifier = Modifier
                                    .alpha(AppBlendAlpha)
                                    .semantics { this.contentDescription = description },
                            )
                        }
                    }
                }
                val monthColors = appMonthColors()
                val indicatorFillColor by animateColor(monthColors.indicatorFill)
                val holidaysFillColor by animateColor(monthColors.holidaysFill)
                val todayOutlineColor by animateColor(monthColors.todayOutline)
                val holidaysColor by animateColor(monthColors.holidays)
                val baseJdn = monthStart - (monthStart.weekDay - weekStart)
                val itemHeight = with(density) { 20.sp.toDp() }
                LazyColumn(state = state) {
                    items(ITEMS_COUNT) { index ->
                        val weekJdn = indexToJdn(baseJdn, index)
                        val context = LocalContext.current
                        val resources = LocalResources.current
                        val deviceEvents = remember(index) { context.readWeekDeviceEvents(weekJdn) }
                        val events = (0..<7).map { index ->
                            readEvents(weekJdn + index, deviceEvents)
                        }.toImmutableList()
                        EventsRow(
                            events = events,
                            horizontalPadding = 0.dp,
                            defaultWidthReduction = defaultWidthReduction,
                            viewEvent = viewEvent,
                            itemsTextStyle = MaterialTheme.typography.labelSmall.copy(
                                textDirection = TextDirection.Content,
                            ),
                            itemHeight = itemHeight,
                            defaultItems = 7,
                            shape = MaterialTheme.shapes.extraSmall,
                            snackbarHostState = snackbarHostState,
                            header = {
                                if (isShowWeekOfYearEnabled) {
                                    val dayDate = weekJdn on mainCalendar
                                    val monthStartDate =
                                        mainCalendar.createDate(dayDate.year, dayDate.month, 1)
                                    val startOfYearJdn =
                                        Jdn(mainCalendar, monthStartDate.year, 1, 1)
                                    val formattedWeekNumber = numeral.format(
                                        weekJdn.getWeekOfYear(startOfYearJdn, weekStart),
                                    )
                                    val description = stringResource(
                                        R.string.nth_week_of_year,
                                        formattedWeekNumber,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .alpha(AppBlendAlpha)
                                            .semantics { this.contentDescription = description }
                                            .weight(weekOfYearWeight),
                                        contentAlignment = Alignment.TopCenter,
                                    ) {
                                        Text(
                                            text = formattedWeekNumber,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    }
                                }
                            },
                        ) { column, content ->
                            val jdn = weekJdn + column
                            val dayDate = jdn on mainCalendar
                            val isHoliday =
                                events[column].any { it.isHoliday } || jdn.weekDay in weekEnds
                            val isToday = jdn == today
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 2.dp + itemHeight * 3 + diameter)
                                    .fillMaxSize()
                                    .clickable {
                                        commandBringDay(jdn)
                                        navigateUp()
                                    },
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(diameter)
                                        .aspectRatio(1f)
                                        .alpha(
                                            animateFloatAsState(
                                                if (focusedDate.month == dayDate.month && focusedDate.year == dayDate.year) 1f
                                                else outOfMonthAlpha,
                                            ).value,
                                        )
                                        .then(
                                            if (isToday) Modifier.border(
                                                width = todayCircleWidth,
                                                color = todayOutlineColor,
                                                shape = CircleShape,
                                            ) else Modifier,
                                        )
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isHoliday && initiallySelectedDay == jdn && !isToday -> holidaysColor
                                                initiallySelectedDay == jdn && !isToday -> indicatorFillColor
                                                isHoliday -> holidaysFillColor
                                                else -> Color.Transparent
                                            },
                                        ),
                                ) {
                                    Text(
                                        mainCalendarNumeral.format(dayDate.dayOfMonth),
                                        color = when {
                                            initiallySelectedDay == jdn && !isToday -> MaterialTheme.colorScheme.background
                                            isHoliday -> holidaysColor
                                            else -> LocalContentColor.current
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.semantics {
                                            if (isTalkBackEnabled) {
                                                this.contentDescription = getA11yDaySummary(
                                                    resources = resources,
                                                    jdn = jdn,
                                                    isToday = isToday,
                                                    deviceCalendarEvents = EventsStore.empty(),
                                                    withZodiac = isToday,
                                                    withOtherCalendars = false,
                                                    withTitle = true,
                                                    withWeekOfYear = false,
                                                )
                                            }
                                        },
                                    )
                                }
                                content()
                            }
                        }
                    }
                }
            }
            ScreenSurface(
                Modifier
                    .align(Alignment.BottomCenter)
                    .height(0.dp),
            ) { Box(Modifier.fillMaxWidth()) }
        }
    }
}

private fun indexToJdn(baseJdn: Jdn, index: Int) = baseJdn + (index - ITEMS_COUNT / 2) * 7

private const val ITEMS_COUNT = 5000 * 2
