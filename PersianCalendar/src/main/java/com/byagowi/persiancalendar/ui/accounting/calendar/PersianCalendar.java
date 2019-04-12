package com.byagowi.persiancalendar.ui.accounting.calendar;

import org.jetbrains.annotations.NotNull;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class PersianCalendar extends GregorianCalendar {
    private static final long serialVersionUID = 5541422440580682494L;
    private String delimiter = "/";
    private int persianDay;
    private int persianMonth;
    private int persianYear;

    private long convertToMilis(long julianDate) {
        return ((PersianCalendarConstants.MILLIS_OF_A_DAY * julianDate) + PersianCalendarConstants.MILLIS_JULIAN_EPOCH) + PersianCalendarUtils.ceil((double) (getTimeInMillis() - PersianCalendarConstants.MILLIS_JULIAN_EPOCH), 8.64E7d);
    }

    PersianCalendar(long millis) {
        setTimeInMillis(millis);
    }

    public PersianCalendar() {
        setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private void calculatePersianDate() {
        long PersianRowDate = PersianCalendarUtils.julianToPersian(((long) Math.floor((double) (getTimeInMillis() - PersianCalendarConstants.MILLIS_JULIAN_EPOCH))) / PersianCalendarConstants.MILLIS_OF_A_DAY);
        long year = PersianRowDate >> 16;
        int month = ((int) (65280 & PersianRowDate)) >> 8;
        int day = (int) (255 & PersianRowDate);
        if (year <= 0) {
            year--;
        }
        persianYear = (int) year;
        persianMonth = month;
        persianDay = day;
    }

    public boolean isPersianLeapYear() {
        return PersianCalendarUtils.isPersianLeapYear(persianYear);
    }

    void setPersianDate(int persianYear, int persianMonth, int persianDay) {
        persianYear = persianYear;
        persianMonth = persianMonth;
        persianDay = persianDay;
        setTimeInMillis(convertToMilis(PersianCalendarUtils.persianToJulian(persianYear > 0 ? (long) persianYear : (long) (persianYear + 1), persianMonth - 1, persianDay)));
    }

    public int getPersianYear() {
        return persianYear;
    }

    public int getPersianMonth() {
        return persianMonth + 1;
    }

    private String getPersianMonthName() {
        return PersianCalendarConstants.persianMonthNames[persianMonth];
    }

    public int getPersianDay() {
        return persianDay;
    }

    public String getPersianWeekDayName() {
        switch (get(7)) {
            case 1:
                return PersianCalendarConstants.persianWeekDays[1];
            case 2:
                return PersianCalendarConstants.persianWeekDays[2];
            case 3:
                return PersianCalendarConstants.persianWeekDays[3];
            case 4:
                return PersianCalendarConstants.persianWeekDays[4];
            case 5:
                return PersianCalendarConstants.persianWeekDays[5];
            case 7:
                return PersianCalendarConstants.persianWeekDays[0];
            default:
                return PersianCalendarConstants.persianWeekDays[6];
        }
    }

    String getPersianLongDate() {
        return getPersianWeekDayName() + "  " + persianDay + "  " + getPersianMonthName() + "  " + persianYear;
    }

    public String getPersianLongDateAndTime() {
        return getPersianLongDate() + " ساعت " + get(11) + ":" + get(12) + ":" + get(13);
    }

    private String getPersianShortDate() {
        return "" + formatToMilitary(persianYear) + delimiter + formatToMilitary(getPersianMonth()) + delimiter + formatToMilitary(persianDay);
    }

    public String getPersianShortDateTime() {
        return "" + formatToMilitary(persianYear) + delimiter + formatToMilitary(getPersianMonth()) + delimiter + formatToMilitary(persianDay) + " " + formatToMilitary(get(11)) + ":" + formatToMilitary(get(12)) + ":" + formatToMilitary(get(13));
    }

    private String formatToMilitary(int i) {
        return i < 9 ? "0" + i : String.valueOf(i);
    }

    public void addPersianDate(int field, int amount) {
        if (amount != 0) {
            if (field < 0 || field >= 15) {
                throw new IllegalArgumentException();
            } else if (field == 1) {
                setPersianDate(persianYear + amount, getPersianMonth(), persianDay);
            } else if (field == 2) {
                setPersianDate(persianYear + ((getPersianMonth() + amount) / 12), (getPersianMonth() + amount) % 12, persianDay);
            } else {
                add(field, amount);
                calculatePersianDate();
            }
        }
    }

    public void parse(String dateString) {
        PersianCalendar p = new PersianDateParser(dateString, delimiter).getPersianDate();
        setPersianDate(p.getPersianYear(), p.getPersianMonth(), p.getPersianDay());
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        delimiter = delimiter;
    }

    @NotNull
    public String toString() {
        String str = super.toString();
        return str.substring(0, str.length() - 1) + ",PersianDate=" + getPersianShortDate() + "]";
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public void set(int field, int value) {
        super.set(field, value);
        calculatePersianDate();
    }

    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);
        calculatePersianDate();
    }

    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        calculatePersianDate();
    }
}
