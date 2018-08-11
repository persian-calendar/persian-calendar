package calendar;

import java.util.Calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class IslamicDate extends AbstractDate {
    private int day;
    private int month;
    private int year;

    public IslamicDate() {
        this(Calendar.getInstance());
    }

    public IslamicDate(Calendar calendar) {
        IslamicDate islamicDate = DateConverter.civilToIslamic(new CivilDate(calendar), 0);
        setDayOfMonth(islamicDate.getDayOfMonth());
        setYear(islamicDate.getYear());
        setMonth(islamicDate.getMonth());
    }

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

    public void setDayOfMonth(int day) {
        // TODO This check is not very exact! But it's not worth of it
        // to compute the number of days in this month exactly
        if (day < 1 || day > 30)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        this.day = day;
    }

    public int getDayOfWeek() {
        CivilDate civilDate = DateConverter.islamicToCivil(this);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, civilDate.getYear());
        cal.set(Calendar.MONTH, civilDate.getMonth() - 1);
        cal.set(Calendar.DAY_OF_MONTH, civilDate.getDayOfMonth());
        return cal.get(Calendar.DAY_OF_WEEK);
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

    public int getDayOfYear() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public int getWeekOfMonth(int firstDayOfWeek) {

        int dowOfFirstDayOfMonth = (new IslamicDate(getYear(), getMonth(), 1)).getDayOfWeek();
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
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public boolean equals(IslamicDate islamicDate) {
        return getDayOfMonth() == islamicDate.getDayOfMonth()
                && getMonth() == islamicDate.getMonth()
                && (getYear() == islamicDate.getYear() || getYear() == -1);
    }

    @Override
    public IslamicDate clone() {
        return new IslamicDate(getYear(), getMonth(), getDayOfMonth());
    }
}
