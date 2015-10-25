package com.byagowi.persiancalendar.view.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Adapter.MonthAdapter;
import com.byagowi.persiancalendar.Entity.Day;
import com.byagowi.persiancalendar.Interface.ClickListener;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import java.util.List;

import calendar.PersianDate;

public class MonthNewFragment extends Fragment implements ClickListener {
    private RecyclerView recyclerView;
    private MonthAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private final Utils utils = Utils.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_month, container, false);
        int offset = getArguments().getInt("offset");
        List<Day> days = utils.getDays(getContext(), offset);
        char[] digits = utils.preferredDigits(getActivity());


        PersianDate persianDate = Utils.getToday();
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

        TextView currentMonthTextView = (TextView) view.findViewById(R.id.currentMonthTextView);
        currentMonthTextView.setText(utils.getMonthYearTitle(persianDate, digits));

        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MonthAdapter(getActivity(), this, days);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClickItem(View v, int position) {

    }
}
