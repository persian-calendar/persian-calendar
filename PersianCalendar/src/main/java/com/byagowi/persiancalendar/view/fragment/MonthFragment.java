package com.byagowi.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.MonthAdapter;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import calendar.DateConverter;
import calendar.PersianDate;

public class MonthFragment extends Fragment implements View.OnClickListener {
    private CalendarFragment calendarFragment;
    private PersianDate persianDate;
    private int offset;

    private MonthAdapter adapter;
    private List<DayEntity> days;
    private int weekOfYearStart;
    private int weeksCount;

    private void fillTheFields() {
        List<DayEntity> days = new ArrayList<>();
        persianDate = Utils.getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        persianDate = new PersianDate(year, month, 1);

        long baseJdn = DateConverter.persianToJdn(persianDate);
        int monthLength = (int) (DateConverter.persianToJdn(month == 12 ? year + 1 : year,
                month == 12 ? 1 : month + 1, 1) - baseJdn);

        int dayOfWeek = DateConverter.jdnToCivil(baseJdn).getDayOfWeek() % 7;

        long todayJdn = DateConverter.persianToJdn(Utils.getToday());
        for (int i = 0; i < monthLength; i++) {
            DayEntity dayEntity = new DayEntity();
            dayEntity.setNum(Utils.formatNumber(i + 1));
            dayEntity.setDayOfWeek(dayOfWeek);

            List<AbstractEvent> events = Utils.getEvents(baseJdn + i);

            if (dayOfWeek == 6 || !TextUtils.isEmpty(Utils.getEventsTitle(events, true))) {
                dayEntity.setHoliday(true);
            }

            if (events.size() > 0) {
                dayEntity.setEvent(true);
            }

            dayEntity.setJdn(baseJdn + i);

            if (baseJdn + i == todayJdn) {
                dayEntity.setToday(true);
            }

            days.add(dayEntity);
            dayOfWeek++;
            if (dayOfWeek == 7) {
                dayOfWeek = 0;
            }
        }
        this.days = days;

        //FIXME: This is wrooong
//        long startOfYearJdn = DateConverter.persianToJdn(year, 1, 1);
//        long firstFridayJdn = startOfYearJdn - DateConverter.jdnToCivil(startOfYearJdn).getDayOfWeek() - 2;
//        weekOfYearStart = (int) (1 + Math.ceil((double) (baseJdn - firstFridayJdn) / 7));
//        weeksCount = (int) (1 + Math.ceil((double) (baseJdn + monthLength - firstFridayJdn) / 7)) - weekOfYearStart;
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

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        fillTheFields();
        adapter = new MonthAdapter(getContext(), this, days, weekOfYearStart, weeksCount);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        calendarFragment = (CalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CalendarFragment.class.getName());

        if (offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragment.selectDay(DateConverter.persianToJdn(Utils.getToday()));
            updateTitle();
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));

        return view;
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getExtras().getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == offset) {
                updateTitle();

                int day = intent.getExtras().getInt(Constants.BROADCAST_FIELD_SELECT_DAY);
                if (day != -1) {
                    adapter.selectDay(day);
                }

            } else if (value == Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY) {
                adapter.clearSelectedDay();
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
//        Toast.makeText(getContext(),
//                weekOfYearStart + "-" + weeksCount + "-" + (weekOfYearStart + weeksCount),
//                Toast.LENGTH_LONG).show();
        Utils.setActivityTitleAndSubtitle(getActivity(), Utils.getMonthName(persianDate),
                Utils.formatNumber(persianDate.getYear()));
    }

}
