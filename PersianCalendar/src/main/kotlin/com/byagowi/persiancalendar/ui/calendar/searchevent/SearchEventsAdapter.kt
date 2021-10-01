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
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import com.byagowi.persiancalendar.utils.gregorianCalendarEvents
import com.byagowi.persiancalendar.utils.irregularCalendarEventsStore
import com.byagowi.persiancalendar.utils.islamicCalendarEvents
import com.byagowi.persiancalendar.utils.persianCalendarEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter private constructor(
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

    companion object {
        private val CalendarEvent<*>.formattedTitle
            get() = when (this) {
                is CalendarEvent.GregorianCalendarEvent,
                is CalendarEvent.IslamicCalendarEvent,
                is CalendarEvent.PersianCalendarEvent -> title
                is CalendarEvent.DeviceCalendarEvent ->
                    if (description.isBlank()) title else "$title ($description)"
            }

        fun attachEventsAdapter(
            searchAutoComplete: SearchView.SearchAutoComplete, context: Context,
            lifecycleOwner: LifecycleOwner,
        ) {
            lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) {
                    val jdn = Jdn.today
                    val events = listOf(
                        context.getAllEnabledAppointments(), persianCalendarEvents.getAllEvents(),
                        islamicCalendarEvents.getAllEvents(), gregorianCalendarEvents.getAllEvents()
                    ).flatten() + listOf(
                        jdn.toPersianCalendar(), jdn.toGregorianCalendar(), jdn.toIslamicCalendar()
                    ).flatMap {
                        irregularCalendarEventsStore.getEventsList(it.year, it.calendarType)
                    }
                    val delimiters = arrayOf(" ", "(", ")", "-", /*ZWNJ*/"\u200c")
                    val itemsWords = events.map { it to it.formattedTitle.split(*delimiters) }
                    withContext(Dispatchers.Main.immediate) {
                        searchAutoComplete.setAdapter(
                            SearchEventsAdapter(context, events, itemsWords)
                        )
                    }
                }
            }
        }
    }
}
