package com.byagowi.persiancalendar;

import java.util.Date;

/**
 * Utilities needed for Persian
 * @author ebraminio
 *
 */
public final class PersianUtils {
	public static char RLM = '\u200f';
	public static char PERSIAN_COMMA = '،';

	public static final String[] dayOfWeekName = { "", "یک‌شنبه", "دوشنبه",
			"سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه" };

	public static String getDayOfWeekName(int dayOfWeek) {
		return dayOfWeekName[dayOfWeek];
	}

	private static final char[] persianDigit = { '۰', '۱', '۲', '۳', '۴', '۵',
			'۶', '۷', '۸', '۹' };

	public static String getPersianNumber(int number) {
		return getPersianNumber(number + "");
	}
	
	public static String getPersianNumber(String number) {
		StringBuilder sb = new StringBuilder();
		for (char i : number.toCharArray()) {
			if (Character.isDigit(i)) {
				sb.append(persianDigit[Integer.parseInt(i + "")]);
			} else {
				sb.append(i);
			}
		}
		return sb.toString();
	}

	private static String AM_IN_PERSIAN = "ق‍.ظ‍";
	private static String PM_IN_PERSIAN = "ب‍.ظ‍";

	public static String getPersianFormattedClock(Date date) {
		StringBuilder sb = new StringBuilder();

		String timeText = date.getHours() > 12 ? PM_IN_PERSIAN : AM_IN_PERSIAN;
		int hour = date.getHours();
		int minute = date.getMinutes();

		sb.append(getPersianNumber(hour) + ":" + getPersianNumber(minute)
				+ " " + timeText);
		return sb.toString();
	}
}
