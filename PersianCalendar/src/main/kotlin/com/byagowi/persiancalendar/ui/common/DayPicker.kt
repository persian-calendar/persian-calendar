package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber

@Composable
fun DayPicker(
    calendarType: CalendarType,
    jdn: Jdn,
    setJdn: (Jdn) -> Unit,
) {
    Crossfade(targetState = calendarType, label = "day picker") { calendar ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val date = remember(jdn.value, calendar) { jdn.toCalendar(calendar) }
            val daysFormat = remember(calendar, date.year, date.month) {
                val monthStart = Jdn(calendar, date.year, date.month, 1);
                { item: Int -> (monthStart + item - 1).dayOfWeekName + " / " + formatNumber(item) }
            }
            val monthsLength = remember(calendar, date.year, date.month) {
                calendar.getMonthLength(date.year, date.month)
            }
            val yearMonths = remember(calendar, date.year) {
                calendar.getYearMonths(date.year)
            }
            val monthsFormat = remember(calendar, date.year) {
                val months = date.calendarType.monthsNames
                { item: Int -> months[item - 1] + " / " + formatNumber(item) }
            }
            val todayYear = remember(calendar) { Jdn.today().toCalendar(calendar).year }
            val startYear = remember(calendar) { todayYear - 200 }
            var monthChangeToken by remember { mutableIntStateOf(0) }
            var previousMonth by remember { mutableIntStateOf(0) }
            if (previousMonth != date.month) ++monthChangeToken
            previousMonth = date.month
            Row(modifier = Modifier.fillMaxWidth()) {
                val view = LocalView.current
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    label = daysFormat,
                    range = 1..monthsLength,
                    value = date.dayOfMonth,
                    onClickLabel = stringResource(R.string.day),
                ) {
                    setJdn(Jdn(calendar, date.year, date.month, it))
                    view.performHapticFeedbackVirtualKey()
                }
                Spacer(modifier = Modifier.width(8.dp))
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    label = monthsFormat,
                    range = 1..yearMonths,
                    value = date.month,
                    onClickLabel = stringResource(R.string.month),
                ) { month ->
                    val day =
                        date.dayOfMonth.coerceIn(1, calendar.getMonthLength(date.year, month))
                    setJdn(Jdn(calendar, date.year, month, day))
                    view.performHapticFeedbackVirtualKey()
                }
                Spacer(modifier = Modifier.width(8.dp))
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    range = startYear..startYear + 400,
                    value = date.year,
                    onClickLabel = stringResource(R.string.year),
                ) { year ->
                    val month = date.month.coerceIn(1, calendar.getYearMonths(year))
                    val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(year, month))
                    setJdn(Jdn(calendar, year, month, day))
                    view.performHapticFeedbackVirtualKey()
                }
            }
        }
    }
}
