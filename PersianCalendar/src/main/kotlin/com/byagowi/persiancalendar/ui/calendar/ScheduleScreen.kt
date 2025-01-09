package com.byagowi.persiancalendar.ui.calendar

import android.content.res.Configuration
import androidx.collection.mutableLongSetOf
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.preferredSwipeUpAction
import com.byagowi.persiancalendar.ui.calendar.reports.monthHtmlReport
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ScheduleScreen(
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    initiallySelectedDay: Jdn,
    navigateUp: () -> Unit,
) {
    var baseJdn by remember { mutableStateOf(initiallySelectedDay) }
    val state = rememberLazyListState(ITEMS_COUNT / 2, 0)
    val today by calendarViewModel.today.collectAsState()
    var isFirstTime by remember { mutableStateOf(true) }
    val firstVisibleItemJdn by remember {
        derivedStateOf { indexToJdn(baseJdn, state.firstVisibleItemIndex) }
    }
    LaunchedEffect(today) {
        if (isFirstTime) {
            isFirstTime = false
        } else if (firstVisibleItemJdn == today - 1) {
            baseJdn = today
            state.animateScrollToItem(ITEMS_COUNT / 2)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val language by language.collectAsState()

    val preferredSwipeUpAction by preferredSwipeUpAction.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val swipeDownModifier = Modifier.detectSwipe {
        { isUp ->
            if (!isLandscape && !isUp) when (preferredSwipeUpAction) {
                SwipeUpAction.Schedule -> navigateUp()
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                modifier = swipeDownModifier,
                title = {
                    val date = firstVisibleItemJdn.inCalendar(mainCalendar)
                    val screenTitle = stringResource(R.string.schedule)
                    Column(Modifier.semantics { this.contentDescription = screenTitle }) {
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
                    TodayActionButton(today != firstVisibleItemJdn) {
                        baseJdn = today
                        coroutineScope.launch {
                            val destination = ITEMS_COUNT / 2
                            if (abs(state.firstVisibleItemIndex - destination) < 30) {
                                state.animateScrollToItem(ITEMS_COUNT / 2)
                            } else state.scrollToItem(ITEMS_COUNT / 2)
                        }
                    }

                    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
                    if (showDatePickerDialog) {
                        DatePickerDialog(firstVisibleItemJdn, {
                            showDatePickerDialog = false
                        }) { jdn ->
                            if (abs(firstVisibleItemJdn - jdn) > 30 || abs(baseJdn - jdn) > 30) {
                                baseJdn = jdn
                            }
                            coroutineScope.launch {
                                state.animateScrollToItem(jdn - baseJdn + ITEMS_COUNT / 2)
                            }
                        }
                    }

                    ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
                        AppDropdownMenuItem({ Text(stringResource(R.string.select_date)) }) {
                            showDatePickerDialog = true
                            closeMenu()
                        }

                        val context = LocalContext.current
                        fun showPrintReport(isWholeYear: Boolean = false) {
                            closeMenu()
                            val date = firstVisibleItemJdn.inCalendar(mainCalendar)
                            runCatching {
                                context.openHtmlInBrowser(
                                    monthHtmlReport(context, date, wholeYear = isWholeYear)
                                )
                            }.onFailure(logException)
                        }
                        AppDropdownMenuItem(
                            text = { Text(stringResource(R.string.print)) },
                            trailingIcon = {
                                Box(
                                    @OptIn(ExperimentalFoundationApi::class) Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(24.dp)
                                        .combinedClickable(
                                            indication = ripple(bounded = false),
                                            interactionSource = null,
                                            onClick = { showPrintReport() },
                                            onClickLabel = stringResource(R.string.print),
                                            onLongClick = { showPrintReport(true) },
                                            onLongClickLabel = language.inParentheses.format(
                                                stringResource(R.string.print),
                                                stringResource(R.string.year)
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
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                val context = LocalContext.current
                val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
                val isVazirEnabled by isVazirEnabled.collectAsState()
                val circleTextStyle =
                    if (mainCalendarDigitsIsArabic || isVazirEnabled) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.titleLarge
                Box {
                    val eventsCache = eventsCache(calendarViewModel)
                    LazyColumn(state = state) {
                        items(ITEMS_COUNT) { index ->
                            val jdn = indexToJdn(baseJdn, index)
                            if (index == 0 || index == ITEMS_COUNT - 1) return@items Box(
                                Modifier.padding(
                                    top = if (index == 0) 20.dp else 16.dp,
                                    bottom = if (index == 0) 8.dp else paddingValues.calculateBottomPadding(),
                                    start = 24.dp,
                                )
                            ) {
                                MoreButton(stringResource(R.string.more)) {
                                    baseJdn = jdn
                                    coroutineScope.launch { state.scrollToItem(ITEMS_COUNT / 2) }
                                }
                            }
                            val events = eventsCache(jdn)
                            Column {
                                val date = jdn.inCalendar(mainCalendar)
                                if (events.isNotEmpty()) Row(
                                    Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .padding(top = 4.dp)
                                            .clickable(
                                                interactionSource = null,
                                                indication = ripple(bounded = false),
                                            ) {
                                                bringDate(calendarViewModel, jdn, context)
                                                navigateUp()
                                            }
                                            .size(36.dp)
                                            .background(
                                                when {
                                                    jdn < today -> MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = .6f
                                                    )

                                                    jdn > today -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.primary
                                                },
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = formatNumber(date.dayOfMonth),
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
                                    Spacer(Modifier.width(8.dp))
                                    Column(
                                        if (baseJdn == jdn) Modifier.sharedBounds(
                                            rememberSharedContentState(SHARED_CONTENT_KEY_EVENTS),
                                            animatedVisibilityScope = animatedContentScope,
                                        ) else Modifier,
                                    ) { DayEvents(events) {} }
                                }
                                if (mainCalendar.getMonthLength(
                                        date.year,
                                        date.month,
                                    ) == date.dayOfMonth
                                ) {
                                    val nextMonth = mainCalendar.getMonthStartFromMonthsDistance(
                                        jdn, 1
                                    )
                                    Text(
                                        if (nextMonth.month == 1) language.my.format(
                                            nextMonth.monthName, formatNumber(nextMonth.year),
                                        ) else nextMonth.monthName,
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .padding(
                                                start = 24.dp, end = 24.dp, top = 24.dp
                                            )
                                            .clickable(
                                                interactionSource = null,
                                                indication = ripple(bounded = false),
                                            ) {
                                                calendarViewModel.changeSelectedMonthOffsetCommand(
                                                    mainCalendar.getMonthsDistance(
                                                        Jdn.today(), Jdn(nextMonth),
                                                    )
                                                )
                                                calendarViewModel.openYearView()
                                                navigateUp()
                                            },
                                    )
                                }
                            }
                        }
                    }
                    ScrollShadow(state, top = true)
                    ScrollShadow(state, top = false)
                }
            }
        }
    }
}

private const val ITEMS_COUNT = 365 * 2
private fun indexToJdn(baseJdn: Jdn, index: Int) = baseJdn + index - ITEMS_COUNT / 2

@Composable
private fun eventsCache(calendarViewModel: CalendarViewModel): @Composable (Jdn) -> List<CalendarEvent<*>> {
    val refreshToken by calendarViewModel.refreshToken.collectAsState()
    val emptyDays by remember(refreshToken) { mutableStateOf(mutableLongSetOf()) }
    val context = LocalContext.current
    return { jdn ->
        if (jdn.value in emptyDays) emptyList() else {
            val deviceEvents = remember(jdn, refreshToken) { context.readDayDeviceEvents(jdn) }
            val events = readEvents(jdn, calendarViewModel, deviceEvents)
            if (events.isEmpty()) emptyDays.add(jdn.value)
            events
        }
    }
}

