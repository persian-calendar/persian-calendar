package calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class PersianDate extends AbstractDate {
    public PersianDate(int year, int month, int day) {
        super(year, month, day);
    }

    public boolean equals(PersianDate persianDate) {
        return getDayOfMonth() == persianDate.getDayOfMonth()
                && getMonth() == persianDate.getMonth()
                && (getYear() == -1 || getYear() == persianDate.getYear());
    }
}
