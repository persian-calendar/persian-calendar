package com.byagowi.persiancalendar.ui

import android.icu.text.DateFormatSymbols
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.DatePickerDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PickerGroup
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.curvedText
import androidx.wear.compose.material3.rememberPickerState
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

    var selectedIndex by remember { mutableIntStateOf(0) }
    val typePickerState = rememberPickerState(3, 0, false)
    val yearOptions = 200
    val typeIndex = typePickerState.selectedOptionIndex
    val yearPickerState = key(typeIndex) {
        rememberPickerState(yearOptions, yearOptions / 2)
    }
    val monthPickerState = key(typeIndex) {
        rememberPickerState(12, today[typeIndex].month - 1)
    }
    var daysOnMonth by remember { mutableIntStateOf(31) }
    val dayPickerState = key(typeIndex, daysOnMonth) {
        rememberPickerState(daysOnMonth, today[typeIndex].dayOfMonth - 1)
    }
    val currentJdn = run {
        val year = yearPickerState.selectedOptionIndex - yearOptions / 2 + today[typeIndex].year
        val month = monthPickerState.selectedOptionIndex + 1
        val day = dayPickerState.selectedOptionIndex + 1
        val date = when (typeIndex) {
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
                typePickerState,
                dayPickerState,
                monthPickerState,
                yearPickerState,
            ).forEachIndexed { i, pickerState ->
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

                            2 -> when (typeIndex) {
                                0 -> persianMonths
                                1 -> gregorianMonths
                                else -> islamicMonths
                            }[optionIndex]

                            else -> persianDigitsFormatter.format(
                                optionIndex + today[typeIndex].year
                                        - yearOptions / 2
                            )
                        },
                        fontSize = MaterialTheme.typography.displayMedium.fontSize,
                        color = if (pickerSelected) DatePickerDefaults.datePickerColors().activePickerContentColor
                        else DatePickerDefaults.datePickerColors().inactivePickerContentColor,
                    )
                }
            }
        }
        val style = MaterialTheme.typography.arcSmall
        run {
            val civilDate = CivilDate(currentJdn)
            val text = persianDigitsFormatter.format(civilDate.dayOfMonth) + " " +
                    gregorianMonths[civilDate.month - 1] + " " +
                    persianDigitsFormatter.format(civilDate.year)
            CurvedLayout(
                anchor = 45f,
                angularDirection = CurvedDirection.Angular.CounterClockwise,
            ) { curvedText(text = text, style = style) }
        }
        run {
            val text = weekDayNames[((currentJdn + 1) % 7 + 1).toInt()]
            CurvedLayout(
                anchor = 90f,
                angularDirection = CurvedDirection.Angular.CounterClockwise,
            ) { curvedText(text = text, style = style) }
        }
        run {
            val islamicDate = IslamicDate(currentJdn)
            val text = persianDigitsFormatter.format(islamicDate.dayOfMonth) + " " +
                    gregorianMonths[islamicDate.month - 1] + " " +
                    persianDigitsFormatter.format(islamicDate.year)
            CurvedLayout(
                anchor = 135f,
                angularDirection = CurvedDirection.Angular.CounterClockwise,
            ) { curvedText(text = text, style = style) }
        }
    }
}

