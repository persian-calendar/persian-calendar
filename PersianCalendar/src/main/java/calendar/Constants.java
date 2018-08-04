package calendar;

public class Constants {
    public final static String DAY = "day";
    public final static String IS_OUT_OF_RANGE = "is out of range!";
    public final static String NOT_IMPLEMENTED_YET = "not implemented yet!";
    public final static String MONTH = "month";
    public final static String YEAR_0_IS_INVALID = "Year 0 is invalid!";
    
    public static final int CIVIL_CALENDAR = 0;
    public static final int PERSIAN_CALENDAR = 1;
    public static final int ISLAMIC_CALENDAR = 2;

    @IntDef({CIVIL_CALENDAR, PERSIAN_CALENDAR,ISLAMIC_CALENDAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CalendarType {
    }
}
