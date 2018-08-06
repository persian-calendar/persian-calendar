package com.byagowi.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.MonthAdapter;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import calendar.AbstractDate;
import calendar.CalendarType;

public class MonthFragment extends Fragment implements View.OnClickListener {
    private CalendarFragment calendarFragment;
    private AbstractDate typedDate;
    private int offset;

    private MonthAdapter adapter;
    private List<DayEntity> days;
    private int weekOfYearStart;
    private int weeksCount;
    private int startingDayOfWeek;
    private long baseJdn;
    private int monthLength;

    private void fillTheFields() {
        CalendarType mainCalendar = Utils.getMainCalendar();
        List<DayEntity> days = new ArrayList<>();
        typedDate = Utils.getTodayOfCalendar(mainCalendar);
        int month = typedDate.getMonth() - offset;
        month -= 1;
        int year = typedDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        typedDate = Utils.getDateOfCalendar(mainCalendar, year, month, 1);

        baseJdn = Utils.getJdnDate(typedDate);
        monthLength = (int) (Utils.getJdnOfCalendar(mainCalendar, month == 12 ? year + 1 : year,
                month == 12 ? 1 : month + 1, 1) - baseJdn);

        int dayOfWeek = Utils.getDayOfWeekFromJdn(baseJdn);

        long todayJdn = Utils.getTodayJdn();
        for (int i = 0; i < monthLength; i++) {
            DayEntity dayEntity = new DayEntity();
            dayEntity.setJdn(baseJdn + i);

            if (baseJdn + i == todayJdn) {
                dayEntity.setToday(true);
            }

            dayEntity.setDayOfWeek(dayOfWeek);

            days.add(dayEntity);
            dayOfWeek++;
            if (dayOfWeek == 7) {
                dayOfWeek = 0;
            }
        }
        this.days = days;

        long startOfYearJdn = Utils.getJdnOfCalendar(mainCalendar, year, 1, 1);
        weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn);
        weeksCount = 1 + calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart;

        startingDayOfWeek = Utils.getDayOfWeekFromJdn(baseJdn);
    }

    private int calculateWeekOfYear(long jdn, long startOfYear) {
        long dayOfYear = jdn - startOfYear;
        return (int) Math.ceil(1 + (dayOfYear - Utils.fixDayOfWeekReverse(Utils.getDayOfWeekFromJdn(jdn))) / 7.);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_month, container, false);
        offset = getArguments().getInt(Constants.OFFSET_ARGUMENT);

        AppCompatImageView prev = view.findViewById(R.id.prev);
        AppCompatImageView next = view.findViewById(R.id.next);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), Utils.isWeekOfYearEnabled() ? 8 : 7);
        recyclerView.setLayoutManager(layoutManager);
        fillTheFields();
        adapter = new MonthAdapter(getContext(), this, days, startingDayOfWeek, weekOfYearStart, weeksCount);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        calendarFragment = (CalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CalendarFragment.class.getName());

        if (calendarFragment.firstTime && offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragment.firstTime = false;
            calendarFragment.selectDay(Utils.getTodayJdn());
            updateTitle();
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));

        return view;
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if (extras == null) return;

            adapter.selectDay(-1);

            int value = extras.getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == offset) {
                updateTitle();

                long jdn = extras.getLong(Constants.BROADCAST_FIELD_SELECT_DAY_JDN);
                long selectedDay = 1 + jdn - baseJdn;
                if (jdn != -1 && jdn >= baseJdn && selectedDay <= monthLength) {
                    adapter.selectDay((int) (1 + jdn - baseJdn));
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(setCurrentMonthReceiver);
        super.onDestroy();
    }

    public void onClickItem(long jdn) {
        calendarFragment.selectDay(jdn);
    }

    public void onLongClickItem(long jdn) {
        calendarFragment.addEventOnCalendar(jdn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                calendarFragment.changeMonth(1);
                break;

            case R.id.prev:
                calendarFragment.changeMonth(-1);
                break;
        }
    }

    private void updateTitle() {
        Utils.setActivityTitleAndSubtitle(getActivity(), Utils.getMonthName(typedDate),
                Utils.formatNumber(typedDate.getYear()));
    }

}
