package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchEventsRepository(private val context: Context) : ISearchEventsRepository {

    private var store: SearchEventsStore? = null

    private suspend fun createStore(context: Context) = withContext(Dispatchers.IO) {
        SearchEventsStore(
            context.getAllEnabledAppointments() +
                    // Hopefully we can get rid of this global variable someday
                    (eventsRepository?.getEnabledEvents(Jdn.today()) ?: emptyList())
        )
    }

    // encapsulate store in repository
    override suspend fun findEvent(query: CharSequence): List<CalendarEvent<*>> =
        (store ?: createStore(context).also { store = it }).query(query)
}
