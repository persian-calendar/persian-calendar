package com.byagowi.persiancalendar.adapter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

public class CalendarAdapter extends FragmentStatePagerAdapter {

    public CalendarAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        MonthFragment fragment = new MonthFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.OFFSET_ARGUMENT, position - Constants.MONTHS_LIMIT / 2);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return Constants.MONTHS_LIMIT;
    }
}
