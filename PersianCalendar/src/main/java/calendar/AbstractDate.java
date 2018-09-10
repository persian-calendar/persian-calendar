package calendar;

/**
 * Abstract class representing a date.
 *
 * @author Amir
 * @author ebraminio
 */
public abstract class AbstractDate {
    // Things needed to be implemented by subclasses
    public abstract long toJdn();
    protected abstract int[] fromJdn(long jdn);
    public abstract CalendarType getType();

    // Concrete things
    final private int year;
    final private int month;
    final private int dayOfMonth;

    public AbstractDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public AbstractDate(long jdn) {
        int[] result = fromJdn(jdn);
        this.year = result[0];
        this.month = result[1];
        this.dayOfMonth = result[2];
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractDate) {
            AbstractDate date = (AbstractDate) obj;
            return getType() == date.getType() &&
                    getYear() == date.getYear() &&
                    getMonth() == date.getMonth() &&
                    getDayOfMonth() == date.getDayOfMonth();
        }
        return false;
    }
}
