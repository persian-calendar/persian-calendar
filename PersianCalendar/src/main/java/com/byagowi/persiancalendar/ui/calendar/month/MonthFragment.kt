package com.byagowi.persiancalendar.ui.calendar.month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.calendar.AbstractDate
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.di.dependencies.AppDependency
import com.byagowi.persiancalendar.di.dependencies.CalendarFragmentDependency
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.entities.DayItem
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentModel
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.Utils
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject

class MonthFragment : DaggerFragment() {

    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    @Inject
    lateinit var calendarFragmentDependency: CalendarFragmentDependency

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val fragmentMonthBinding = FragmentMonthBinding.inflate(inflater,
                container, false)
        val calendarFragment = calendarFragmentDependency.calendarFragment
        val isRTL = Utils.isRTL(mainActivityDependency.mainActivity)
        val args = arguments
        val offset = args?.getInt(Constants.OFFSET_ARGUMENT) ?: 0

        fragmentMonthBinding.next.setImageResource(if (isRTL)
            R.drawable.ic_keyboard_arrow_left
        else
            R.drawable.ic_keyboard_arrow_right)
        fragmentMonthBinding.next.setOnClickListener { v -> calendarFragment.changeMonth(if (isRTL) -1 else 1) }

        fragmentMonthBinding.prev.setImageResource(if (isRTL)
            R.drawable.ic_keyboard_arrow_right
        else
            R.drawable.ic_keyboard_arrow_left)
        fragmentMonthBinding.prev.setOnClickListener { v -> calendarFragment.changeMonth(if (isRTL) 1 else -1) }

        fragmentMonthBinding.monthDays.setHasFixedSize(true)


        fragmentMonthBinding.monthDays.layoutManager = GridLayoutManager(mainActivityDependency.mainActivity,
                if (Utils.isWeekOfYearEnabled()) 8 else 7)
        ///////
        ///////
        ///////
        val mainCalendar = Utils.getMainCalendar()
        val days = ArrayList<DayItem>()

        val date = getDateFromOffset(mainCalendar, offset)
        val baseJdn = date.toJdn()
        val monthLength = Utils.getMonthLength(mainCalendar, date.year, date.month)

        var dayOfWeek = Utils.getDayOfWeekFromJdn(baseJdn)

        val todayJdn = Utils.getTodayJdn()
        for (i in 0 until monthLength) {
            val jdn = baseJdn + i
            days.add(DayItem(jdn == todayJdn, jdn, dayOfWeek))
            dayOfWeek++
            if (dayOfWeek == 7) {
                dayOfWeek = 0
            }
        }

        val startOfYearJdn = Utils.getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()
        val weekOfYearStart = Utils.calculateWeekOfYear(baseJdn, startOfYearJdn)
        val weeksCount = 1 + Utils.calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart

        val startingDayOfWeek = Utils.getDayOfWeekFromJdn(baseJdn)
        ///////
        ///////
        ///////

        val calendarFragmentModel = ViewModelProviders.of(calendarFragment).get(CalendarFragmentModel::class.java)

        val adapter = MonthAdapter(calendarFragmentDependency, days,
                startingDayOfWeek, weekOfYearStart, weeksCount)
        fragmentMonthBinding.monthDays.adapter = adapter
        fragmentMonthBinding.monthDays.itemAnimator = null

        if (calendarFragmentModel.isTheFirstTime &&
                offset == 0 && calendarFragment.viewPagerPosition == offset) {
            calendarFragmentModel.isTheFirstTime = false
            calendarFragmentModel.selectDay(Utils.getTodayJdn())
            updateTitle(date)
        }

        calendarFragmentModel.monthFragmentsHandler.observe(this, Observer { command ->
            if (command.target == offset) {
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
                Utils.getMonthName(date),
                Utils.formatNumber(date.year))
    }

    companion object {

        fun getDateFromOffset(calendar: CalendarType, offset: Int): AbstractDate {
            val date = Utils.getTodayOfCalendar(calendar)
            var month = date.month - offset
            month -= 1
            var year = date.year

            year = year + month / 12
            month = month % 12
            if (month < 0) {
                year -= 1
                month += 12
            }
            month += 1
            return Utils.getDateOfCalendar(calendar, year, month, 1)
        }
    }
}
