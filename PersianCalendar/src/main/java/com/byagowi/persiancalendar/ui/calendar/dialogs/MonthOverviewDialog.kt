package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthOverviewDialogBinding
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MonthOverviewDialog : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainActivity = activity as MainActivity

        val baseJdn = arguments?.getLong(BUNDLE_KEY, -1L)
                ?.takeUnless { it == -1L } ?: getTodayJdn()
        val date = getDateFromJdnOfCalendar(mainCalendar, baseJdn)
        val deviceEvents = readMonthDeviceEvents(mainActivity, baseJdn)
        val monthLength = getMonthLength(mainCalendar, date.year, date.month).toLong()
        val events = (0 until monthLength).mapNotNull {
            val jdn = baseJdn + it
            val events = getEvents(jdn, deviceEvents)
            val holidays = getEventsTitle(
                    events,
                    holiday = true,
                    compact = false,
                    showDeviceCalendarEvents = false,
                    insertRLM = false,
                    addIsHoliday = isHighTextContrastEnabled
            )
            val nonHolidays = getEventsTitle(
                    events,
                    holiday = false,
                    compact = false,
                    showDeviceCalendarEvents = true,
                    insertRLM = false,
                    addIsHoliday = false
            )
            if (holidays.isEmpty() && nonHolidays.isEmpty()) null
            else MonthOverviewRecord(
                    dayTitleSummary(
                            getDateFromJdnOfCalendar(mainCalendar, jdn)
                    ), holidays, nonHolidays
            )
        }.takeUnless { it.isEmpty() } ?: listOf(
                MonthOverviewRecord(getString(R.string.warn_if_events_not_set), "", "")
        )

        val binding = MonthOverviewDialogBinding.inflate(
                mainActivity.layoutInflater, null, false
        ).apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ItemAdapter(events)
        }

        return BottomSheetDialog(mainActivity).apply {
            setContentView(binding.root)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
    }

    internal class MonthOverviewRecord(
            val title: String, val holidays: String, val nonHolidays: String
    )

    private inner class ItemAdapter(private val rows: List<MonthOverviewRecord>) :
            RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
                MonthOverviewItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount(): Int = rows.size

        inner class ViewHolder(private val binding: MonthOverviewItemBinding) :
                RecyclerView.ViewHolder(binding.root) {

            fun bind(position: Int) {
                val record = rows[position]
                binding.apply {
                    title.text = record.title
                    holidays.text = record.holidays
                    holidays.visibility = if (record.holidays.isEmpty()) View.GONE else View.VISIBLE
                    nonHolidays.text = record.nonHolidays
                    nonHolidays.visibility =
                            if (record.nonHolidays.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = MonthOverviewDialog().apply {
            arguments = Bundle().apply {
                putLong(BUNDLE_KEY, jdn)
            }
        }
    }
}
