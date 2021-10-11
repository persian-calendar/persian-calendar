package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.gregorianMonths
import com.byagowi.persiancalendar.utils.islamicMonths
import com.byagowi.persiancalendar.utils.persianMonths
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

enum class CalendarType(@StringRes val title: Int, @StringRes val shortTitle: Int) {
    // So vital, don't ever change names of these
    SHAMSI(R.string.shamsi_calendar, R.string.shamsi_calendar_short),
    ISLAMIC(R.string.islamic_calendar, R.string.islamic_calendar_short),
    GREGORIAN(R.string.gregorian_calendar, R.string.gregorian_calendar_short);

    fun createDate(year: Int, month: Int, day: Int) = when (this) {
        ISLAMIC -> IslamicDate(year, month, day)
        GREGORIAN -> CivilDate(year, month, day)
        SHAMSI -> PersianDate(year, month, day)
    }

    // 1 means Saturday on it and 7 means Friday
    fun getLastWeekDayOfMonth(year: Int, month: Int, dayOfWeek: Int): Int {
        val monthLength = this.getMonthLength(year, month)
        val endOfMonthJdn = Jdn(this, year, month, monthLength)
        return monthLength - ((endOfMonthJdn.value - dayOfWeek + 3L) % 7).toInt()
    }

    fun getMonthLength(year: Int, month: Int): Int {
        val nextMonthYear = if (month == 12) year + 1 else year
        val nextMonthMonth = if (month == 12) 1 else month + 1
        val nextMonthStartingDay = Jdn(this, nextMonthYear, nextMonthMonth, 1)
        val thisMonthStartingDay = Jdn(this, year, month, 1)
        return nextMonthStartingDay - thisMonthStartingDay
    }

    val monthsNames: List<String>
        get() = when (this) {
            SHAMSI -> persianMonths
            ISLAMIC -> islamicMonths
            GREGORIAN -> gregorianMonths
        }
}
