package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.dependencies.AppDependency
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.utils.Utils
import dagger.android.support.DaggerAppCompatDialogFragment
import java.util.*
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

        Utils.updateStoredPreference(mainActivityDependency.mainActivity)
        val enabledCalendarTypes = Utils.getEnabledCalendarTypes()
        val orderedCalendarTypes = Utils.getOrderedCalendarEntities(mainActivityDependency.mainActivity)
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
                        putString(Constants.PREF_MAIN_CALENDAR_KEY, ordering[0])
                        putString(Constants.PREF_OTHER_CALENDARS_KEY, TextUtils.join(",",
                                ordering.subList(1, ordering.size)))
                    }
                }
            }
        }.create()
    }

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper?.startDrag(viewHolder)
    }
}
