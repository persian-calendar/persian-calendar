package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.utils.EventsRepository

interface ISearchEventsRepository {
    suspend fun findEvent(query: CharSequence, eventsRepository: EventsRepository?):
            List<CalendarEvent<*>>
}
