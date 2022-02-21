package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.entities.CalendarEvent

interface ISearchEventsRepository {
    suspend fun findEvent(query: CharSequence): List<CalendarEvent<*>>
}
