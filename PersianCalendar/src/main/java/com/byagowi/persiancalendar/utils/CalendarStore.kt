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

fun HashMap<Int, ArrayList<DeviceCalendarEvent>>.addToStore(ev: DeviceCalendarEvent) {
    val entry = ev.date.month * 100 + ev.date.dayOfMonth
    (this[entry] ?: ArrayList<DeviceCalendarEvent>().also { this[entry] = it }).add(ev)
}

fun HashMap<Int, ArrayList<PersianCalendarEvent>>.addToStore(ev: PersianCalendarEvent) {
    val entry = ev.date.month * 100 + ev.date.dayOfMonth
    (this[entry] ?: ArrayList<PersianCalendarEvent>().also { this[entry] = it }).add(ev)
}

fun HashMap<Int, ArrayList<IslamicCalendarEvent>>.addToStore(ev: IslamicCalendarEvent) {
    val entry = ev.date.month * 100 + ev.date.dayOfMonth
    (this[entry] ?: ArrayList<IslamicCalendarEvent>().also { this[entry] = it }).add(ev)
}

fun HashMap<Int, ArrayList<GregorianCalendarEvent>>.addToStore(ev: GregorianCalendarEvent) {
    val entry = ev.date.month * 100 + ev.date.dayOfMonth
    (this[entry] ?: ArrayList<GregorianCalendarEvent>().also { this[entry] = it }).add(ev)
}

private fun AbstractDate.holidayAwareEqualCheck(date: AbstractDate): Boolean =
    (this.dayOfMonth == date.dayOfMonth && this.month == date.month &&
            (this.year == -1 || date.year == -1 || this.year == date.year))

fun PersianCalendarEventsStore.getEvents(persianDate: PersianDate) =
    this[persianDate.month * 100 + persianDate.dayOfMonth]?.filter {
        it.date.holidayAwareEqualCheck(persianDate)
    } ?: emptyList()

fun IslamicCalendarEventsStore.getEvents(islamicDate: IslamicDate) =
    this[islamicDate.month * 100 + islamicDate.dayOfMonth]?.filter {
        it.date.holidayAwareEqualCheck(islamicDate)
    } ?: emptyList()

fun GregorianCalendarEventsStore.getEvents(civilDate: CivilDate) =
    this[civilDate.month * 100 + civilDate.dayOfMonth]?.filter {
        it.date.holidayAwareEqualCheck(civilDate)
    } ?: emptyList()

fun DeviceCalendarEventsStore.getDeviceEvents(civilDate: CivilDate) =
    this[civilDate.month * 100 + civilDate.dayOfMonth]?.filter {
        // holidayAwareEqualCheck is not needed as they won't have -1 on year field
        it.date == civilDate
    } ?: emptyList()