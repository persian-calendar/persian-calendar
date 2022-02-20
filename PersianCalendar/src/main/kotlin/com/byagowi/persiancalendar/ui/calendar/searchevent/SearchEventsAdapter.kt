package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel.Companion.formattedTitle
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import com.byagowi.persiancalendar.utils.gregorianCalendarEvents
import com.byagowi.persiancalendar.utils.irregularCalendarEventsStore
import com.byagowi.persiancalendar.utils.islamicCalendarEvents
import com.byagowi.persiancalendar.utils.nepaliCalendarEvents
import com.byagowi.persiancalendar.utils.persianCalendarEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(
    context: Context,
    private val originalItems: List<CalendarEvent<*>>,
    private val itemsWords: List<Pair<CalendarEvent<*>, List<String>>>
) : ArrayAdapter<CalendarEvent<*>>(context, R.layout.suggestion, R.id.text, originalItems) {

    private var showingItems: List<CalendarEvent<*>> = originalItems

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
            if (constraint.isNullOrBlank()) originalItems
            else itemsWords.mapNotNull { (event: CalendarEvent<*>, words: List<String>) ->
                event.takeIf { words.any { word -> word.startsWith(constraint) } }
            }
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
