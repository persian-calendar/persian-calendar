package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.daypickerview.DayPickerView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import calendar.CalendarType;

public class ConverterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

        FragmentConverterBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_converter, container, false);
        DayPickerView dayPickerView = binding.dayPickerView;

        binding.calendarsView.expand(true);
        binding.calendarsView.hideMoreIcon();
        binding.calendarsView.setOnTodayButtonClickListener(
                () -> dayPickerView.setDayJdnOnView(CalendarUtils.getTodayJdn()));

        dayPickerView.setOnSelectedDayChangedListener(jdn -> {
            if (jdn == -1) {
                binding.calendarsView.setVisibility(View.GONE);
            } else {
                binding.calendarsView.setVisibility(View.VISIBLE);
                CalendarType selectedCalendarType = dayPickerView.getSelectedCalendarType();
                List<CalendarType> orderedCalendarTypes = Utils.getOrderedCalendarTypes();
                orderedCalendarTypes.remove(selectedCalendarType);
                binding.calendarsView.showCalendars(jdn, selectedCalendarType, orderedCalendarTypes);
            }
        });
        dayPickerView.setDayJdnOnView(CalendarUtils.getTodayJdn());

        return binding.getRoot();
    }
}
