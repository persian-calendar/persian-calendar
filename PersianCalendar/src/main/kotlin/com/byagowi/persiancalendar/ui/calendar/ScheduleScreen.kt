package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.DeviceEventInterval
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.customFontName
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarNumeral
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.preferredSwipeUpAction
import com.byagowi.persiancalendar.shared.ShareFacilitator
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.more
import com.byagowi.persiancalendar.shared.generated.resources.print
import com.byagowi.persiancalendar.shared.generated.resources.schedule
import com.byagowi.persiancalendar.shared.generated.resources.select_date
import com.byagowi.persiancalendar.shared.generated.resources.year
import com.byagowi.persiancalendar.ui.calendar.reports.monthHtmlReport
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.isLandscape
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.deviceCalendarQuerySettings
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.queryCalendarProvider
import com.byagowi.persiancalendar.utils.readDeviceEvents
import com.byagowi.persiancalendar.utils.viewEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.time.Duration.Companion.days

@Composable
fun SharedTransitionScope.ScheduleScreen(
    refreshToken: Int,
    refreshCalendar: () -> Unit,
    commandBringDay: (Jdn) -> Unit,
    initiallySelectedDay: Jdn,
    navigateUp: () -> Unit,
    navigateToHolidaysSettings: (String?) -> Unit,
    today: Jdn,
    modifier: Modifier = Modifier,
) {
    var baseJdn by remember { mutableStateOf(initiallySelectedDay) }
    val listState = rememberLazyListState(ITEMS_COUNT / 2, 0)
    var isFirstTime by remember { mutableStateOf(true) }
    val firstVisibleItemJdn by remember {
        derivedStateOf { indexToJdn(baseJdn, listState.firstVisibleItemIndex) }
    }
    LaunchedEffect(today) {
        if (isFirstTime) {
            isFirstTime = false
        } else if (firstVisibleItemJdn == today - 1) {
            baseJdn = today
            listState.animateScrollToItem(ITEMS_COUNT / 2)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var printJob by remember { mutableStateOf<Job?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) printJob?.cancel()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            printJob?.cancel()
        }
    }
    val language = language

    val preferredSwipeUpAction = preferredSwipeUpAction
    val isLandscape = isLandscape()
    val swipeDownModifier = Modifier.detectSwipe {
        { isUp ->
            if (!isLandscape && !isUp) when (preferredSwipeUpAction) {
                SwipeUpAction.Schedule -> navigateUp()
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                modifier = swipeDownModifier,
                title = {
                    val date = firstVisibleItemJdn on mainCalendar
                    val screenTitle = stringResource(Res.string.schedule)
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
                    TodayActionButton(today != firstVisibleItemJdn) {
                        baseJdn = today
                        coroutineScope.launch {
                            val destination = ITEMS_COUNT / 2
                            if (abs(listState.firstVisibleItemIndex - destination) < 30) {
                                listState.animateScrollToItem(ITEMS_COUNT / 2)
                            } else listState.scrollToItem(ITEMS_COUNT / 2)
                        }
                    }

                    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
                    if (showDatePickerDialog) {
                        DatePickerDialog(
                            initialJdn = firstVisibleItemJdn,
                            onDismissRequest = { showDatePickerDialog = false },
                            today = today,
                        ) { jdn ->
                            if (abs(firstVisibleItemJdn - jdn) > 30 || abs(baseJdn - jdn) > 30) {
                                baseJdn = jdn
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(jdn - baseJdn + ITEMS_COUNT / 2)
                            }
                        }
                    }

                    ThreeDotsDropdownMenu { closeMenu ->
                        AppDropdownMenuItem({ Text(stringResource(Res.string.select_date)) }) {
                            showDatePickerDialog = true
                            closeMenu()
                        }

                        val context = LocalContext.current
                        val shareFacilitator = ShareFacilitator.create(
                            stringResource(Res.string.schedule),
                        )

                        fun showPrintReport(isWholeYear: Boolean = false) {
                            closeMenu()
                            val date = firstVisibleItemJdn on mainCalendar
                            val settings = deviceCalendarQuerySettings()
                            val dates =
                                if (isWholeYear) (1..date.calendar.getYearMonths(date.year)).map {
                                    date.calendar.createDate(date.year, it, 1)
                                } else listOf(date)
                            printJob?.cancel()
                            printJob = coroutineScope.launch {
                                runCatching {
                                    val deviceEvents = queryCalendarProvider { signal ->
                                        dates.associate { month ->
                                            val jdn = Jdn(month)
                                            jdn to DeviceCalendarEventsStore(
                                                readDeviceEvents(
                                                    context.applicationContext,
                                                    DeviceEventInterval.startOfDay(
                                                        jdn.toCivilDate(),
                                                        settings.timeZone,
                                                    ),
                                                    32.days,
                                                    750,
                                                    settings = settings,
                                                    cancellationSignal = signal,
                                                ),
                                            )
                                        }
                                    }
                                    if (settings != deviceCalendarQuerySettings() || date.calendar != mainCalendar) {
                                        return@launch
                                    }
                                    // HTML formatting, preference reads, and launching stay on Main.
                                    shareFacilitator.openHtmlInBrowser(
                                        monthHtmlReport(context, date, isWholeYear, deviceEvents),
                                    )
                                }.onFailure { error ->
                                    if (error is CancellationException) throw error
                                    logException(error)
                                }
                            }
                        }
                        AppDropdownMenuItem(
                            text = { Text(stringResource(Res.string.print)) },
                            trailingIcon = {
                                Box(
                                    Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(24.dp)
                                        .combinedClickable(
                                            indication = ripple(bounded = false),
                                            interactionSource = null,
                                            onClick = { showPrintReport() },
                                            onClickLabel = stringResource(Res.string.print),
                                            onLongClick = { showPrintReport(true) },
                                            onLongClickLabel = language.inParentheses.format(
                                                stringResource(Res.string.print),
                                                stringResource(Res.string.year),
                                            ),
                                        ),
                                ) { /*Icon(Icons.Default.Print, contentDescription = "Print")*/ }
                            },
                        ) { showPrintReport() }
                    }
                },
            )
        },
    ) { paddingValues ->
        ScreenSurface(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val circleTextStyle =
                if (!mainCalendarNumeral.isArabicIndicVariants || customFontName != null) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.titleLarge
            val viewEvent = viewEvent(refreshCalendar)
            LazyColumn(
                state = listState,
                contentPadding = WindowInsets.safeDrawing.only(
                    sides = WindowInsetsSides.Horizontal,
                ).asPaddingValues(),
            ) {
                items(ITEMS_COUNT) { index ->
                    val jdn = indexToJdn(baseJdn, index)
                    if (index == 0 || index == ITEMS_COUNT - 1) return@items MoreButton(
                        title = stringResource(Res.string.more),
                        modifier = Modifier.padding(
                            top = if (index == 0) 20.dp else 16.dp,
                            bottom = if (index == 0) 8.dp else paddingValues.calculateBottomPadding(),
                            start = 24.dp,
                        ),
                    ) {
                        baseJdn = jdn
                        coroutineScope.launch { listState.scrollToItem(ITEMS_COUNT / 2) }
                    }
                    val deviceEvents = rememberDeviceCalendarEvents(
                        jdn, days = 1, limit = 120, refreshToken = refreshToken,
                    )
                    val events = eventsRepository.getEvents(jdn, deviceEvents.data)
                    Column {
                        // Give pending days a size so LazyColumn does not enqueue hundreds of
                        // zero-height rows before the first asynchronous result arrives.
                        if (events.isEmpty() && deviceEvents.status == CalendarProviderStatus.Loading) {
                            Spacer(Modifier.height(64.dp))
                        }
                        val date = jdn on mainCalendar
                        if (events.isNotEmpty()) Row(
                            Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp),
                        ) {
                            Box(
                                Modifier
                                    .clickable(
                                        interactionSource = null,
                                        indication = ripple(bounded = false),
                                    ) {
                                        commandBringDay(jdn)
                                        navigateUp()
                                    }
                                    .size(32.dp)
                                    .background(
                                        when {
                                            jdn < today -> MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = .6f,
                                            )

                                            jdn > today -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = numeral.format(date.dayOfMonth),
                                    style = circleTextStyle,
                                    color = when {
                                        jdn < today -> MaterialTheme.colorScheme.onPrimaryContainer
                                        jdn > today -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onPrimary
                                    },
                                    modifier = Modifier.semantics {
                                        if (isTalkBackEnabled) this.contentDescription =
                                            formatDate(date, forceNonNumerical = true)
                                    },
                                )
                            }
                            DayEvents(
                                events = events,
                                navigateToHolidaysSettings = navigateToHolidaysSettings,
                                viewEvent = viewEvent,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        if (mainCalendar.getMonthLength(
                                date.year,
                                date.month,
                            ) == date.dayOfMonth
                        ) {
                            val nextMonth = mainCalendar.getMonthStartFromMonthsDistance(
                                jdn, 1,
                            )
                            Text(
                                if (nextMonth.month == 1) language.my.format(
                                    nextMonth.monthName,
                                    numeral.format(nextMonth.year),
                                ) else nextMonth.monthName,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .padding(
                                        start = 24.dp, end = 24.dp, top = 24.dp,
                                    )
                                    .clickable(
                                        interactionSource = null,
                                        indication = ripple(bounded = false),
                                    ) {
                                        // This used to bring year view directly but now
                                        // only brings the day on the calendar
                                        commandBringDay(Jdn(nextMonth))
                                        navigateUp()
                                    },
                            )
                        }
                    }
                }
            }
            ScrollShadow(listState)
        }
    }
}

private const val ITEMS_COUNT = 365 * 2
private fun indexToJdn(baseJdn: Jdn, index: Int) = baseJdn + index - ITEMS_COUNT / 2
