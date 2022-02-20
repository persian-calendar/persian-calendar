package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository.Companion.formattedTitle
import com.byagowi.persiancalendar.variants.debugAssertNotNull

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(
    context: Context, private val eventsRepository: ISearchEventsRepository
) : ArrayAdapter<CalendarEvent<*>>(
    context, R.layout.suggestion, R.id.text, eventsRepository.events
) {

    private var showingItems: List<CalendarEvent<*>> = eventsRepository.events

    override fun getItem(position: Int): CalendarEvent<*> = showingItems[position]
    override fun getCount(): Int = showingItems.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return SuggestionBinding.bind(super.getView(position, convertView, parent)).also {
            it.text.text = getItem(position).formattedTitle
        }.root
    }

    private val filterInstance = ArrayFilter()
    override fun getFilter() = filterInstance

    // Inspired from ArrayAdapter.ArrayFilter
    inner class ArrayFilter : Filter() {

        private fun <T> createFilterResults(list: List<T>): FilterResults = FilterResults().apply {
            values = list
            count = list.size
        }

        override fun performFiltering(constraint: CharSequence?) = createFilterResults(
            if (constraint.isNullOrBlank()) eventsRepository.events
            else eventsRepository.query(constraint)
        )

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            // Suppress unchecked cast just as ArrayAdapter.ArrayFilter.publishResults
            @Suppress("UNCHECKED_CAST")
            showingItems =
                (results.values as? List<CalendarEvent<*>>).debugAssertNotNull ?: emptyList()
            if (results.count > 0)
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
        }
    }
}
