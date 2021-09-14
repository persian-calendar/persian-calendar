package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.getMonthLength
import com.byagowi.persiancalendar.utils.getWeekOfYear
import com.byagowi.persiancalendar.utils.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.utils.mainCalendar
import io.github.persiancalendar.calendar.AbstractDate

class MonthView(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {

    init {
        setHasFixedSize(true)
        layoutManager = GridLayoutManager(context, if (isShowWeekOfYearEnabled) 8 else 7)
    }

    private var daysAdapter: DaysAdapter? = null

    fun initialize(sharedDayViewData: SharedDayViewData, calendarPager: CalendarPager) {
        daysAdapter = DaysAdapter(context, sharedDayViewData, calendarPager)
        adapter = daysAdapter
    }

    fun initializeForWidget(@ColorInt textColor: Int, today: AbstractDate) {
        daysAdapter = DaysAdapter(context, SharedDayViewData(context, textColor), null)
        adapter = daysAdapter
        val jdn = Jdn(mainCalendar, today.year, today.month, 1)
        bind(jdn, jdn.toCalendar(mainCalendar))
    }

    fun bind(monthStartJdn: Jdn, monthStartDate: AbstractDate) {
        daysAdapter?.let {
            val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
            val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
            it.startingDayOfWeek = monthStartJdn.dayOfWeek
            it.weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)
            it.weeksCount = (monthStartJdn + monthLength - 1).getWeekOfYear(startOfYearJdn) -
                    it.weekOfYearStart + 1
            it.days = monthStartJdn.createMonthDaysList(monthLength)
            it.initializeMonthEvents()
            it.notifyItemRangeChanged(0, it.itemCount)
        }
    }

    fun initializeMonthEvents() {
        daysAdapter?.initializeMonthEvents()
    }

    fun selectDay(dayOfMonth: Int) {
        daysAdapter?.selectDay(dayOfMonth)
    }
}
