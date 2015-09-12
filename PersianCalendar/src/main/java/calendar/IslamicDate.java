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
            throw new DayOutOfRangeException("day " + day + " is out of range!");

        this.day = day;
    }

    public int getDayOfWeek() {
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
        setDayOfMonth(day);

        this.month = month;
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

    public void rollDay(int amount, boolean up) {
        throw new RuntimeException("not implemented yet!");
    }

    public void rollMonth(int amount, boolean up) {
        throw new RuntimeException("not implemented yet!");
    }

    public void rollYear(int amount, boolean up) {
        throw new RuntimeException("not implemented yet!");
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

    @Override
    public IslamicDate clone() {
        return new IslamicDate(getYear(), getMonth(), getDayOfMonth());
    }
}
