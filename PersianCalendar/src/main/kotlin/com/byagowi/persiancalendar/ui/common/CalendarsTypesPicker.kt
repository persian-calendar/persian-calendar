package com.byagowi.persiancalendar.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarsTypesPicker(
    current: Calendar,
    calendarsList: List<Calendar>,
    setCurrent: (Calendar) -> Unit,
) {
    val selectedTabIndex = calendarsList.indexOf(current)
        // If user returned from disabling one of the calendar, do a fallback
        .coerceAtLeast(0)
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        divider = {},
        containerColor = Color.Transparent,
        indicator = {
            val offset = selectedTabIndex.coerceAtMost(calendarsList.size - 1)
            TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(offset))
        },
    ) {
        val language by language.collectAsState()
        val view = LocalView.current
        calendarsList.forEach { calendar ->
            val title = stringResource(
                if (language.betterToUseShortCalendarName) calendar.shortTitle else calendar.title
            )
            Tab(
                text = { Text(title) },
                modifier = Modifier.clip(MaterialTheme.shapes.large),
                selected = current == calendar,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    setCurrent(calendar)
                    view.performHapticFeedbackVirtualKey()
                },
            )
        }
    }
}
