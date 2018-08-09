package calendar;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract class representing a date. Instances of this class should be
 * mutable. Varios getters and setters are provided so that date manipulation is
 * as convenient as possible.
 *
 * @author Amir
 * @author ebraminio
 */
public abstract class AbstractDate {

    public static AbstractDate getInstance() {
        return getDateImp(null, null, null, CalendarType.GREGORIAN);
    }

    public static AbstractDate getInstance(CalendarType type) {
        return getDateImp(null, TimeZone.getDefault(), Locale.getDefault(), type);
    }

    public static AbstractDate getInstance(TimeZone zone, CalendarType type) {
        return getDateImp(null, zone, Locale.getDefault(), type);
    }

    public static AbstractDate getInstance(Calendar calendar, CalendarType type) {
        return getDateImp(calendar, null, null, type);
    }

    public static AbstractDate getInstance(TimeZone zone, Locale aLocale, CalendarType type) {
        return getDateImp(null, zone, aLocale, type);
    }

    private static AbstractDate getDateImp(Calendar calendar, TimeZone zone, Locale aLocale,
                                           CalendarType type) {
        switch (type) {
            case SHAMSI:
                if (calendar != null) {
                    return new PersianDate(calendar);
                } else {
                    return new PersianDate(Calendar.getInstance(zone, aLocale));
                }
            case ISLAMIC:
                if (calendar != null) {
                    return new IslamicDate(calendar);
                } else {
                    return new IslamicDate(Calendar.getInstance(zone, aLocale));
                }
            default:
            case GREGORIAN:
                if (calendar != null) {
                    return new CivilDate(calendar);
                } else {
                    return new CivilDate(Calendar.getInstance(zone, aLocale));
                }
        }
    }

    public void setDate(int year, int month, int day) {
        setYear(year);
        setMonth(month);
        setDayOfMonth(day);
    }

    public abstract int getYear();

    public abstract void setYear(int year);

    public abstract int getMonth();

    public abstract void setMonth(int month);

    public abstract int getDayOfMonth();

    public abstract void setDayOfMonth(int day);

    public abstract int getDayOfWeek();

    public abstract int getDayOfYear();

    public abstract int getWeekOfYear();

    public abstract int getWeekOfMonth(int firstDayOfWeek);

    public abstract void rollDay(int amount, boolean up);

    public abstract void rollMonth(int amount, boolean up);

    public abstract void rollYear(int amount, boolean up);

    /**
     * Returns a string specifying the event of this date, or null if there are
     * no events for this year.
     */
    public abstract String getEvent();

    public abstract boolean isLeapYear();

    public abstract AbstractDate clone();
}
