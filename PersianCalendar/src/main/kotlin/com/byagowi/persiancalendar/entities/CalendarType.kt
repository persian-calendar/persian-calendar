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
        val monthLength = getMonthLength(year, month)
        return monthLength - (Jdn(this, year, month, monthLength) - dayOfWeek + 1).dayOfWeek
    }

    fun getMonthLength(year: Int, month: Int) =
        Jdn(getMonthStartFromMonthsDistance(year, month, 1)) - Jdn(this, year, month, 1)

    private fun getMonthStartFromMonthsDistance(
        baseYear: Int, baseMonth: Int, monthsDistance: Int
    ): AbstractDate {
        var month = baseMonth + monthsDistance - 1
        var year = baseYear + month / 12
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
        return (date.year - base.year) * 12 + date.month - base.month
    }

    fun getMonthStartFromMonthsDistance(baseJdn: Jdn, monthsDistance: Int): AbstractDate {
        val date = baseJdn.toCalendar(this)
        return getMonthStartFromMonthsDistance(date.year, date.month, monthsDistance)
    }

    val monthsNames: List<String>
        get() = when (this) {
            SHAMSI -> persianMonths
            ISLAMIC -> islamicMonths
            GREGORIAN -> gregorianMonths
        }
}
