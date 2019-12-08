package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.CalendarItemBinding
import com.byagowi.persiancalendar.utils.*

class CalendarItemAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<CalendarItemAdapter.ViewHolder>() {

    private val calendarFont: Typeface = getCalendarFragmentFont(context)
    private var calendars: List<CalendarType> = emptyList()
    internal var isExpanded = false
        set(expanded) {
            field = expanded
            calendars.indices.forEach(::notifyItemChanged)
        }
    private var jdn: Long = 0

    internal fun setDate(calendars: List<CalendarType>, jdn: Long) {
        this.calendars = calendars
        this.jdn = jdn
        calendars.indices.forEach(::notifyItemChanged)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        CalendarItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = calendars.size

    inner class ViewHolder(private val binding: CalendarItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            val applyLineMultiplier = !isCustomFontEnabled

            binding.monthYear.typeface = calendarFont
            binding.day.typeface = calendarFont
            if (applyLineMultiplier) binding.monthYear.setLineSpacing(0f, .6f)

            binding.container.setOnClickListener(this)
            binding.linear.setOnClickListener(this)
        }

        fun bind(position: Int) {
            val date = getDateFromJdnOfCalendar(calendars[position], jdn)

            binding.linear.text = toLinearDate(date)
            binding.linear.contentDescription = toLinearDate(date)
            val firstCalendarString = formatDate(date)
            binding.container.contentDescription = firstCalendarString
            binding.day.contentDescription = ""
            binding.day.text = formatNumber(date.dayOfMonth)
            binding.monthYear.contentDescription = ""
            binding.monthYear.text =
                listOf(getMonthName(date), formatNumber(date.year)).joinToString("\n")
        }

        override fun onClick(view: View?) =
            copyToClipboard(view, "converted date", view?.contentDescription)
    }
}
