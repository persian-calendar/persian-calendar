package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.dependencies.AppDependency
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.Utils

import java.util.ArrayList

import javax.inject.Inject
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerAppCompatDialogFragment

class CalendarPreferenceDialog : DaggerAppCompatDialogFragment() {
    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    private var mItemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(mainActivityDependency.mainActivity)

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

        val recyclerView = RecyclerView(mainActivityDependency.mainActivity)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(mainActivityDependency.mainActivity)
        val adapter = RecyclerListAdapter(this,
                mainActivityDependency, titles, values, enabled)
        recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(recyclerView)
        builder.setView(recyclerView)
        builder.setTitle(R.string.calendars_priority)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.accept) { dialogInterface, i ->
            val edit = appDependency.sharedPreferences.edit()
            val ordering = adapter.result
            if (ordering.size != 0) {
                edit.putString(Constants.PREF_MAIN_CALENDAR_KEY, ordering[0])
                edit.putString(Constants.PREF_OTHER_CALENDARS_KEY, TextUtils.join(",",
                        ordering.subList(1, ordering.size)))
            }
            edit.apply()
        }

        return builder.create()
    }

    internal fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }
}
