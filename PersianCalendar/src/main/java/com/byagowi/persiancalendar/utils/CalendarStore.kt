package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.entities.GregorianCalendarEvent
import com.byagowi.persiancalendar.entities.IslamicCalendarEvent
import com.byagowi.persiancalendar.entities.PersianCalendarEvent
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*

typealias PersianCalendarEventsStore = Map<Int, List<PersianCalendarEvent>>
typealias IslamicCalendarEventsStore = Map<Int, List<IslamicCalendarEvent>>
typealias GregorianCalendarEventsStore = Map<Int, List<GregorianCalendarEvent>>
typealias DeviceCalendarEventsStore = Map<Int, List<DeviceCalendarEvent>>

private fun AbstractDate.entry() = month * 100 + dayOfMonth

// Just adds all the events to a just generated a map, nothing special
fun List<DeviceCalendarEvent>.toDeviceEventsStore(): DeviceCalendarEventsStore =
    fold(HashMap<Int, ArrayList<DeviceCalendarEvent>>()) { s, ev ->
        (s[ev.date.entry()] ?: ArrayList<DeviceCalendarEvent>().also {
            s[ev.date.entry()] = it
        }).add(ev)
        s
    }

fun List<PersianCalendarEvent>.toPersianEventsStore(): PersianCalendarEventsStore =
    fold(HashMap<Int, ArrayList<PersianCalendarEvent>>()) { s, ev ->
        (s[ev.date.entry()] ?: ArrayList<PersianCalendarEvent>().also {
            s[ev.date.entry()] = it
        }).add(ev)
        s
    }

fun List<IslamicCalendarEvent>.toIslamicEventsStore(): IslamicCalendarEventsStore =
    fold(HashMap<Int, ArrayList<IslamicCalendarEvent>>()) { s, ev ->
        (s[ev.date.entry()] ?: ArrayList<IslamicCalendarEvent>().also {
            s[ev.date.entry()] = it
        }).add(ev)
        s
    }

fun List<GregorianCalendarEvent>.toGregorianEventsStore(): GregorianCalendarEventsStore =
    fold(HashMap<Int, ArrayList<GregorianCalendarEvent>>()) { s, ev ->
        (s[ev.date.entry()] ?: ArrayList<GregorianCalendarEvent>().also {
            s[ev.date.entry()] = it
        }).add(ev)
        s
    }

private fun AbstractDate.holidayAwareEqualCheck(date: AbstractDate): Boolean =
    (this.dayOfMonth == date.dayOfMonth && this.month == date.month &&
            (this.year == -1 || date.year == -1 || this.year == date.year))

fun PersianCalendarEventsStore.getEvents(date: PersianDate) =
    this[date.entry()]?.filter { it.date.holidayAwareEqualCheck(date) } ?: emptyList()

fun IslamicCalendarEventsStore.getEvents(date: IslamicDate) =
    this[date.entry()]?.filter { it.date.holidayAwareEqualCheck(date) } ?: emptyList()

fun GregorianCalendarEventsStore.getEvents(date: CivilDate) =
    this[date.entry()]?.filter { it.date.holidayAwareEqualCheck(date) } ?: emptyList()

// holidayAwareEqualCheck is not needed as they won't have -1 on year field
fun DeviceCalendarEventsStore.getDeviceEvents(civilDate: CivilDate) =
    this[civilDate.entry()]?.filter { it.date == civilDate } ?: emptyList()