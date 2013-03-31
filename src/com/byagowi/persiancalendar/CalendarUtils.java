package com.byagowi.persiancalendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;
import com.azizhuss.arabicreshaper.ArabicShaping;
import com.byagowi.common.IterableNodeList;
import com.github.praytimes.CalculationMethod;
import com.github.praytimes.Coordinate;
import com.github.praytimes.Locations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Common utilities that needed for this calendar
 * 
 * @author ebraminio
 */
public class CalendarUtils {
	private static CalendarUtils myInstance;

	public static CalendarUtils getInstance() {
		if (myInstance == null) {
			myInstance = new CalendarUtils();
		}
		return myInstance;
	}

	public final char PERSIAN_COMMA = '،';

	public String textShaper(String text) {
		return ArabicShaping.shape(text);
	}

	public String programVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(context.getPackageName(),
					"Name not found on PersianCalendarUtils.programVersion");
		}
		return "";
	}

	public final String shamsi = textShaper("هجری خورشیدی");
	public final String islamic = textShaper("هجری قمری");
	public final String georgian = textShaper("میلادی");
	public final String equalWith = textShaper("برابر با");

	private final char[] arabicDigits = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9' };
	private final char[] persianDigits = { '۰', '۱', '۲', '۳', '۴', '۵', '۶',
			'۷', '۸', '۹' };
	public final char[] arabicIndicDigits = { '٠', '١', '٢', '٣', '٤', '٥',
			'٦', '٧', '٨', '٩' };

	private final String[] dayOfWeekName = { "", "یکشنبه", "دوشنبه", "سه‌شنبه",
			"چهارشنبه", "پنجشنبه", "جمعه", "شنبه" };

	public String getDayOfWeekName(int dayOfWeek) {
		return dayOfWeekName[dayOfWeek];
	}

	private Typeface typeface;

	public void prepareTextView(TextView textView) {
		if (typeface == null) {
			typeface = Typeface.createFromAsset(textView.getContext()
					.getAssets(), "fonts/DroidNaskh-Regular.ttf");
		}
		textView.setTypeface(typeface);
		textView.setLineSpacing(0f, 0.8f);
	}

	public CalculationMethod getCalculationMethod(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return CalculationMethod.valueOf(prefs.getString("PrayTimeMethod",
				"Jafari")); // Seems Iran using Jafari method
	}

	public Coordinate getCoordinate(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		String province = prefs.getString("Location", "CUSTOM");
		if (!province.equals("CUSTOM")) {
			return Locations.valueOf(province).getCoordinate();
		}

		Coordinate coord;
		try {
			coord = new Coordinate(Double.parseDouble(prefs.getString(
					"Latitude", "0")), Double.parseDouble(prefs.getString(
					"Longitude", "0")), Double.parseDouble(prefs.getString(
					"Altitude", "0")));
		} catch (NumberFormatException e) {
			return null;
		}

		// If latitude or longitude is zero probably preference not set yet
		if (coord.getLatitude() == 0 && coord.getLongitude() == 0) {
			return null;
		}
		return coord;
	}

	public char[] preferredDigits(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("PersianDigits", true) ? persianDigits
				: arabicDigits;
	}

	public void setTheme(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String key = prefs.getString("Theme", "");

		int theme = R.style.LightTheme; // default theme

		if (key.equals("LightTheme"))
			theme = R.style.LightTheme;
		if (key.equals("DarkTheme"))
			theme = R.style.DarkTheme;

		context.setTheme(theme);
	}

	public boolean isDariVersion(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("DariVersion", false);
	}

	private String addExtraZeroForClock(int num) {

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

	public Calendar makeCalendarFromDate(Date date, boolean iranTime) {
		Calendar calendar = Calendar.getInstance();
		if (iranTime) {
			calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
		}
		calendar.setTime(date);
		return calendar;
	}

	private String AM_IN_PERSIAN = "ق.ظ";
	private String PM_IN_PERSIAN = "ب.ظ";

	public String getPersianFormattedClock(Calendar calendar, char[] digits,
			boolean in24) {
		String timeText = null;

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (!in24) {
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
		if (!in24) {
			result = result + " " + timeText;
		}
		return result;
	}

	public String formatNumber(int number, char[] digits) {
		return formatNumber(Integer.toString(number), digits);
	}

	public String formatNumber(String number, char[] digits) {
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

	public String dateToString(AbstractDate date, char[] digits,
			boolean showYear) {
		StringBuilder sb = new StringBuilder();
		sb.append(formatNumber(date.getDayOfMonth(), digits));
		sb.append(' ');
		sb.append(date.getMonthName());

		if (showYear) {
			sb.append(' ');
			sb.append(formatNumber(date.getYear(), digits));
		}

		return sb.toString();
	}

	public String dayTitleSummary(CivilDate civilDate, char[] digits) {
		return getDayOfWeekName(civilDate.getDayOfWeek())
				+ PERSIAN_COMMA
				+ " "
				+ dateToString(DateConverter.civilToPersian(civilDate), digits,
						true);
	}

	public String infoForSpecificDay(CivilDate civilDate, char[] digits) {
		return dayTitleSummary(civilDate, digits)
				+ "\n\n"
				+ equalWith
				+ ":\n"
				+ dateToString(civilDate, digits, true)
				+ "\n"
				+ dateToString(DateConverter.civilToIslamic(civilDate), digits,
						true) + "\n";
	}

	public String getMonthYearTitle(PersianDate persianDate, char[] digits) {
		return textShaper(persianDate.getMonthName() + ' '
				+ formatNumber(persianDate.getYear(), digits));
	}

	public void quickToast(String message, Context context) {
		Toast.makeText(context, textShaper(message), Toast.LENGTH_SHORT).show();
	}

	private int[] daysIcons = { 0, R.drawable.day1, R.drawable.day2,
			R.drawable.day3, R.drawable.day4, R.drawable.day5, R.drawable.day6,
			R.drawable.day7, R.drawable.day8, R.drawable.day9,
			R.drawable.day10, R.drawable.day11, R.drawable.day12,
			R.drawable.day13, R.drawable.day14, R.drawable.day15,
			R.drawable.day16, R.drawable.day17, R.drawable.day18,
			R.drawable.day19, R.drawable.day20, R.drawable.day21,
			R.drawable.day22, R.drawable.day23, R.drawable.day24,
			R.drawable.day25, R.drawable.day26, R.drawable.day27,
			R.drawable.day28, R.drawable.day29, R.drawable.day30,
			R.drawable.day31 };

	public int getDayIconResource(int day) {
		try {
			return daysIcons[day];
		} catch (IndexOutOfBoundsException e) {
			Log.e("com.byagowi.calendar", "No such field is available");
			return 0;
		}
	}

	private List<Holiday> holidays;

	public void loadHolidays(InputStream xmlStream) {
		holidays = new ArrayList<Holiday>();
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = builder.parse(xmlStream);

			NodeList holidaysNodes = document.getElementsByTagName("holiday");
			for (Node node : new IterableNodeList(holidaysNodes)) {
				NamedNodeMap attrs = node.getAttributes();

				int year = Integer.parseInt(attrs.getNamedItem("year")
						.getNodeValue());
				int month = Integer.parseInt(attrs.getNamedItem("month")
						.getNodeValue());
				int day = Integer.parseInt(attrs.getNamedItem("day")
						.getNodeValue());

				String holidayTitle = node.getFirstChild().getNodeValue();

				holidays.add(new Holiday(new PersianDate(year, month, day),
						holidayTitle));
			}

		} catch (ParserConfigurationException e) {
			Log.e("com.byagowi.persiancalendar", e.getMessage());
		} catch (SAXException e) {
			Log.e("com.byagowi.persiancalendar", e.getMessage());
		} catch (IOException e) {
			Log.e("com.byagowi.persiancalendar", e.getMessage());
		}
	}

	public String getHolidayTitle(PersianDate day) {
		for (Holiday holiday : holidays) {
			if (holiday.getDate().equals(day)) {
				return holiday.getTitle();
			}
		}
		return null;
	}
}
