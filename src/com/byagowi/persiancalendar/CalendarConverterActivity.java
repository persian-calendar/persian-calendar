package com.byagowi.persiancalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import calendar.*;
import com.byagowi.common.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * Program activity for android
 * 
 * @author ebraminio
 */
public class CalendarConverterActivity extends Activity {
	private final CalendarUtils utils = CalendarUtils.getInstance();

	private Spinner calendarTypeSpinner;
	private Spinner yearSpinner;
	private Spinner monthSpinner;
	private Spinner daySpinner;
	private TextView convertedDateTextView;
	private int startingYearOnYearSpinner = 0;
	private final int yearDiffRange = 200;

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
		//

		// fill views
		List<String> calendarsTypes = new ArrayList<String>();
		calendarsTypes.add(utils.georgian);
		calendarsTypes.add(utils.shamsi);
		calendarsTypes.add(utils.islamic);
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
	private class CalendarSpinnersListener implements
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

	private class CalendarTypeSpinnerListener implements
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

	void fillCalendarInfo() {
		int year = startingYearOnYearSpinner
				+ yearSpinner.getSelectedItemPosition();
		int month = monthSpinner.getSelectedItemPosition() + 1;
		int day = daySpinner.getSelectedItemPosition() + 1;
		CalendarType calendarType = detectSelectedCalendar();

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
				persianDate = DateConverter.civilToPersian(civilDate);
				islamicDate = DateConverter.civilToIslamic(civilDate);

				calendarsTextList.add(utils.dateToString(civilDate, digits,
						true));
				calendarsTextList.add(utils.dateToString(persianDate, digits,
						true));
				calendarsTextList.add(utils.dateToString(islamicDate, digits,
						true));
				break;
			case ISLAMIC:
				islamicDate = new IslamicDate(year, month, day);
				civilDate = DateConverter.islamicToCivil(islamicDate);
				persianDate = DateConverter.islamicToPersian(islamicDate);

				calendarsTextList.add(utils.dateToString(islamicDate, digits,
						true));
				calendarsTextList.add(utils.dateToString(civilDate, digits,
						true));
				calendarsTextList.add(utils.dateToString(persianDate, digits,
						true));
				break;
			case SHAMSI:
				persianDate = new PersianDate(year, month, day);
				civilDate = DateConverter.persianToCivil(persianDate);
				islamicDate = DateConverter.persianToIslamic(persianDate);

				calendarsTextList.add(utils.dateToString(persianDate, digits,
						true));
				calendarsTextList.add(utils.dateToString(civilDate, digits,
						true));
				calendarsTextList.add(utils.dateToString(islamicDate, digits,
						true));
				break;
			}

			sb.append(utils.getDayOfWeekName(civilDate.getDayOfWeek()));
			sb.append(utils.PERSIAN_COMMA);
			sb.append(" ");
			sb.append(calendarsTextList.get(0));
			sb.append("\n\n");
			sb.append("برابر با:\n");
			sb.append(calendarsTextList.get(1));
			sb.append("\n");
			sb.append(calendarsTextList.get(2));
			sb.append("\n");

			utils.prepareTextView(convertedDateTextView);
			convertedDateTextView.setText(utils.textShaper(sb.toString()));
		} catch (RuntimeException e) {
			convertedDateTextView.setText("Date you entered was not valid!");
		}
	}

	void fillYearMonthDaySpinners() {
		char[] digits = utils.preferredDigits(this);

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
			yearsList.add(utils.formatNumber(i, digits));
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
			monthsList.add(utils.textShaper(monthsArray[i] + " / "
					+ utils.formatNumber(i, digits)));
		}
		ArrayAdapter<String> monthArrayAdaptor = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, monthsList);
		monthSpinner.setAdapter(monthArrayAdaptor);

		monthSpinner.setSelection(date.getMonth() - 1);
		//

		// days spinner init.
		List<String> daysList = new ArrayList<String>();
		for (int i : new Range(1, 31)) {
			daysList.add(utils.formatNumber(i, digits));
		}
		ArrayAdapter<String> dayArrayAdaptor = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, daysList);
		daySpinner.setAdapter(dayArrayAdaptor);

		daySpinner.setSelection(date.getDayOfMonth() - 1);
		//

	}

	CalendarType detectSelectedCalendar() {
		CalendarType calendarType = null;
		if (utils.georgian.equals(calendarTypeSpinner.getSelectedItem()
				.toString())) {
			calendarType = CalendarType.GEORGIAN;
		} else if (utils.shamsi.equals(calendarTypeSpinner.getSelectedItem()
				.toString())) {
			calendarType = CalendarType.SHAMSI;
		} else if (utils.islamic.equals(calendarTypeSpinner.getSelectedItem()
				.toString())) {
			calendarType = CalendarType.ISLAMIC;
		}
		return calendarType;
	}
}