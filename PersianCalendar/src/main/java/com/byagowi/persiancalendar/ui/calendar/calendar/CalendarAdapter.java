package com.byagowi.persiancalendar.ui.calendar.calendar;

import android.os.Bundle;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CalendarAdapter extends FragmentStatePagerAdapter {
    private CalendarAdapterHelper mCalendarAdapterHelper;

    public CalendarAdapter(FragmentManager fm, CalendarAdapterHelper calendarAdapterHelper) {
        super(fm);
        mCalendarAdapterHelper = calendarAdapterHelper;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        MonthFragment fragment = new MonthFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.OFFSET_ARGUMENT, mCalendarAdapterHelper.positionToOffset(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return mCalendarAdapterHelper.getMonthsLimit();
    }

    public static class CalendarAdapterHelper {
        private final int MONTHS_LIMIT = 5000; // this should be an even number
        private boolean isRTL;

        public CalendarAdapterHelper(boolean isRTL) {
            this.isRTL = isRTL;
        }

        public void gotoOffset(ViewPager monthViewPager, int offset) {
            if (monthViewPager.getCurrentItem() != offsetToPosition(offset)) {
                monthViewPager.setCurrentItem(offsetToPosition(offset));
            }
        }

        public int positionToOffset(int position) {
            return isRTL ? position - MONTHS_LIMIT / 2 : MONTHS_LIMIT / 2 - position;
        }

        int offsetToPosition(int position) {
            return (isRTL ? position : -position) + MONTHS_LIMIT / 2;
        }

        int getMonthsLimit() {
            return MONTHS_LIMIT;
        }
    }
}
