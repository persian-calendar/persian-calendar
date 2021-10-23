package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.gregorianMonths
import com.byagowi.persiancalendar.global.islamicMonths
import com.byagowi.persiancalendar.global.persianMonths
import io.github.persiancalendar.calendar.AbstractDate
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

    fun getMonthStartFromMonthsDistance(baseJdn: Jdn, monthsDistance: Int): AbstractDate {
        val date = baseJdn.toCalendar(this)
        var month = date.month - monthsDistance
        month -= 1
        var year = date.year + month / 12
        month %= 12
        if (month < 0) {
            year -= 1
            month += 12
        }
        month += 1
        return createDate(year, month, 1)
    }

    fun getMonthsDistance(baseJdn: Jdn, toJdn: Jdn): Int {
        val base = baseJdn.toCalendar(this)
        val date = toJdn.toCalendar(this)
        return (base.year - date.year) * 12 + base.month - date.month
    }

    val monthsNames: List<String>
        get() = when (this) {
            SHAMSI -> persianMonths
            ISLAMIC -> islamicMonths
            GREGORIAN -> gregorianMonths
        }
}
