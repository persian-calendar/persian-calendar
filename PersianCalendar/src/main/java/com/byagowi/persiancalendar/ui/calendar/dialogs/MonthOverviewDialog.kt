package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthOverviewDialogBinding
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding
import com.byagowi.persiancalendar.utils.Jdn
import com.byagowi.persiancalendar.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getMonthLength
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MonthOverviewDialog : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()

        val baseJdn = arguments?.getLong(BUNDLE_KEY, -1L)?.takeIf { it != -1L }?.let(::Jdn) ?: Jdn.today
        val date = baseJdn.toCalendar(mainCalendar)
        val deviceEvents = readMonthDeviceEvents(activity, baseJdn)
        val events = (0 until getMonthLength(mainCalendar, date.year, date.month)).mapNotNull {
            val jdn = baseJdn + it
            val events = getEvents(jdn, deviceEvents)
            val holidays = getEventsTitle(
                events, holiday = true, compact = false, showDeviceCalendarEvents = false,
                insertRLM = false, addIsHoliday = isHighTextContrastEnabled
            )
            val nonHolidays = getEventsTitle(
                events, holiday = false, compact = false, showDeviceCalendarEvents = true,
                insertRLM = false, addIsHoliday = false
            )
            if (holidays.isEmpty() && nonHolidays.isEmpty()) null
            else MonthOverviewRecord(
                dayTitleSummary(jdn.toCalendar(mainCalendar)), holidays, nonHolidays
            )
        }.takeIf { it.isNotEmpty() } ?: listOf(
            MonthOverviewRecord(getString(R.string.warn_if_events_not_set), "", "")
        )

        return BottomSheetDialog(activity).apply {
            setContentView(
                MonthOverviewDialogBinding.inflate(
                    activity.layoutInflater, null, false
                ).apply {
                    recyclerView.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = ItemAdapter(events)
                        setPadding(0, 4.dp, 0, 0)
                    }
                }.root
            )
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
    }

    internal class MonthOverviewRecord(
        val title: String, val holidays: String, val nonHolidays: String
    ) {
        override fun toString() = listOf(title, holidays, nonHolidays)
            .filter { it.isNotEmpty() }.joinToString("\n")
    }

    private inner class ItemAdapter(private val rows: List<MonthOverviewRecord>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            MonthOverviewItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount(): Int = rows.size

        inner class ViewHolder(private val binding: MonthOverviewItemBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {

            init {
                binding.root.setOnClickListener(this)
            }

            fun bind(position: Int) = binding.run {
                val record = rows[position]
                title.text = record.title
                holidays.text = record.holidays
                holidays.visibility = if (record.holidays.isEmpty()) View.GONE else View.VISIBLE
                nonHolidays.text = record.nonHolidays
                nonHolidays.visibility =
                    if (record.nonHolidays.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun onClick(v: View?) = copyToClipboard(
                binding.root, "Events", rows[bindingAdapterPosition].toString(),
                showToastInstead = true
            )
        }
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = MonthOverviewDialog().apply {
            arguments = bundleOf(BUNDLE_KEY to jdn)
        }
    }
}
