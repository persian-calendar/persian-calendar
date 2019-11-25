package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.utils.*

class PagerAdapter(private val calendarPager: CalendarPager) :
    RecyclerView.Adapter<PagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = CalendarPager.monthsLimit

    inner class ViewHolder(val binding: FragmentMonthBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val daysAdapter = DaysAdapter(
            binding.root.context, DaysPaintResources(binding.root.context), calendarPager
        )

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

            binding.monthDays.adapter = daysAdapter
        }

        fun bind(position: Int) {
            val offset = CalendarPager.applyOffset(position)
            val date = CalendarPager.getDateFromOffset(mainCalendar, offset)
            val baseJdn = date.toJdn()
            val monthLength = getMonthLength(mainCalendar, date.year, date.month)
            val startOfYearJdn = getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()

            daysAdapter.apply {
                startingDayOfWeek = getDayOfWeekFromJdn(baseJdn)
                weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
                weeksCount = calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) -
                        weekOfYearStart + 1
                days = (baseJdn until baseJdn + monthLength).toList()
                initializeMonthEvents()
                notifyItemRangeChanged(0, daysAdapter.itemCount)
            }

            update = fun(target: Int, isEventsModification: Boolean, jdn: Long) {
                if (target == offset) {
                    if (isEventsModification) {
                        daysAdapter.initializeMonthEvents()
                        calendarPager.onDayClicked(jdn)
                    } else {
                        daysAdapter.selectDay(-1)
                        calendarPager.onPageSelectedWithDate(date)
                    }

                    val selectedDay = 1 + jdn - baseJdn
                    if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength)
                        daysAdapter.selectDay(selectedDay.toInt())
                } else daysAdapter.selectDay(-1)
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
