package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.entities.CalendarEvent

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(context: Context, private val originalItems: List<CalendarEvent<*>>)
    : ArrayAdapter<CalendarEvent<*>>(
        context,
        R.layout.suggestion,
        android.R.id.text1,
        originalItems
) {

    private var showingItems: List<CalendarEvent<*>> = originalItems
    override fun getItem(position: Int): CalendarEvent<*> = showingItems[position]
    override fun getCount(): Int = showingItems.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent).apply {
            val item = (getItem(position).debugAssertNotNull ?: return@apply)
            (this as TextView).text = when (item) {
                is CalendarEvent.GregorianCalendarEvent,
                is CalendarEvent.IslamicCalendarEvent,
                is CalendarEvent.PersianCalendarEvent -> item.title
                is CalendarEvent.DeviceCalendarEvent -> "${item.title} (${item.description})"
            }
        }
    }

    private var filter: ArrayFilter? = null
    override fun getFilter(): Filter {
        return filter ?: ArrayFilter().also { filter = it }
    }

    // inspired from ArrayAdapter.ArrayFilter
    private inner class ArrayFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()
            if (constraint.isNullOrBlank()) {
                result.values = originalItems
                result.count = originalItems.size
            } else {
                fun String.startWithOrContains(query: CharSequence): Boolean =
                        startsWith(query) or split(" ").any { it.startsWith(query) }

                val filteredItems = originalItems.filter {
                    when (it) {
                        is CalendarEvent.GregorianCalendarEvent,
                        is CalendarEvent.IslamicCalendarEvent,
                        is CalendarEvent.PersianCalendarEvent -> it.title.startWithOrContains(constraint)
                        is CalendarEvent.DeviceCalendarEvent -> it.title.startWithOrContains(constraint) or it.description.startWithOrContains(constraint)
                    }
                }
                result.values = filteredItems
                result.count = filteredItems.size
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