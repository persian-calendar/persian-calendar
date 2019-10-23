package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.calendar.CivilDate

class GregorianCalendarEvent(date: CivilDate, title: String, holiday: Boolean) : AbstractEvent<CivilDate>() {
    init {
        this.date = date
        this.title = title
        this.isHoliday = holiday
    }
}
