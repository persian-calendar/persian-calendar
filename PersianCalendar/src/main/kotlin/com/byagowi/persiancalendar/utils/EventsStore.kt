package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate

@JvmInline
value class EventsStore<T : CalendarEvent<out AbstractDate>>
private constructor(private val store: Map<Int, List<T>>) {
    constructor(eventsList: List<T>) : this(eventsList.groupBy { it.date.hash })

    fun getEvents(date: AbstractDate) = store[date.hash]?.filter {
        // dayOfMonth and month are already checked with hashing so only check year equality here
        it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
    } ?: emptyList()

    companion object {
        private val AbstractDate.hash get() = this.month * 100 + this.dayOfMonth
        fun <T : CalendarEvent<out AbstractDate>> empty() = EventsStore<T>(emptyMap())
    }
}

typealias PersianCalendarEventsStore = EventsStore<CalendarEvent.PersianCalendarEvent>
typealias IslamicCalendarEventsStore = EventsStore<CalendarEvent.IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = EventsStore<CalendarEvent.GregorianCalendarEvent>
typealias DeviceCalendarEventsStore = EventsStore<CalendarEvent.DeviceCalendarEvent>
