package calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class IslamicDate extends AbstractDate {
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

    public void setDayOfMonth(int day) {
        // TODO This check is not very exact! But it's not worth of it
        // to compute the number of days in this month exactly
        if (day < 1 || day > 30)
            throw new DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE);

        this.day = day;
    }

    public int getDayOfWeek() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
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

    public int getWeekOfMonth() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    public boolean isLeapYear() {
        throw new RuntimeException(Constants.NOT_IMPLEMENTED_YET);
    }

    @Override
    public IslamicDate clone() {
        return new IslamicDate(getYear(), getMonth(), getDayOfMonth());
    }
}
