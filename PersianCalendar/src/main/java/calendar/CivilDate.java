package calendar;

import com.byagowi.persiancalendar.Utils;

import java.util.Calendar;

/**
 * @author Amir
 * @author ebraminio
 */
public class CivilDate extends AbstractDate {
    private static final int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31,
            31, 30, 31, 30, 31};
    private int year;
    private int month;
    private int day;
    public CivilDate() {
        this(Calendar.getInstance());
    }

    public CivilDate(Calendar calendar) {
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
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

    public void setDayOfMonth(int day) {
        if (day < 1)
            throw new DayOutOfRangeException("day " + day + " is out of range!");

        if (month != 2 && day > daysInMonth[month])
            throw new DayOutOfRangeException("day " + day + " is out of range!");

        if (month == 2 && isLeapYear() && day > 29)
            throw new DayOutOfRangeException("day " + day + " is out of range!");

        if (month == 2 && (!isLeapYear()) && day > 28)
            throw new DayOutOfRangeException("day " + day + " is out of range!");

        // TODO check for the case of leap year for February
        this.day = day;
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

    public void setMonth(int month) {
        if (month < 1 || month > 12)
            throw new MonthOutOfRangeException("month " + month
                    + " is out of range!");

        // Set the day again, so that exceptions are thrown if the
        // day is out of range
        setDayOfMonth(getDayOfMonth());

        this.month = month;
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

    public void setYear(int year) {
        if (year == 0)
            throw new YearOutOfRangeException("Year 0 is invalid!");

        this.year = year;
    }

    public boolean isLeapYear() {
        if (year % 400 == 0)
            return true;
        else if (year % 100 == 0)
            return false;
        else return year % 4 == 0;
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

    public boolean equals(CivilDate civilDate) {
        return this.getDayOfMonth() == civilDate.getDayOfMonth()
                && this.getMonth() == civilDate.getMonth()
                && this.getYear() == civilDate.getYear();
    }

    @Override
    public CivilDate clone() {
        return new CivilDate(getYear(), getMonth(), getDayOfMonth());
    }
}
