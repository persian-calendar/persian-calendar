package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
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

class CalendarPreferenceDialog : AppCompatDialogFragment(),
    CalendarItemTouchCallback.ItemTouchCallback {

    private var itemTouchHelper: ItemTouchHelper? = null
    private lateinit var calendarsAdapter: RecyclerListAdapter
    private lateinit var calendarLayoutManager: LinearLayoutManager
    private val itemsListLiveData: MutableLiveData<List<RecyclerListAdapter.Item>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsListLiveData.value = getOrderedCalendarEntities(requireContext()).map {
            RecyclerListAdapter.Item(
                it.toString(),
                it.type.toString(),
                it.type in getEnabledCalendarTypes()
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity as Activity
        updateStoredPreference(activity)
        calendarLayoutManager = LinearLayoutManager(context)
        calendarsAdapter = RecyclerListAdapter(this, itemsListLiveData.value!!)

        val recyclerView = RecyclerView(activity).apply {
            setHasFixedSize(true)
            layoutManager = calendarLayoutManager
            adapter = calendarsAdapter
        }

        val callback = CalendarItemTouchCallback(this)
        itemTouchHelper = ItemTouchHelper(callback).apply {
            attachToRecyclerView(recyclerView)
        }

        return AlertDialog.Builder(activity).apply {
            setView(recyclerView)
            setTitle(R.string.calendars_priority)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.accept) { _, _ ->
                val ordering = itemsListLiveData.value!!
                activity.appPrefs.edit {
                    if (ordering.isNotEmpty()) {
                        putString(PREF_MAIN_CALENDAR_KEY, ordering[0].toString())
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
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun onOrderItem(fromPos: Int, toPos: Int) {
        calendarsAdapter.onItemMoved(fromPos, toPos)
    }

    override fun onSwipedForRemove(itemPos: Int) {
        calendarsAdapter.onItemDismissed(itemPos)
    }
}
