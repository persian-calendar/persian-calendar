package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.gregorianMonths
import com.byagowi.persiancalendar.global.islamicMonths
import com.byagowi.persiancalendar.global.nepaliMonths
import com.byagowi.persiancalendar.global.persianMonths
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate

enum class Calendar(
    @StringRes val title: Int, @StringRes val shortTitle: Int, val preferredDigits: CharArray
) {
    // So vital, don't ever change names of these
    SHAMSI(
        R.string.shamsi_calendar, R.string.shamsi_calendar_short, Language.PERSIAN_DIGITS
    ),
    ISLAMIC(
        R.string.islamic_calendar, R.string.islamic_calendar_short, Language.ARABIC_INDIC_DIGITS
    ),
    GREGORIAN(
        R.string.gregorian_calendar, R.string.gregorian_calendar_short, Language.ARABIC_DIGITS
    ),
    NEPALI(
        R.string.nepali_calendar, R.string.nepali_calendar_short, Language.DEVANAGARI_DIGITS
    );

    fun createDate(year: Int, month: Int, day: Int): AbstractDate = when (this) {
        ISLAMIC -> IslamicDate(year, month, day)
        GREGORIAN -> CivilDate(year, month, day)
        SHAMSI -> PersianDate(year, month, day)
        NEPALI -> NepaliDate(year, month, day)
    }

    // 1 means Saturday on it and 7 means Friday
    fun getNthWeekDayOfMonth(year: Int, month: Int, weekDay: Int, nth: Int): Int {
        val monthStartWeekDay = Jdn(this, year, month, 1).weekDay
        return weekDay - monthStartWeekDay + nth * 7 - if (monthStartWeekDay < weekDay) 7 else 0
    }

    fun getLastWeekDayOfMonth(year: Int, month: Int, weekDay: Int): Int {
        val monthLength = getMonthLength(year, month)
        return monthLength - (Jdn(this, year, month, monthLength) - weekDay + 1).weekDay
    }

    fun getYearMonths(year: Int): Int =
        (Jdn(this, year + 1, 1, 1) - 1).inCalendar(this).month

    fun getMonthLength(year: Int, month: Int): Int =
        Jdn(getMonthStartFromMonthsDistance(year, month, 1)) - Jdn(this, year, month, 1)

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
        val date = baseJdn.inCalendar(this)
        return getMonthStartFromMonthsDistance(date.year, date.month, monthsDistance)
    }

    val monthsNames: List<String>
        get() = when (this) {
            SHAMSI -> persianMonths
            ISLAMIC -> islamicMonths
            GREGORIAN -> gregorianMonths
            NEPALI -> nepaliMonths
        }
}
