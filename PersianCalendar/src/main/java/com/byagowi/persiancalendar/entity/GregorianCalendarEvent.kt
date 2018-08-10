package com.byagowi.persiancalendar.entity

import calendar.CivilDate

class GregorianCalendarEvent(val date: CivilDate, title: String,
                             holiday: Boolean) : AbstractEvent(title, holiday)