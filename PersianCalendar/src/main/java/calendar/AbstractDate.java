package calendar;

/**
 * Abstract class representing a date.
 *
 * @author Amir
 * @author ebraminio
 */
public abstract class AbstractDate {
    private int year;
    private int month;
    private int day;

    public AbstractDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return day;
    }
}
