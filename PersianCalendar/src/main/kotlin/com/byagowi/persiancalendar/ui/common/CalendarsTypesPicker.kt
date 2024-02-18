package com.byagowi.persiancalendar.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.PrimaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.stringResource

@Composable
fun CalendarsTypesPicker(current: CalendarType, setCurrent: (CalendarType) -> Unit) {
    val selectedTabIndex = enabledCalendars.indexOf(current)
        // If user returned from disabling one of the calendar, do a fallback
        .coerceAtLeast(0)
    TabRow(
        selectedTabIndex = selectedTabIndex,
        divider = {},
        containerColor = Color.Transparent,
        indicator = @Composable { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                PrimaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]))
            }
        },
    ) {
        enabledCalendars.forEach { calendarType ->
            val title = stringResource(
                if (language.value.betterToUseShortCalendarName) calendarType.shortTitle
                else calendarType.title
            )
            val view = LocalView.current
            Tab(
                text = { Text(title) },
                selected = current == calendarType,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    setCurrent(calendarType)
                    view.performHapticFeedbackVirtualKey()
                },
            )
        }
    }
}
