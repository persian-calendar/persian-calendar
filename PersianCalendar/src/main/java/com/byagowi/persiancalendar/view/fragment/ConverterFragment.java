package com.byagowi.persiancalendar.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

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

    private Spinner calendarTypeSpinner;
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private Spinner daySpinner;
    private int startingYearOnYearSpinner = 0;
    private TextView weekDayName;
    private TextView shamsiDateDay;
    private TextView shamsiDate;
    private TextView gregorianDateDay;
    private TextView gregorianDate;
    private TextView islamicDateDay;
    private TextView islamicDate;
    private CardView calendars_card;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

        View view = inflater.inflate(R.layout.fragment_converter, container, false);

        AppCompatImageView iv = view.findViewById(R.id.calendars_card_icon);
        iv.setImageResource(R.drawable.ic_swap_vertical_circle);

        // fill members
        calendarTypeSpinner = view.findViewById(R.id.calendarTypeSpinner);
        yearSpinner = view.findViewById(R.id.yearSpinner);
        monthSpinner = view.findViewById(R.id.monthSpinner);
        daySpinner = view.findViewById(R.id.daySpinner);

        weekDayName = view.findViewById(R.id.week_day_name);

        shamsiDateDay = view.findViewById(R.id.shamsi_date_day);
        shamsiDate = view.findViewById(R.id.shamsi_date);
        gregorianDateDay = view.findViewById(R.id.gregorian_date_day);
        gregorianDate = view.findViewById(R.id.gregorian_date);
        islamicDateDay = view.findViewById(R.id.islamic_date_day);
        islamicDate = view.findViewById(R.id.islamic_date);

        calendars_card = view.findViewById(R.id.calendars_card);

        // fill views
        calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.calendar_type)));
        calendarTypeSpinner.setSelection(0);

        startingYearOnYearSpinner = Utils.fillYearMonthDaySpinners(getContext(),
                calendarTypeSpinner, yearSpinner, monthSpinner, daySpinner);

        calendarTypeSpinner.setOnItemSelectedListener(this);

        yearSpinner.setOnItemSelectedListener(this);
        monthSpinner.setOnItemSelectedListener(this);
        daySpinner.setOnItemSelectedListener(this);
        //
        return view;
    }

    private void fillCalendarInfo() {
        int year = startingYearOnYearSpinner + yearSpinner.getSelectedItemPosition();
        int month = monthSpinner.getSelectedItemPosition() + 1;
        int day = daySpinner.getSelectedItemPosition() + 1;

        CivilDate civilDate = null;
        PersianDate persianDate;
        IslamicDate hijriDate;

        try {
            switch (Utils.calendarTypeFromPosition(calendarTypeSpinner.getSelectedItemPosition())) {
                case GREGORIAN:
                    civilDate = new CivilDate(year, month, day);
                    hijriDate = DateConverter.civilToIslamic(civilDate, 0);
                    persianDate = DateConverter.civilToPersian(civilDate);
                    break;

                case ISLAMIC:
                    hijriDate = new IslamicDate(year, month, day);
                    civilDate = DateConverter.islamicToCivil(hijriDate);
                    persianDate = DateConverter.islamicToPersian(hijriDate);
                    break;

                case SHAMSI:
                default:
                    persianDate = new PersianDate(year, month, day);
                    civilDate = DateConverter.persianToCivil(persianDate);
                    hijriDate = DateConverter.persianToIslamic(persianDate);
                    break;
            }

            weekDayName.setText(Utils.getWeekDayName(persianDate));

            shamsiDateDay.setText(Utils.formatNumber(persianDate.getDayOfMonth()));
            shamsiDate.setText(Utils.getMonthName(persianDate) + "\n" + Utils.formatNumber(persianDate.getYear()));

            gregorianDateDay.setText(Utils.formatNumber(civilDate.getDayOfMonth()));
            gregorianDate.setText(Utils.getMonthName(civilDate) + "\n" + Utils.formatNumber(civilDate.getYear()));

            islamicDateDay.setText(Utils.formatNumber(hijriDate.getDayOfMonth()));
            islamicDate.setText(Utils.getMonthName(hijriDate) + "\n" + Utils.formatNumber(hijriDate.getYear()));

            calendars_card.setVisibility(View.VISIBLE);

        } catch (RuntimeException e) {
            calendars_card.setVisibility(View.GONE);
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
                startingYearOnYearSpinner = Utils.fillYearMonthDaySpinners(getContext(),
                        calendarTypeSpinner, yearSpinner, monthSpinner, daySpinner);
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
                Utils.copyToClipboard(getContext(), shamsiDateDay.getText() + " " +
                        shamsiDate.getText().toString().replace("\n", " "));
                break;

            case R.id.gregorian_date:
            case R.id.gregorian_date_day:
                Utils.copyToClipboard(getContext(), gregorianDateDay.getText() + " " +
                        gregorianDate.getText().toString().replace("\n", " "));
                break;

            case R.id.islamic_date:
            case R.id.islamic_date_day:
                Utils.copyToClipboard(getContext(), islamicDateDay.getText() + " " +
                        islamicDate.getText().toString().replace("\n", " "));
                break;
        }
    }
}
