package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SuggestionBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsStore.Companion.formattedTitle
import com.byagowi.persiancalendar.variants.debugLog

/**
 * Created by Farhad Beigirad on 4/23/21.
 */
class SearchEventsAdapter(
    context: Context,
    onQueryChanged: (CharSequence) -> Unit
) : ArrayAdapter<CalendarEvent<*>>(
    context, R.layout.suggestion, R.id.text
) {
    init {
        setNotifyOnChange(false) // reduce auto notifying after clear() & addAdd()
    }

    // we need to this filter object only for listening query changes
    private val filterInstance by lazy {
        object : Filter() {
            override fun performFiltering(constraint: CharSequence?) =
                FilterResults().also {
                    onQueryChanged(constraint ?: "")
                    debugLog("looking for '$constraint'")
                }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
        }
    }

    fun setData(items: List<CalendarEvent<*>>) {
        clear()
        addAll(items)
        notifyDataSetChanged()
    }

    override fun getFilter() = filterInstance

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return SuggestionBinding.bind(super.getView(position, convertView, parent)).also {
            it.text.text = getItem(position)?.formattedTitle
        }.root
    }
}
