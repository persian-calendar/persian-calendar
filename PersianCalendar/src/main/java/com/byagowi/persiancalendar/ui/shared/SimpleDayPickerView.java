package com.byagowi.persiancalendar.ui.shared;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.databinding.SimpleDayPickerViewBinding;
import com.byagowi.persiancalendar.entities.CalendarTypeItem;
import com.byagowi.persiancalendar.entities.StringWithValueItem;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;

public class SimpleDayPickerView extends FrameLayout implements AdapterView.OnItemSelectedListener,
        DayPickerView {
    SimpleDayPickerViewBinding binding;
    private long jdn = -1;
    private OnSelectedDayChangedListener selectedDayListener = jdn -> {
    };

    public SimpleDayPickerView(Context context) {
        super(context);
        init(context);
    }

    public SimpleDayPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimpleDayPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.simple_day_picker_view, this, true);

        binding.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Utils.getOrderedCalendarEntities(getContext())));

        binding.calendarTypeSpinner.setSelection(0);
        binding.calendarTypeSpinner.setOnItemSelectedListener(this);

        binding.yearSpinner.setOnItemSelectedListener(this);
        binding.monthSpinner.setOnItemSelectedListener(this);
        binding.daySpinner.setOnItemSelectedListener(this);
    }

    public long getDayJdnFromView() {
        int year = ((StringWithValueItem) binding.yearSpinner.getSelectedItem()).getValue();
        int month = ((StringWithValueItem) binding.monthSpinner.getSelectedItem()).getValue();
        int day = ((StringWithValueItem) binding.daySpinner.getSelectedItem()).getValue();

        try {
            CalendarType selectedCalendarType = getSelectedCalendarType();
            if (day > Utils.getMonthLength(selectedCalendarType, year, month))
                throw new Exception("Not a valid day");

            return Utils.getDateOfCalendar(selectedCalendarType, year, month, day).toJdn();
        } catch (Exception e) {
            Utils.createAndShowShortSnackbar(getRootView(), R.string.date_exception);
            Log.e("SelectDayDialog", "", e);
        }

        return -1;
    }

    public void setDayJdnOnView(long jdn) {
        this.jdn = jdn;

        Context context = getContext();
        if (context == null) return;

        if (jdn == -1) {
            jdn = Utils.getTodayJdn();
        }

        AbstractDate date = Utils.getDateFromJdnOfCalendar(
                getSelectedCalendarType(),
                jdn);

        // years spinner init.
        List<StringWithValueItem> years = new ArrayList<>();
        final int YEARS = 200;
        int startingYearOnYearSpinner = date.getYear() - YEARS / 2;
        for (int i = 0; i < YEARS; ++i) {
            years.add(new StringWithValueItem(i + startingYearOnYearSpinner,
                    Utils.formatNumber(i + startingYearOnYearSpinner)));
        }
        binding.yearSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, years));
        binding.yearSpinner.setSelection(YEARS / 2);
        //

        // month spinner init.
        List<StringWithValueItem> months = new ArrayList<>();
        String[] monthsTitle = Utils.monthsNamesOfCalendar(date);
        for (int i = 1; i <= 12; ++i) {
            months.add(new StringWithValueItem(i,
                    monthsTitle[i - 1] + " / " + Utils.formatNumber(i)));
        }
        binding.monthSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, months));
        binding.monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        List<StringWithValueItem> days = new ArrayList<>();
        for (int i = 1; i <= 31; ++i) {
            days.add(new StringWithValueItem(i, Utils.formatNumber(i)));
        }
        binding.daySpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, days));
        binding.daySpinner.setSelection(date.getDayOfMonth() - 1);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.calendarTypeSpinner) setDayJdnOnView(jdn);
        else jdn = getDayJdnFromView();

        selectedDayListener.onSelectedDayChanged(jdn);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public CalendarType getSelectedCalendarType() {
        return ((CalendarTypeItem) binding.calendarTypeSpinner.getSelectedItem()).getType();
    }

    @Override
    public void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener) {
        selectedDayListener = listener;
    }
}
