package com.byagowi.persiancalendar.entity

import calendar.IslamicDate

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
class IslamicCalendarEvent(val date: IslamicDate, title: String,
                           holiday: Boolean) : AbstractEvent() {

    init {
        this.title = title
        this.holiday = holiday
    }
}