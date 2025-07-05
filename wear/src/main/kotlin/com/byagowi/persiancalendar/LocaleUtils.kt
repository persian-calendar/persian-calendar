package com.byagowi.persiancalendar

import android.icu.text.DateFormatSymbols
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.ULocale
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate

class LocaleUtils {
    private val persianLocale = ULocale("fa_IR@calendar=persian")
    private val formatSymbols = DateFormatSymbols.getInstance(persianLocale)
    private val weekDayNames = formatSymbols.weekdays.asList()
    val narrowWeekdays: List<String> =
        formatSymbols.getWeekdays(DateFormatSymbols.STANDALONE, DateFormatSymbols.NARROW).asList()
    val persianMonths: List<String> = formatSymbols.months.asList()
    val gregorianMonths: List<String> =
        DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=gregorian")).months.asList()
    val islamicMonths: List<String> =
        DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=islamic")).months.asList()

    fun persianMonth(persianDate: PersianDate): String = persianMonths[persianDate.month - 1]

    private val noSeparatorFormatter = run {
        val symbols = DecimalFormatSymbols.getInstance(persianLocale)
        symbols.groupingSeparator = '\u0000'
        DecimalFormat("#", symbols)
    }

    fun weekDayName(jdn: Jdn): String = weekDayNames[((jdn.value + 1) % 7 + 1).toInt()]

    fun format(value: Int): String = noSeparatorFormatter.format(value)

    fun format(date: AbstractDate) =
        format(date.dayOfMonth) + " " + monthsFromDate(date)[date.month - 1] + " " + format(date.year)

    private fun monthsFromDate(date: AbstractDate) = when (date) {
        is PersianDate -> persianMonths
        is CivilDate -> gregorianMonths
        else -> islamicMonths
    }
}
