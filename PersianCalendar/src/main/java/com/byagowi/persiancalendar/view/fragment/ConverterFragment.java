package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding;
import com.byagowi.persiancalendar.entity.CalendarTypeEntity;
import com.byagowi.persiancalendar.entity.FormattedIntEntity;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import calendar.CalendarType;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class ConverterFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentConverterBinding binding;
    private long lastSelectedJdn = -1;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_converter, container,
                false);

        binding.calendarsView.expand(true);
        binding.calendarsView.hideMoreIcon();
        binding.calendarsView.setOnTodayClicked(() -> {
            lastSelectedJdn = -1;
            UIUtils.fillSelectDaySpinners(getContext(), binding.selectdayFragment, lastSelectedJdn);
        });

        // fill views
        binding.selectdayFragment.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Utils.getOrderedCalendarEntities(getContext())));

        binding.selectdayFragment.calendarTypeSpinner.setSelection(0);
        UIUtils.fillSelectDaySpinners(getContext(), binding.selectdayFragment, lastSelectedJdn);

        binding.selectdayFragment.calendarTypeSpinner.setOnItemSelectedListener(this);

        binding.selectdayFragment.yearSpinner.setOnItemSelectedListener(this);
        binding.selectdayFragment.monthSpinner.setOnItemSelectedListener(this);
        binding.selectdayFragment.daySpinner.setOnItemSelectedListener(this);

        return binding.getRoot();
    }

    private void fillCalendarInfo() {
        int year = ((FormattedIntEntity)
                binding.selectdayFragment.yearSpinner.getSelectedItem()).getValue();
        int month = ((FormattedIntEntity)
                binding.selectdayFragment.monthSpinner.getSelectedItem()).getValue();
        int day = ((FormattedIntEntity)
                binding.selectdayFragment.daySpinner.getSelectedItem()).getValue();

        try {
            CalendarType calendarType = ((CalendarTypeEntity)
                    binding.selectdayFragment.calendarTypeSpinner.getSelectedItem()).getType();
            if (day > CalendarUtils.getMonthLength(calendarType, year, month)) {
                binding.calendarsView.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.date_exception), Toast.LENGTH_SHORT).show();
            } else {
                long jdn = CalendarUtils.getJdnOfCalendar(calendarType, year, month, day);
                List<CalendarType> orderedCalendarTypes = Utils.getOrderedCalendarTypes();
                orderedCalendarTypes.remove(calendarType);

                binding.calendarsView.fillCalendarsCard(jdn, calendarType, orderedCalendarTypes);
                lastSelectedJdn = jdn;
//                if (CalendarUtils.getTodayJdn() == jdn) {
//                    binding.calendarsTabContent.diffDateContainer.setVisibility(View.VISIBLE);
//                }

                binding.calendarsView.setVisibility(View.VISIBLE);
            }
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.yearSpinner:
            case R.id.monthSpinner:
            case R.id.daySpinner:
                fillCalendarInfo();
                break;

            case R.id.calendarTypeSpinner:
                UIUtils.fillSelectDaySpinners(getContext(), binding.selectdayFragment,
                        lastSelectedJdn);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
