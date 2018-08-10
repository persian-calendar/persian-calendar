package com.byagowi.persiancalendar.entity

import calendar.IslamicDate

class IslamicCalendarEvent(val date: IslamicDate, title: String,
                           holiday: Boolean) : AbstractEvent() {

  init {
    this.title = title
    this.holiday = holiday
  }
}