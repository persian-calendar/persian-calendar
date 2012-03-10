package calendar;

import com.byagowi.persiancalendar.ArabicUtils;
import com.byagowi.persiancalendar.PersianUtils;

/**
 * 
 * @author Amir
 * @author ebraminio
 * 
 */

public class IslamicDate extends AbstractDate {

	private static final String[] monthName = {
			"",
			"\u0645\u062D\u0631\u0645", // Moharram
			"\u0635\u0641\u0631", // Safar
			"\u0631\u0628\u064A\u0639 \u0627\u0644\u0627\u0648\u0644", // Rabi 1
			"\u0631\u0628\u064A\u0639 \u0627\u0644\u062B\u0627\u0646\u064A", // Rabi
																				// 2
			"\u062C\u0645\u0627\u062F\u064A \u0627\u0644\u0627\u0648\u0644", // Jamdi
																				// 1
			"\u062C\u0645\u0627\u062F\u064A \u0627\u0644\u062B\u0627\u0646\u064A", // Jamadi
																					// 2
			"\u0631\u062C\u0628", // Rajab
			"\u0634\u0639\u0628\u0627\u0646", // Shaban
			"\u0631\u0645\u0636\u0627\u0646", // Ramezan
			"\u0634\u0648\u0627\u0644", // Shavval
			"\u0630\u064A \u0627\u0644\u0642\u0639\u062F\u0647", // Zel Ghade
			"\u0630\u064A \u0627\u0644\u062D\u062C\u0647" // Zel Hajje
	};

	private int day;

	private int month;

	private int year;

	public IslamicDate(int year, int month, int day) {
		setYear(year);
		// Initialize day, so that we get no exceptions when setting month
		this.day = 1;
		setMonth(month);
		setDayOfMonth(day);
	}

	public int getDayOfMonth() {
		return day;
	}

	public int getDayOfWeek() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getMonth() {
		return month;
	}

	public String getMonthName() {
		return monthName[month];
	}

	public int getWeekOfYear() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getYear() {
		return year;
	}

	public void rollDay(int amount, boolean up) {
		throw new RuntimeException("not implemented yet!");
	}

	public void rollMonth(int amount, boolean up) {
		throw new RuntimeException("not implemented yet!");
	}

	public void rollYear(int amount, boolean up) {
		throw new RuntimeException("not implemented yet!");
	}

	public void setDayOfMonth(int day) {
		// TODO This check is not very exact! But it's not worth of it
		// to compute the number of days in this month exactly
		if (day < 1 || day > 30)
			throw new DayOutOfRangeException("day " + day + " is out of range!");

		this.day = day;
	}

	public void setMonth(int month) {
		if (month < 1 || month > 12)
			throw new MonthOutOfRangeException("month " + month
					+ " is out of range!");

		// Set the day again, so that exceptions are thrown if the
		// day is out of range
		setDayOfMonth(day);

		this.month = month;
	}

	public void setYear(int year) {
		if (year == 0)
			throw new YearOutOfRangeException("Year 0 is invalid!");

		this.year = year;
	}

	public String getEvent() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getDayOfYear() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getWeekOfMonth() {
		throw new RuntimeException("not implemented yet!");
	}

	public boolean isLeapYear() {
		throw new RuntimeException("not implemented yet!");
	}

	public String toString() {
		return ArabicUtils.getArabicNumber(day) + " " + monthName[month]
				+ " " + ArabicUtils.getArabicNumber(year);
	}

}
