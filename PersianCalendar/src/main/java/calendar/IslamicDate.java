package calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class IslamicDate extends AbstractDate {
    public IslamicDate(int year, int month, int day) {
        super(year, month, day);
    }

    public boolean equals(IslamicDate islamicDate) {
        return getDayOfMonth() == islamicDate.getDayOfMonth()
                && getMonth() == islamicDate.getMonth()
                && (getYear() == -1 || getYear() == islamicDate.getYear());
    }
}
