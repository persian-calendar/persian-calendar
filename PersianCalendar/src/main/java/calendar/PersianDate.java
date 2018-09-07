package calendar;

/**
 * @author Amir
 * @author ebraminio (implementing isLeapYear)
 */

public class PersianDate extends AbstractDate {
    private int year;
    private int month;
    private int day;

    public PersianDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public int getDayOfMonth() {
        return day;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getYear() {
        return year;
    }
}
