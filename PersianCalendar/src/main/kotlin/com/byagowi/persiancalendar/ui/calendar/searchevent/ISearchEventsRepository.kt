package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.entities.CalendarEvent

interface ISearchEventsRepository {
    val events: List<CalendarEvent<*>>
    fun query(constraint: CharSequence): List<CalendarEvent<*>>
}
