package com.byagowi.persiancalendar.entity

import calendar.PersianDate

class PersianCalendarEvent(val date: PersianDate, title: String,
                           holiday: Boolean) : AbstractEvent(title, holiday)