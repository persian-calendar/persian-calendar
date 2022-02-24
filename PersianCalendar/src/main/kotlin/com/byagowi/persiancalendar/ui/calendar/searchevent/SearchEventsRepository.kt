package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.EventsRepository
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchEventsRepository(private val context: Context) : ISearchEventsRepository {

    private var eventsRepository: EventsRepository? = null
    private var storeTimestamp = 0L
    private var store: SearchEventsStore? = null

    private suspend fun createStore(context: Context, eventsRepository: EventsRepository?) =
        withContext(Dispatchers.IO) {
            SearchEventsStore(
                context.getAllEnabledAppointments() +
                        // Hopefully we can get rid of this global variable someday
                        (eventsRepository?.getEnabledEvents(Jdn.today()) ?: listOf())
            )
        }

    // encapsulate store in repository
    override suspend fun findEvent(
        query: CharSequence,
        eventsRepository: EventsRepository?
    ): List<CalendarEvent<*>> {
        val now = System.currentTimeMillis()
        return (store.takeIf {
            this.eventsRepository === eventsRepository &&
                    storeTimestamp + ONE_MINUTE_IN_MILLIS > now
        } ?: createStore(context, eventsRepository).also { store = it; storeTimestamp = now })
            .query(query)
    }
}
