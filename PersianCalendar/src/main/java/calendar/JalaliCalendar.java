package calendar;

import android.content.Context;

import com.byagowi.persiancalendar.Utils;

public class JalaliCalendar {
    private static final String TAG = "JalaliCalendar";
    private static JalaliCalendar thisInstance;
    private Utils utils = Utils.getInstance();
    private Context context;

    private JalaliCalendar(Context ctx) {
        context = ctx;
    }

    public static JalaliCalendar getInstance(Context ctx) {
        if (thisInstance == null) {
            thisInstance = new JalaliCalendar(ctx);
        }
        return thisInstance;
    }

    public MonthNameType getType() {
        return utils.getMonthNameType(context);
    }

    public PersianDate getToday() {
        CivilDate civilDate = new CivilDate();
        PersianDate equivalentPersianDate = DateConverter.civilToPersian(civilDate);
        equivalentPersianDate.setNameType(getType());
        return equivalentPersianDate;
    }
}
