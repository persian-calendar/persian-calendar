package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate

@JvmInline
value class EventsStore<T : CalendarEvent<out AbstractDate>>
private constructor(private val store: Map<Int, List<T>>) {
    constructor(eventsList: List<T>) : this(eventsList.groupBy { it.date.hash })

    private fun getEntryEvents(date: AbstractDate) = store[date.hash]?.filter {
        // dayOfMonth and month are already checked with hashing so only check year equality here
        it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
    } ?: emptyList()

    fun getEvents(date: AbstractDate) = getEntryEvents(date) +
            // Handle Islamic events happening in 30th day but the month has only 29 days
            if (
                date is IslamicDate && date.dayOfMonth == 29 &&
                CalendarType.ISLAMIC.getMonthLength(date.year, date.month) == 29
            ) getEntryEvents(IslamicDate(date.year, date.month, 30)) else emptyList()

    fun getEvents(
        date: CivilDate, deviceEvents: DeviceCalendarEventsStore
    ): List<CalendarEvent<*>> = deviceEvents.getEntryEvents(date) + getEvents(date)

    companion object {
        private val AbstractDate.hash get() = this.month * 100 + this.dayOfMonth
        fun <T : CalendarEvent<out AbstractDate>> empty() = EventsStore<T>(emptyMap())
    }
}

typealias PersianCalendarEventsStore = EventsStore<CalendarEvent.PersianCalendarEvent>
typealias IslamicCalendarEventsStore = EventsStore<CalendarEvent.IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = EventsStore<CalendarEvent.GregorianCalendarEvent>
typealias DeviceCalendarEventsStore = EventsStore<CalendarEvent.DeviceCalendarEvent>
