package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.title
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.yearAwareMonthsNames
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.day
import com.byagowi.persiancalendar.shared.generated.resources.month
import com.byagowi.persiancalendar.shared.generated.resources.next_x
import com.byagowi.persiancalendar.shared.generated.resources.previous_x
import com.byagowi.persiancalendar.shared.generated.resources.select_day
import com.byagowi.persiancalendar.shared.generated.resources.select_month
import com.byagowi.persiancalendar.shared.generated.resources.select_year
import com.byagowi.persiancalendar.shared.generated.resources.year
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import org.jetbrains.compose.resources.stringResource

@Composable
fun DatePicker(
    today: Jdn,
    calendar: Calendar,
    pendingConfirms: SnapshotStateList<() -> Unit>,
    value: Jdn,
    modifier: Modifier = Modifier,
    onValueChange: (Jdn) -> Unit,
) {
    Crossfade(
        modifier = modifier,
        targetState = calendar,
    ) { calendarState ->
        DatePickerContent(
            today = today,
            calendar = calendarState,
            pendingConfirms = pendingConfirms,
            value = value,
            onValueChange = onValueChange,
        )
    }
}

@Composable
private fun DatePickerContent(
    today: Jdn,
    calendar: Calendar,
    pendingConfirms: SnapshotStateList<() -> Unit>,
    value: Jdn,
    modifier: Modifier = Modifier,
    onValueChange: (Jdn) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        val yearsLimit = 5000 // let's just don't care about accuracy of distant time
        val date = remember(value.value, calendar) { value on calendar }
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
        val months = yearAwareMonthsNames(date)
        val monthsFormat = remember(numeral, months) {
            { item: Int -> language.inParentheses.format(months[item - 1], numeral.format(item)) }
        }
        val todayYear = remember(calendar, today) { (today on calendar).year }
        val startYear = remember(calendar) { todayYear - yearsLimit / 2 }
        val view = LocalView.current
        NumberPicker(
            modifier = Modifier.weight(1f),
            label = daysFormat,
            range = 1..monthsLength,
            value = date.dayOfMonth,
            onClickLabel = stringResource(Res.string.select_day),
            onPreviousLabel = stringResource(Res.string.previous_x, stringResource(Res.string.day)),
            onNextLabel = stringResource(Res.string.next_x, stringResource(Res.string.day)),
            pendingConfirms = pendingConfirms,
        ) {
            onValueChange(Jdn(calendar, date.year, date.month, it))
            view.performHapticFeedbackVirtualKey()
        }
        Spacer(Modifier.width(8.dp))
        NumberPicker(
            modifier = Modifier.weight(1f),
            label = monthsFormat,
            range = 1..yearMonths,
            value = date.month,
            onClickLabel = stringResource(Res.string.select_month),
            onPreviousLabel = stringResource(
                Res.string.previous_x,
                stringResource(Res.string.month),
            ),
            onNextLabel = stringResource(Res.string.next_x, stringResource(Res.string.month)),
            pendingConfirms = pendingConfirms,
        ) { month ->
            val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(date.year, month))
            onValueChange(Jdn(calendar, date.year, month, day))
            view.performHapticFeedbackVirtualKey()
        }
        Spacer(Modifier.width(8.dp))
        NumberPicker(
            modifier = Modifier.weight(1f),
            range = startYear..startYear + yearsLimit,
            value = date.year,
            onClickLabel = stringResource(Res.string.select_year),
            onPreviousLabel = stringResource(
                Res.string.previous_x,
                stringResource(Res.string.year),
            ),
            onNextLabel = stringResource(Res.string.next_x, stringResource(Res.string.year)),
            pendingConfirms = pendingConfirms,
        ) { year ->
            val month = date.month.coerceIn(1, calendar.getYearMonths(year))
            val day = date.dayOfMonth.coerceIn(1, calendar.getMonthLength(year, month))
            onValueChange(Jdn(calendar, year, month, day))
            view.performHapticFeedbackVirtualKey()
        }
    }
}
