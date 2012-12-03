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

import java.util.Calendar;
import java.util.Date;

import com.azizhuss.arabicreshaper.ArabicShaping;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
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
public class CalendarUtils {
	public static char PERSIAN_COMMA = '،';
	public static char LRO = '\u202D';
	public static char POP = '\u202C';

	// TODO: textShaper must become private in future
	public static String textShaper(String text) {
		return ArabicShaping.shape(text);
	}

	public static String programVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(context.getPackageName(),
					"Name not found on PersianCalendarUtils.programVersion");
		}
		return "";
	}

	public static final String shamsi = CalendarUtils.textShaper("هجری شمسی");
	public static final String islamic = CalendarUtils.textShaper("هجری قمری");
	public static final String georgian = CalendarUtils.textShaper("میلادی");

	public static final char[] arabicDigits = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9' };
	public static final char[] persianDigits = { '۰', '۱', '۲', '۳', '۴', '۵',
			'۶', '۷', '۸', '۹' };
	public static final char[] arabicIndicDigits = { '٠', '١', '٢', '٣', '٤',
			'٥', '٦', '٧', '٨', '٩' };

	public static final String[] dayOfWeekName = { "", "یکشنبه", "دوشنبه",
			"سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه" };

	public static String getDayOfWeekName(int dayOfWeek) {
		return dayOfWeekName[dayOfWeek];
	}

	private static Typeface typeface;

	public static TextView prepareTextView(TextView textView) {
		if (typeface == null) {
			typeface = Typeface.createFromAsset(textView.getContext()
					.getAssets(), "fonts/DroidNaskh-Regular.ttf");
		}
		textView.setTypeface(typeface);
		textView.setLineSpacing(0f, 0.8f);
		return textView;
	}

	public static void setLocation(Location location, Context context) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		editor.putString("Latitude", String.valueOf(location.getLatitude()));
		editor.putString("Longitude", String.valueOf(location.getLongitude()));
		editor.commit();
	}

	public static Location getLocation(Context context) {
		Location location = new Location((String) null);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		try {
			location.setLatitude(Double.parseDouble(prefs.getString("Latitude",
					"0")));
			location.setLongitude(Double.parseDouble(prefs.getString(
					"Longitude", "0")));
		} catch (RuntimeException e) {
			location.setLatitude(0);
			location.setLongitude(0);
			setLocation(location, context);
			return null;
		}

		// we had not any location before
		if (location.getLatitude() == 0 && location.getLatitude() == 0) {
			return null;
		}
		return location;
	}

	public static char[] preferenceDigits(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("PersianDigits", true) ? CalendarUtils.persianDigits
				: CalendarUtils.arabicDigits;
	}

	public static boolean isDariVersion(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("DariVersion", false);
	}

	public static boolean blackWidget(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("BlackWidget", false);
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

	public static String getPersianFormattedClock(Date date, char[] digits,
			boolean in24) {

		String timeText = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (in24) {
			if (calendar.get(Calendar.HOUR_OF_DAY) > 12) {
				timeText = PM_IN_PERSIAN;
				hour -= 12;
			} else {
				timeText = AM_IN_PERSIAN;
			}
		}

		String result = formatNumber(addExtraZeroForClock(hour), digits)
				+ ":"
				+ formatNumber(
						addExtraZeroForClock(calendar.get(Calendar.MINUTE)),
						digits);
		if (in24) {
			result = result + " " + timeText;
		}
		return result;
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

	public static String dateToString(AbstractDate date, char[] digits,
			boolean showYear) {
		StringBuilder sb = new StringBuilder();
		sb.append(CalendarUtils.formatNumber(date.getDayOfMonth(), digits));
		sb.append(' ');
		sb.append(date.getMonthName());
		if (showYear) {
			sb.append(' ');
			sb.append(CalendarUtils.formatNumber(date.getYear(), digits));
		}

		return sb.toString();
	}
	
	public static String dayTitleSummary(CivilDate civilDate, char[] digits) {
		StringBuilder sb = new StringBuilder();
		sb.append(getDayOfWeekName(civilDate.getDayOfWeek()));
		sb.append(PERSIAN_COMMA);
		sb.append(" ");
		sb.append(dateToString(DateConverter.civilToPersian(civilDate), digits,
				true));
		return sb.toString();
	}

	public static String infoForSpecificDay(CivilDate civilDate, char[] digits) {
		StringBuilder sb = new StringBuilder();
		sb.append(dayTitleSummary(civilDate, digits));
		sb.append("\n\nبرابر با:\n");
		sb.append(dateToString(civilDate, digits, true));
		sb.append("\n");
		sb.append(dateToString(DateConverter.civilToIslamic(civilDate), digits,
				true));
		sb.append("\n");
		return sb.toString();
	}

	public static String getMonthYearTitle(PersianDate persianDate,
			char[] digits) {
		StringBuilder sb = new StringBuilder();
		sb.append(persianDate.getMonthName());
		sb.append(' ');
		sb.append(formatNumber(persianDate.getYear(), digits));
		return textShaper(sb.toString());
	}

	public static void quickToast(String message, Context context) {
		Toast.makeText(context, CalendarUtils.textShaper(message),
				Toast.LENGTH_SHORT).show();
	}
}
