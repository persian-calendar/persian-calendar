package com.byagowi.persiancalendar;

import java.util.Date;

import com.azizhuss.arabicreshaper.ArabicReshape;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;


/**
 * Common utilities that needed for this calendar
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarUtils {
	public static char PERSIAN_COMMA = '،';
	public static char LRO = '\u202D';
	public static char POP = '\u202C';

	public static final char[] arabicDigits = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9' };
	public static final char[] persianDigits = { '۰', '۱', '۲', '۳', '۴', '۵',
			'۶', '۷', '۸', '۹' };
	public static final char[] arabicIndicDigits = { '٠', '١', '٢', '٣', '٤',
			'٥', '٦', '٧', '٨', '٩' };
	
	public static final String[] dayOfWeekName = { "", "یک‌شنبه", "دوشنبه",
			"سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه" };

	public static String getDayOfWeekName(int dayOfWeek) {
		return dayOfWeekName[dayOfWeek];
	}

	public static char[] getDigitsFromPreference(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("PersianDigits", true) ? PersianCalendarUtils.persianDigits
				: PersianCalendarUtils.arabicDigits;
	}
	
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
	
	private static String AM_IN_PERSIAN = "ق.ظ";
	private static String PM_IN_PERSIAN = "ب.ظ";

	public static String getPersianFormattedClock(Date date, char[] digits) {

		String timeText = null;
		int hour = date.getHours();
		if (date.getHours() > 12) {
			timeText = PM_IN_PERSIAN;
			hour -= 12;
		} else {
			timeText = AM_IN_PERSIAN;
		}

		return String.format("%s:%s %s",
				formatNumber(addExtraZeroForClock(hour), digits),
				formatNumber(addExtraZeroForClock(date.getMinutes()), digits),
				timeText);
	}

	public static String formatNumber(int number, char[] digits) {
		return formatNumber(Integer.toString(number), digits);
	}

	public static String formatNumber(String number, char[] digits) {
		if (digits == arabicDigits)
			return number;
		
		StringBuilder sb = new StringBuilder();
		for (char i : number.toCharArray()) {
			if (Character.isDigit(i)) {
				sb.append(digits[Integer.parseInt(i + "")]);
			} else {
				sb.append(i);
			}
		}
		return sb.toString();
	}

	// TODO: textShaper must become private in future
	public static String textShaper(String text) {
		return ArabicReshape.reshape(text);
	}

	public static String dateToString(AbstractDate date, char[] digits) {
		StringBuilder sb = new StringBuilder();
		sb.append(PersianCalendarUtils.formatNumber(date.getDayOfMonth(),
				digits));
		sb.append(' ');
		sb.append(date.getMonthName());
		sb.append(' ');
		sb.append(PersianCalendarUtils.formatNumber(date.getYear(), digits));

		return textShaper(sb.toString());
	}

	public static String getCalendarInfo(CivilDate civilDate, char[] digits) {
		StringBuilder sb = new StringBuilder();
		sb.append("امروز:\n");
		sb.append(getDayOfWeekName(civilDate.getDayOfWeek()));
		sb.append(PERSIAN_COMMA);
		sb.append(" ");
		sb.append(dateToString(DateConverter.civilToPersian(civilDate), digits));
		sb.append(" هجری خورشیدی\n\n");
		sb.append("برابر با:\n");
		sb.append(dateToString(civilDate, digits));
		sb.append(" میلادی\n");
		sb.append(dateToString(DateConverter.civilToIslamic(civilDate), digits));
		sb.append(" هجری قمری\n");
		return textShaper(sb.toString());
	}

	public static String getMonthYearTitle(PersianDate persianDate,
			char[] digits) {
		StringBuilder sb = new StringBuilder();
		sb.append(persianDate.getMonthName());
		sb.append(' ');
		sb.append(formatNumber(persianDate.getYear(), digits));
		return textShaper(sb.toString());
	}
}
