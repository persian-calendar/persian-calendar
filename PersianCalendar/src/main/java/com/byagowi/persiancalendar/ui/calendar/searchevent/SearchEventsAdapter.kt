package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(
    context: Context, private val originalItems: List<CalendarEvent<*>>
) : ArrayAdapter<CalendarEvent<*>>(context, R.layout.suggestion, R.id.text, originalItems) {

    private var showingItems: List<CalendarEvent<*>> = originalItems
    private val itemsWords: List<Pair<CalendarEvent<*>, List<String>>> by lazy {
        val delimiters = arrayOf(" ", "(", ")", "-", /*ZWNJ*/"\u200c")
        originalItems.map { it to it.formattedTitle.split(*delimiters) }
    }

    override fun getItem(position: Int): CalendarEvent<*> = showingItems[position]
    override fun getCount(): Int = showingItems.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return SuggestionBinding.bind(super.getView(position, convertView, parent)).also {
            it.text.text = getItem(position).formattedTitle
        }.root
    }

    private val CalendarEvent<*>.formattedTitle
        get() = when (this) {
            is CalendarEvent.GregorianCalendarEvent,
            is CalendarEvent.IslamicCalendarEvent,
            is CalendarEvent.PersianCalendarEvent -> title
            is CalendarEvent.DeviceCalendarEvent ->
                if (description.isBlank()) title else "$title ($description)"
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
            if (constraint.isNullOrBlank()) originalItems
            else itemsWords.filter { (_: CalendarEvent<*>, words: List<String>) ->
                words.any { word -> word.startsWith(constraint) }
            }.map { it.first }
        )

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            // Suppress unchecked cast just as ArrayAdapter.ArrayFilter.publishResults
            @Suppress("UNCHECKED_CAST")
            showingItems = results.values as List<CalendarEvent<*>>
            if (results.count > 0)
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
        }
    }
}