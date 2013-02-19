package calendar;

import java.util.Calendar;

/**
 * @author Amir
 * @author ebraminio
 */
public class CivilDate extends AbstractDate {
    private static final String[] monthName = {"", "ژانویه", "فوریه", "مارس",
            "آوریل", "مه", "ژون", "جولای", "آگوست", "سپتامبر", "اکتبر",
            "نوامبر", "دسامبر"};

    public String[] getMonthsList() {
        return monthName;
    }

    private static final String[] weekdayName = {"", "یکشنبه", "دوشنبه",
            "سه شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"};

    private static final int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31,
            30, 31, 30, 31};

    private int year;
    private int month;
    private int day;

    public CivilDate() {
        Calendar cal = Calendar.getInstance();
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH) + 1;
        this.day = cal.get(Calendar.DAY_OF_MONTH);
    }

    public CivilDate(int year, int month, int day) {
        this();
        setYear(year);
        setMonth(month);
        setDayOfMonth(day);
    }

    public int getDayOfMonth() {
        return day;
    }

    public int getDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfYear() {
        throw new RuntimeException("not implemented yet!");
    }

    public String getEvent() {
        throw new RuntimeException("not implemented yet!");
    }

    public int getMonth() {
        return month;
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
        return year;
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

        if (day > daysInMonth[month])
            throw new DayOutOfRangeException("day " + day + " is out of range!");

        // TODO check for the case of leap year for February
        this.day = day;
    }

    public void setMonth(int month) {
        if (month < 1 || month > 12)
            throw new MonthOutOfRangeException("month " + month
                    + " is out of range!");

        // Set the day again, so that exceptions are thrown if the
        // day is out of range
        setDayOfMonth(getDayOfMonth());

        this.month = month;
    }

    public void setYear(int year) {
        if (year == 0)
            throw new YearOutOfRangeException("Year 0 is invalid!");

        this.year = year;
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

    public boolean equals(CivilDate civilDate) {
        if (this.getDayOfMonth() == civilDate.getDayOfMonth()
                && this.getMonth() == civilDate.getMonth()
                && this.getYear() == civilDate.getYear())
            return true;
        return false;
    }
}
