package com.byagowi.persiancalendar.view.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Adapter.CalendarAdapter;
import com.byagowi.persiancalendar.Interface.changeMonth;
import com.byagowi.persiancalendar.R;

/**
 * Created by behdad on 10/25/15.
 */
public class CalendarNewFragment extends Fragment implements ViewPager.OnPageChangeListener, changeMonth {
    public static final int MONTHS_LIMIT = 1200;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_calendar, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.calendar_pager);
        viewPager.setAdapter(new CalendarAdapter(getActivity().getSupportFragmentManager(), this));
        viewPager.setCurrentItem(MONTHS_LIMIT / 2);
        viewPager.addOnPageChangeListener(this);

        return view;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void changeMonth(int position) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + position, true);
    }
}
