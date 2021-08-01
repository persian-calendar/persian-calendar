package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getEnabledCalendarTypes
import com.byagowi.persiancalendar.utils.getOrderedCalendarEntities
import com.byagowi.persiancalendar.utils.updateStoredPreference

fun Fragment.showCalendarPreferenceDialog() {
    val context = context ?: return
    var dialog: AlertDialog? = null

    updateStoredPreference(context)
    val enabledCalendarTypes = getEnabledCalendarTypes()
    val adapter = RecyclerListAdapter(getOrderedCalendarEntities(context).map {
        RecyclerListAdapter.Item(it.title, it.type.name, it.type in enabledCalendarTypes)
    }, onAllItemsSwipped = {
        dialog?.dismiss()
        // Easter egg when all items are swiped
        val view = activity?.findViewById<View?>(android.R.id.content) ?: return@RecyclerListAdapter
        android.animation.ValueAnimator.ofFloat(0f, 360f).also {
            it.duration = 3000L
            it.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            it.addUpdateListener { value -> view.rotation = value.animatedValue as Float }
        }.start()
    })
    val recyclerView = RecyclerView(context).also {
        it.setHasFixedSize(true)
        it.layoutManager = LinearLayoutManager(activity)
        it.adapter = adapter
    }
    adapter.itemTouchHelper.attachToRecyclerView(recyclerView)

    dialog = AlertDialog.Builder(context)
        .setView(recyclerView)
        .setTitle(R.string.calendars_priority)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.accept) { _, _ ->
            adapter.result.takeIf { it.isNotEmpty() }?.let { ordering ->
                this.context?.appPrefs?.edit {
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
