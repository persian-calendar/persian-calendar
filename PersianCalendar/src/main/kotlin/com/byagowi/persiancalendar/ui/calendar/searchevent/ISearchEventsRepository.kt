package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.entities.CalendarEvent

interface ISearchEventsRepository {
    suspend fun findEvent(query: CharSequence): List<CalendarEvent<*>>

    companion object {
        fun empty() = object : ISearchEventsRepository {
            override suspend fun findEvent(query: CharSequence): List<CalendarEvent<*>> =
                emptyList()
        }
    }
}
