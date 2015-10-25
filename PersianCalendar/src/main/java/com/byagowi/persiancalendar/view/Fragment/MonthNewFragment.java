package com.byagowi.persiancalendar.view.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.byagowi.common.Range;
import com.byagowi.persiancalendar.Adapter.MonthAdapter;
import com.byagowi.persiancalendar.ClickDayListener;
import com.byagowi.persiancalendar.Entity.Day;
import com.byagowi.persiancalendar.Interface.ClickListener;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.Activity.MainActivity;

import java.util.List;

import calendar.DateConverter;
import calendar.DayOutOfRangeException;
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
