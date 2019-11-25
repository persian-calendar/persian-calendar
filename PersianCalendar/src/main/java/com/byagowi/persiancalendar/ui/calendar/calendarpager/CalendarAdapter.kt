package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.utils.*

class CalendarAdapter(private val calendarPager: CalendarPager) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = CalendarPager.monthsLimit

    inner class ViewHolder(val binding: FragmentMonthBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var update = fun(_: Int, _: Boolean, _: Long) {}

        init {
            val isRTL = isRTL(binding.root.context)

            binding.next.apply {
                setImageResource(
                    if (isRTL) R.drawable.ic_keyboard_arrow_left
                    else R.drawable.ic_keyboard_arrow_right
                )
                setOnClickListener { calendarPager.changeMonth(1) }
            }

            binding.prev.apply {
                setImageResource(
                    if (isRTL) R.drawable.ic_keyboard_arrow_right
                    else R.drawable.ic_keyboard_arrow_left
                )
                setOnClickListener { calendarPager.changeMonth(-1) }
            }

            binding.monthDays.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(
                    binding.root.context, if (isShowWeekOfYearEnabled) 8 else 7
                )
            }

            calendarPager.addViewHolder(this)
        }

        private val dayPaintResources = DaysPaintResources(binding.root.context)

        fun bind(position: Int) {
            val offset = CalendarPager.applyOffset(position)
            val date = CalendarPager.getDateFromOffset(mainCalendar, offset)
            val baseJdn = date.toJdn()
            val monthLength = getMonthLength(mainCalendar, date.year, date.month)
            val startingDayOfWeek = getDayOfWeekFromJdn(baseJdn)
            val startOfYearJdn = getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()
            val weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
            val weeksCount =
                1 + calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart
            val adapter = MonthAdapter(
                binding.root.context, dayPaintResources, calendarPager,
                (baseJdn until baseJdn + monthLength).toList(),
                startingDayOfWeek, weekOfYearStart, weeksCount
            )
            binding.monthDays.let {
                it.adapter = adapter
                it.itemAnimator = null
            }

            update = fun(target: Int, isEventsModification: Boolean, jdn: Long) {
                if (target == offset) {
                    if (isEventsModification) {
                        adapter.initializeMonthEvents(binding.root.context)
                        calendarPager.onDayClicked(jdn)
                    } else {
                        adapter.selectDay(-1)
                        calendarPager.onPageSelectedWithDate(date)
                    }

                    val selectedDay = 1 + jdn - baseJdn
                    if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength)
                        adapter.selectDay(selectedDay.toInt())
                } else adapter.selectDay(-1)
            }

            if (calendarPager.getCurrentSelection() == position) {
                if (calendarPager.isTheFirstTime && offset == 0) {
                    calendarPager.isTheFirstTime = false
                    calendarPager.onDayClicked(getTodayJdn())
                }
                calendarPager.updateMonthFragments(offset, false)
            }
        }
    }
}
