package com.byagowi.persiancalendar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.byagowi.common.Range;
import com.byagowi.persiancalendar.locale.CalendarStrings;

import java.util.ArrayList;
import java.util.List;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class ConverterFragment extends Fragment {
    private final Utils utils = Utils.getInstance();
    private final int yearDiffRange = 200;
    private Spinner calendarTypeSpinner;
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private Spinner daySpinner;
    private TextView convertedDateTextView;
    private int startingYearOnYearSpinner = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_converter, container, false);

//        utils.setTheme(this);
//        utils.loadLanguageFromSettings(this);


        // fill members
        calendarTypeSpinner = (Spinner) view.findViewById(R.id.calendarTypeSpinner);
        yearSpinner = (Spinner) view.findViewById(R.id.yearSpinner);
        monthSpinner = (Spinner) view.findViewById(R.id.monthSpinner);
        daySpinner = (Spinner) view.findViewById(R.id.daySpinner);
        convertedDateTextView = (TextView) view.findViewById(R.id.convertedDateTextView);

        TextView labelDay = (TextView) view.findViewById(R.id.converterLabelDay);
        TextView labelMonth = (TextView) view.findViewById(R.id.converterLabelMonth);
        TextView labelYear = (TextView) view.findViewById(R.id.converterLabelYear);
        labelDay.setText(utils.getString(CalendarStrings.DAY));
        labelMonth.setText(utils.getString(CalendarStrings.MONTH));
        labelYear.setText(utils.getString(CalendarStrings.YEAR));

        //

        // fill views
        calendarTypeSpinner.setAdapter(new CalendarTypesSpinnerAdapter(getContext(), android.R.layout.select_dialog_item));
        calendarTypeSpinner.setSelection(0);

        fillYearMonthDaySpinners();

        calendarTypeSpinner.setOnItemSelectedListener(new CalendarTypeSpinnerListener());

        CalendarSpinnersListener csl = new CalendarSpinnersListener();
        yearSpinner.setOnItemSelectedListener(csl);
        monthSpinner.setOnItemSelectedListener(csl);
        daySpinner.setOnItemSelectedListener(csl);
        //
        return view;
    }

    void fillCalendarInfo() {
        int year = startingYearOnYearSpinner
                + yearSpinner.getSelectedItemPosition();
        int month = monthSpinner.getSelectedItemPosition() + 1;
        int day = daySpinner.getSelectedItemPosition() + 1;
        CalendarType calendarType = (CalendarType) calendarTypeSpinner.getSelectedItem();

        CivilDate civilDate = null;
        PersianDate persianDate;
        IslamicDate islamicDate;

        char[] digits = utils.preferredDigits(getContext());
        StringBuilder sb = new StringBuilder();

        try {
            List<String> calendarsTextList = new ArrayList<>();
            switch (calendarType) {
                case GEORGIAN:
                    civilDate = new CivilDate(year, month, day);
                    islamicDate = DateConverter.civilToIslamic(civilDate);
                    persianDate = DateConverter.civilToPersian(civilDate);

                    calendarsTextList.add(utils.dateToString(civilDate, digits));
                    calendarsTextList.add(utils.dateToString(persianDate, digits));
                    calendarsTextList.add(utils.dateToString(islamicDate, digits));
                    break;
                case ISLAMIC:
                    islamicDate = new IslamicDate(year, month, day);
                    civilDate = DateConverter.islamicToCivil(islamicDate);
                    persianDate = DateConverter.islamicToPersian(islamicDate);

                    calendarsTextList.add(utils.dateToString(islamicDate, digits));
                    calendarsTextList.add(utils.dateToString(civilDate, digits));
                    calendarsTextList.add(utils.dateToString(persianDate, digits));
                    break;
                case SHAMSI:
                    persianDate = new PersianDate(year, month, day);
                    civilDate = DateConverter.persianToCivil(persianDate);
                    islamicDate = DateConverter.persianToIslamic(persianDate);

                    calendarsTextList.add(utils.dateToString(persianDate, digits));
                    calendarsTextList.add(utils.dateToString(civilDate, digits));
                    calendarsTextList.add(utils.dateToString(islamicDate, digits));
                    break;
            }

            sb.append(utils.getWeekDayName(civilDate));
            sb.append(Utils.PERSIAN_COMMA);
            sb.append(" ");
            sb.append(calendarsTextList.get(0));
            sb.append("\n\n");
            sb.append(utils.getString(CalendarStrings.EQUALS_WITH));
            sb.append(":\n");
            sb.append(calendarsTextList.get(1));
            sb.append("\n");
            sb.append(calendarsTextList.get(2));
            sb.append("\n");

            utils.prepareTextView(convertedDateTextView);
            convertedDateTextView.setText(Utils.textShaper(sb.toString()));
        } catch (RuntimeException e) {
            convertedDateTextView.setText("Date you entered was not valid!");
        }
    }

    void fillYearMonthDaySpinners() {
        char[] digits = utils.preferredDigits(getContext());

        AbstractDate date = null;
        PersianDate newDatePersian = Utils.getToday();
        CivilDate newDateCivil = DateConverter.persianToCivil(newDatePersian);
        IslamicDate newDateIslamic = DateConverter.persianToIslamic(newDatePersian);

        CalendarType selectedCalendarType = (CalendarType) calendarTypeSpinner.getSelectedItem();
        switch (selectedCalendarType) {
            case GEORGIAN:
                date = newDateCivil;
                break;
            case ISLAMIC:
                date = newDateIslamic;
                break;
            case SHAMSI:
                date = newDatePersian;
                break;
        }

        // years spinner init.
        List<String> yearsList = new ArrayList<>();
        startingYearOnYearSpinner = date.getYear() - yearDiffRange / 2;
        for (int i : new Range(startingYearOnYearSpinner, yearDiffRange)) {
            yearsList.add(Utils.formatNumber(i, digits));
        }
        ArrayAdapter<String> yearArrayAdaptor = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, yearsList);
        yearSpinner.setAdapter(yearArrayAdaptor);

        yearSpinner.setSelection(yearDiffRange / 2);
        //

        // month spinner init.
        List<String> monthsList = utils.getMonthNameList(date);
        ArrayAdapter<String> monthArrayAdaptor = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, monthsList);
        monthSpinner.setAdapter(monthArrayAdaptor);

        monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        List<String> daysList = new ArrayList<>();
        for (int i : new Range(1, 31)) {
            daysList.add(Utils.formatNumber(i, digits));
        }
        ArrayAdapter<String> dayArrayAdaptor = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, daysList);
        daySpinner.setAdapter(dayArrayAdaptor);

        daySpinner.setSelection(date.getDayOfMonth() - 1);
        //

    }

    // inner classes
    private class CalendarSpinnersListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            fillCalendarInfo();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class CalendarTypeSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            fillYearMonthDaySpinners();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
