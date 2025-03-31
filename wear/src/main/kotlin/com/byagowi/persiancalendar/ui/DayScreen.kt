package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.getEventsOfDay

@Composable
fun DayScreen(day: Jdn, localeUtils: LocaleUtils, preferences: Preferences?) {
    val persianDate = day.toPersianDate()
    val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
    val events = getEventsOfDay(enabledEvents, day.toCivilDate())
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    ScreenScaffold(scrollState = scrollState) {
        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                ListHeader {
                    Box(contentAlignment = Alignment.TopCenter) {
                        Text(
                            localeUtils.format(persianDate.dayOfMonth),
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            localeUtils.persianMonth(persianDate),
                            modifier = Modifier.padding(top = 44.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            items(events) { EventView(it) }
        }
        Box(
            Modifier
                .scrollAway(scrollState)
                .fillMaxSize(),
        ) {
            OtherCalendars(
                localeUtils = localeUtils,
                day = day,
                withWeekDayName = events.size < 2,
                onTop = true,
            )
        }
    }
}
