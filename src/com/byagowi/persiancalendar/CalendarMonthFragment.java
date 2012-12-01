package com.byagowi.persiancalendar;

import static com.byagowi.persiancalendar.CalendarUtils.formatNumber;
import static com.byagowi.persiancalendar.CalendarUtils.getMonthYearTitle;
import static com.byagowi.persiancalendar.CalendarUtils.isDariVersion;
import static com.byagowi.persiancalendar.CalendarUtils.preferenceDigits;
import static com.byagowi.persiancalendar.CalendarUtils.prepareTextView;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.PersianDate;

import com.byagowi.common.Range;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CalendarMonthFragment extends Fragment {
	private int offset;

	private CalendarActivity calendarActivity;

	public void setCalendarActivity(CalendarActivity calendarActivity) {
		this.calendarActivity = calendarActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		offset = args.getInt("offset");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View calendar = inflater.inflate(R.layout.calendar_table, null);

		PersianDate persianDate = DateConverter.civilToPersian(new CivilDate());
		int month = persianDate.getMonth() - offset;
		month -= 1;
		int year = persianDate.getYear();

		year = year + (month / 12);
		month = month % 12;
		if (month < 0) {
			year -= 1;
			month += 12;
		}

		month += 1;
		persianDate.setMonth(month);
		persianDate.setYear(year);
		persianDate.setDayOfMonth(1);
		persianDate.setDari(isDariVersion(calendar.getContext()));

		fillCalendarMonth(persianDate, calendar);
		return calendar;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("dummy", true);
	}

	private void fillCalendarMonth(PersianDate persianDate, View calendar) {

		char[] digits = preferenceDigits(calendar.getContext());

		int weekOfMonth = 1;
		int dayOfWeek = DateConverter.persianToCivil(persianDate)
				.getDayOfWeek() % 7;

		TextView currentMonthTextView = getTextViewInView(
				"currentMonthTextView", calendar);
		prepareTextView(currentMonthTextView);
		currentMonthTextView.setText(getMonthYearTitle(persianDate, digits));

		String[] firstCharOfDaysOfWeekName = { "ش", "ی", "د", "س", "چ", "پ",
				"ج" };
		for (int i : new Range(0, 7)) {
			TextView textView = getTextViewInView("calendarCell0" + (i + 1),
					calendar);
			prepareTextView(currentMonthTextView);
			textView.setText(firstCharOfDaysOfWeekName[i]);
		}
		
		try {
			PersianDate today = DateConverter.civilToPersian(new CivilDate());
			for (int i : new Range(1, 31)) {
				persianDate.setDayOfMonth(i);

				TextView textView = getTextViewInView("calendarCell"
						+ weekOfMonth + (dayOfWeek + 1), calendar);
				prepareTextView(currentMonthTextView);
				textView.setText(formatNumber(i, digits));

				dayOfWeek++;
				if (dayOfWeek == 7) {
					weekOfMonth++;
					dayOfWeek = 0;
				}

				final String holidayTitle = Holidays
						.getHolidayTitle(persianDate);
				if (holidayTitle != null) {
					textView.setBackgroundResource(R.drawable.holiday_background);

					textView.setTextColor(getResources().getColor(
							R.color.holidays_text_color));
				}
				ClickDayListener listener = new ClickDayListener(holidayTitle,
						persianDate.clone(), calendarActivity);
				textView.setOnClickListener(listener);
				textView.setOnLongClickListener(listener);

				if (persianDate.equals(today)) {
					textView.setBackgroundResource(R.drawable.today_background);
				}

			}
		} catch (DayOutOfRangeException e) {
			// okay, it was expected
		}
	}

	private TextView getTextViewInView(String name, View view) {
		try {
			return (TextView) view.findViewById(R.id.class.getField(name)
					.getInt(null)); // null for static field of classes
		} catch (Exception e) {
			Log.e(view.getContext().getPackageName(),
					"Error on retriving cell: " + e.getMessage());
			return null;
		}
	}
}
