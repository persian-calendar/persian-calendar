package com.byagowi.persiancalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.widget.TextView;

import com.byagowi.persiancalendar.utils.Utils;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

/**
 * Pray time helper. It is like an aspect for activity class somehow
 * 
 * @author ebraminio
 */
class PrayTimeActivityHelper {
	private final Utils utils;

	private final MainActivity calendarActivity;
	private Date date = new Date();
	private final TextView prayTimeTextView;

	public PrayTimeActivityHelper(MainActivity calendarActivity) {
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

		char[] digits = utils.preferredDigits(calendarActivity);

		sb.append("اذان صبح: ");
		sb.append(utils.clockToString(prayTimes.get(PrayTime.Imsak), digits));

		sb.append("\nطلوع آفتاب: ");
		sb.append(utils.clockToString(prayTimes.get(PrayTime.Sunrise), digits));

		sb.append("\nاذان ظهر: ");
		sb.append(utils.clockToString(prayTimes.get(PrayTime.Dhuhr), digits));

		sb.append("\nغروب آفتاب: ");
		sb.append(utils.clockToString(prayTimes.get(PrayTime.Sunset), digits));

		sb.append("\nاذان مغرب: ");
		sb.append(utils.clockToString(prayTimes.get(PrayTime.Maghrib), digits));

		sb.append("\nنیمه وقت شرعی: ");
		sb.append(utils.clockToString(prayTimes.get(PrayTime.Midnight), digits));

		utils.prepareTextView(prayTimeTextView);
		prayTimeTextView.setText(utils.textShaper(utils.formatNumber(
				sb.toString(), digits)));
	}

	public void clearInfo() {
		prayTimeTextView.setText("");
	}
}
