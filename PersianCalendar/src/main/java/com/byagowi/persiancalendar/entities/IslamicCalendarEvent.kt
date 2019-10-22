package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.calendar.IslamicDate

class IslamicCalendarEvent(date: IslamicDate, title: String, holiday: Boolean) : AbstractEvent<IslamicDate>() {
    init {
        this.date = date
        this.title = title
        this.isHoliday = holiday
    }
}
