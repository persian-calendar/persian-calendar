package calendar;

import java.util.Calendar;

/**
 * @author Amir
 * @author ebraminio (implementing isLeapYear)
 */

public class PersianDate extends AbstractDate {
    private int year;
    private int month;
    private int day;

    public PersianDate() {
        this(Calendar.getInstance());
    }

    public PersianDate(Calendar calendar) {
        PersianDate persianDate = DateConverter.civilToPersian
                (new CivilDate(calendar));
        setDayOfMonth(persianDate.getDayOfMonth());
        setYear(persianDate.getYear());
        setMonth(persianDate.getMonth());
    }

    public PersianDate(int year, int month, int day) {
        setYear(year);
        // Initialize day, so that we get no exceptions when setting month
        this.day = 1;
        setMonth(month);
        setDayOfMonth(day);
    }

    public PersianDate clone() {
        return new PersianDate(getYear(), getMonth(), getDayOfMonth());
    }

    public int getDayOfMonth() {
        return day;
    }

    public void setDayOfMonth(int day) {
        if (day < 1)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        if (month <= 6 && day > 31)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        if (month > 6 && month <= 12 && day > 30)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        if (isLeapYear() && month == 12 && day > 30)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        if ((!isLeapYear()) && month == 12 && day > 29)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        if (month < 1 || month > 12)
            throw new MonthOutOfRangeException(
                    Constants.MONTH + " " + month + " " + Constants.IS_OUT_OF_RANGE);

        // Set the day again, so that exceptions are thrown if the
        // day is out of range
        setDayOfMonth(day);

        this.month = month;
    }

    public int getWeekOfYear() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year == 0)
            throw new YearOutOfRangeException(Constants.YEAR_0_IS_INVALID);

        this.year = year;
    }

    public void rollDay(int amount, boolean up) {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public void rollMonth(int amount, boolean up) {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public void rollYear(int amount, boolean up) {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public String getEvent() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public int getDayOfWeek() {
        CivilDate civilDate = DateConverter.persianToCivil(this);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, civilDate.getYear());
        cal.set(Calendar.MONTH, civilDate.getMonth() - 1);
        cal.set(Calendar.DAY_OF_MONTH, civilDate.getDayOfMonth());
        cal.getTimeInMillis();

        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfYear() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public int getWeekOfMonth(int firstDayOfWeek) {
        int dowOfFirstDayOfMonth = (new PersianDate(year, month, 1)).getDayOfWeek();
        int dayCountInFirstWeek = (7 - dowOfFirstDayOfMonth) + firstDayOfWeek;
        if (dayCountInFirstWeek > 7)
            dayCountInFirstWeek = dayCountInFirstWeek % 7;

        int week1 = dayCountInFirstWeek;
        int week2 = week1 + 7;
        int week3 = week2 + 7;
        int week4 = week3 + 7;
        int week5 = week4 + 7;
        int week6 = week5 + 7;
        int week7 = week6 + 7;

        if (day <= week1)
            return 1;
        else if (day <= week2)
            return 2;
        else if (day <= week3)
            return 3;
        else if (day <= week4)
            return 4;
        else if (day <= week5)
            return 5;
        else if (day <= week6)
            return 6;
        else if (day <= week7)
            return 7;
        return 0;
    }

    public boolean isLeapYear() {
        int y;
        if (year > 0)
            y = year - 474;
        else
            y = 473;
        return (((((y % 2820) + 474) + 38) * 682) % 2816) < 682;
    }

    public boolean equals(PersianDate persianDate) {
        return getDayOfMonth() == persianDate.getDayOfMonth()
                && getMonth() == persianDate.getMonth()
                && (getYear() == persianDate.getYear() || getYear() == -1);
    }
}
