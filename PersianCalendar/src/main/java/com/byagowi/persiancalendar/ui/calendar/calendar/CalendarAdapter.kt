package com.byagowi.persiancalendar.ui.calendar.calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.OFFSET_ARGUMENT
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment

class CalendarAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = monthsLimit

    override fun createFragment(position: Int): Fragment =
        MonthFragment().apply {
            arguments = Bundle().apply {
                putInt(OFFSET_ARGUMENT, applyOffset(position))
            }
        }

    companion object {
        const val monthsLimit = 5000 // this should be an even number

        fun gotoOffset(monthViewPager: ViewPager2, offset: Int, smoothScroll: Boolean = true) {
            if (monthViewPager.currentItem != applyOffset(offset))
                monthViewPager.setCurrentItem(applyOffset(offset), smoothScroll)
        }

        fun applyOffset(position: Int) = monthsLimit / 2 - position
    }
}
