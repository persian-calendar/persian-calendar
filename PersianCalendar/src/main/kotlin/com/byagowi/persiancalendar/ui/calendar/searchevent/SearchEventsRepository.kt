package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import com.byagowi.persiancalendar.utils.gregorianCalendarEvents
import com.byagowi.persiancalendar.utils.irregularCalendarEventsStore
import com.byagowi.persiancalendar.utils.islamicCalendarEvents
import com.byagowi.persiancalendar.utils.nepaliCalendarEvents
import com.byagowi.persiancalendar.utils.persianCalendarEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchEventsRepository(private val context: Context) : ISearchEventsRepository {

    private var store: SearchEventsStore? = null

    private suspend fun createStore(context: Context) = withContext(Dispatchers.IO) {
        val jdn = Jdn.today()
        // Hopefully we can get rid of these global variables someday
        val events: List<CalendarEvent<*>> = listOf(
            context.getAllEnabledAppointments(), persianCalendarEvents.getAllEvents(),
            islamicCalendarEvents.getAllEvents(), nepaliCalendarEvents.getAllEvents(),
            gregorianCalendarEvents.getAllEvents(),
        ).flatten() + listOf(
            jdn.toPersianCalendar(), jdn.toGregorianCalendar(), jdn.toIslamicCalendar()
        ).flatMap {
            irregularCalendarEventsStore.getEventsList(it.year, it.calendarType)
        }
        SearchEventsStore(events)
    }

    // encapsulate store in repository
    override suspend fun findEvent(query: CharSequence): List<CalendarEvent<*>> =
        (store ?: createStore(context).also { store = it }).query(query)
}
