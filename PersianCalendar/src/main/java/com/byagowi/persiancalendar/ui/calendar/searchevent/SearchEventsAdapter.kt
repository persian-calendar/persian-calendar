package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(
    context: Context, private val originalItems: List<CalendarEvent<*>>
) : ArrayAdapter<CalendarEvent<*>>(context, R.layout.suggestion, R.id.text, originalItems) {

    private var showingItems: List<CalendarEvent<out AbstractDate>> = originalItems
    private val itemsWords by lazy {
        val delimiters = arrayOf(" ", "(", ")", "-", /*ZWNJ*/"\u200c")
        originalItems.map {
            it to when (it) {
                is CalendarEvent.GregorianCalendarEvent,
                is CalendarEvent.IslamicCalendarEvent,
                is CalendarEvent.PersianCalendarEvent -> it.title.split(*delimiters)
                is CalendarEvent.DeviceCalendarEvent ->
                    "${it.title} ${it.description}".split(*delimiters)
            }
        }
    }

    override fun getItem(position: Int): CalendarEvent<out AbstractDate> = showingItems[position]
    override fun getCount(): Int = showingItems.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return SuggestionBinding.bind(super.getView(position, convertView, parent)).also {
            it.text.text = when (
                val item = getItem(position).debugAssertNotNull
            ) {
                is CalendarEvent.GregorianCalendarEvent,
                is CalendarEvent.IslamicCalendarEvent,
                is CalendarEvent.PersianCalendarEvent -> item.title
                is CalendarEvent.DeviceCalendarEvent -> {
                    if (item.description.isBlank()) item.title
                    else "${item.title} (${item.description})"
                }
            }
        }.root
    }

    private val filterInstance = ArrayFilter()
    override fun getFilter() = filterInstance

    // inspired from ArrayAdapter.ArrayFilter
    inner class ArrayFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()
            if (constraint.isNullOrBlank()) {
                result.values = originalItems
                result.count = originalItems.size
            } else {
                itemsWords.filter {
                    it.second.any { word -> word.startsWith(constraint) }
                }.map { it.first }.let {
                    result.values = it
                    result.count = it.size
                }
            }

            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            showingItems = results.values as List<CalendarEvent<*>>
            if (results.count > 0)
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
        }
    }
}