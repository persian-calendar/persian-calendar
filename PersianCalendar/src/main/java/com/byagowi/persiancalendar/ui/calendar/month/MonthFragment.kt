package com.byagowi.persiancalendar.ui.calendar.month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.byagowi.persiancalendar.OFFSET_ARGUMENT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.di.AppDependency
import com.byagowi.persiancalendar.di.CalendarFragmentDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.entities.DayItem
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentModel
import com.byagowi.persiancalendar.ui.calendar.calendar.CalendarAdapter
import com.byagowi.persiancalendar.utils.*
import dagger.android.support.DaggerFragment
import io.github.persiancalendar.calendar.AbstractDate
import javax.inject.Inject

class MonthFragment : DaggerFragment() {

    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    @Inject
    lateinit var calendarFragmentDependency: CalendarFragmentDependency

    override fun onResume() {
        super.onResume()
        calendarFragmentDependency.calendarFragment.onDaySelected(
            arguments?.getInt(OFFSET_ARGUMENT) ?: 0
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentMonthBinding = FragmentMonthBinding.inflate(
            inflater,
            container, false
        )
        val calendarFragment = calendarFragmentDependency.calendarFragment
        val isRTL = isRTL(mainActivityDependency.mainActivity)
        val offset = arguments?.getInt(OFFSET_ARGUMENT) ?: 0

        fragmentMonthBinding.next.setImageResource(
            if (isRTL)
                R.drawable.ic_keyboard_arrow_left
            else
                R.drawable.ic_keyboard_arrow_right
        )
        fragmentMonthBinding.next.setOnClickListener { calendarFragment.changeMonth(1) }

        fragmentMonthBinding.prev.setImageResource(
            if (isRTL)
                R.drawable.ic_keyboard_arrow_right
            else
                R.drawable.ic_keyboard_arrow_left
        )
        fragmentMonthBinding.prev.setOnClickListener { calendarFragment.changeMonth(-1) }

        fragmentMonthBinding.monthDays.setHasFixedSize(true)
        fragmentMonthBinding.monthDays.layoutManager = GridLayoutManager(
            mainActivityDependency.mainActivity,
            if (isWeekOfYearEnabled()) 8 else 7
        )
        ///////
        ///////
        ///////
        val mainCalendar = mainCalendar
        val days = ArrayList<DayItem>()

        val date = getDateFromOffset(mainCalendar, offset)
        val baseJdn = date.toJdn()
        val monthLength = getMonthLength(mainCalendar, date.year, date.month)

        var dayOfWeek = getDayOfWeekFromJdn(baseJdn)

        val todayJdn = getTodayJdn()
        for (i in 0 until monthLength) {
            val jdn = baseJdn + i
            days.add(DayItem(jdn == todayJdn, jdn, dayOfWeek))
            dayOfWeek++
            if (dayOfWeek == 7) {
                dayOfWeek = 0
            }
        }

        val startOfYearJdn = getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()
        val weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
        val weeksCount =
            1 + calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart

        val startingDayOfWeek = getDayOfWeekFromJdn(baseJdn)
        ///////
        ///////
        ///////

        val calendarFragmentModel =
            ViewModelProviders.of(calendarFragment).get(CalendarFragmentModel::class.java)

        val adapter = MonthAdapter(
            mainActivityDependency, calendarFragmentDependency, days,
            startingDayOfWeek, weekOfYearStart, weeksCount
        )
        fragmentMonthBinding.monthDays.adapter = adapter
        fragmentMonthBinding.monthDays.itemAnimator = null

        if (calendarFragmentModel.isTheFirstTime &&
            offset == 0 && calendarFragment.viewPagerPosition == offset
        ) {
            calendarFragmentModel.isTheFirstTime = false
            calendarFragmentModel.selectDay(getTodayJdn())
            updateTitle(date)
        }

        calendarFragmentModel.monthFragmentsHandler.observe(this, Observer { command ->
            if (command.target == CalendarAdapter.applyOffset(offset)) {
                val jdn = command.currentlySelectedJdn

                if (command.isEventsModification) {
                    adapter.initializeMonthEvents(mainActivityDependency.mainActivity)
                    calendarFragmentModel.selectDay(jdn)
                } else {
                    adapter.selectDay(-1)
                    updateTitle(date)
                }

                val selectedDay = 1 + jdn - baseJdn
                if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength) {
                    adapter.selectDay((1 + jdn - baseJdn).toInt())
                }
            } else {
                adapter.selectDay(-1)
            }
        })

        return fragmentMonthBinding.root
    }

    private fun updateTitle(date: AbstractDate) {
        mainActivityDependency.mainActivity.setTitleAndSubtitle(
            getMonthName(date),
            formatNumber(date.year)
        )
    }

    companion object {
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
