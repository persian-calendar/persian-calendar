package com.byagowi.persiancalendar.ui.calendar.calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.byagowi.persiancalendar.OFFSET_ARGUMENT

import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment

class CalendarAdapter(fm: FragmentManager, private val mCalendarAdapterHelper: CalendarAdapterHelper) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment =
            MonthFragment().apply {
                arguments = Bundle().apply {
                    putInt(OFFSET_ARGUMENT, mCalendarAdapterHelper.positionToOffset(position))
                }
            }

    override fun getCount() = mCalendarAdapterHelper.monthsLimit

    class CalendarAdapterHelper(private val isRTL: Boolean) {
        val monthsLimit = 5000 // this should be an even number

        fun gotoOffset(monthViewPager: ViewPager, offset: Int) {
            if (monthViewPager.currentItem != offsetToPosition(offset)) {
                monthViewPager.currentItem = offsetToPosition(offset)
            }
        }

        fun positionToOffset(position: Int) = if (isRTL) position - monthsLimit / 2 else monthsLimit / 2 - position

        private fun offsetToPosition(position: Int) = (if (isRTL) position else -position) + monthsLimit / 2
    }
}
