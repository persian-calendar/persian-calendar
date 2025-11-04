package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate

enum class Calendar(
    @get:StringRes val title: Int,
    @get:StringRes val shortTitle: Int,
    val preferredNumeral: Numeral,
) {
    // So vital, don't ever change names of these
    SHAMSI(
        R.string.shamsi_calendar, R.string.shamsi_calendar_short, Numeral.PERSIAN
    ),
    ISLAMIC(
        R.string.islamic_calendar, R.string.islamic_calendar_short, Numeral.ARABIC_INDIC
    ),
    GREGORIAN(
        R.string.gregorian_calendar, R.string.gregorian_calendar_short, Numeral.ARABIC
    ),
    NEPALI(
        R.string.nepali_calendar, R.string.nepali_calendar_short, Numeral.DEVANAGARI
    );

    fun createDate(year: Int, month: Int, day: Int): AbstractDate = when (this) {
        ISLAMIC -> IslamicDate(year, month, day)
        GREGORIAN -> CivilDate(year, month, day)
        SHAMSI -> PersianDate(year, month, day)
        NEPALI -> NepaliDate(year, month, day)
    }

    fun getNthWeekDayOfMonth(year: Int, month: Int, weekDay: Int, nth: Int): Int {
        val appWeekDay = WeekDay.fromISO8601(weekDay).ordinal
        val monthStartWeekDay = Jdn(this, year, month, 1).weekDay.ordinal
        return appWeekDay + 1 - monthStartWeekDay + nth * 7 - if (monthStartWeekDay <= appWeekDay) 7 else 0
    }

    fun getLastWeekDayOfMonth(year: Int, month: Int, weekDay: Int): Int {
        val appWeekDay = WeekDay.fromISO8601(weekDay).ordinal
        val monthLength = getMonthLength(year, month)
        return monthLength - (Jdn(this, year, month, monthLength) - appWeekDay).weekDay.ordinal
    }

    fun getYearMonths(year: Int): Int =
        ((Jdn(this, year + 1, 1, 1) - 1) on this).month

    fun getMonthLength(year: Int, month: Int): Int =
        ((Jdn(getMonthStartFromMonthsDistance(year, month, 1)) - 1) on this).dayOfMonth

    private fun getMonthStartFromMonthsDistance(
        baseYear: Int, baseMonth: Int, monthsDistance: Int
    ): AbstractDate = when (this) {
        ISLAMIC -> IslamicDate(baseYear, baseMonth, 1).monthStartOfMonthsDistance(monthsDistance)
        GREGORIAN -> CivilDate(baseYear, baseMonth, 1).monthStartOfMonthsDistance(monthsDistance)
        SHAMSI -> PersianDate(baseYear, baseMonth, 1).monthStartOfMonthsDistance(monthsDistance)
        NEPALI -> NepaliDate(baseYear, baseMonth, 1).monthStartOfMonthsDistance(monthsDistance)
    }

    fun getMonthsDistance(baseJdn: Jdn, toJdn: Jdn): Int = when (this) {
        ISLAMIC -> baseJdn.toIslamicDate().monthsDistanceTo(toJdn.toIslamicDate())
        GREGORIAN -> baseJdn.toCivilDate().monthsDistanceTo(toJdn.toCivilDate())
        SHAMSI -> baseJdn.toPersianDate().monthsDistanceTo(toJdn.toPersianDate())
        NEPALI -> baseJdn.toNepaliDate().monthsDistanceTo(toJdn.toNepaliDate())
    }

    fun getMonthStartFromMonthsDistance(baseJdn: Jdn, monthsDistance: Int): AbstractDate {
        val date = baseJdn on this
        return getMonthStartFromMonthsDistance(date.year, date.month, monthsDistance)
    }
}
