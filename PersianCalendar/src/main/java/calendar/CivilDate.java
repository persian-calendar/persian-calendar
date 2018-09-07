package calendar;

import java.util.Calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class CivilDate extends AbstractDate {
    private static final int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private int year;
    private int month;
    private int day;

    public CivilDate(Calendar calendar) {
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public CivilDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public int getDayOfMonth() {
        return day;
    }

    public Calendar asCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getYear() {
        return year;
    }

    public boolean equals(CivilDate civilDate) {
        return getDayOfMonth() == civilDate.getDayOfMonth()
                && getMonth() == civilDate.getMonth()
                && (getYear() == civilDate.getYear() || getYear() == -1);
    }
}
