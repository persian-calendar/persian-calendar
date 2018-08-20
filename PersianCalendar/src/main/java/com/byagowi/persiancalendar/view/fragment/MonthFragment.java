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
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
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
    private AbstractDate typedDate;
    private int offset;

    private RecyclerView recyclerView;
    private long baseJdn;
    private int monthLength;

    static boolean isRTL = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_month, container, false);
        isRTL = UIUtils.isRTL(getContext());
        offset = getArguments().getInt(Constants.OFFSET_ARGUMENT);

        // We deliberately like to avoid DataBinding thing here, at least for now
        AppCompatImageView prev = view.findViewById(R.id.prev);
        AppCompatImageView next = view.findViewById(R.id.next);
        prev.setImageResource(isRTL
                ? R.drawable.ic_keyboard_arrow_right
                : R.drawable.ic_keyboard_arrow_left);
        next.setImageResource(isRTL
                ? R.drawable.ic_keyboard_arrow_left
                : R.drawable.ic_keyboard_arrow_right);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        recyclerView = view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), Utils.isWeekOfYearEnabled() ? 8 : 7);
        recyclerView.setLayoutManager(layoutManager);
        ///////
        ///////
        ///////
        CalendarType mainCalendar = Utils.getMainCalendar();
        List<DayEntity> days = new ArrayList<>();
        typedDate = CalendarUtils.getTodayOfCalendar(mainCalendar);
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
        typedDate = CalendarUtils.getDateOfCalendar(mainCalendar, year, month, 1);

        baseJdn = CalendarUtils.getJdnDate(typedDate);
        monthLength = (int) (CalendarUtils.getJdnOfCalendar(mainCalendar, month == 12 ? year + 1 : year,
                month == 12 ? 1 : month + 1, 1) - baseJdn);

        int dayOfWeek = CalendarUtils.getDayOfWeekFromJdn(baseJdn);

        long todayJdn = CalendarUtils.getTodayJdn();
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

        long startOfYearJdn = CalendarUtils.getJdnOfCalendar(mainCalendar, year, 1, 1);
        int weekOfYearStart = CalendarUtils.calculateWeekOfYear(baseJdn, startOfYearJdn);
        int weeksCount = 1 + CalendarUtils.calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart;

        int startingDayOfWeek = CalendarUtils.getDayOfWeekFromJdn(baseJdn);
        ///////
        ///////
        ///////
        recyclerView.setAdapter(new MonthAdapter(getContext(), days, startingDayOfWeek, weekOfYearStart, weeksCount));
        recyclerView.setItemAnimator(null);

        CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CalendarFragment.class.getName());

        if (calendarFragment.firstTime && offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragment.firstTime = false;
            calendarFragment.selectDay(CalendarUtils.getTodayJdn());
            updateTitle();
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));

        return view;
    }

    @Override
    public void onDestroyView() {
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView = null;
        }
        super.onDestroyView();
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if (extras == null || recyclerView == null) return;

            RecyclerView.Adapter baseAdapter = recyclerView.getAdapter();
            if (baseAdapter == null || !(baseAdapter instanceof MonthAdapter)) return;
            MonthAdapter adapter = (MonthAdapter) baseAdapter;

            int value = extras.getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == offset) {
                long jdn = extras.getLong(Constants.BROADCAST_FIELD_SELECT_DAY_JDN);

                if (extras.getBoolean(Constants.BROADCAST_FIELD_EVENT_ADD_MODIFY, false)) {
                    adapter.initializeMonthEvents(context);

                    CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag(CalendarFragment.class.getName());

                    if (calendarFragment != null) {
                        calendarFragment.selectDay(jdn);
                    }
                } else {
                    adapter.selectDay(-1);
                    updateTitle();
                }

                long selectedDay = 1 + jdn - baseJdn;
                if (jdn != -1 && jdn >= baseJdn && selectedDay <= monthLength) {
                    adapter.selectDay((int) (1 + jdn - baseJdn));
                }
            } else {
                adapter.selectDay(-1);
            }
        }
    };

    @Override
    public void onDestroy() {
        if (setCurrentMonthReceiver != null) {
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(setCurrentMonthReceiver);
            setCurrentMonthReceiver = null;
        }

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CalendarFragment.class.getName());

        switch (v.getId()) {
            case R.id.next:
                calendarFragment.changeMonth(isRTL ? -1 : 1);
                break;

            case R.id.prev:
                calendarFragment.changeMonth(isRTL ? 1 : -1);
                break;
        }
    }

    private void updateTitle() {
        UIUtils.setActivityTitleAndSubtitle(getActivity(), CalendarUtils.getMonthName(typedDate),
                Utils.formatNumber(typedDate.getYear()));
    }
}
