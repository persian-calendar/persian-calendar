package com.byagowi.persiancalendar.ui.calendar

import androidx.collection.mutableLongSetOf
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AgendaScreen(
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
) {
    var baseJdn by remember { mutableStateOf(calendarViewModel.selectedDay.value) }
    val state = key(baseJdn) { rememberLazyListState(ITEMS_COUNT / 2, 0) }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    val jdn by remember {
                        derivedStateOf { indexToJdn(baseJdn, state.firstVisibleItemIndex) }
                    }
                    val date = jdn.inCalendar(mainCalendar)
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
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                val circleColor = MaterialTheme.colorScheme.surfaceVariant
                val radius = with(LocalDensity.current) { 16.dp.toPx() }
                val context = LocalContext.current
                val language by language.collectAsState()
                Box(modifier = Modifier.fillMaxSize()) {
                    val eventsCache = eventsCache(calendarViewModel)
                    LazyColumn(state = state) {
                        items(ITEMS_COUNT) { index ->
                            val jdn = indexToJdn(baseJdn, index)
                            if (index == 0 || index == ITEMS_COUNT - 1) return@items Box(
                                Modifier.padding(
                                    top = if (index == 0) 24.dp else 8.dp,
                                    bottom = if (index == 0) 8.dp else paddingValues.calculateBottomPadding(),
                                )
                            ) { MoreButton { baseJdn = jdn } }
                            val events = eventsCache(jdn)
                            Column(
                                if (events.isEmpty()) Modifier else Modifier.padding(
                                    top = if (index == ITEMS_COUNT / 2) 24.dp else 0.dp,
                                    bottom = if (index == ITEMS_COUNT / 2 - 1) 0.dp else 24.dp,
                                )
                            ) {
                                val date = jdn.inCalendar(mainCalendar)
                                if (events.isNotEmpty()) Row(
                                    Modifier
                                        .padding(start = 24.dp, end = 24.dp)
                                        .clickable(
                                            interactionSource = null,
                                            indication = ripple(bounded = false),
                                        ) {
                                            bringDate(calendarViewModel, jdn, context)
                                            navigateUp()
                                        },
                                ) {
                                    Box(
                                        Modifier
                                            .width(32.dp)
                                            .drawBehind { drawCircle(circleColor, radius) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = formatNumber(date.dayOfMonth),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column { DayEvents(events) {} }
                                }
                                if (mainCalendar.getMonthLength(
                                        date.year, date.month
                                    ) == date.dayOfMonth
                                ) {
                                    val nextMonth =
                                        mainCalendar.getMonthStartFromMonthsDistance(jdn, 1)
                                    Text(
                                        language.my.format(
                                            nextMonth.monthName, formatNumber(nextMonth.year)
                                        ),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier
                                            .padding(horizontal = 24.dp)
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

