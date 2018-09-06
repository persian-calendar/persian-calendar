package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.SelectDayViewBinding;
import com.byagowi.persiancalendar.entity.CalendarTypeEntity;
import com.byagowi.persiancalendar.entity.FormattedIntEntity;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.DateConverter;

public class SelectDayView extends FrameLayout implements AdapterView.OnItemSelectedListener {
    SelectDayViewBinding binding;

    public SelectDayView(Context context) {
        super(context);
        init();
    }

    public SelectDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private long jdn = -1;

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.select_day_view, this,
                true);

        binding.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Utils.getOrderedCalendarEntities(getContext())));

        binding.calendarTypeSpinner.setSelection(0);
        binding.calendarTypeSpinner.setOnItemSelectedListener(this);

        binding.yearSpinner.setOnItemSelectedListener(this);
        binding.monthSpinner.setOnItemSelectedListener(this);
        binding.daySpinner.setOnItemSelectedListener(this);
    }

    public long getJdnFromView() {
        int year = ((FormattedIntEntity) binding.yearSpinner.getSelectedItem()).getValue();
        int month = ((FormattedIntEntity) binding.monthSpinner.getSelectedItem()).getValue();
        int day = ((FormattedIntEntity) binding.daySpinner.getSelectedItem()).getValue();

        try {
            switch (getSelectedCalendarType()) {
                case GREGORIAN:
                    return DateConverter.civilToJdn(year, month, day);

                case ISLAMIC:
                    return DateConverter.islamicToJdn(year, month, day);

                case SHAMSI:
                    return DateConverter.persianToJdn(year, month, day);
            }
        } catch (RuntimeException e) {
            Toast.makeText(getContext(), getContext().getString(R.string.date_exception),
                    Toast.LENGTH_SHORT).show();
            Log.e("SelectDayDialog", "", e);
        }

        return -1;
    }

    public void setJdnOnView(long jdn) {
        this.jdn = jdn;

        Context context = getContext();
        if (context == null) return;

        if (jdn == -1) {
            jdn = CalendarUtils.getTodayJdn();
        }

        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(
                getSelectedCalendarType(),
                jdn);

        // years spinner init.
        List<FormattedIntEntity> years = new ArrayList<>();
        final int YEARS = 200;
        int startingYearOnYearSpinner = date.getYear() - YEARS / 2;
        for (int i = 0; i < YEARS; ++i) {
            years.add(new FormattedIntEntity(i + startingYearOnYearSpinner,
                    Utils.formatNumber(i + startingYearOnYearSpinner)));
        }
        binding.yearSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, years));
        binding.yearSpinner.setSelection(YEARS / 2);
        //

        // month spinner init.
        List<FormattedIntEntity> months = new ArrayList<>();
        String[] monthsTitle = Utils.monthsNamesOfCalendar(date);
        for (int i = 1; i <= 12; ++i) {
            months.add(new FormattedIntEntity(i,
                    monthsTitle[i - 1] + " / " + Utils.formatNumber(i)));
        }
        binding.monthSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, months));
        binding.monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        List<FormattedIntEntity> days = new ArrayList<>();
        for (int i = 1; i <= 31; ++i) {
            days.add(new FormattedIntEntity(i, Utils.formatNumber(i)));
        }
        binding.daySpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, days));
        binding.daySpinner.setSelection(date.getDayOfMonth() - 1);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.calendarTypeSpinner) setJdnOnView(jdn);
        else jdn = getJdnFromView();

        selectedDayListener.onSelectedDayChanged(jdn);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public CalendarType getSelectedCalendarType() {
        return ((CalendarTypeEntity) binding.calendarTypeSpinner.getSelectedItem()).getType();
    }

    public interface OnSelectedDayChangedListener {
        void onSelectedDayChanged(long jdn);
    }

    private OnSelectedDayChangedListener selectedDayListener = jdn -> {
    };

    public void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener) {
        selectedDayListener = listener;
    }
}
