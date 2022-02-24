package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.utils.EnabledHolidays

interface ISearchEventsRepository {
    suspend fun findEvent(
        query: CharSequence,
        enabledHolidays: EnabledHolidays
    ): List<CalendarEvent<*>>
}
