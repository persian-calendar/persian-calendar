package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.getDateOfCalendar
import com.byagowi.persiancalendar.utils.getTodayOfCalendar
import io.github.persiancalendar.calendar.AbstractDate
import java.lang.ref.WeakReference
import java.util.*

class CalendarPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val viewPager = ViewPager2(context, attrs)

    var isTheFirstTime: Boolean = true
    var lastSelectedJdn: Long = -1

    var onDayClicked = fun(_: Long) {}
    var onDayLongClicked = fun(_: Long) {}
    var onNonDefaultPageSelected = fun() {}
    var onPageSelectedWithDate = fun(_: AbstractDate) {}

    private val pagesViewHolders = ArrayList<WeakReference<CalendarAdapter.ViewHolder>>()

    fun addViewHolder(vh: CalendarAdapter.ViewHolder) = pagesViewHolders.add(WeakReference(vh))

    fun updateMonthFragments(toWhich: Int, addOrModify: Boolean) = pagesViewHolders.forEach {
        it.get()?.apply { update(toWhich, addOrModify, lastSelectedJdn) }
    }

    fun resetMonthFragments() = updateMonthFragments(Int.MAX_VALUE, false)

    init {
        viewPager.adapter = CalendarAdapter(this)
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val offset = applyOffset(position)
                    updateMonthFragments(offset, false)
                    if (offset != 0) onNonDefaultPageSelected()
                }
            }
        )
        addView(viewPager)
        gotoOffset(0, false)
    }

    fun changeMonth(position: Int) =
        viewPager.setCurrentItem(viewPager.currentItem + position, true)

    fun getCurrentSelection() = viewPager.currentItem

    fun gotoOffset(offset: Int, smoothScroll: Boolean = true) {
        if (viewPager.currentItem != applyOffset(offset))
            viewPager.setCurrentItem(applyOffset(offset), smoothScroll)
    }

    companion object {
        const val monthsLimit = 5000 // this should be an even number

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