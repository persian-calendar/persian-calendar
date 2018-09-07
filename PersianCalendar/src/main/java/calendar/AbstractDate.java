package calendar;

/**
 * Abstract class representing a date. Instances of this class should be
 * mutable. Varios getters and setters are provided so that date manipulation is
 * as convenient as possible.
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
