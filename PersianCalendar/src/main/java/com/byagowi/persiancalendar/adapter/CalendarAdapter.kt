package com.byagowi.persiancalendar.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.view.fragment.MonthFragment

class CalendarAdapter(fm: FragmentManager, isRTL: Boolean) : FragmentStatePagerAdapter(fm) {
  init {
    CalendarAdapter.isRTL = isRTL
  }

  override fun getItem(position: Int): Fragment {
    val fragment = MonthFragment()
    val bundle = Bundle()
    bundle.putInt(Constants.OFFSET_ARGUMENT, positionToOffset(position))
    fragment.arguments = bundle
    return fragment
  }

  override fun getCount(): Int = MONTHS_LIMIT

  companion object {
    private val MONTHS_LIMIT = 5000 // this should be an even number
    private var isRTL: Boolean = false

    fun gotoOffset(monthViewPager: ViewPager, offset: Int) {
      if (monthViewPager.currentItem != offsetToPosition(offset)) {
        monthViewPager.currentItem = offsetToPosition(offset)
      }
    }

    fun positionToOffset(position: Int): Int =
        if (isRTL) position - MONTHS_LIMIT / 2 else MONTHS_LIMIT / 2 - position

    private fun offsetToPosition(position: Int): Int =
        (if (isRTL) position else -position) + MONTHS_LIMIT / 2
  }
}
