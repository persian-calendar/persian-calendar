package com.byagowi.persiancalendar.entity

import calendar.CivilDate

/**
 * PersianCalendarEvent
 *
 * @author ebraminio
 */
class GregorianCalendarEvent(val date: CivilDate, title: String,
                             holiday: Boolean) : AbstractEvent() {

    init {
        this.title = title
        this.holiday = holiday
    }
}