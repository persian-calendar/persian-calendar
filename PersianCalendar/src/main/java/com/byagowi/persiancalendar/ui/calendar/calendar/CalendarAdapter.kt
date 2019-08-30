package com.byagowi.persiancalendar.ui.calendar.calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment

class CalendarAdapter(fm: FragmentManager, private val mCalendarAdapterHelper: CalendarAdapterHelper) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val fragment = MonthFragment()
        val bundle = Bundle()
        bundle.putInt(Constants.OFFSET_ARGUMENT, mCalendarAdapterHelper.positionToOffset(position))
        fragment.arguments = bundle
        return fragment
    }

    override fun getCount(): Int {
        return mCalendarAdapterHelper.monthsLimit
    }

    class CalendarAdapterHelper(private val isRTL: Boolean) {
        internal val monthsLimit = 5000 // this should be an even number

        fun gotoOffset(monthViewPager: ViewPager, offset: Int) {
            if (monthViewPager.currentItem != offsetToPosition(offset)) {
                monthViewPager.currentItem = offsetToPosition(offset)
            }
        }

        fun positionToOffset(position: Int): Int {
            return if (isRTL) position - monthsLimit / 2 else monthsLimit / 2 - position
        }

        internal fun offsetToPosition(position: Int): Int {
            return (if (isRTL) position else -position) + monthsLimit / 2
        }
    }
}
