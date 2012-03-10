package calendar;

import java.util.Calendar;

import com.byagowi.persiancalendar.PersianUtils;

/**
 * 
 * @author Amir
 * @author ebraminio
 * 
 */
public class CivilDate extends AbstractDate {
	private static final String[] monthName = { "",
			"\u0698\u0627\u0646\u0648\u064A\u0647", // Janvie
			"\u0641\u0648\u0631\u064A\u0647", // Fevrie
			"\u0645\u0627\u0631\u0633", // Mars
			"\u0622\u0648\u0631\u064A\u0644", // Avril
			"\u0645\u0647", // Meh
			"\u0698\u0648\u0646", // Juan
			"\u062C\u0648\u0644\u0627\u064A", // July
			"\u0622\u06AF\u0648\u0633\u062A", // Agost
			"\u0633\u067E\u062A\u0627\u0645\u0628\u0631", // Septambr
			"\u0627\u0643\u062A\u0628\u0631", // Octobr
			"\u0646\u0648\u0627\u0645\u0628\u0631", // Novambr
			"\u062F\u0633\u0627\u0645\u0628\u0631" // Desambr
	};

	private static final String[] weekdayName = { "",
			"\u064a\u06A9\u0634\u0646\u0628\u0647", // 1 shanbeh
			"\u062F\u0648\u0634\u0646\u0628\u0647", // 2 shanbeh
			"\u0633\u0647 \u0634\u0646\u0628\u0647", // 3 shanbeh
			"\u0686\u0647\u0627\u0631\u0634\u0646\u0628\u0647", // 4 shanbeh
			"\u067E\u0646\u062C\u0634\u0646\u0628\u0647", // 5 shanbeh
			"\u062C\u0645\u0639\u0647", // jome
			"\u0634\u0646\u0628\u0647" // shanbe
	};

	private static final int[] daysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31,
			30, 31, 30, 31 };
	private Calendar cal; // Gregorian calendar instance

	public CivilDate() {
		// Default to today
		cal = Calendar.getInstance();
	}

	public CivilDate(int year, int month, int day) {
		this();
		setYear(year);
		setMonth(month);
		setDayOfMonth(day);
	}

	public int getDayOfMonth() {
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public int getDayOfWeek() {
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public int getDayOfYear() {
		throw new RuntimeException("not implemented yet!");
	}

	public String getEvent() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getMonth() {
		return cal.get(Calendar.MONTH) + 1;
	}

	public String getMonthName() {
		return monthName[getMonth()];
	}

	public int getWeekOfMonth() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getWeekOfYear() {
		throw new RuntimeException("not implemented yet!");
	}

	public int getYear() {
		return cal.get(Calendar.YEAR);
	}

	public boolean isLeapYear() {
		throw new RuntimeException("not implemented yet!");
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
		if (day < 1)
			throw new DayOutOfRangeException("day " + day + " is out of range!");

		if (day > daysInMonth[getMonth() - 1])
			throw new DayOutOfRangeException("day " + day + " is out of range!");

		// TODO check for the case of leap year for February

		cal.set(Calendar.DAY_OF_MONTH, day);
	}

	public void setMonth(int month) {
		if (month < 1 || month > 12)
			throw new MonthOutOfRangeException("month " + month
					+ " is out of range!");

		// Set the day again, so that exceptions are thrown if the
		// day is out of range
		setDayOfMonth(getDayOfMonth());

		cal.set(Calendar.MONTH, month - 1);
	}

	public void setYear(int year) {
		if (year == 0)
			throw new YearOutOfRangeException("Year 0 is invalid!");

		cal.set(Calendar.YEAR, year);
	}

	/**
	 * This method should have been in PersianDate rather than here. It is here
	 * because finding the weekday is much easier here, since we use the JDK's
	 * Calendar class to compute weekday, instead of computing it ourselves.
	 * 
	 * @return The name of day of week
	 */
	public String getDayOfWeekName() {
		return weekdayName[getDayOfWeek()];
	}

	public String toString() {
		return PersianUtils.getPersianNumber(getDayOfMonth()) + " "
				+ getMonthName() + " "
				+ PersianUtils.getPersianNumber(getYear());
	}
}
