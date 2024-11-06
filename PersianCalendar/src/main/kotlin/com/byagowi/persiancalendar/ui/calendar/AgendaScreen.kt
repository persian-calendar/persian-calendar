package com.byagowi.persiancalendar.ui.calendar

import androidx.collection.mutableLongSetOf
import androidx.compose.animation.AnimatedContentScope
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AgendaScreen(
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {},
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                var baseJdn by remember { mutableStateOf(calendarViewModel.selectedDay.value) }
                val primaryColor = MaterialTheme.colorScheme.primary
                val radius = with(LocalDensity.current) { 16.dp.toPx() }
                val context = LocalContext.current
                Box(modifier = Modifier.fillMaxSize()) {
                    key(baseJdn) {
                        val state = rememberLazyListState(ITEMS_COUNT / 2, 0)
                        val eventsCache = eventsCache(calendarViewModel)
                        LazyColumn(state = state) {
                            items(ITEMS_COUNT) {
                                val jdn = baseJdn + it - ITEMS_COUNT / 2
                                if (it == 0 || it == ITEMS_COUNT - 1) return@items Box(
                                    Modifier.padding(
                                        top = if (it == 0) 24.dp else 8.dp,
                                        bottom = if (it == 0) 8.dp else paddingValues.calculateBottomPadding(),
                                    )
                                ) { MoreButton { baseJdn = jdn } }
                                val events = eventsCache(jdn)
                                if (events.isNotEmpty()) Column(Modifier.padding(top = 24.dp)) {
                                    Row(
                                        Modifier
                                            .padding(horizontal = 24.dp)
                                            .clickable {
                                                bringDate(calendarViewModel, jdn, context)
                                                navigateUp()
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        val date = jdn.inCalendar(mainCalendar)
                                        Box(
                                            Modifier
                                                .width(32.dp)
                                                .drawBehind { drawCircle(primaryColor, radius) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = formatNumber(date.dayOfMonth),
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(dayTitleSummary(jdn, date))
                                    }
                                    DayEvents(animatedContentScope, events, baseJdn == jdn) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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

private const val ITEMS_COUNT = 365 * 2
