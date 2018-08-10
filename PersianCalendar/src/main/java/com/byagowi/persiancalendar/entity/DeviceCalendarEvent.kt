package com.byagowi.persiancalendar.entity

import java.util.Date

import calendar.CivilDate

class DeviceCalendarEvent(val id: Int, title: String, val description: String,
                          val start: Date, val end: Date, val date: String, val civilDate: CivilDate,
                          val color: String) : AbstractEvent(title, false)
