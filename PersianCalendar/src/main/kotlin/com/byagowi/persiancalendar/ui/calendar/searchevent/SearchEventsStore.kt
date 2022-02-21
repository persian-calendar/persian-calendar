package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.entities.CalendarEvent

class SearchEventsStore(val events: List<CalendarEvent<*>>) {

    private val delimiters = arrayOf(" ", "(", ")", "-", /*ZWNJ*/"\u200c")
    private val itemsWords = events.map { it to it.formattedTitle.split(*delimiters) }
    fun query(constraint: CharSequence?): List<CalendarEvent<*>> {
        return if (constraint == null) events
        else itemsWords.mapNotNull { (event: CalendarEvent<*>, words: List<String>) ->
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
