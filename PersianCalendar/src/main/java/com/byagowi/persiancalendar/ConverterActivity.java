package com.byagowi.persiancalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.byagowi.common.Range;

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
public class ConverterActivity extends Activity {
    private final Utils utils = Utils.getInstance();
    private final int yearDiffRange = 200;
    private Spinner calendarTypeSpinner;
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private Spinner daySpinner;
    private TextView convertedDateTextView;
    private int startingYearOnYearSpinner = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        utils.setTheme(this);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.calendar_converter);

        // fill members
        calendarTypeSpinner = (Spinner) findViewById(R.id.calendarTypeSpinner);
        yearSpinner = (Spinner) findViewById(R.id.yearSpinner);
        monthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        daySpinner = (Spinner) findViewById(R.id.daySpinner);
        convertedDateTextView = (TextView) findViewById(R.id.convertedDateTextView);

        TextView labelDay = (TextView) findViewById(R.id.converterLabelDay);
        TextView labelMonth = (TextView) findViewById(R.id.converterLabelMonth);
        TextView labelYear = (TextView) findViewById(R.id.converterLabelYear);
        labelDay.setText(Utils.textShaper(getString(R.string.day)));
        labelMonth.setText(Utils.textShaper(getString(R.string.month)));
        labelYear.setText(Utils.textShaper(getString(R.string.year)));

        //

        // fill views
        calendarTypeSpinner.setAdapter(new CalendarTypesSpinnerAdapter(this, android.R.layout.select_dialog_item));
        calendarTypeSpinner.setSelection(0);

        fillYearMonthDaySpinners();

        calendarTypeSpinner.setOnItemSelectedListener(new CalendarTypeSpinnerListener());

        CalendarSpinnersListener csl = new CalendarSpinnersListener();
        yearSpinner.setOnItemSelectedListener(csl);
        monthSpinner.setOnItemSelectedListener(csl);
        daySpinner.setOnItemSelectedListener(csl);
        //
    }

    void fillCalendarInfo() {
        int year = startingYearOnYearSpinner
                + yearSpinner.getSelectedItemPosition();
        int month = monthSpinner.getSelectedItemPosition() + 1;
        int day = daySpinner.getSelectedItemPosition() + 1;
        CalendarType calendarType = (CalendarType) calendarTypeSpinner.getSelectedItem();

        CivilDate civilDate = null;
        PersianDate persianDate = null;
        IslamicDate islamicDate = null;

        char[] digits = utils.preferredDigits(this);
        StringBuilder sb = new StringBuilder();

        try {
            List<String> calendarsTextList = new ArrayList<String>();
            switch (calendarType) {
                case GEORGIAN:
                    civilDate = new CivilDate(year, month, day);
                    islamicDate = DateConverter.civilToIslamic(civilDate);
                    persianDate = DateConverter.civilToPersian(civilDate);

                    calendarsTextList.add(Utils.dateToString(civilDate, digits));
                    calendarsTextList.add(Utils.dateToString(persianDate, digits));
                    calendarsTextList.add(Utils.dateToString(islamicDate, digits));
                    break;
                case ISLAMIC:
                    islamicDate = new IslamicDate(year, month, day);
                    civilDate = DateConverter.islamicToCivil(islamicDate);
                    persianDate = DateConverter.islamicToPersian(islamicDate);

                    calendarsTextList.add(Utils.dateToString(islamicDate, digits));
                    calendarsTextList.add(Utils.dateToString(civilDate, digits));
                    calendarsTextList.add(Utils.dateToString(persianDate, digits));
                    break;
                case SHAMSI:
                    persianDate = new PersianDate(year, month, day);
                    civilDate = DateConverter.persianToCivil(persianDate);
                    islamicDate = DateConverter.persianToIslamic(persianDate);

                    calendarsTextList.add(Utils.dateToString(persianDate, digits));
                    calendarsTextList.add(Utils.dateToString(civilDate, digits));
                    calendarsTextList.add(Utils.dateToString(islamicDate, digits));
                    break;
            }

            sb.append(civilDate.getDayOfWeekName());
            sb.append(Utils.PERSIAN_COMMA);
            sb.append(" ");
            sb.append(calendarsTextList.get(0));
            sb.append("\n\n");
            sb.append(getString(R.string.equals_with));
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
        char[] digits = utils.preferredDigits(this);

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
        List<String> yearsList = new ArrayList<String>();
        startingYearOnYearSpinner = date.getYear() - yearDiffRange / 2;
        for (int i : new Range(startingYearOnYearSpinner, yearDiffRange)) {
            yearsList.add(Utils.formatNumber(i, digits));
        }
        ArrayAdapter<String> yearArrayAdaptor = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, yearsList);
        yearSpinner.setAdapter(yearArrayAdaptor);

        yearSpinner.setSelection(yearDiffRange / 2);
        //

        // month spinner init.
        List<String> monthsList = new ArrayList<String>();
        String[] monthsArray = date.getMonthsList();
        for (int i : new Range(1, 12)) {
            String monthName = selectedCalendarType == CalendarType.ISLAMIC ? monthsArray[i] : Utils.getCalendarItemName(monthsArray[i]);
            monthsList.add(Utils.textShaper(monthName + " / "
                    + Utils.formatNumber(i, digits)));
        }
        ArrayAdapter<String> monthArrayAdaptor = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, monthsList);
        monthSpinner.setAdapter(monthArrayAdaptor);

        monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        List<String> daysList = new ArrayList<String>();
        for (int i : new Range(1, 31)) {
            daysList.add(Utils.formatNumber(i, digits));
        }
        ArrayAdapter<String> dayArrayAdaptor = new ArrayAdapter<String>(this,
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
