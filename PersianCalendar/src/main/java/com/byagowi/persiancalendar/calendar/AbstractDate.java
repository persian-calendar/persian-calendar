package com.byagowi.persiancalendar.calendar;

/**
 * Abstract class representing a date.
 *
 * @author Amir
 * @author ebraminio
 */

public abstract class AbstractDate {
    // Concrete things
    final private int year;
    final private int month;
    final private int dayOfMonth;

    /* What JDN (Julian Day Number) means?
     *
     * From https://en.wikipedia.org/wiki/Julian_day:
     * Julian day is the continuous count of days since the beginning of the
     * Julian Period and is used primarily by astronomers, and in software for
     * easily calculating elapsed days between two events (e.g. food production
     * date and sell by date).
     */

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

    public AbstractDate(AbstractDate date) {
        this(date.toJdn());
    }

    // Things needed to be implemented by subclasses
    public abstract long toJdn();

    protected abstract int[] fromJdn(long jdn);

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
        if (getClass() != obj.getClass()) return false;
        if (obj instanceof AbstractDate) {
            AbstractDate date = (AbstractDate) obj;
            return getYear() == date.getYear() &&
                    getMonth() == date.getMonth() &&
                    getDayOfMonth() == date.getDayOfMonth();
        }
        return false;
    }
}
