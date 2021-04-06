package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var cachedView: View? = null
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

        return AlertDialog.Builder(requireContext()).apply {
            setView(onCreateView(LayoutInflater.from(context), null, savedInstanceState))
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        cachedView ?: RecyclerView(requireContext()).also { cachedView = it }

    override fun getView(): View? = cachedView

    override fun onViewCreated(neverUsedView: View, savedInstanceState: Bundle?) {
        calendarLayoutManager = LinearLayoutManager(context)
        calendarsAdapter = RecyclerListAdapter(this, itemsListLiveData.value!!)

        // in this trick we must not use first argument and use #getView
        val recyclerView = requireNotNull(view as RecyclerView) { "in #onViewCreated view must not be null`" }
        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = calendarLayoutManager
            adapter = calendarsAdapter
        }

        val callback = CalendarItemTouchCallback(this)
        itemTouchHelper = ItemTouchHelper(callback).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    override fun onDestroyView() {
        cachedView = null
        super.onDestroyView()
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
