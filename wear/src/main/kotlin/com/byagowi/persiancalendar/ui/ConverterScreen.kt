package com.byagowi.persiancalendar.ui

import android.icu.text.DateFormatSymbols
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.DatePickerDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PickerGroup
import androidx.wear.compose.material3.PickerState
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.curvedText
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.GregorianCalendar

@Composable
fun ConverterScreen() {
    val persianLocale = ULocale("fa_IR@calendar=persian")
    val persianDigitsFormatter = run {
        val symbols = DecimalFormatSymbols.getInstance(persianLocale)
        symbols.groupingSeparator = '\u0000'
        DecimalFormat("#", symbols)
    }
    val persianSymbols = DateFormatSymbols.getInstance(persianLocale)
    val weekDayNames = persianSymbols.weekdays
    val persianMonths = persianSymbols.months
    val gregorianMonths = DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=gregorian")).months
    val islamicMonths = DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=islamic")).months

    val today = run {
        val gregorianCalendar = GregorianCalendar.getInstance()
        val civilDate = CivilDate(
            gregorianCalendar[Calendar.YEAR],
            gregorianCalendar[Calendar.MONTH] + 1,
            gregorianCalendar[Calendar.DAY_OF_MONTH]
        )
        listOf(PersianDate(civilDate), civilDate, IslamicDate(civilDate))
    }

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
        date.toJdn()
    }

    AppScaffold {
        PickerGroup {
            listOf(
                calendarPickerState,
                dayPickerState,
                monthPickerState,
                yearPickerState,
            ).forEachIndexed { i, pickerState ->
                key(if (i == 0) 0 else calendarIndex) {
                    PickerGroupItem(
                        pickerState,
                        selected = selectedIndex == i, { selectedIndex = i },
                    ) { optionIndex, pickerSelected ->
                        Text(
                            when (i) {
                                0 -> when (optionIndex) {
                                    0 -> "شمسی"
                                    1 -> "میلادی"
                                    else -> "قمری"
                                }

                                1 -> persianDigitsFormatter.format(optionIndex + 1)

                                2 -> when (calendarIndex) {
                                    0 -> persianMonths
                                    1 -> gregorianMonths
                                    else -> islamicMonths
                                }[optionIndex]

                                else -> persianDigitsFormatter.format(
                                    optionIndex + today[calendarIndex].year
                                            - yearsLimit / 2
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
        }
        val weekDayName = weekDayNames[((currentJdn + 1) % 7 + 1).toInt()]

        fun monthsFromDate(date: AbstractDate) = when (date) {
            is PersianDate -> persianMonths
            is CivilDate -> gregorianMonths
            else -> islamicMonths
        }

        val firstText = run {
            val date = when (calendarIndex) {
                0 -> CivilDate(currentJdn)
                1 -> PersianDate(currentJdn)
                else -> PersianDate(currentJdn)
            }
            persianDigitsFormatter.format(date.dayOfMonth) + " " +
                    monthsFromDate(date)[date.month - 1] + " " +
                    persianDigitsFormatter.format(date.year)
        }
        val secondText = run {
            val date = when (calendarIndex) {
                0 -> IslamicDate(currentJdn)
                1 -> IslamicDate(currentJdn)
                else -> CivilDate(currentJdn)
            }
            persianDigitsFormatter.format(date.dayOfMonth) + " " +
                    monthsFromDate(date)[date.month - 1] + " " +
                    persianDigitsFormatter.format(date.year)
        }
        if (LocalConfiguration.current.isScreenRound) {
            val style = MaterialTheme.typography.arcMedium
            CurvedLayout(
                anchor = 90f,
                angularDirection = CurvedDirection.Angular.CounterClockwise,
            ) { curvedText(text = weekDayName, style = style) }
            CurvedLayout(
                anchor = 45f,
                angularDirection = CurvedDirection.Angular.CounterClockwise,
            ) { curvedText(text = firstText, style = style) }
            CurvedLayout(
                anchor = 135f,
                angularDirection = CurvedDirection.Angular.CounterClockwise,
            ) { curvedText(text = secondText, style = style) }
        } else {
            Text(
                weekDayName,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp),
            )
            Column(
                Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(firstText)
                Text(secondText)
            }
        }
    }
}

