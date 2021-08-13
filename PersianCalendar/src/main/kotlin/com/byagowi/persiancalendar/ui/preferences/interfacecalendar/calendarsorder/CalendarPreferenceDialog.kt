package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getEnabledCalendarTypes
import com.byagowi.persiancalendar.utils.getOrderedCalendarEntities
import com.byagowi.persiancalendar.utils.updateStoredPreference

fun showCalendarPreferenceDialog(context: Context, onAllItemsSwipped: () -> Unit) {
    var dialog: AlertDialog? = null

    updateStoredPreference(context)
    val enabledCalendarTypes = getEnabledCalendarTypes()
    val adapter = RecyclerListAdapter(getOrderedCalendarEntities(context).map { (type, title) ->
        RecyclerListAdapter.Item(title, type.name, type in enabledCalendarTypes)
    }, onAllItemsSwipped = {
        dialog?.dismiss()
        onAllItemsSwipped()
    })
    val recyclerView = RecyclerView(context).also {
        it.setHasFixedSize(true)
        it.layoutManager = LinearLayoutManager(context)
        it.adapter = adapter
    }
    adapter.itemTouchHelper.attachToRecyclerView(recyclerView)

    dialog = AlertDialog.Builder(context)
        .setView(recyclerView)
        .setTitle(R.string.calendars_priority)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.accept) { _, _ ->
            adapter.result.takeIf { it.isNotEmpty() }?.let { ordering ->
                context.appPrefs.edit {
                    putString(PREF_MAIN_CALENDAR_KEY, ordering.first())
                    putString(
                        PREF_OTHER_CALENDARS_KEY, ordering.drop(1).joinToString(",")
                    )
                }
            }
        }
        .create()
    dialog.show()
}
