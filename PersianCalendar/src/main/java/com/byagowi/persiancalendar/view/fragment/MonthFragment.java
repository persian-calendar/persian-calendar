package com.byagowi.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.MonthAdapter;
import com.byagowi.persiancalendar.di.dependencies.CalendarFragmentDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.di.dependencies.MonthFragmentDependency;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import calendar.AbstractDate;
import calendar.CalendarType;
import dagger.android.support.DaggerFragment;

public class MonthFragment extends DaggerFragment implements View.OnClickListener {
    private AbstractDate typedDate;
    private int offset;
    private MonthAdapter adapter;

    private long baseJdn;
    private int monthLength;

    private static boolean isRTL = false;

    @Inject
    MainActivityDependency mainActivityDependency;

    @Inject
    CalendarFragmentDependency calendarFragmentDependency;

    @Inject
    MonthFragmentDependency monthFragmentDependency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month, container, false);
        isRTL = UIUtils.isRTL(mainActivityDependency.getMainActivity());
        Bundle args = getArguments();
        offset = args == null ? 0 : args.getInt(Constants.OFFSET_ARGUMENT);

        // We deliberately like to avoid DataBinding thing here, at least for now
        ImageView next = view.findViewById(R.id.next);
        next.setImageResource(isRTL
                ? R.drawable.ic_keyboard_arrow_left
                : R.drawable.ic_keyboard_arrow_right);
        next.setOnClickListener(this);

        ImageView prev = view.findViewById(R.id.prev);
        prev.setImageResource(isRTL
                ? R.drawable.ic_keyboard_arrow_right
                : R.drawable.ic_keyboard_arrow_left);
        prev.setOnClickListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(new GridLayoutManager(mainActivityDependency.getMainActivity(),
                Utils.isWeekOfYearEnabled() ? 8 : 7));
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

        baseJdn = typedDate.toJdn();
        monthLength = CalendarUtils.getMonthLength(mainCalendar, year, month);

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
        CalendarFragment calendarFragment = calendarFragmentDependency.getCalendarFragment();

        adapter = new MonthAdapter(calendarFragmentDependency, days,
                startingDayOfWeek, weekOfYearStart, weeksCount);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        if (calendarFragment.firstTime &&
                offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragment.firstTime = false;
            calendarFragment.selectDay(CalendarUtils.getTodayJdn());
            updateTitle();
        }

        LocalBroadcastManager.getInstance(mainActivityDependency.getMainActivity()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));

        return view;
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if (extras == null) return;

            int value = extras.getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == offset) {
                long jdn = extras.getLong(Constants.BROADCAST_FIELD_SELECT_DAY_JDN);

                if (extras.getBoolean(Constants.BROADCAST_FIELD_EVENT_ADD_MODIFY, false)) {
                    adapter.initializeMonthEvents(context);
                    calendarFragmentDependency.getCalendarFragment().selectDay(jdn);
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
        LocalBroadcastManager.getInstance(mainActivityDependency.getMainActivity())
                .unregisterReceiver(setCurrentMonthReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                calendarFragmentDependency.getCalendarFragment().changeMonth(isRTL ? -1 : 1);
                break;

            case R.id.prev:
                calendarFragmentDependency.getCalendarFragment().changeMonth(isRTL ? 1 : -1);
                break;
        }
    }

    private void updateTitle() {
        mainActivityDependency.getMainActivity().setTitleAndSubtitle(
                CalendarUtils.getMonthName(typedDate),
                Utils.formatNumber(typedDate.getYear()));
    }
}
