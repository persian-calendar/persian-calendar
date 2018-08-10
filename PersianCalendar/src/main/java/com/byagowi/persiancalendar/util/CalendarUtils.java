package com.byagowi.persiancalendar.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

public class CalendarUtils {
    static public AbstractDate getDateOfCalendar(CalendarType calendar, int year, int month, int day) {
        switch (calendar) {
            case ISLAMIC:
                return new IslamicDate(year, month, day);
            case GREGORIAN:
                return new CivilDate(year, month, day);
            case SHAMSI:
            default:
                return new PersianDate(year, month, day);
        }
    }

    static public long getJdnOfCalendar(CalendarType calendar, int year, int month, int day) {
        switch (calendar) {
            case ISLAMIC:
                return DateConverter.islamicToJdn(year, month, day);
            case GREGORIAN:
                return DateConverter.civilToJdn(year, month, day);
            case SHAMSI:
            default:
                return DateConverter.persianToJdn(year, month, day);
        }
    }

    static public AbstractDate getDateFromJdnOfCalendar(CalendarType calendar, long jdn) {
        switch (calendar) {
            case ISLAMIC:
                return DateConverter.jdnToIslamic(jdn);
            case GREGORIAN:
                return DateConverter.jdnToCivil(jdn);
            case SHAMSI:
            default:
                return DateConverter.jdnToPersian(jdn);
        }
    }

    static public long getJdnDate(AbstractDate date) {
        if (date instanceof PersianDate) {
            return DateConverter.persianToJdn((PersianDate) date);
        } else if (date instanceof IslamicDate) {
            return DateConverter.islamicToJdn((IslamicDate) date);
        } else if (date instanceof CivilDate) {
            return DateConverter.civilToJdn((CivilDate) date);
        } else {
            return 0;
        }
    }

    static public Calendar makeCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (Utils.isIranTime()) {
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return calendar;
    }

    static public String toLinearDate(AbstractDate date) {
        return String.format("%s/%s/%s", Utils.formatNumber(date.getYear()),
                Utils.formatNumber(date.getMonth()), Utils.formatNumber(date.getDayOfMonth()));
    }

    static public PersianDate getPersianToday() {
        return DateConverter.civilToPersian(new CivilDate(makeCalendarFromDate(new Date())));
    }

    static public long getTodayJdn() {
        return DateConverter.civilToJdn(new CivilDate(makeCalendarFromDate(new Date())));
    }

    static public IslamicDate getIslamicToday() {
        return DateConverter.civilToIslamic(new CivilDate(makeCalendarFromDate(new Date())), Utils.getIslamicOffset());
    }

    static public CivilDate getGregorianToday() {
        return new CivilDate(makeCalendarFromDate(new Date()));
    }

    static public AbstractDate getTodayOfCalendar(CalendarType calendar) {
        switch (calendar) {
            case ISLAMIC:
                return getIslamicToday();
            case GREGORIAN:
                return getGregorianToday();
            case SHAMSI:
            default:
                return getPersianToday();
        }
    }

    static public String dateStringOfOtherCalendar(CalendarType calendar, long jdn) {
        switch (calendar) {
            case ISLAMIC:
                return Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
                        Utils.getComma() + " " +
                        Utils.dateToString(DateConverter.jdnToCivil(jdn));
            case GREGORIAN:
                return Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
                        Utils.getComma() + " " +
                        Utils.dateToString(DateConverter.civilToIslamic(
                                DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()));
            case SHAMSI:
            default:
                return Utils.dateToString(DateConverter.jdnToCivil(jdn)) +
                        Utils.getComma() + " " +
                        Utils.dateToString(DateConverter.civilToIslamic(
                                DateConverter.jdnToCivil(jdn), Utils.getIslamicOffset()));
        }
    }

    static public String dayTitleSummary(AbstractDate date) {
        return Utils.getWeekDayName(date) + Utils.getComma() + " " + Utils.dateToString(date);
    }

    static public String getMonthName(AbstractDate date) {
        return Utils.monthsNamesOfCalendar(date)[date.getMonth() - 1];
    }

    static public int getDayOfWeekFromJdn(long jdn) {
        return DateConverter.jdnToCivil(jdn).getDayOfWeek() % 7;
    }

    // based on R.array.calendar_type order
    static public CalendarType calendarTypeFromPosition(int position) {
        switch (position) {
            case 0:
                return CalendarType.SHAMSI;
            case 1:
                return CalendarType.ISLAMIC;
            default:
                return CalendarType.GREGORIAN;
        }
    }

    static public int positionFromCalendarType(CalendarType calendar) {
        switch (calendar) {
            case SHAMSI:
                return 0;
            case ISLAMIC:
                return 1;
            default:
                return 2;
        }
    }
}
