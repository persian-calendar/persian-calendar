/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
package com.byagowi.persiancalendar;

import java.util.ArrayList;
import java.util.List;

import com.byagowi.common.Range;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import static com.byagowi.persiancalendar.CalendarUtils.*;

public class CalendarConverterActivity extends Activity {

	Spinner calendarTypeSpinner;
	Spinner yearSpinner;
	Spinner monthSpinner;
	Spinner daySpinner;
	TextView convertedDateTextView;
	int startingYearOnYearSpinner = 0;
	int yearDiffRange = 200;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.calendar_converter);

		// fill members
		calendarTypeSpinner = (Spinner) findViewById(R.id.calendarTypeSpinner);
		yearSpinner = (Spinner) findViewById(R.id.yearSpinner);
		monthSpinner = (Spinner) findViewById(R.id.monthSpinner);
		daySpinner = (Spinner) findViewById(R.id.daySpinner);
		convertedDateTextView = (TextView) findViewById(R.id.convertedDateTextView);
		//

		// fill views
		List<String> calendarsTypes = new ArrayList<String>();
		calendarsTypes.add(georgian);
		calendarsTypes.add(shamsi);
		calendarsTypes.add(islamic);
		ArrayAdapter<String> arrayAdaptor = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, calendarsTypes);
		calendarTypeSpinner.setAdapter(arrayAdaptor);
		calendarTypeSpinner.setSelection(0);

		fillYearMonthDaySpinners();

		calendarTypeSpinner
				.setOnItemSelectedListener(new CalendarTypeSpinnerListener());

		CalendarSpinnersListener csl = new CalendarSpinnersListener();
		yearSpinner.setOnItemSelectedListener(csl);
		monthSpinner.setOnItemSelectedListener(csl);
		daySpinner.setOnItemSelectedListener(csl);
		//
	}

	// inner classes
	class CalendarSpinnersListener implements
			AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			fillCalendarInfo();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class CalendarTypeSpinnerListener implements
			AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			fillYearMonthDaySpinners();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	public void fillCalendarInfo() {
		int year = startingYearOnYearSpinner
				+ yearSpinner.getSelectedItemPosition();
		int month = monthSpinner.getSelectedItemPosition() + 1;
		int day = daySpinner.getSelectedItemPosition() + 1;
		CalendarType calendarType = detectSelectedCalendar();

		CivilDate civilDate = null;
		PersianDate persianDate = null;
		IslamicDate islamicDate = null;

		char[] digits = preferenceDigits(this);
		StringBuilder sb = new StringBuilder();

		try {
			List<String> calendarsTextList = new ArrayList<String>();
			switch (calendarType) {
			case GEORGIAN:
				civilDate = new CivilDate(year, month, day);
				persianDate = DateConverter.civilToPersian(civilDate);
				islamicDate = DateConverter.civilToIslamic(civilDate);
	
				calendarsTextList.add(dateToString(civilDate, digits));
				calendarsTextList.add(dateToString(persianDate, digits));
				calendarsTextList.add(dateToString(islamicDate, digits));
				break;
			case ISLAMIC:
				islamicDate = new IslamicDate(year, month, day);
				civilDate = DateConverter.islamicToCivil(islamicDate);
				persianDate = DateConverter.islamicToPersian(islamicDate);
	
				calendarsTextList.add(dateToString(islamicDate, digits));
				calendarsTextList.add(dateToString(civilDate, digits));
				calendarsTextList.add(dateToString(persianDate, digits));
				break;
			case SHAMSI:
				persianDate = new PersianDate(year, month, day);
				civilDate = DateConverter.persianToCivil(persianDate);
				islamicDate = DateConverter.persianToIslamic(persianDate);
	
				calendarsTextList.add(dateToString(persianDate, digits));
				calendarsTextList.add(dateToString(civilDate, digits));
				calendarsTextList.add(dateToString(islamicDate, digits));
				break;
			}
	
			sb.append(getDayOfWeekName(civilDate.getDayOfWeek()));
			sb.append(PERSIAN_COMMA);
			sb.append(" ");
			sb.append(calendarsTextList.get(0));
			sb.append("\n\n");
			sb.append("برابر با:\n");
			sb.append(calendarsTextList.get(1));
			sb.append("\n");
			sb.append(calendarsTextList.get(2));
			sb.append("\n");
	
			prepareTextView(convertedDateTextView);
			convertedDateTextView.setText(textShaper(sb.toString()));
		} catch (RuntimeException e) {
			convertedDateTextView.setText("Date you entered was not valid!");
		}
	}

	void fillYearMonthDaySpinners() {
		char[] digits = preferenceDigits(this);

		AbstractDate date = null;
		switch (detectSelectedCalendar()) {
		case GEORGIAN:
			date = new CivilDate();
			break;
		case ISLAMIC:
			date = DateConverter.civilToIslamic(new CivilDate());
			break;
		case SHAMSI:
			date = DateConverter.civilToPersian(new CivilDate());
			break;
		}

		// years spinner init.
		List<String> yearsList = new ArrayList<String>();
		startingYearOnYearSpinner = date.getYear() - yearDiffRange / 2;
		for (int i : new Range(startingYearOnYearSpinner, yearDiffRange)) {
			yearsList.add(formatNumber(i, digits));
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
			monthsList.add(textShaper(monthsArray[i]
					+ " / " + formatNumber(i, digits)));
		}
		ArrayAdapter<String> monthArrayAdaptor = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, monthsList);
		monthSpinner.setAdapter(monthArrayAdaptor);

		monthSpinner.setSelection(date.getMonth() - 1);
		//

		// days spinner init.
		List<String> daysList = new ArrayList<String>();
		for (int i : new Range(1, 31)) {
			daysList.add(formatNumber(i, digits));
		}
		ArrayAdapter<String> dayArrayAdaptor = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, daysList);
		daySpinner.setAdapter(dayArrayAdaptor);

		daySpinner.setSelection(date.getDayOfMonth() - 1);
		//

	}

	CalendarType detectSelectedCalendar() {
		CalendarType calendarType = null;
		if (georgian.equals(calendarTypeSpinner.getSelectedItem().toString())) {
			calendarType = CalendarType.GEORGIAN;
		} else if (shamsi.equals(calendarTypeSpinner.getSelectedItem()
				.toString())) {
			calendarType = CalendarType.SHAMSI;
		} else if (islamic.equals(calendarTypeSpinner.getSelectedItem()
				.toString())) {
			calendarType = CalendarType.ISLAMIC;
		}
		return calendarType;
	}
}