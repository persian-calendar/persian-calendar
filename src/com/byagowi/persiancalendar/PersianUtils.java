package com.byagowi.persiancalendar;

import java.util.Date;

/**
 * Utilities needed for Persian
 * 
 * @author ebraminio
 * 
 */
public final class PersianUtils {
	public static char PERSIAN_COMMA = '،';
	public static char LRO = '\u202D';
	public static char POP = '\u202C';

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

	private static String AM_IN_PERSIAN = "ق.ظ";
	private static String PM_IN_PERSIAN = "ب.ظ";

	public static String addExtraZeroForClock(int num) {
		
		String str = Integer.toString(num);
		int strLength = str.length();
		if (strLength == 1) {
			return "0" + str;
		} else if (strLength == 2) {
			return str;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static String getPersianFormattedClock(Date date) {
		
		String timeText = null;
		int hour = date.getHours();
		if (date.getHours() > 12) {
			timeText = PM_IN_PERSIAN;
			hour -= 12;
		} else {
			timeText = AM_IN_PERSIAN;
		}

		return String.format("%s:%s %s",
				getPersianNumber(addExtraZeroForClock(hour)),
				getPersianNumber(addExtraZeroForClock(date.getMinutes())),
				timeText);
	}
}
