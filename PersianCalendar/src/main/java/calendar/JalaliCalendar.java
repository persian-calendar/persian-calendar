package calendar;

public class JalaliCalendar {
    private static JalaliCalendar thisInstance;

    public enum MONTH_NAMES_TYPE {PERSIAN, DARI}

    private MONTH_NAMES_TYPE type = MONTH_NAMES_TYPE.PERSIAN;

    public static JalaliCalendar getInstance() {
        if (thisInstance == null) {
            thisInstance = new JalaliCalendar();
        }
        return thisInstance;
    }

    public void setType(MONTH_NAMES_TYPE type) {
        this.type = type;
    }

    public MONTH_NAMES_TYPE getType() {
        return type;
    }

    public PersianDate getToday() {
        CivilDate civilDate = new CivilDate();
        PersianDate equivalentPersianDate = DateConverter.civilToPersian(civilDate);
        switch (getType()) {
            case DARI:
                equivalentPersianDate.setDari(true);
                break;
            default:
                equivalentPersianDate.setDari(false);
                break;
        }
        return equivalentPersianDate;
    }
}
