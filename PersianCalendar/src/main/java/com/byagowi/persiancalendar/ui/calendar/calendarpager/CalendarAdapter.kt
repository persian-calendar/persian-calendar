package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.utils.*
import io.github.persiancalendar.calendar.AbstractDate

class CalendarAdapter(private val calendarFragment: CalendarFragment) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = monthsLimit

    inner class ViewHolder(val binding: FragmentMonthBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var onUpdateCommandReceived =
            fun(_: CalendarFragment.MonthFragmentUpdateCommand) {}

        init {
            val isRTL = isRTL(binding.root.context)

            binding.next.apply {
                setImageResource(
                    if (isRTL) R.drawable.ic_keyboard_arrow_left
                    else R.drawable.ic_keyboard_arrow_right
                )
                setOnClickListener { calendarFragment.changeMonth(1) }
            }

            binding.prev.apply {
                setImageResource(
                    if (isRTL) R.drawable.ic_keyboard_arrow_right
                    else R.drawable.ic_keyboard_arrow_left
                )
                setOnClickListener { calendarFragment.changeMonth(-1) }
            }

            binding.monthDays.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(
                    binding.root.context, if (isShowWeekOfYearEnabled) 8 else 7
                )
            }

            calendarFragment.monthFragmentsHandler
                .observe(calendarFragment, Observer { onUpdateCommandReceived(it) })
        }

        private val dayPaintResources = DaysPaintResources(binding.root.context)

        fun bind(position: Int) {
            val offset = applyOffset(position)
            val date = getDateFromOffset(mainCalendar, offset)
            val baseJdn = date.toJdn()
            val monthLength = getMonthLength(mainCalendar, date.year, date.month)
            val startingDayOfWeek = getDayOfWeekFromJdn(baseJdn)
            val startOfYearJdn = getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()
            val weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
            val weeksCount =
                1 + calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart
            val adapter = MonthAdapter(
                binding.root.context, dayPaintResources, calendarFragment,
                (baseJdn until baseJdn + monthLength).toList(),
                startingDayOfWeek, weekOfYearStart, weeksCount
            )
            binding.monthDays.let {
                it.adapter = adapter
                it.itemAnimator = null
            }

            onUpdateCommandReceived = fun(cmd: CalendarFragment.MonthFragmentUpdateCommand) {
                if (cmd.target == offset) {
                    val jdn = cmd.currentlySelectedJdn

                    if (cmd.isEventsModification) {
                        adapter.initializeMonthEvents(binding.root.context)
                        calendarFragment.selectDay(jdn)
                    } else {
                        adapter.selectDay(-1)
                        updateTitle(date)
                    }

                    val selectedDay = 1 + jdn - baseJdn
                    if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength)
                        adapter.selectDay(selectedDay.toInt())
                } else adapter.selectDay(-1)
            }

            if (calendarFragment.getCurrentSelection() == position) {
                if (calendarFragment.isTheFirstTime && offset == 0) {
                    calendarFragment.isTheFirstTime = false
                    calendarFragment.selectDay(getTodayJdn())
                }
                calendarFragment.onDaySelected(offset)
            }
        }

        private fun updateTitle(date: AbstractDate) =
            calendarFragment.mainActivityDependency.mainActivity.setTitleAndSubtitle(
                getMonthName(date),
                formatNumber(date.year)
            )
    }

    companion object {
        const val monthsLimit = 5000 // this should be an even number

        fun gotoOffset(monthViewPager: ViewPager2, offset: Int, smoothScroll: Boolean = true) {
            if (monthViewPager.currentItem != applyOffset(offset))
                monthViewPager.setCurrentItem(applyOffset(offset), smoothScroll)
        }

        fun applyOffset(position: Int) = monthsLimit / 2 - position

        fun getDateFromOffset(calendar: CalendarType, offset: Int): AbstractDate {
            val date = getTodayOfCalendar(calendar)
            var month = date.month - offset
            month -= 1
            var year = date.year

            year += month / 12
            month %= 12
            if (month < 0) {
                year -= 1
                month += 12
            }
            month += 1
            return getDateOfCalendar(calendar, year, month, 1)
        }
    }
}
