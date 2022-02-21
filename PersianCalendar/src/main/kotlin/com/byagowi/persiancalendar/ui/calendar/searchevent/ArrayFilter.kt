package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.widget.Filter
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.variants.debugAssertNotNull

// Inspired from ArrayAdapter.ArrayFilter
class ArrayFilter(
    private val store: SearchEventsStore,
    private val publish: (List<CalendarEvent<*>>) -> Unit
) : Filter() {

    private fun <T> createFilterResults(list: List<T>): FilterResults = FilterResults().apply {
        values = list
        count = list.size
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults =
        createFilterResults(store.query(constraint))

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // Suppress unchecked cast just as ArrayAdapter.ArrayFilter.publishResults
        @Suppress("UNCHECKED_CAST")
        publish((results.values as? List<CalendarEvent<*>>).debugAssertNotNull ?: emptyList())
    }
}
