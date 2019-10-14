package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.calendar.PersianDate

class PersianCalendarEvent(date: PersianDate, title: String, holiday: Boolean) : AbstractEvent<PersianDate>() {
    init {
        this.date = date
        this.title = title
        this.isHoliday = holiday
    }
}
