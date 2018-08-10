package com.byagowi.persiancalendar.entity

import calendar.PersianDate

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
class PersianCalendarEvent(val date: PersianDate, title: String,
                           holiday: Boolean) : AbstractEvent() {

    init {
        this.title = title
        this.holiday = holiday
    }
}