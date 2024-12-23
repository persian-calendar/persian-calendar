package com.byagowi.persiancalendar.ui.calendar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.verticalDrag
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.core.util.lruCache
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.calendarpager.Month
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthColors
import com.byagowi.persiancalendar.ui.calendar.calendarpager.PagerArrow
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.pagerArrowSizeAndPadding
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ShrinkingFloatingActionButton
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.toCivilDate
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DayWeekScreen(
    calendarViewModel: CalendarViewModel,
    initialSelectedDay: Jdn,
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
    isInitiallyWeek: Boolean,
    navigateToSchedule: (Jdn) -> Unit,
) {
    var selectedDay by remember { mutableStateOf(initialSelectedDay) }
    val date = selectedDay.inCalendar(mainCalendar)
    val today by calendarViewModel.today.collectAsState()
    val context = LocalContext.current
    var isClickedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val weekInitialPage = remember(today) { weekPageFromJdn(initialSelectedDay, today) }
    val weekPagerState = rememberPagerState(initialPage = weekInitialPage) { weeksLimit }
    val dayInitialPage = remember(today) { dayPageFromJdn(selectedDay, today) }
    val dayPagerState = rememberPagerState(initialPage = dayInitialPage) { daysLimit }
    val setSelectedDayInWeekPager = { jdn: Jdn ->
        selectedDay = jdn
        coroutineScope.launch {
            val destination = dayPageFromJdn(jdn, today)
            if (abs(destination - dayPagerState.currentPage) > 1) {
                dayPagerState.scrollToPage(destination)
            } else dayPagerState.animateScrollToPage(destination)
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

    val todayButtonAction = {
        selectedDay = today
        coroutineScope.launch {
            weekPagerState.scrollToPage(weeksLimit / 2)
            dayPagerState.scrollToPage(daysLimit / 2)
        }
    }

    var isFirstTime by remember { mutableStateOf(true) }
    LaunchedEffect(today) {
        if (isFirstTime) isFirstTime = false else if (selectedDay == today - 1) todayButtonAction()
    }

    var isWeekView by rememberSaveable { mutableStateOf(isInitiallyWeek) }
    var weekViewAddAction by remember { mutableStateOf({}) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ShrinkingFloatingActionButton(
                modifier = Modifier.padding(end = 8.dp),
                isVisible = isWeekView,
                action = weekViewAddAction,
                icon = Icons.Default.Add,
                title = stringResource(R.string.add_event),
                noTitle = true,
            )
        },
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
                    TodayActionButton(today != selectedDay) { todayButtonAction() }
                    IconButton({ isWeekView = !isWeekView }) {
                        Crossfade(isWeekView, label = "icon") { state ->
                            if (state) Icon(
                                Icons.Default.CalendarViewDay,
                                contentDescription = stringResource(R.string.week_view),
                            ) else Icon(
                                Icons.Default.CalendarViewWeek,
                                contentDescription = stringResource(R.string.day_view),
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        val monthColors = appMonthColors()
        val bottomPadding = paddingValues.calculateBottomPadding()
        BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val pagerSize =
                calendarPagerSize(false, this.maxWidth, this.maxHeight, bottomPadding, true)
            // Don't show weeks pager if there isn't enough space
            Column {
                val refreshToken by calendarViewModel.refreshToken.collectAsState()
                HorizontalPager(
                    state = weekPagerState,
                    verticalAlignment = Alignment.Top,
                    pageSpacing = 2.dp,
                ) { page ->
                    Column {
                        if (hasWeeksPager) WeekPage(
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
                        if (isWeekView) Spacer(Modifier.height(8.dp))
                        if (isWeekView) ScreenSurface(
                            animatedContentScope = animatedContentScope,
                            disableSharedContent = page != weeksLimit / 2,
                        ) {
                            WeekView(
                                bottomPadding = bottomPadding,
                                setAddAction = {
                                    if (weekPagerState.currentPage == page) weekViewAddAction = it
                                },
                                selectedDay = selectedDay,
                                setSelectedDay = { selectedDay = it },
                                addEvent = addEvent,
                                refreshCalendar = calendarViewModel::refreshCalendar,
                                refreshToken = refreshToken,
                                now = calendarViewModel.now.collectAsState().value,
                            )
                        }
                    }
                }
                if (!isWeekView) Spacer(Modifier.height(8.dp))
                if (!isWeekView) ScreenSurface(animatedContentScope) {
                    HorizontalPager(
                        state = dayPagerState,
                        verticalAlignment = Alignment.Top,
                    ) { page ->
                        val isCurrentPage = dayPagerState.currentPage == page
                        LaunchedEffect(isCurrentPage) {
                            if (isCurrentPage) {
                                selectedDay = today + (page - daysLimit / 2)
                                val destination = weekPageFromJdn(selectedDay, today)
                                if (abs(destination - weekPagerState.currentPage) > 1) {
                                    weekPagerState.scrollToPage(destination)
                                } else weekPagerState.animateScrollToPage(destination)
                            }
                        }

                        DayView(
                            selectedDay = today + (page - daysLimit / 2),
                            refreshToken = refreshToken,
                            calendarViewModel = calendarViewModel,
                            animatedContentScope = animatedContentScope,
                            addEvent = addEvent,
                            bottomPadding = bottomPadding.coerceAtLeast(16.dp),
                            navigateUp = navigateUp,
                            navigateToSchedule = navigateToSchedule,
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

private fun hoursFractionOfDay(date: GregorianCalendar): Float =
    date[Calendar.HOUR_OF_DAY] + date[Calendar.MINUTE] / 60f

@Composable
private fun WeekView(
    setAddAction: (() -> Unit) -> Unit,
    selectedDay: Jdn,
    setSelectedDay: (Jdn) -> Unit,
    bottomPadding: Dp,
    addEvent: (AddEventData) -> Unit,
    refreshCalendar: () -> Unit,
    refreshToken: Int,
    now: Long,
) {
    val weekStart = selectedDay - applyWeekStartOffsetToWeekDay(selectedDay.weekDay)
    val scale = remember { Animatable(1f) }
    val cellHeight by remember(scale.value) { mutableStateOf((64 * scale.value).dp) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val initial = remember(density) { with(density) { (cellHeight * 7 - 16.dp).roundToPx() } }
    val scrollState = rememberScrollState(initial)
    val events = (weekStart..weekStart + 7).toList().map { jdn ->
        readEvents(jdn, refreshToken)
            .filterIsInstance<CalendarEvent.DeviceCalendarEvent>().filter { it.time != null }
    }

    Column(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        coroutineScope.launch {
                            val value = scale.value * event.calculateZoom()
                            scale.snapTo(value.coerceIn(.5f, 2f))
                        }
                    } while (event.changes.fastAny { it.pressed })
                }
            },
    ) {
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .height(cellHeight),
//        ) {
//            repeat(8) {
//                Box(
//                    modifier = Modifier
//                        .height(cellHeight)
//                        .weight(1f),
//                    contentAlignment = Alignment.Center,
//                ) { Text(it.toString()) }
//            }
//        }
        BoxWithConstraints {
            val tableWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
            val maxWidthPx = tableWidthPx * 7f / 8
            val cellHeightPx = with(density) { cellHeight.toPx() }
            var offsetPosition by remember(tableWidthPx) { mutableStateOf(Offset.Zero) }
            val cellWidthPx = tableWidthPx / 8
            var duration by remember { mutableFloatStateOf(cellHeightPx) }
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            val directionSign = if (isRtl) -1 else 1

            Box(Modifier.verticalScroll(scrollState)) {
                val clockCache = remember {
                    lruCache(1024, create = { minutes: Int ->
                        Clock.fromMinutesCount(minutes).toBasicFormatString()
                    })
                }
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth()) {
                        val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(.5f)
                        repeat(8) { column ->
                            Column(Modifier.weight(1f)) {
                                if (column == 0) Spacer(Modifier.height(cellHeight / 2))
                                repeat(24) { row ->
                                    Box(
                                        Modifier
                                            .height(
                                                if (column == 0 && row == 23) 0.dp
                                                else if (column == 0 || row != 23) cellHeight
                                                else cellHeight + bottomPadding
                                            )
                                            .fillMaxWidth()
                                            .then(
                                                if (column == 0) Modifier else Modifier
                                                    .border(Dp.Hairline, outlineColor)
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = null,
                                                    ) {
                                                        offsetPosition = Offset(
                                                            cellWidthPx * column.toFloat(),
                                                            cellHeightPx * row / scale.value
                                                        )
                                                        duration = cellHeightPx / scale.value
                                                        setSelectedDay(weekStart + column - 1)
                                                    },
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (column == 0 && row != 23) {
                                            Text(clockCache[(row + 1) * 60])
                                            HorizontalDivider(
                                                Modifier
                                                    .width(8.dp)
                                                    .align(Alignment.CenterEnd),
                                                Dp.Hairline,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val defaultWidth = .95f
                val launcher = rememberLauncherForActivityResult(ViewEventContract()) {
                    refreshCalendar()
                }
                val context = LocalContext.current
                events.mapIndexed { i, it ->
                    it.map { event ->
                        val start = hoursFractionOfDay(event.startTime)
                        val end = hoursFractionOfDay(event.endTime)
                        val color = eventColor(event)
                        Text(
                            " " + event.title,
                            color = eventTextColor(color),
                            maxLines = 1,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (cellWidthPx * (i + 1)).roundToInt(),
                                        (start * cellHeightPx).roundToInt()
                                    )
                                }
                                .requiredSize(
                                    with(density) { (cellWidthPx * defaultWidth).toDp() },
                                    with(density) { ((end - start) * cellHeightPx).toDp() - 3.dp },
                                )
                                .clickable { launcher.viewEvent(event, context) }
                                .background(color, MaterialTheme.shapes.small)
                        )
                    }
                }

                val x = (offsetPosition.x / cellWidthPx).roundToInt()
                val ySteps = (cellHeightPx / 4).roundToInt()
                val y = (offsetPosition.y * scale.value / ySteps).roundToInt()
                val offset = if (offsetPosition == Offset.Zero) Offset.Zero
                else animateOffsetAsState(
                    Offset(x * cellWidthPx, y * ySteps.toFloat()),
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
                    label = "offset"
                ).value
                val dy = (duration / (cellHeightPx / 4) * scale.value).roundToInt()
                val animatedDuration by animateFloatAsState(
                    targetValue = dy * (cellHeightPx / 4),
                    label = "duration"
                )

                var resetOnNextRefresh by remember { mutableStateOf(false) }
                val addAction = {
                    val cellHeightScaledPx = cellHeightPx * scale.value
                    if (offsetPosition == Offset.Zero) offsetPosition = Offset(
                        cellWidthPx * (applyWeekStartOffsetToWeekDay(selectedDay.weekDay) + 1f),
                        ceil(scrollState.value / cellHeightScaledPx) * cellHeightScaledPx
                    ) else {
                        val time = selectedDay.toGregorianCalendar()
                        run {
                            val minutes = y * 15
                            time[GregorianCalendar.HOUR_OF_DAY] = minutes / 60
                            time[GregorianCalendar.MINUTE] = minutes % 60
                        }
                        time[GregorianCalendar.SECOND] = 0
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

                val resumeToken by resumeToken.collectAsState()
                LaunchedEffect(resumeToken) {
                    if (resetOnNextRefresh) {
                        duration = cellHeightPx / 4 * 4f
                        offsetPosition = Offset.Zero
                        resetOnNextRefresh = false
                    }
                }

                val widthFraction = remember { Animatable(defaultWidth) }
                val radius = with(density) { 4.dp.toPx() }
                val lineSize = with(density) { 1.dp.toPx() }

                Box(
                    Modifier
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .size(
                            with(density) { cellWidthPx.toDp() - 1.dp },
                            with(density) { animatedDuration.toDp() },
                        )
                        .clickable(indication = null, interactionSource = null) { addAction() }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val id = awaitFirstDown().id
                                coroutineScope.launch { widthFraction.animateTo(1f) }
                                var action = Float.NaN
                                drag(id) {
                                    val delta = it.positionChange()
                                    if (action.isNaN()) action = when {
                                        abs(it.position.y - duration * scale.value) < cellHeightPx * .2f -> 1f
                                        abs(it.position.y) < cellHeightPx * .2f -> -1f
                                        else -> 0f
                                    }
                                    when (action) {
                                        1f -> duration =
                                            (duration + delta.y / scale.value).coerceIn(
                                                minimumValue = ySteps * 1f,
                                                maximumValue = (ySteps * 24 * 4) - offsetPosition.y
                                            )

                                        -1f -> {
                                            val newValueY = offsetPosition.y + delta.y / scale.value
                                            offsetPosition = offsetPosition.copy(
                                                y = newValueY.coerceIn(0f, cellHeightPx * 23)
                                            )
                                            duration = (duration - delta.y / scale.value).coerceIn(
                                                ySteps * 1f, ySteps * 16f
                                            )
                                        }

                                        else -> {
                                            val newValueX =
                                                offsetPosition.x + directionSign * delta.x
                                            val newValueY = offsetPosition.y + delta.y / scale.value
                                            offsetPosition = Offset(
                                                newValueX.coerceIn(tableWidthPx / 8f, maxWidthPx),
                                                newValueY.coerceIn(0f, cellHeightPx * 24 - duration)
                                            )

                                            val effectiveColumn =
                                                (offsetPosition.x / cellWidthPx).roundToInt()
                                            setSelectedDay(weekStart + effectiveColumn - 1)
                                        }
                                    }
                                    it.consume()
                                }
                                coroutineScope.launch { widthFraction.animateTo(defaultWidth) }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) addEventRectangle@{
                    val alpha by animateFloatAsState(
                        if (offset == Offset.Zero) 0f else 1f, animationSpec = spring(
                            Spring.DampingRatioNoBouncy, Spring.StiffnessLow
                        ), label = "alpha"
                    )
                    if (offset == Offset.Zero) return@addEventRectangle
                    val circleBorder = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
                    val background = MaterialTheme.colorScheme.surface.copy(alpha = AppBlendAlpha)
                    val primary = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    Canvas(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxSize(),
                    ) {
                        val rectTopLeft = Offset(
                            x = if (isRtl) this.size.width * (1 - widthFraction.value) else 0f,
                            y = 0f
                        )
                        val rectSize = Size(
                            width = this.size.width * widthFraction.value,
                            height = this.size.height - lineSize * 2
                        )
                        drawRoundRect(
                            background,
                            size = rectSize,
                            topLeft = rectTopLeft,
                            cornerRadius = CornerRadius(radius, radius),
                        )
                        drawRoundRect(
                            primary,
                            topLeft = rectTopLeft,
                            size = Size(
                                width = this.size.width * widthFraction.value,
                                height = this.size.height - lineSize * 2,
                            ),
                            style = Stroke(lineSize),
                            cornerRadius = CornerRadius(radius, radius),
                        )
                        val offset1 = Offset(
                            x = this.center.x - (this.size.width / 2 - radius - lineSize * 2) * directionSign,
                            y = 0f
                        )
                        drawCircle(circleBorder, radius + lineSize * 2, offset1)
                        drawCircle(primary, radius, offset1)
                        val offset2 = Offset(
                            x = this.center.x + (this.size.width * widthFraction.value / 2 - radius - lineSize * 2) * directionSign,
                            y = this.size.height - lineSize * 2
                        )
                        drawCircle(circleBorder, radius + lineSize * 2, offset2)
                        drawCircle(primary, radius, offset2)
                    }
                    val from = clockCache[y * 15]
                    val to = clockCache[(y + dy) * 15]
                    Text("$from\n$to", textAlign = TextAlign.Center)
                }

                val time = GregorianCalendar().also { it.timeInMillis = now }
                val dayOfWeek = Jdn(time.toCivilDate()) - weekStart
                val primary = MaterialTheme.colorScheme.primary
                if (dayOfWeek in 0..6) Canvas(Modifier
                    .offset {
                        IntOffset(
                            (cellWidthPx * (dayOfWeek + 1)).roundToInt(),
                            (hoursFractionOfDay(time) * cellHeightPx).roundToInt()
                        )
                    }
                    .size(1.dp)
                ) {
                    drawCircle(primary, radius)
                    drawLine(
                        color = primary,
                        start = Offset.Zero,
                        end = Offset(directionSign * cellWidthPx, 0f),
                        strokeWidth = lineSize
                    )
                }
            }
            ScrollShadow(scrollState, top = true)
            ScrollShadow(scrollState, top = false)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DayView(
    selectedDay: Jdn,
    refreshToken: Int,
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    addEvent: (AddEventData) -> Unit,
    bottomPadding: Dp,
    navigateUp: () -> Unit,
    navigateToSchedule: (Jdn) -> Unit,
) {
    val events = readEvents(selectedDay, refreshToken)
    val eventsWithTime =
        events.filterIsInstance<CalendarEvent.DeviceCalendarEvent>().filter { it.time != null }
    val eventsWithoutTime = events - eventsWithTime.toSet()
    val hoursEvents = eventsWithTime.groupBy { it.startTime[Calendar.HOUR_OF_DAY] }
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

        val hasHeader by remember(events) {
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
                            navigateToSchedule(selectedDay)
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
                            val hourEvents = hoursEvents[hour]
                            if (hourEvents != null) DayEvents(hourEvents) {
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
                time[GregorianCalendar.HOUR_OF_DAY] = hour
                time[GregorianCalendar.MINUTE] = 0
                time[GregorianCalendar.SECOND] = 0
                val beginTime = time.time
                time[GregorianCalendar.HOUR_OF_DAY] = hour + 1
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
    Box(Modifier.height(pagerSize.height)) {
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
        val arrowHeight = height / 2 + (if (language.isArabicScript) 4 else 0).dp
        PagerArrow(arrowHeight, coroutineScope, weekPagerState, page, true, week)
        Box(modifier = Modifier.padding(horizontal = pagerArrowSizeAndPadding.dp)) {
            val monthOffset = mainCalendar.getMonthsDistance(today, selectedDay)
            Month(
                offset = monthOffset,
                width = pagerSize.width - (pagerArrowSizeAndPadding * 2).dp,
                height = height,
                addEvent = addEvent,
                monthColors = monthColors,
                animatedContentScope = animatedContentScope,
                onlyWeek = week,
                today = today,
                isHighlighted = true,
                selectedDay = selectedDay,
                refreshToken = refreshToken,
                setSelectedDay = {
                    setClickedOnce()
                    setSelectedDay(it)
                },
            )
        }
        PagerArrow(arrowHeight, coroutineScope, weekPagerState, page, false, week)
    }
}

private const val weeksLimit = 25000 // this should be an even number
private const val daysLimit = 175000 // this should be an even number
