package com.byagowi.persiancalendar.ui.calendar.searchevent

import com.byagowi.persiancalendar.ZWNJ
import com.byagowi.persiancalendar.entities.CalendarEvent

class SearchEventsStore(val events: List<CalendarEvent<*>>) {
    private val delimiters = arrayOf(" ", "(", ")", "-", ZWNJ)
    private val itemsWords = events.map { it to it.formattedTitle.split(*delimiters) }

    fun query(constraint: CharSequence?): List<CalendarEvent<*>> {
        return if (constraint == null) events
        else itemsWords.mapNotNull { (event: CalendarEvent<*>, words: List<String>) ->
            event.takeIf { words.any { word -> word.startsWith(constraint) } }
        }
    }
}
