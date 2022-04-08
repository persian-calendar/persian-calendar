package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
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
        addCellSpacing(4.dp.toInt())
    }

    fun initializeForRendering(@ColorInt textColor: Int, height: Int, today: AbstractDate) {
        val sharedData = SharedDayViewData(context, height / 7f, textColor)
        daysAdapter = DaysAdapter(context, sharedData, null)
        adapter = daysAdapter
        val jdn = Jdn(mainCalendar, today.year, today.month, 1)
        bind(jdn, jdn.toCalendar(mainCalendar))
    }

    private fun addCellSpacing(space: Int) {
        addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: State
            ) {
                if (parent.paddingBottom != space) {
                    parent.updatePadding(bottom = space)
                    parent.clipToPadding = false
                }
                outRect.set(0, 0, 0, space)
            }
        })
    }

    private var monthName = ""

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // This is important, don't remove it ever

        // Widget only tweak
        val sharedData = daysAdapter?.sharedDayViewData ?: return
        val widgetFooterTextPaint = sharedData.widgetFooterTextPaint ?: return
        canvas.drawText(monthName, width / 2f, height * .95f, widgetFooterTextPaint)
    }

    fun bind(monthStartJdn: Jdn, monthStartDate: AbstractDate) {
        val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
        monthName = language.my.format(monthStartDate.monthName, formatNumber(monthStartDate.year))
        contentDescription = monthName

        daysAdapter?.let {
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
