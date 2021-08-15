package com.byagowi.persiancalendar.utils

import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

enum class CalendarType {
    // So vital, don't ever change names of these
    SHAMSI, ISLAMIC, GREGORIAN;

    fun createDate(year: Int, month: Int, day: Int) = when (this) {
        ISLAMIC -> IslamicDate(year, month, day)
        GREGORIAN -> CivilDate(year, month, day)
        SHAMSI -> PersianDate(year, month, day)
    }
}
