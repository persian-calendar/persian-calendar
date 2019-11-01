package com.byagowi.persiancalendar.utils

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

fun getCalendarTypeFromDate(date: AbstractDate): CalendarType = when (date) {
    is IslamicDate -> CalendarType.ISLAMIC
    is CivilDate -> CalendarType.GREGORIAN
    else -> CalendarType.SHAMSI
}

fun getDateOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): AbstractDate =
    when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(year, month, day)
        CalendarType.GREGORIAN -> CivilDate(year, month, day)
        CalendarType.SHAMSI -> PersianDate(year, month, day)
    }

fun getMonthLength(calendar: CalendarType, year: Int, month: Int): Int {
    val yearOfNextMonth = if (month == 12) year + 1 else year
    val nextMonth = if (month == 12) 1 else month + 1
    return (getDateOfCalendar(calendar, yearOfNextMonth, nextMonth, 1).toJdn() - getDateOfCalendar(
        calendar,
        year,
        month,
        1
    ).toJdn()).toInt()
}