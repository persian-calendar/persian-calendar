package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate

@JvmInline
value class EventsStore<T : CalendarEvent<out AbstractDate>>
private constructor(private val store: Map<Int, List<T>>) {
    constructor(eventsList: List<T>) : this(eventsList.groupBy { hash(it.date) })

    private fun getEventsEntry(date: AbstractDate) = store[hash(date)]?.filter {
        // dayOfMonth and month are already checked by hashing so only check year equality here
        it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
    } ?: emptyList()

    fun getEvents(date: AbstractDate) = getEventsEntry(date) +
            irregularCalendarEventsStore.getEvents(date)

    fun getEvents(
        date: CivilDate, deviceEvents: DeviceCalendarEventsStore
    ): List<CalendarEvent<*>> = deviceEvents.getEventsEntry(date) + getEvents(date)

    fun getAllEvents(): List<T> = store.values.flatten()

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun hash(date: AbstractDate) = date.month * 100 + date.dayOfMonth
        fun <T : CalendarEvent<out AbstractDate>> empty() = EventsStore<T>(emptyMap())
    }
}

typealias PersianCalendarEventsStore = EventsStore<CalendarEvent.PersianCalendarEvent>
typealias IslamicCalendarEventsStore = EventsStore<CalendarEvent.IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = EventsStore<CalendarEvent.GregorianCalendarEvent>
typealias DeviceCalendarEventsStore = EventsStore<CalendarEvent.DeviceCalendarEvent>
