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

fun <T : CalendarEvent<out AbstractDate>> List<T>.toEventsStore(): BaseStore<T> {
    val result = HashMap<Int, ArrayList<T>>()
    this.forEach {
        (result[it.date.entry()] ?: ArrayList<T>().apply { result[it.date.entry()] = this }).add(it)
    }
    return result
}

fun <T : AbstractDate> BaseStore<CalendarEvent<T>>.getEvents(date: T) = this[date.entry()]?.filter {
    // dayOfMonth and month are already checked with #entry() so only checking year equality here
    it.date.year == date.year || it.date.year == -1 // -1 means it is occurring every year
} ?: emptyList()

fun <T> emptyEventsStore(): BaseStore<T> = emptyMap()
