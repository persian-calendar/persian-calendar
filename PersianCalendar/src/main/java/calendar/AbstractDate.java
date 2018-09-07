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
    public abstract int getYear();
    public abstract int getMonth();
    public abstract int getDayOfMonth();
}
