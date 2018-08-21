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

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import calendar.CalendarType;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class ConverterFragment extends Fragment implements
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    private FragmentConverterBinding binding;
    private long lastSelectedJdn = -1;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_converter, container,
                false);

        // Always on
        binding.calendarsTabContent.moreCalendar.setVisibility(View.GONE);

        binding.calendarsTabContent.today.setVisibility(View.GONE);
        binding.calendarsTabContent.todayIcon.setVisibility(View.GONE);
        binding.calendarsTabContent.today.setOnClickListener(this);
        binding.calendarsTabContent.todayIcon.setOnClickListener(this);

        binding.calendarsTabContent.firstCalendarDateLinear.setOnClickListener(this);
        binding.calendarsTabContent.firstCalendarDateDay.setOnClickListener(this);
        binding.calendarsTabContent.firstCalendarDate.setOnClickListener(this);
        binding.calendarsTabContent.secondCalendarDateLinear.setOnClickListener(this);
        binding.calendarsTabContent.secondCalendarDateDay.setOnClickListener(this);
        binding.calendarsTabContent.secondCalendarDate.setOnClickListener(this);
        binding.calendarsTabContent.thirdCalendarDateLinear.setOnClickListener(this);
        binding.calendarsTabContent.thirdCalendarDateDay.setOnClickListener(this);
        binding.calendarsTabContent.thirdCalendarDate.setOnClickListener(this);

        // fill views
        binding.selectdayFragment.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Utils.getOrderedCalendarEntities(getContext())));

        binding.selectdayFragment.calendarTypeSpinner.setSelection(0);
        UIUtils.fillSelectdaySpinners(getContext(), binding.selectdayFragment, lastSelectedJdn);

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
            binding.calendarsTabContent.firstCalendarContainer.setVisibility(View.GONE);
            binding.calendarsTabContent.secondCalendarContainer.setVisibility(View.VISIBLE);
            binding.calendarsTabContent.thirdCalendarContainer.setVisibility(View.VISIBLE);

            CalendarType calendarType = ((CalendarTypeEntity)
                    binding.selectdayFragment.calendarTypeSpinner.getSelectedItem()).getType();
            long jdn = CalendarUtils.getJdnOfCalendarWithException(calendarType, year, month, day);

            UIUtils.fillCalendarsCard(getContext(), jdn, binding.calendarsTabContent, calendarType,
                    Utils.getOrderedCalendarTypes());
            lastSelectedJdn = jdn;
            if (CalendarUtils.getTodayJdn() == jdn) {
                binding.calendarsTabContent.diffDateContainer.setVisibility(View.VISIBLE);
            }

            binding.calendarsTabContent.getRoot().setVisibility(View.VISIBLE);

        } catch (RuntimeException e) {
            binding.calendarsTabContent.getRoot().setVisibility(View.GONE);
            Toast.makeText(getContext(), getString(R.string.date_exception), Toast.LENGTH_SHORT).show();
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
                UIUtils.fillSelectdaySpinners(getContext(), binding.selectdayFragment,
                        lastSelectedJdn);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.first_calendar_date:
            case R.id.first_calendar_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsTabContent.firstCalendarDateDay.getText() + " " +
                        binding.calendarsTabContent.firstCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.first_calendar_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsTabContent.firstCalendarDateLinear.getText());
                break;

            case R.id.second_calendar_date:
            case R.id.second_calendar_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsTabContent.secondCalendarDateDay.getText() + " " +
                        binding.calendarsTabContent.secondCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.second_calendar_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsTabContent.secondCalendarDateLinear.getText());
                break;

            case R.id.third_calendar_date:
            case R.id.third_calendar_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsTabContent.thirdCalendarDateDay.getText() + " " +
                        binding.calendarsTabContent.thirdCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.third_calendar_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsTabContent.thirdCalendarDateLinear.getText());
                break;

            case R.id.today:
            case R.id.today_icon:
                lastSelectedJdn = -1;
                UIUtils.fillSelectdaySpinners(getContext(),
                        binding.selectdayFragment, lastSelectedJdn);
                break;
        }
    }
}
