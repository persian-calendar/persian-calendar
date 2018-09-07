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

    public boolean equals(IslamicDate islamicDate) {
        return getDayOfMonth() == islamicDate.getDayOfMonth()
                && getMonth() == islamicDate.getMonth()
                && (getYear() == islamicDate.getYear() || getYear() == -1);
    }
}
