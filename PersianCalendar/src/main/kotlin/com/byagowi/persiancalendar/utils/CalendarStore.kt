package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate

@JvmInline
value class CalendarStore<T : CalendarEvent<out AbstractDate>>
private constructor(private val store: Map<Int, List<T>>) {
    constructor(list: List<T>) : this(list.groupBy { it.date.hash })

    fun getEvents(date: AbstractDate) = store[date.hash]?.filter {
        // dayOfMonth and month are already checked with hashing so only check year equality here
        it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
    } ?: emptyList()

    companion object {
        private val AbstractDate.hash get() = this.month * 100 + this.dayOfMonth
        fun <T : CalendarEvent<out AbstractDate>> empty() = CalendarStore<T>(emptyMap())
    }
}

typealias PersianCalendarEventsStore = CalendarStore<CalendarEvent.PersianCalendarEvent>
typealias IslamicCalendarEventsStore = CalendarStore<CalendarEvent.IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = CalendarStore<CalendarEvent.GregorianCalendarEvent>
typealias DeviceCalendarEventsStore = CalendarStore<CalendarEvent.DeviceCalendarEvent>
