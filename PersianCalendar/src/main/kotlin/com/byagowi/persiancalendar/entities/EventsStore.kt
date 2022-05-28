package com.byagowi.persiancalendar.entities

import androidx.annotation.VisibleForTesting
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

    fun getEvents(
        date: AbstractDate, irregularCalendarEventsStore: IrregularCalendarEventsStore
    ): List<T> {
        return getEventsEntry(date) + irregularCalendarEventsStore.getEvents(date)
    }

    fun getEvents(
        date: CivilDate, irregularCalendarEventsStore: IrregularCalendarEventsStore,
        deviceEvents: DeviceCalendarEventsStore
    ): List<CalendarEvent<*>> {
        return deviceEvents.getEventsEntry(date) + getEvents(date, irregularCalendarEventsStore)
    }

    fun getAllEvents(): List<T> = store.values.flatten()

    companion object {
        @VisibleForTesting
        fun hash(date: AbstractDate) = date.month * 100 + date.dayOfMonth
        fun <T : CalendarEvent<out AbstractDate>> empty() = EventsStore<T>(emptyMap())
    }
}

typealias PersianCalendarEventsStore = EventsStore<CalendarEvent.PersianCalendarEvent>
typealias IslamicCalendarEventsStore = EventsStore<CalendarEvent.IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = EventsStore<CalendarEvent.GregorianCalendarEvent>
typealias NepaliCalendarEventsStore = EventsStore<CalendarEvent.NepaliCalendarEvent>
typealias DeviceCalendarEventsStore = EventsStore<CalendarEvent.DeviceCalendarEvent>
