package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.calendar.CivilDate
import java.util.*

class DeviceCalendarEvent(val id: Int, title: String, val description: String,
                          val start: Date, val end: Date, val dateString: String, date: CivilDate,
                          val color: String) : AbstractEvent<CivilDate>() {

    init {
        this.title = title
        this.date = date
    }
}
