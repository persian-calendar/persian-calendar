package com.byagowi.persiancalendar.entity

import calendar.IslamicDate

class IslamicCalendarEvent(val date: IslamicDate, title: String,
                           holiday: Boolean) : AbstractEvent(title, holiday)