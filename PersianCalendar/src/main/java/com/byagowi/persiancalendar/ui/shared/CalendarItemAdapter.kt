package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.CalendarItemBinding
import com.byagowi.persiancalendar.utils.*
import java.util.*

class CalendarItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<CalendarItemAdapter.ViewHolder>() {
    private val mCalendarFont: Typeface = getCalendarFragmentFont(context)
    private var mCalendars: List<CalendarType> = ArrayList()
    internal var isExpanded = false
        set(expanded) {
            field = expanded
            for (i in mCalendars.indices) notifyItemChanged(i)
        }
    private var mJdn: Long = 0

    internal fun setDate(calendars: List<CalendarType>, jdn: Long) {
        mCalendars = calendars
        mJdn = jdn
        for (i in mCalendars.indices) notifyItemChanged(i)
        //        notifyDataSetChanged();
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CalendarItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = mCalendars.size

    inner class ViewHolder(private val binding: CalendarItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            val applyLineMultiplier = !isCustomFontEnabled

            binding.monthYear.typeface = mCalendarFont
            binding.day.typeface = mCalendarFont
            if (applyLineMultiplier) binding.monthYear.setLineSpacing(0f, .6f)

            binding.container.setOnClickListener(this)
            binding.linear.setOnClickListener(this)
        }

        fun bind(position: Int) {
            val date = Utils.getDateFromJdnOfCalendar(mCalendars[position], mJdn)

            binding.linear.text = toLinearDate(date)
            binding.linear.contentDescription = toLinearDate(date)
            val firstCalendarString = formatDate(date)
            binding.container.contentDescription = firstCalendarString
            binding.day.contentDescription = ""
            binding.day.text = formatNumber(date.dayOfMonth)
            binding.monthYear.contentDescription = ""
            binding.monthYear.text = String.format("%s\n%s",
                    Utils.getMonthName(date),
                    formatNumber(date.year))
        }

        override fun onClick(view: View?) {
            Utils.copyToClipboard(view, "converted date", view?.contentDescription)
        }
    }
}
