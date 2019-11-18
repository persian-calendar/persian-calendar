package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.*
import io.github.persiancalendar.calendar.AbstractDate
import java.util.*

private typealias BaseStore<T> = Map<Int, List<T>>
typealias PersianCalendarEventsStore = BaseStore<PersianCalendarEvent>
typealias IslamicCalendarEventsStore = BaseStore<IslamicCalendarEvent>
typealias GregorianCalendarEventsStore = BaseStore<GregorianCalendarEvent>
typealias DeviceCalendarEventsStore = BaseStore<DeviceCalendarEvent>

private fun AbstractDate.entry() = month * 100 + dayOfMonth

fun <T : CalendarEvent<out AbstractDate>> List<T>.toEventsStore(): BaseStore<T> =
    HashMap<Int, ArrayList<T>>().also { result ->
        this.forEach { event ->
            val entry = event.date.entry()
            (result[entry] ?: ArrayList<T>().also { result[entry] = it }).add(event)
        }
    }

fun <T : AbstractDate> BaseStore<CalendarEvent<T>>.getEvents(date: T) = this[date.entry()]?.filter {
    // dayOfMonth and month are already checked with #entry() so only checking year equality here
    it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
} ?: emptyList()

fun <T> emptyEventsStore(): BaseStore<T> = emptyMap()
