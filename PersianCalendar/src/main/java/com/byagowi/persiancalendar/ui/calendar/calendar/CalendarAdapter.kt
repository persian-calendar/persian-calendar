package com.byagowi.persiancalendar.ui.calendar.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.entities.DayItem
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentModel
import com.byagowi.persiancalendar.ui.calendar.month.DaysPaintResources
import com.byagowi.persiancalendar.ui.calendar.month.MonthAdapter
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
            fun(_: CalendarFragmentModel.MonthFragmentUpdateCommand) {}

        init {
            val isRTL = isRTL(binding.root.context)

            binding.next.setImageResource(
                if (isRTL) R.drawable.ic_keyboard_arrow_left
                else R.drawable.ic_keyboard_arrow_right
            )
            binding.next.setOnClickListener { calendarFragment.changeMonth(1) }

            binding.prev.setImageResource(
                if (isRTL) R.drawable.ic_keyboard_arrow_right
                else R.drawable.ic_keyboard_arrow_left
            )
            binding.prev.setOnClickListener { calendarFragment.changeMonth(-1) }

            binding.monthDays.setHasFixedSize(true)
            binding.monthDays.layoutManager = GridLayoutManager(
                binding.root.context,
                if (isShowWeekOfYearEnabled) 8 else 7
            )

            ViewModelProviders.of(calendarFragment)[CalendarFragmentModel::class.java]
                .monthFragmentsHandler
                .observe(calendarFragment, Observer { onUpdateCommandReceived(it) })
        }

        fun bind(position: Int) {
            val offset = applyOffset(position)
            val date = getDateFromOffset(mainCalendar, offset)
            val baseJdn = date.toJdn()
            val monthLength = getMonthLength(mainCalendar, date.year, date.month)
            val startingDayOfWeek = getDayOfWeekFromJdn(baseJdn)
            val todayJdn = getTodayJdn()
            val startOfYearJdn = getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()
            val weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
            val weeksCount =
                1 + calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart
            val adapter = MonthAdapter(
                binding.root.context, DaysPaintResources(binding.root.context), calendarFragment,
                ArrayList<DayItem>().apply {
                    var dayOfWeek = startingDayOfWeek
                    (0 until monthLength).forEach {
                        val jdn = baseJdn + it
                        add(DayItem(jdn, dayOfWeek = dayOfWeek, isToday = jdn == todayJdn))
                        dayOfWeek++
                        if (dayOfWeek == 7) dayOfWeek = 0
                    }
                }, startingDayOfWeek, weekOfYearStart, weeksCount
            )
            binding.monthDays.let {
                it.adapter = adapter
                it.itemAnimator = null
            }

            val calendarFragmentModel =
                ViewModelProviders.of(calendarFragment)[CalendarFragmentModel::class.java]
            if (calendarFragmentModel.isTheFirstTime &&
                offset == 0 && calendarFragment.viewPagerPosition == offset
            ) {
                calendarFragmentModel.isTheFirstTime = false
                calendarFragmentModel.selectDay(todayJdn)
                updateTitle(date)
            }

            onUpdateCommandReceived = fun(cmd: CalendarFragmentModel.MonthFragmentUpdateCommand) {
                if (cmd.target == offset) {
                    val jdn = cmd.currentlySelectedJdn

                    if (cmd.isEventsModification) {
                        adapter.initializeMonthEvents(binding.root.context)
                        calendarFragmentModel.selectDay(jdn)
                    } else {
                        adapter.selectDay(-1)
                        updateTitle(date)
                    }

                    val selectedDay = 1 + jdn - baseJdn
                    if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength)
                        adapter.selectDay(selectedDay.toInt())
                } else adapter.selectDay(-1)
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
