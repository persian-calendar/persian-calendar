package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.DatePickerDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PickerGroup
import androidx.wear.compose.material3.PickerState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

@Composable
fun ConverterScreen(todayJdn: Jdn) {
    val localeUtils = LocaleUtils()
    val today = listOf(todayJdn.toPersianDate(), todayJdn.toCivilDate(), todayJdn.toIslamicDate())

    var selectedIndex by remember { mutableIntStateOf(1) }
    val calendarPickerState = remember { PickerState(3, 0, false) }
    val calendarIndex = calendarPickerState.selectedOptionIndex
    val yearsLimit = 200
    val yearPickerState = remember(calendarIndex, yearsLimit) {
        PickerState(yearsLimit, yearsLimit / 2)
    }
    val monthPickerState = remember(calendarIndex, today[calendarIndex].month) {
        PickerState(12, today[calendarIndex].month - 1)
    }
    var daysOnMonth by remember { mutableIntStateOf(31) }
    val dayPickerState = remember(calendarIndex, daysOnMonth, today[calendarIndex].dayOfMonth) {
        PickerState(daysOnMonth, today[calendarIndex].dayOfMonth - 1)
    }
    val currentJdn = run {
        val year = yearPickerState.selectedOptionIndex - yearsLimit / 2 + today[calendarIndex].year
        val month = monthPickerState.selectedOptionIndex + 1
        val day = dayPickerState.selectedOptionIndex + 1
        val date = when (calendarIndex) {
            0 -> PersianDate(year, month, day)
            1 -> CivilDate(year, month, day)
            else -> IslamicDate(year, month, day)
        }
        val currentMonth = when (date) {
            is PersianDate -> PersianDate(date.year, date.month, 1)
            is CivilDate -> CivilDate(date.year, date.month, 1)
            is IslamicDate -> IslamicDate(date.year, date.month, 1)
            else -> date
        }.toJdn()
        val nextMonthDate = when (date) {
            is PersianDate -> date.monthStartOfMonthsDistance(1)
            is CivilDate -> date.monthStartOfMonthsDistance(1)
            is IslamicDate -> date.monthStartOfMonthsDistance(1)
            else -> date
        }.toJdn()
        daysOnMonth = (nextMonthDate - currentMonth).toInt()
        Jdn(date)
    }

    ScreenScaffold {
        val pickerStates = listOf(
            calendarPickerState,
            dayPickerState,
            monthPickerState,
            yearPickerState,
        )
        PickerGroup(selectedPickerState = pickerStates[selectedIndex]) {
            pickerStates.forEachIndexed { i, pickerState ->
                PickerGroupItem(
                    pickerState = pickerState,
                    selected = selectedIndex == i,
                    onSelected = { selectedIndex = i },
                ) { optionIndex, pickerSelected ->
                    Text(
                        when (i) {
                            0 -> when (optionIndex) {
                                0 -> "شمسی"
                                1 -> "میلادی"
                                else -> "قمری"
                            }

                            1 -> localeUtils.format(optionIndex + 1)

                            2 -> when (calendarIndex) {
                                0 -> localeUtils.persianMonths
                                1 -> localeUtils.gregorianMonths
                                else -> localeUtils.islamicMonths
                            }[optionIndex]

                            else -> localeUtils.format(
                                optionIndex + today[calendarIndex].year - yearsLimit / 2
                            )
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.defaultMinSize(minWidth = 40.dp),
                        fontSize = MaterialTheme.typography.displayMedium.fontSize,
                        color = if (pickerSelected) DatePickerDefaults.datePickerColors().activePickerContentColor
                        else DatePickerDefaults.datePickerColors().inactivePickerContentColor,
                    )
                }
            }
        }
        OtherCalendars(
            localeUtils = localeUtils,
            day = currentJdn,
            calendarIndex = calendarIndex,
        )
    }
}
