package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate

@JvmInline
value class CalendarStore<T : CalendarEvent<out AbstractDate>> private constructor(val value: Map<Int, List<T>>) {
    constructor(list: List<T>) : this(list.groupBy { it.date.entry() })

    fun getEvents(date: AbstractDate) = value[date.entry()]?.filter {
        // dayOfMonth and month are already checked with #entry() so only checking year equality here
        it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
    } ?: emptyList()

    companion object {
        private fun AbstractDate.entry() = month * 100 + dayOfMonth
        fun <T : CalendarEvent<out AbstractDate>> empty() = CalendarStore<T>(emptyMap())
    }
}

typealias PersianCalendarEventsStore = CalendarStore<CalendarEvent.PersianCalendarEvent>
typealias IslamicCalendarEventsStore = CalendarStore<CalendarEvent.IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = CalendarStore<CalendarEvent.GregorianCalendarEvent>
typealias DeviceCalendarEventsStore = CalendarStore<CalendarEvent.DeviceCalendarEvent>
