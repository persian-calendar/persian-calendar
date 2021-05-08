package com.byagowi.persiancalendar.utils

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*
import kotlin.math.ceil

// Julian day number, basically a day counter starting from some day in concept
// https://en.wikipedia.org/wiki/Julian_day
@JvmInline
value class Jdn(val value: Long) {
    constructor(value: AbstractDate) : this(value.toJdn())
    constructor(calendar: CalendarType, year: Int, month: Int, day: Int) : this(when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(year, month, day)
        CalendarType.GREGORIAN -> CivilDate(year, month, day)
        CalendarType.SHAMSI -> PersianDate(year, month, day)
    })

    // 0 means Saturday on it, see #test_day_of_week_from_jdn() in the testsuite
    val dayOfWeek: Int
        get() = ((value + 2L) % 7L).toInt()

    fun toCalendar(calendar: CalendarType): AbstractDate = when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(value)
        CalendarType.GREGORIAN -> CivilDate(value)
        CalendarType.SHAMSI -> PersianDate(value)
    }

    fun getWeekOfYear(startOfYearJdn: Jdn): Int {
        val dayOfYear = this - startOfYearJdn
        return ceil(1 + (dayOfYear - applyWeekStartOffsetToWeekDay(dayOfWeek)) / 7.0).toInt()
    }

    infix fun until(other: Jdn) = value until other.value

    operator fun plus(other: Int): Jdn = Jdn(value + other)
    operator fun minus(other: Int): Jdn = Jdn(value - other)

    // Difference of two Jdn values in days
    operator fun minus(other: Jdn): Int = (value - other.value).toInt()

    companion object {
        val today: Jdn
            get() = Jdn(calendarToCivilDate(makeCalendarFromDate(Date())).toJdn())
    }
}
