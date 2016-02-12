package com.byagowi.persiancalendar.view.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Adapter.MonthAdapter;
import com.byagowi.persiancalendar.Entity.Day;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.malinskiy.materialicons.widget.IconTextView;

import java.util.List;

import calendar.PersianDate;

public class MonthNewFragment extends Fragment implements View.OnClickListener {
    private final Utils utils = Utils.getInstance();
    private CalendarMainFragment calendarMainFragment;
    private PersianDate persianDate;
    private char[] digits;
    private int offset;
    private IntentFilter filter;
    private BroadcastReceiver receiver;
    private MonthAdapter adapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.calendar_month, container, false);
        offset = getArguments().getInt("offset");
        List<Day> days = utils.getDays(getContext(), offset);
        digits = utils.preferredDigits(getActivity());

        IconTextView prev = (IconTextView) view.findViewById(R.id.prev);
        IconTextView next = (IconTextView) view.findViewById(R.id.next);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

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
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MonthAdapter(getActivity(), this, days);
        recyclerView.setAdapter(adapter);

        calendarMainFragment = (CalendarMainFragment) getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag("CalendarMainFragment");

        if (calendarMainFragment != null && offset == 0) {
            calendarMainFragment.selectDay(Utils.getToday());
        }

        if (offset == 0) {
            UpdateTitle();
        }

        filter = new IntentFilter("com.byagowi.persiancalendar.changemounth");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int value =  intent.getExtras().getInt("value");
                if (value == offset) {
                    UpdateTitle();
                } else if (value == 2000) {
                    resetSelectDay();
                }
            }
        };

        return view;
    }

    public void onClickItem(PersianDate day) {
        calendarMainFragment.selectDay(day);
    }

    public void onLongClickItem(PersianDate day) {
        calendarMainFragment.addEventOnCalendar(day);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.next:
                calendarMainFragment.changeMonth(1);
                break;

            case R.id.prev:
                calendarMainFragment.changeMonth(-1);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    private void UpdateTitle() {
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setSubtitle(Utils.formatNumber(persianDate.getYear(), digits));

        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(utils.getMonthName(persianDate));
    }

    private void resetSelectDay() {
        if (adapter.select_Day != -1) {
            adapter.select_Day = -1;
            adapter.notifyDataSetChanged();
        }
    }
}
