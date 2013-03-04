package com.byagowi.persiancalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.PersianDate;
import com.byagowi.common.Range;

/**
 * Calendar month view fragment
 * 
 * @author ebraminio
 */
public class CalendarMonthFragment extends Fragment {
	private final CalendarUtils utils = CalendarUtils.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Preparing Calendar Month View
		Context context = getActivity();
		LinearLayout root = new LinearLayout(context);
		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.setOrientation(LinearLayout.VERTICAL);

		// currentMonthTextView
		TextView currentMonthTextView = new TextView(context);
		currentMonthTextView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		currentMonthTextView.setGravity(Gravity.RIGHT);
		currentMonthTextView.setPadding(0, 0, 10, 2);
		currentMonthTextView.setTextSize(25);
		utils.prepareTextView(currentMonthTextView);

		root.addView(currentMonthTextView);
		// end

		// table
		TableLayout table = new TableLayout(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		table.setPadding(1, 0, 1, 0);

		TextView[][] daysTextViews = new TextView[7][7];
		for (int i : new Range(0, 7)) {
			TableRow row = new TableRow(context);
			row.setGravity(Gravity.CENTER_HORIZONTAL);
			if (i == 0) {
				row.setBackgroundResource(R.drawable.calendar_firstrow);
				row.setPadding(0, 0, 0, 10);
			}
			for (int j : new Range(0, 7)) {
				TextView tv = new TextView(context);
				utils.prepareTextView(tv);
				tv.setGravity(Gravity.CENTER);
				tv.setTextSize(20);
				if (i == 0) {
					tv.setBackgroundResource(R.color.first_row_background_color);
					tv.setTextColor(getResources().getColor(
							R.color.first_row_text_color));
				}
				daysTextViews[i][j] = tv;
				row.addView(tv);
			}
			table.addView(row);
		}

		table.setShrinkAllColumns(true);
		table.setStretchAllColumns(true);
		root.addView(table);
		// end

		// Calendar Logic
		int offset = getArguments().getInt("offset");
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
		persianDate.setDari(utils.isDariVersion(context));

		char[] digits = utils.preferredDigits(getActivity());

		int weekOfMonth = 1;
		int dayOfWeek = DateConverter.persianToCivil(persianDate)
				.getDayOfWeek() % 7;

		currentMonthTextView.setText(utils.getMonthYearTitle(persianDate,
				digits));

		String[] firstCharOfDaysOfWeekName = { "ش", "ی", "د", "س", "چ", "پ",
				"ج" };
		for (int i : new Range(0, 7)) {
			TextView textView = daysTextViews[0][6 - i];
			textView.setText(firstCharOfDaysOfWeekName[i]);
		}
		try {
			PersianDate today = DateConverter.civilToPersian(new CivilDate());
			for (int i : new Range(1, 31)) {
				persianDate.setDayOfMonth(i);

				TextView textView = daysTextViews[weekOfMonth][6 - dayOfWeek];
				textView.setText(utils.formatNumber(i, digits));
				textView.setBackgroundResource(R.drawable.days);

				String holidayTitle = utils.getHolidayTitle(persianDate);
				if (holidayTitle != null) {
					textView.setBackgroundResource(R.drawable.holidays);
				}
				if (holidayTitle != null || dayOfWeek == 6) {
					textView.setTextColor(getResources().getColor(
							R.color.holidays_text_color));
				}
				ClickDayListener listener = new ClickDayListener(holidayTitle,
						persianDate.clone(), (CalendarActivity) getActivity());
				textView.setOnClickListener(listener);
				textView.setOnLongClickListener(listener);

				if (persianDate.equals(today)) {
					textView.setBackgroundResource(R.drawable.today_background);
				}

				dayOfWeek++;
				if (dayOfWeek == 7) {
					weekOfMonth++;
					dayOfWeek = 0;
				}
			}
		} catch (DayOutOfRangeException e) {
			// okay, it was expected
		}
		return root;
	}
}
