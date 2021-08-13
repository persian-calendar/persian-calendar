package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthOverviewDialogBinding
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getMonthLength
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.persiancalendar.calendar.AbstractDate

fun Fragment.showMonthOverviewDialog(date: AbstractDate) {
    val baseJdn = Jdn(date)
    val deviceEvents = (context ?: return).readMonthDeviceEvents(baseJdn)
    val events = (0 until mainCalendar.getMonthLength(date.year, date.month)).mapNotNull {
        val jdn = baseJdn + it
        val events = deviceEvents.getEvents(jdn)
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
            dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)), holidays, nonHolidays
        )
    }.takeIf { it.isNotEmpty() } ?: listOf(
        MonthOverviewRecord(getString(R.string.warn_if_events_not_set), "", "")
    )

    BottomSheetDialog(layoutInflater.context, R.style.BottomSheetDialog).also { dialog ->
        dialog.setContentView(
            MonthOverviewDialogBinding.inflate(layoutInflater).also { binding ->
                binding.recyclerView.also {
                    it.layoutManager = LinearLayoutManager(context)
                    it.adapter = MonthOverviewItemAdapter(events)
                }
            }.root
        )
    }.show()
}

private class MonthOverviewRecord(
    val title: String, val holidays: String, val nonHolidays: String
) {
    override fun toString() = listOf(title, holidays, nonHolidays)
        .filter { it.isNotEmpty() }.joinToString("\n")
}

private class MonthOverviewItemAdapter(private val rows: List<MonthOverviewRecord>) :
    RecyclerView.Adapter<MonthOverviewItemAdapter.ViewHolder>() {

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

        fun bind(position: Int) = binding.let {
            val record = rows[position]
            it.title.text = record.title
            it.holidays.text = record.holidays
            it.holidays.isVisible = record.holidays.isNotEmpty()
            it.nonHolidays.text = record.nonHolidays
            it.nonHolidays.isVisible = record.nonHolidays.isNotEmpty()
        }

        override fun onClick(v: View?) =
            v?.context.copyToClipboard(rows[bindingAdapterPosition].toString())
    }
}
