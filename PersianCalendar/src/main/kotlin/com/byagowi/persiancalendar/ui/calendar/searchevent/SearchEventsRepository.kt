package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import androidx.annotation.WorkerThread
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.*

@WorkerThread // can be a bit costly so preferably better to be done off main thread
class SearchEventsRepository(context: Context) {

    private val jdn = Jdn.today()
    val events: List<CalendarEvent<*>> = listOf(
        context.getAllEnabledAppointments(), persianCalendarEvents.getAllEvents(),
        islamicCalendarEvents.getAllEvents(), nepaliCalendarEvents.getAllEvents(),
        gregorianCalendarEvents.getAllEvents(),
    ).flatten() + listOf(
        jdn.toPersianCalendar(), jdn.toGregorianCalendar(), jdn.toIslamicCalendar()
    ).flatMap {
        irregularCalendarEventsStore.getEventsList(it.year, it.calendarType)
    }
    private val delimiters = arrayOf(" ", "(", ")", "-", /*ZWNJ*/"\u200c")
    private val itemsWords = events.map { it to it.formattedTitle.split(*delimiters) }

    fun query(constraint: CharSequence): List<CalendarEvent<*>> {
        return itemsWords.mapNotNull { (event: CalendarEvent<*>, words: List<String>) ->
            event.takeIf { words.any { word -> word.startsWith(constraint) } }
        }
    }

    companion object {
        val CalendarEvent<*>.formattedTitle
            get() = when (this) {
                is CalendarEvent.GregorianCalendarEvent,
                is CalendarEvent.IslamicCalendarEvent,
                is CalendarEvent.PersianCalendarEvent,
                is CalendarEvent.NepaliCalendarEvent -> title
                is CalendarEvent.DeviceCalendarEvent ->
                    if (description.isBlank()) title else "$title ($description)"
            }
    }
}
