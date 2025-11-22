package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.yearMonthNameOfDate
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey

@Composable
fun DatePicker(
    calendar: Calendar,
    pendingConfirms: MutableCollection<() -> Unit>,
    jdn: Jdn,
    setJdn: (Jdn) -> Unit
) {
    Crossfade(targetState = calendar, label = "day picker") { calendarState ->
        Row(modifier = Modifier.fillMaxWidth()) {
            DatePickerContent(
                calendarState,
                pendingConfirms,
                jdn,
                setJdn,
            )
        }
    }
}

@Composable
private fun RowScope.DatePickerContent(
    calendar: Calendar,
    pendingConfirms: MutableCollection<() -> Unit>,
    jdn: Jdn,
    setJdn: (Jdn) -> Unit,
) {
    val yearsLimit = 5000 // let's just don't care about accuracy of distant time
    val date = remember(jdn.value, calendar) { jdn on calendar }
    val numeral by numeral.collectAsState()
    val daysFormat = remember(calendar, date.year, date.month) {
        val monthStart = Jdn(calendar, date.year, date.month, 1);
        { item: Int -> numeral.format(item) + " / " + (monthStart + item - 1).weekDay.title }
    }
    val monthsLength = remember(calendar, date.year, date.month) {
        calendar.getMonthLength(date.year, date.month)
    }
    val yearMonths = remember(calendar, date.year) {
        calendar.getYearMonths(date.year)
    }
    val monthsFormat = remember(calendar, date.year) {
        val months = yearMonthNameOfDate(date);
        { item: Int -> numeral.format(item) + " / " + months[item - 1] }
    }
    val todayYear = remember(calendar) { (Jdn.today() on calendar).year }
    val startYear = remember(calendar) { todayYear - yearsLimit / 2 }
    val view = LocalView.current
    NumberPicker(
        modifier = Modifier.weight(1f),
        label = daysFormat,
        range = 1..monthsLength,
        value = date.dayOfMonth,
        onClickLabel = stringResource(R.string.select_day),
        onPreviousLabel = stringResource(R.string.previous_x, stringResource(R.string.day)),
        onNextLabel = stringResource(R.string.next_x, stringResource(R.string.day)),
        pendingConfirms = pendingConfirms,
    ) {
        setJdn(Jdn(calendar, date.year, date.month, it))
        view.performHapticFeedbackVirtualKey()
    }
    Spacer(Modifier.width(8.dp))
    NumberPicker(
        modifier = Modifier.weight(1f),
        label = monthsFormat,
        range = 1..yearMonths,
        value = date.month,
        onClickLabel = stringResource(R.string.select_month),
        onPreviousLabel = stringResource(R.string.previous_x, stringResource(R.string.month)),
        onNextLabel = stringResource(R.string.next_x, stringResource(R.string.month)),
        pendingConfirms = pendingConfirms,
    ) { month ->
        val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(date.year, month))
        setJdn(Jdn(calendar, date.year, month, day))
        view.performHapticFeedbackVirtualKey()
    }
    Spacer(Modifier.width(8.dp))
    NumberPicker(
        modifier = Modifier.weight(1f),
        range = startYear..startYear + yearsLimit,
        value = date.year,
        onClickLabel = stringResource(R.string.select_year),
        onPreviousLabel = stringResource(R.string.previous_x, stringResource(R.string.year)),
        onNextLabel = stringResource(R.string.next_x, stringResource(R.string.year)),
        pendingConfirms = pendingConfirms,
    ) { year ->
        val month = date.month.coerceIn(1, calendar.getYearMonths(year))
        val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(year, month))
        setJdn(Jdn(calendar, year, month, day))
        view.performHapticFeedbackVirtualKey()
    }
}
