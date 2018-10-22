package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.countdown.CountdownView;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.support.DaggerFragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class CountdownFragment extends DaggerFragment {

    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        MainActivity activity = mainActivityDependency.getMainActivity();
        activity.setTitleAndSubtitle(getString(R.string.countdown), "");

        CountdownView countdownView = view.findViewById(R.id.countdown_shamsi);
        CountdownView countdownView2 = view.findViewById(R.id.countdown_miladi);
        CountdownView countdownView3 = view.findViewById(R.id.countdown_today);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss");
        try {
            countdownView.start(simpleDateFormat.parse("2019-03-21.01-40-00").getTime() - new Date().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            countdownView2.start(simpleDateFormat.parse("2019-01-01.00-00-00").getTime() - new Date().getTime());
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            countdownView3.start(simpleDateFormat.parse("2020-12-12.23-00-00").getTime() - new Date().getTime());
        } catch (Exception e22) {
            e22.printStackTrace();
        }

        AbstractDate today = CalendarUtils.getTodayOfCalendar(Utils.getMainCalendar());

        TextView txt_name_year = view.findViewById(R.id.txt_name_year);
        TextView txt_name_month = view.findViewById(R.id.txt_name_month);
        TextView txt_name_day = view.findViewById(R.id.txt_name_day);
        txt_name_year.setText(com.byagowi.persiancalendar.util.Utils.formatNumber(String.valueOf(today.getYear())));
        txt_name_month.setText(com.byagowi.persiancalendar.util.Utils.formatNumber(CalendarUtils.getMonthName(today)));
        txt_name_day.setText(com.byagowi.persiancalendar.util.Utils.formatNumber(String.valueOf(today.getDayOfMonth())));

        return view;
    }
}