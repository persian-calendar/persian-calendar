package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.calendar.CivilDate

/** Provider instances are occurrences, and can occupy several dates. No annual-event hashing. */
class DeviceCalendarEventsStore(events: List<CalendarEvent.DeviceCalendarEvent>) {
    private val occurrences = events.sortedBy { it.interval.beginMillis }

    fun getEvents(date: CivilDate): List<CalendarEvent.DeviceCalendarEvent> =
        occurrences.filter { it.interval.occupies(date) }

    fun getAllEvents(): List<CalendarEvent.DeviceCalendarEvent> = occurrences

    companion object {
        fun empty() = DeviceCalendarEventsStore(emptyList())
    }
}
