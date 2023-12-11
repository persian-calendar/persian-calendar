package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey

@Composable
fun CalendarsTypesPicker(current: CalendarType, setCurrent: (CalendarType) -> Unit) {
    // TODO: Should be scrollable?
    TabRow(
        selectedTabIndex = enabledCalendars.indexOf(current),
        divider = {},
        containerColor = Color.Transparent,
        indicator = @Composable { tabPositions ->
            val selectedTabIndex = enabledCalendars.indexOf(current)
            if (selectedTabIndex < tabPositions.size) {
                SecondaryIndicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = ExtraLargeShapeCornerSize.dp),
                    height = 2.dp,
                )
            }
        },
    ) {
        enabledCalendars.forEach { calendarType ->
            val title = stringResource(
                if (language.betterToUseShortCalendarName) calendarType.shortTitle
                else calendarType.title
            )
            val view = LocalView.current
            Tab(
                text = { Text(title) },
                selected = current == calendarType,
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    setCurrent(calendarType)
                    view.performHapticFeedbackVirtualKey()
                },
            )
        }
    }
}
