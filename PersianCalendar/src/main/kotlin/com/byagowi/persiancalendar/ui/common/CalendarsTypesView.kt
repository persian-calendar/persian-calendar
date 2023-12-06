package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import kotlinx.coroutines.flow.MutableStateFlow

class CalendarsTypesView(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    var onValueChangeListener = fun(_: CalendarType) {}
    private val valueFlow = MutableStateFlow(mainCalendar)
    var value: CalendarType
        get() = valueFlow.value
        set(value) {
            valueFlow.value = value
        }

    init {
        val root = ComposeView(context)
        root.setContent {
            AppTheme {
                val current by valueFlow.collectAsState()
                onValueChangeListener(current)
                CalendarsTypes(current) { valueFlow.value = it }
            }
        }
        addView(root)
    }
}

@Composable
fun CalendarsTypes(current: CalendarType, setCurrent: (CalendarType) -> Unit) {
    // TODO: Should be scrollable?
    TabRow(
        selectedTabIndex = enabledCalendars.indexOf(current),
        divider = {},
        containerColor = Color.Transparent,
        indicator = @Composable { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[enabledCalendars.indexOf(current)])
                    .padding(horizontal = ExtraLargeShapeCornerSize.dp),
                height = 2.dp,
            )
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
