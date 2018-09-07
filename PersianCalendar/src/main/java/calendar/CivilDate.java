package calendar;

import java.util.Calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class CivilDate extends AbstractDate {
    public CivilDate(int year, int month, int day) {
        super(year, month, day);
    }

    public boolean equals(CivilDate civilDate) {
        return getDayOfMonth() == civilDate.getDayOfMonth()
                && getMonth() == civilDate.getMonth()
                && (getYear() == -1 || getYear() == civilDate.getYear());
    }

    public int getDayOfWeek() {
        return toCalendar().get(Calendar.DAY_OF_WEEK);
    }

    public Calendar toCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.MONTH, getMonth() - 1);
        cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth());
        return cal;
    }

    public static CivilDate fromCalendar(Calendar calendar) {
        return new CivilDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }
}
