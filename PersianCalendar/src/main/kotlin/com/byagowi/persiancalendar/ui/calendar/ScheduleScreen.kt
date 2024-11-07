package com.byagowi.persiancalendar.ui.calendar

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.ui.calendar.reports.monthHtmlReport
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ScheduleScreen(
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
) {
    var baseJdn by remember { mutableStateOf(calendarViewModel.selectedDay.value) }
    val state = rememberLazyListState(ITEMS_COUNT / 2, 0)
    val firstVisibleItemJdn by remember {
        derivedStateOf { indexToJdn(baseJdn, state.firstVisibleItemIndex) }
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    val date = firstVisibleItemJdn.inCalendar(mainCalendar)
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
                    val today by calendarViewModel.today.collectAsState()
                    TodayActionButton(today != firstVisibleItemJdn) {
                        baseJdn = today
                        coroutineScope.launch {
                            val destination = ITEMS_COUNT / 2
                            if (abs(state.firstVisibleItemIndex - destination) < 10)
                                state.animateScrollToItem(ITEMS_COUNT / 2)
                            else state.scrollToItem(ITEMS_COUNT / 2)
                        }
                    }
                    val context = LocalContext.current
                    fun showPrintReport(isLongClick: Boolean) {
                        val date = firstVisibleItemJdn.inCalendar(mainCalendar)
                        runCatching {
                            context.openHtmlInBrowser(
                                monthHtmlReport(context, date, wholeYear = isLongClick)
                            )
                        }.onFailure(logException)
                    }
                    Box(
                        @OptIn(ExperimentalFoundationApi::class) Modifier
                            .minimumInteractiveComponentSize()
                            .size(24.dp)
                            .combinedClickable(
                                indication = ripple(bounded = false),
                                interactionSource = null,
                                onClick = { showPrintReport(isLongClick = false) },
                                onClickLabel = "Print",
                                onLongClick = { showPrintReport(isLongClick = true) },
                                onLongClickLabel = stringResource(R.string.year),
                            ),
                    ) { Icon(Icons.Default.Print, contentDescription = "Print") }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                val context = LocalContext.current
                val language by language.collectAsState()
                val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
                val isVazirEnabled by isVazirEnabled.collectAsState()
                val circleTextStyle =
                    if (mainCalendarDigitsIsArabic || isVazirEnabled) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.titleLarge
                Box(modifier = Modifier.fillMaxSize()) {
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
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = formatNumber(date.dayOfMonth),
                                            style = circleTextStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                navigateUp()
                                            },
                                    )
                                }
                            }
                        }
                    }
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
    return { jdn ->
        if (jdn.value in emptyDays) emptyList() else {
            val events = readEvents(jdn, refreshToken)
            if (events.isEmpty()) emptyDays.add(jdn.value)
            events
        }
    }
}

