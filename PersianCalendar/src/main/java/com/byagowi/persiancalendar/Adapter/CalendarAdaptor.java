package com.byagowi.persiancalendar.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.byagowi.persiancalendar.view.Fragment.CalendarNewFragment;
import com.byagowi.persiancalendar.view.Fragment.MonthNewFragment;

/**
 * Created by behdad on 10/25/15.
 */
public class CalendarAdaptor extends FragmentPagerAdapter {
    public CalendarAdaptor(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        MonthNewFragment fragment = new MonthNewFragment();
        Bundle args = new Bundle();
        args.putInt("offset", position - CalendarNewFragment.MONTHS_LIMIT / 2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return CalendarNewFragment.MONTHS_LIMIT;
    }
}
