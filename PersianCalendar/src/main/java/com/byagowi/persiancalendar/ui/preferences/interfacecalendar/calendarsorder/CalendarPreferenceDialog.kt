package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.AppDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.utils.getEnabledCalendarTypes
import com.byagowi.persiancalendar.utils.getOrderedCalendarEntities
import com.byagowi.persiancalendar.utils.updateStoredPreference
import dagger.android.support.DaggerAppCompatDialogFragment
import javax.inject.Inject

class CalendarPreferenceDialog : DaggerAppCompatDialogFragment() {

    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    private var mItemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val values = ArrayList<String>()
        val titles = ArrayList<String>()
        val enabled = ArrayList<Boolean>()

        updateStoredPreference(mainActivityDependency.mainActivity)
        val enabledCalendarTypes = getEnabledCalendarTypes()
        val orderedCalendarTypes = getOrderedCalendarEntities(mainActivityDependency.mainActivity)
        for (entity in orderedCalendarTypes) {
            values.add(entity.type.toString())
            titles.add(entity.toString())
            enabled.add(enabledCalendarTypes.contains(entity.type))
        }
        val adapter = RecyclerListAdapter(this, mainActivityDependency, titles, values, enabled)
        val recyclerView = RecyclerView(mainActivityDependency.mainActivity).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(mainActivityDependency.mainActivity)
            this.adapter = adapter
        }

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback).apply {
            attachToRecyclerView(recyclerView)
        }

        return AlertDialog.Builder(mainActivityDependency.mainActivity).apply {
            setView(recyclerView)
            setTitle(R.string.calendars_priority)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.accept) { _, _ ->
                val ordering = adapter.result
                appDependency.sharedPreferences.edit {
                    if (ordering.isNotEmpty()) {
                        putString(PREF_MAIN_CALENDAR_KEY, ordering[0])
                        putString(
                            PREF_OTHER_CALENDARS_KEY,
                            ordering.subList(1, ordering.size).joinToString(",")
                        )
                    }
                }
            }
        }.create()
    }

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper?.startDrag(viewHolder)
    }
}
