package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getEnabledCalendarTypes

fun showCalendarPreferenceDialog(activity: Activity, onEmpty: () -> Unit) {
    val enabledCalendarTypes = getEnabledCalendarTypes()
    val orderedCalendarTypes =
        getEnabledCalendarTypes().let { it + (CalendarType.values().toList() - it.toSet()) }
    val adapter = RecyclerListAdapter(orderedCalendarTypes.map { calendarType ->
        RecyclerListAdapter.Item(
            activity.getString(calendarType.title), calendarType.name,
            calendarType in enabledCalendarTypes
        )
    })

    AlertDialog.Builder(activity)
        .setView(RecyclerView(activity).also {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = adapter
        })
        .setTitle(R.string.calendars_priority)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.accept) accept@{ _, _ ->
            val ordering = adapter.result.takeIf { it.isNotEmpty() } ?: return@accept onEmpty()
            activity.appPrefs.edit {
                putString(PREF_MAIN_CALENDAR_KEY, ordering.first())
                putString(PREF_OTHER_CALENDARS_KEY, ordering.drop(1).joinToString(","))
            }
        }
        .show()
}
