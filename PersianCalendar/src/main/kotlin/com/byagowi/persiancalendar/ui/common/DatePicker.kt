package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.yearMonthNameOfDate
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.formatNumber

@Composable
fun DatePicker(calendar: Calendar, jdn: Jdn, setJdn: (Jdn) -> Unit) {
    Crossfade(targetState = calendar, label = "day picker") { calendarState ->
        Row(modifier = Modifier.fillMaxWidth()) { DatePickerContent(calendarState, jdn, setJdn) }
    }
}

@Composable
private fun RowScope.DatePickerContent(calendar: Calendar, jdn: Jdn, setJdn: (Jdn) -> Unit) {
    val yearsLimit = 5000 // let's just don't care about accuracy of distant time
    val date = remember(jdn.value, calendar) { jdn on calendar }
    val daysFormat = remember(calendar, date.year, date.month) {
        val monthStart = Jdn(calendar, date.year, date.month, 1);
        { item: Int -> (monthStart + item - 1).weekDayName + " / " + formatNumber(item) }
    }
    val monthsLength = remember(calendar, date.year, date.month) {
        calendar.getMonthLength(date.year, date.month)
    }
    val yearMonths = remember(calendar, date.year) {
        calendar.getYearMonths(date.year)
    }
    val monthsFormat = remember(calendar, date.year) {
        val months = yearMonthNameOfDate(date);
        { item: Int -> months[item - 1] + " / " + formatNumber(item) }
    }
    val todayYear = remember(calendar) { Jdn.today().on(calendar).year }
    val startYear = remember(calendar) { todayYear - yearsLimit / 2 }
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
        val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(date.year, month))
        setJdn(Jdn(calendar, date.year, month, day))
        view.performHapticFeedbackVirtualKey()
    }
    Spacer(modifier = Modifier.width(8.dp))
    NumberPicker(
        modifier = Modifier.weight(1f),
        range = startYear..startYear + yearsLimit,
        value = date.year,
        onClickLabel = stringResource(R.string.year),
    ) { year ->
        val month = date.month.coerceIn(1, calendar.getYearMonths(year))
        val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(year, month))
        setJdn(Jdn(calendar, year, month, day))
        view.performHapticFeedbackVirtualKey()
    }
}
