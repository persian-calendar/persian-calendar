package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getEnabledCalendarTypes
import com.byagowi.persiancalendar.utils.getOrderedCalendarEntities
import com.byagowi.persiancalendar.utils.updateStoredPreference

class CalendarPreferenceDialog : AppCompatDialogFragment() {

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        updateStoredPreference(activity)
        val enabledCalendarTypes = getEnabledCalendarTypes()
        val adapter = RecyclerListAdapter(this, getOrderedCalendarEntities(activity).map {
            RecyclerListAdapter.Item(it.title, it.type.name, it.type in enabledCalendarTypes)
        })
        val recyclerView = RecyclerView(activity).also {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = adapter
        }

        val callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback).also {
            it.attachToRecyclerView(recyclerView)
        }

        return AlertDialog.Builder(activity)
            .setView(recyclerView)
            .setTitle(R.string.calendars_priority)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.accept) { _, _ ->
                adapter.result.takeIf { it.isNotEmpty() }?.let { ordering ->
                    activity.appPrefs.edit {
                        putString(PREF_MAIN_CALENDAR_KEY, ordering.first())
                        putString(
                            PREF_OTHER_CALENDARS_KEY, ordering.drop(1).joinToString(",")
                        )
                    }
                }
            }
            .create()
    }

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }
}
