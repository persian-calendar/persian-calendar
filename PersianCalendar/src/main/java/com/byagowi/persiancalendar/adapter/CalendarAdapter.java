package com.byagowi.persiancalendar.adapter;

import android.os.Bundle;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CalendarAdapter extends FragmentStatePagerAdapter {
    private static final int MONTHS_LIMIT = 5000; // this should be an even number
    private static boolean isRTL;

    public CalendarAdapter(FragmentManager fm, boolean isRTL) {
        super(fm);
        CalendarAdapter.isRTL = isRTL;
    }

    @Override
    public Fragment getItem(int position) {
        MonthFragment fragment = new MonthFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.OFFSET_ARGUMENT, positionToOffset(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return MONTHS_LIMIT;
    }

    public static void gotoOffset(ViewPager monthViewPager, int offset) {
        if (monthViewPager.getCurrentItem() != offsetToPosition(offset)) {
            monthViewPager.setCurrentItem(offsetToPosition(offset));
        }
    }

    public static int positionToOffset(int position) {
        return isRTL ? position - MONTHS_LIMIT / 2 : MONTHS_LIMIT / 2 - position;
    }

    private static int offsetToPosition(int position) {
        return (isRTL ? position : -position) + MONTHS_LIMIT / 2;
    }
}
