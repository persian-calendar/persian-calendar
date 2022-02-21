package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsStore.Companion.formattedTitle

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(
    context: Context, store: SearchEventsStore
) : ArrayAdapter<CalendarEvent<*>>(
    context, R.layout.suggestion, R.id.text, store.events
) {

    private var showingItems: List<CalendarEvent<*>> = store.events
    private val filterInstance = ArrayFilter(store) {
        showingItems = it
        if (count > 0) notifyDataSetChanged() else notifyDataSetInvalidated()
    }

    override fun getFilter() = filterInstance
    override fun getItem(position: Int): CalendarEvent<*> = showingItems[position]
    override fun getCount(): Int = showingItems.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return SuggestionBinding.bind(super.getView(position, convertView, parent)).also {
            it.text.text = getItem(position).formattedTitle
        }.root
    }
}
