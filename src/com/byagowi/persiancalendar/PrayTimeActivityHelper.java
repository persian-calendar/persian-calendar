package com.byagowi.persiancalendar;

import android.widget.TextView;
import com.github.praytimes.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Pray time helper. It is like an aspect for activity class somehow
 * 
 * @author ebraminio
 */
class PrayTimeActivityHelper {
	private final CalendarUtils utils;

	private final CalendarActivity calendarActivity;
	private Date date = new Date();
	private final TextView prayTimeTextView;

	public PrayTimeActivityHelper(CalendarActivity calendarActivity) {
		this.calendarActivity = calendarActivity;
		this.utils = calendarActivity.utils;
		prayTimeTextView = (TextView) calendarActivity
				.findViewById(R.id.today_praytimes);
	}

	public void setDate(int year, int month, int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, dayOfMonth);
		date = c.getTime();
	}

	public void fillPrayTime() {
		if (utils.getCoordinate(calendarActivity) == null) {
			return;
		}
		Coordinate coord = utils.getCoordinate(calendarActivity);

		PrayTimesCalculator ptc = new PrayTimesCalculator(
				utils.getCalculationMethod(calendarActivity));
		StringBuilder sb = new StringBuilder();
		Map<PrayTime, Clock> prayTimes = ptc.calculate(date, coord);

		sb.append("اذان صبح: ");
		sb.append(prayTimes.get(PrayTime.Imsak).toString());

		sb.append("\nطلوع آفتاب: ");
		sb.append(prayTimes.get(PrayTime.Sunrise).toString());

		sb.append("\nاذان ظهر: ");
		sb.append(prayTimes.get(PrayTime.Dhuhr).toString());

		sb.append("\nغروب آفتاب: ");
		sb.append(prayTimes.get(PrayTime.Sunset).toString());

		sb.append("\nاذان مغرب: ");
		sb.append(prayTimes.get(PrayTime.Maghrib).toString());

		sb.append("\nنیمه وقت شرعی: ");
		sb.append(prayTimes.get(PrayTime.Midnight).toString());

		char[] digits = utils.preferredDigits(calendarActivity);

		utils.prepareTextView(prayTimeTextView);
		prayTimeTextView.setText(utils.textShaper(utils.formatNumber(
				sb.toString(), digits)));
	}

	public void clearInfo() {
		prayTimeTextView.setText("");
	}
}
