package com.byagowi.persiancalendar.entity

import calendar.CivilDate
import java.util.*

class DeviceCalendarEvent(val id: Int, title: String, val description: String,
                          val start: Date, val end: Date, val date: String, val civilDate: CivilDate,
                          val color: String) : AbstractEvent(title, false)
