package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
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
import com.byagowi.persiancalendar.utils.*
import java.util.*

class CalendarPreferenceDialog : AppCompatDialogFragment(),
    CalendarItemTouchCallback.ItemTouchCallback,
    CalendarItemAdapter.CalendarsOrderItemCallback {

    private var cachedView: View? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private lateinit var calendarsAdapter: CalendarItemAdapter
    private lateinit var calendarLayoutManager: LinearLayoutManager
    private val itemsListLiveData: MutableLiveData<List<CalendarItemAdapter.Item>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enabledCalendarTypes = getEnabledCalendarTypes()
        itemsListLiveData.value = getOrderedCalendarEntities(requireContext()).map {
            CalendarItemAdapter.Item(
                it.toString(),
                it.type.toString(),
                it.type in enabledCalendarTypes
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
        calendarsAdapter = CalendarItemAdapter(this)

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

        itemsListLiveData.observe(this) {
            calendarsAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        cachedView = null
        super.onDestroyView()
    }

    override fun onOrderItem(fromPos: Int, toPos: Int) {
        val newList = itemsListLiveData.value!!.toMutableList()
        Collections.swap(newList, fromPos, toPos)
        itemsListLiveData.value = newList
    }

    override fun onSwipedForRemove(itemPos: Int) {
        itemsListLiveData.value = itemsListLiveData.value
            ?.filterIndexed { index, _ -> index != itemPos }

        // Easter egg when all are swiped
        // Do it here because its an event and should not be store in livedata
        if (itemsListLiveData.value.isNullOrEmpty())
            rotateScreen()
    }

    override fun onItemToggle(itemPosition: Int) {
        itemsListLiveData.value = itemsListLiveData.value?.mapIndexed { index, item ->
            if (itemPosition == index)
                item.copy(enabled = item.enabled.not())
            else
                item
        }
    }

    private fun rotateScreen() {
        runCatching {
            val activityContentView = activity?.findViewById<View>(android.R.id.content)
            ValueAnimator.ofFloat(0f, 360f).apply {
                duration = 3000L
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { value ->
                    activityContentView?.rotation = value.animatedValue as Float
                }
            }.start()
        }.onFailure(logException)
        dismiss()
    }
}
