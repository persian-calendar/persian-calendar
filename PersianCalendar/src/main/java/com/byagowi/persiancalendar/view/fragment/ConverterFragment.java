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
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class ConverterFragment extends Fragment implements
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    private int startingYearOnYearSpinner = 0;

    private FragmentConverterBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_converter, container,
                false);

        binding.calendarsCard.calendarsCardIcon.setImageResource(R.drawable.ic_swap_vertical_circle);

        binding.calendarsCard.today.setVisibility(View.GONE);
        binding.calendarsCard.todayIcon.setVisibility(View.GONE);
        binding.calendarsCard.today.setOnClickListener(this);
        binding.calendarsCard.todayIcon.setOnClickListener(this);

        // Hide the button, we don't need it here
        binding.calendarsCard.moreCalendar.setVisibility(View.GONE);

        binding.calendarsCard.shamsiDateLinear.setOnClickListener(this);
        binding.calendarsCard.shamsiDateDay.setOnClickListener(this);
        binding.calendarsCard.shamsiDate.setOnClickListener(this);
        binding.calendarsCard.gregorianDateLinear.setOnClickListener(this);
        binding.calendarsCard.gregorianDateDay.setOnClickListener(this);
        binding.calendarsCard.gregorianDate.setOnClickListener(this);
        binding.calendarsCard.islamicDateLinear.setOnClickListener(this);
        binding.calendarsCard.islamicDateDay.setOnClickListener(this);
        binding.calendarsCard.islamicDate.setOnClickListener(this);

        // fill views
        binding.selectdayFragment.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.calendar_type)));

        binding.selectdayFragment.calendarTypeSpinner.setSelection(CalendarUtils.positionFromCalendarType(Utils.getMainCalendar()));
        startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(getContext(), binding.selectdayFragment);

        binding.selectdayFragment.calendarTypeSpinner.setOnItemSelectedListener(this);

        binding.selectdayFragment.yearSpinner.setOnItemSelectedListener(this);
        binding.selectdayFragment.monthSpinner.setOnItemSelectedListener(this);
        binding.selectdayFragment.daySpinner.setOnItemSelectedListener(this);

        return binding.getRoot();
    }

    private void fillCalendarInfo() {
        int year = startingYearOnYearSpinner + binding.selectdayFragment.yearSpinner.getSelectedItemPosition();
        int month = binding.selectdayFragment.monthSpinner.getSelectedItemPosition() + 1;
        int day = binding.selectdayFragment.daySpinner.getSelectedItemPosition() + 1;

        long jdn;

        try {
            binding.calendarsCard.shamsiContainer.setVisibility(View.VISIBLE);
            binding.calendarsCard.gregorianContainer.setVisibility(View.VISIBLE);
            binding.calendarsCard.islamicContainer.setVisibility(View.VISIBLE);

            switch (CalendarUtils.calendarTypeFromPosition(binding.selectdayFragment.calendarTypeSpinner.getSelectedItemPosition())) {
                case GREGORIAN:
                    jdn = DateConverter.civilToJdn(new CivilDate(year, month, day));
                    binding.calendarsCard.gregorianContainer.setVisibility(View.GONE);
                    break;

                case ISLAMIC:
                    jdn = DateConverter.islamicToJdn(new IslamicDate(year, month, day));
                    binding.calendarsCard.islamicContainer.setVisibility(View.GONE);
                    break;

                case SHAMSI:
                default:
                    jdn = DateConverter.persianToJdn(new PersianDate(year, month, day));
                    binding.calendarsCard.shamsiContainer.setVisibility(View.GONE);
                    break;
            }

            boolean isToday = CalendarUtils.getTodayJdn() == jdn;
            UIUtils.fillCalendarsCard(getContext(), jdn, binding.calendarsCard, isToday);

            binding.calendarsCard.calendarsCard.setVisibility(View.VISIBLE);

            long diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn);
            CivilDate civilBase = new CivilDate(2000, 1, 1);
            CivilDate civilOffset = DateConverter.jdnToCivil(diffDays + DateConverter.civilToJdn(civilBase));
            int yearDiff = civilOffset.getYear() - 2000;
            int monthDiff = civilOffset.getMonth() - 1;
            int dayOfMonthDiff = civilOffset.getDayOfMonth() - 1;
            binding.calendarsCard.diffDate.setText(String.format(getString(R.string.date_diff_text),
                    Utils.formatNumber((int) diffDays),
                    Utils.formatNumber(yearDiff),
                    Utils.formatNumber(monthDiff),
                    Utils.formatNumber(dayOfMonthDiff)));
            binding.calendarsCard.diffDate.setVisibility(diffDays == 0 ? View.GONE : View.VISIBLE);
            binding.calendarsCard.today.setVisibility(diffDays == 0 ? View.GONE : View.VISIBLE);
            binding.calendarsCard.todayIcon.setVisibility(diffDays == 0 ? View.GONE : View.VISIBLE);

        } catch (RuntimeException e) {
            binding.calendarsCard.calendarsCard.setVisibility(View.GONE);
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
                startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(getContext(), binding.selectdayFragment);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.shamsi_date:
            case R.id.shamsi_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.shamsiDateDay.getText() + " " +
                        binding.calendarsCard.shamsiDate.getText().toString().replace("\n", " "));
                break;

            case R.id.shamsi_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.shamsiDateLinear.getText());
                break;

            case R.id.gregorian_date:
            case R.id.gregorian_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.gregorianDateDay.getText() + " " +
                        binding.calendarsCard.gregorianDate.getText().toString().replace("\n", " "));
                break;

            case R.id.gregorian_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.gregorianDateLinear.getText());
                break;

            case R.id.islamic_date:
            case R.id.islamic_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.islamicDateDay.getText() + " " +
                        binding.calendarsCard.islamicDate.getText().toString().replace("\n", " "));
                break;

            case R.id.islamic_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.islamicDateLinear.getText());
                break;

            case R.id.today:
            case R.id.today_icon:
                startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(getContext(), binding.selectdayFragment);
                break;
        }
    }
}
