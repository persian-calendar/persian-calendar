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

        Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

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

        binding.selectdayFragment.calendarTypeSpinner.setSelection(Utils.positionFromCalendarType(Utils.getMainCalendar()));
        startingYearOnYearSpinner = Utils.fillSelectdaySpinners(getContext(), binding.selectdayFragment);

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

        CivilDate civilDate = null;
        PersianDate persianDate;
        IslamicDate hijriDate;

        try {
            binding.calendarsCard.shamsiContainer.setVisibility(View.VISIBLE);
            binding.calendarsCard.gregorianContainer.setVisibility(View.VISIBLE);
            binding.calendarsCard.islamicContainer.setVisibility(View.VISIBLE);

            switch (Utils.calendarTypeFromPosition(binding.selectdayFragment.calendarTypeSpinner.getSelectedItemPosition())) {
                case GREGORIAN:
                    civilDate = new CivilDate(year, month, day);
                    hijriDate = DateConverter.civilToIslamic(civilDate, 0);
                    persianDate = DateConverter.civilToPersian(civilDate);
                    binding.calendarsCard.gregorianContainer.setVisibility(View.GONE);
                    break;

                case ISLAMIC:
                    hijriDate = new IslamicDate(year, month, day);
                    civilDate = DateConverter.islamicToCivil(hijriDate);
                    persianDate = DateConverter.islamicToPersian(hijriDate);
                    binding.calendarsCard.islamicContainer.setVisibility(View.GONE);
                    break;

                case SHAMSI:
                default:
                    persianDate = new PersianDate(year, month, day);
                    civilDate = DateConverter.persianToCivil(persianDate);
                    hijriDate = DateConverter.persianToIslamic(persianDate);
                    binding.calendarsCard.shamsiContainer.setVisibility(View.GONE);
                    break;
            }


            binding.calendarsCard.weekDayName.setText(Utils.getWeekDayName(persianDate));

            binding.calendarsCard.shamsiDateLinear.setText(Utils.toLinearDate(persianDate));
            binding.calendarsCard.shamsiDateDay.setText(Utils.formatNumber(persianDate.getDayOfMonth()));
            binding.calendarsCard.shamsiDate.setText(Utils.getMonthName(persianDate) + "\n" + Utils.formatNumber(persianDate.getYear()));

            binding.calendarsCard.gregorianDateLinear.setText(Utils.toLinearDate(civilDate));
            binding.calendarsCard.gregorianDateDay.setText(Utils.formatNumber(civilDate.getDayOfMonth()));
            binding.calendarsCard.gregorianDate.setText(Utils.getMonthName(civilDate) + "\n" + Utils.formatNumber(civilDate.getYear()));

            binding.calendarsCard.islamicDateLinear.setText(Utils.toLinearDate(hijriDate));
            binding.calendarsCard.islamicDateDay.setText(Utils.formatNumber(hijriDate.getDayOfMonth()));
            binding.calendarsCard.islamicDate.setText(Utils.getMonthName(hijriDate) + "\n" + Utils.formatNumber(hijriDate.getYear()));

            binding.calendarsCard.calendarsCard.setVisibility(View.VISIBLE);

            PersianDate today = Utils.getToday();
            long diffDays = Math.abs(DateConverter.persianToJdn(today) - DateConverter.persianToJdn(persianDate));
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
                startingYearOnYearSpinner = Utils.fillSelectdaySpinners(getContext(), binding.selectdayFragment);
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
                Utils.copyToClipboard(getContext(), binding.calendarsCard.shamsiDateDay.getText() + " " +
                        binding.calendarsCard.shamsiDate.getText().toString().replace("\n", " "));
                break;

            case R.id.shamsi_date_linear:
                Utils.copyToClipboard(getContext(), binding.calendarsCard.shamsiDateLinear.getText());
                break;

            case R.id.gregorian_date:
            case R.id.gregorian_date_day:
                Utils.copyToClipboard(getContext(), binding.calendarsCard.gregorianDateDay.getText() + " " +
                        binding.calendarsCard.gregorianDate.getText().toString().replace("\n", " "));
                break;

            case R.id.gregorian_date_linear:
                Utils.copyToClipboard(getContext(), binding.calendarsCard.gregorianDateLinear.getText());
                break;

            case R.id.islamic_date:
            case R.id.islamic_date_day:
                Utils.copyToClipboard(getContext(), binding.calendarsCard.islamicDateDay.getText() + " " +
                        binding.calendarsCard.islamicDate.getText().toString().replace("\n", " "));
                break;

            case R.id.islamic_date_linear:
                Utils.copyToClipboard(getContext(), binding.calendarsCard.islamicDateLinear.getText());
                break;

            case R.id.today:
            case R.id.today_icon:
                startingYearOnYearSpinner = Utils.fillSelectdaySpinners(getContext(), binding.selectdayFragment);
                break;
        }
    }
}
