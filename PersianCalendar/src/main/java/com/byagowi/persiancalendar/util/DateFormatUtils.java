package com.byagowi.persiancalendar.util;

import android.content.Context;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

public class DateFormatUtils {
    private static final String TAG = "DateFormatUtils";
    private static DateFormatUtils instance;
    private Context context;

    private enum PERSIAN_MONTH_NAME_CODES {
        FARVARDIN, ORDIBEHESHT, KHORDARD, TIR, MORDAD,
        SHAHRIVAR, MEHR, ABAN, AZAR, DEY, BAHMAN,
        ESFAND
    }

    private enum ISLAMIC_MONTH_NAME_CODES {
        MUHARRAM, SAFAR, RABI_OL_AWAL, RABI_O_THANI,
        JAMADI_OL_AWLA, JAMADI_O_THANI, RAJAB, SHABAN,
        RAMADAN, SHAWAL, ZOLQADA, ZOLHAJJA
    }

    private enum CIVIL_MONTH_NAME_CODES {
        JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE,
        JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER,
        DECEMBER
    }

    private enum WEEKDAY_NAME_CODES {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY,
        FRIDAY, SATURDAY
    }

    public enum DateField {
        // year
        YEAR,

        // month
        MONTH,

        // week
        WEEK_OF_MONTH, WEEK_OF_YEAR,

        // day
        DAY_OF_MONTH, DAY_OF_WEEK, DAY_OF_YEAR,

        // strings
        MONTH_NAME, DAY_OF_WEEK_NAME, COMMA
    }

    private DateFormatUtils() {}

    private DateFormatUtils(Context c) {
        context = c;
    }

    public static DateFormatUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DateFormatUtils(context);
        }
        return instance;
    }

    public String format(PersianDate date, DateField[] dateFieldOrder) {
        StringBuilder sb = new StringBuilder();
        for (DateField df : dateFieldOrder) {
            switch (df) {
                case YEAR:
                    sb.append(date.getYear());
                    break;
                case MONTH:
                    sb.append(date.getMonth());
                    break;
                case WEEK_OF_MONTH:
                    sb.append(date.getWeekOfMonth());
                    break;
                case WEEK_OF_YEAR:
                    sb.append(date.getWeekOfYear());
                    break;
                case DAY_OF_MONTH:
                    sb.append(date.getDayOfMonth());
                    break;
                case DAY_OF_WEEK:
                    sb.append(date.getDayOfWeek());
                    break;
                case DAY_OF_YEAR:
                    sb.append(date.getDayOfYear());
                    break;

                case MONTH_NAME:
                    sb.append(getMonthName(date));
                    break;
                case DAY_OF_WEEK_NAME:
                    sb.append(getWeekDayName(date));
                    break;
                case COMMA:
                    sb.append(Utils.PERSIAN_COMMA);
                    break;
            }
            sb.append(" ");
        }

        return sb.toString();
    }


    public String getWeekDayName(AbstractDate date) {
        // only CivilDate return a good value for dayOfWeek
        CivilDate civilDate;
        if (date.getClass().equals(PersianDate.class)) {
            civilDate = DateConverter.persianToCivil((PersianDate) date);
        } else if (date.getClass().equals(IslamicDate.class)) {
            civilDate = DateConverter.islamicToCivil((IslamicDate) date);
        } else {
            civilDate = (CivilDate) date;
        }

        int dayOfWeek = civilDate.getDayOfWeek() - 1;
        WEEKDAY_NAME_CODES weekday_name_code = WEEKDAY_NAME_CODES.values()[dayOfWeek];

        String weekdayName;
        switch (weekday_name_code) {
            case MONDAY:
                weekdayName = context.getString(R.string.MONDAY);
                break;
            case TUESDAY:
                weekdayName = context.getString(R.string.TUESDAY);
                break;
            case WEDNESDAY:
                weekdayName = context.getString(R.string.WEDNESDAY);
                break;
            case THURSDAY:
                weekdayName = context.getString(R.string.THURSDAY);
                break;
            case FRIDAY:
                weekdayName = context.getString(R.string.FRIDAY);
                break;
            case SATURDAY:
                weekdayName = context.getString(R.string.SATURDAY);
                break;
            case SUNDAY:
            default:
                weekdayName = context.getString(R.string.SUNDAY);
                break;
        }
        return weekdayName;
    }

    public String getMonthName(AbstractDate date) {
        String monthName = "";
        int monthOrdinal = date.getMonth() - 1;
        if (date.getClass().equals(PersianDate.class)) {
            switch (PERSIAN_MONTH_NAME_CODES.values()[monthOrdinal]) {
                case ORDIBEHESHT:
                    monthName = context.getString(R.string.ORDIBEHESHT);
                    break;
                case KHORDARD:
                    monthName = context.getString(R.string.KHORDARD);
                    break;
                case TIR:
                    monthName = context.getString(R.string.TIR);
                    break;
                case MORDAD:
                    monthName = context.getString(R.string.MORDAD);
                    break;
                case SHAHRIVAR:
                    monthName = context.getString(R.string.SHAHRIVAR);
                    break;
                case MEHR:
                    monthName = context.getString(R.string.MEHR);
                    break;
                case ABAN:
                    monthName = context.getString(R.string.ABAN);
                    break;
                case AZAR:
                    monthName = context.getString(R.string.AZAR);
                    break;
                case DEY:
                    monthName = context.getString(R.string.DEY);
                    break;
                case BAHMAN:
                    monthName = context.getString(R.string.BAHMAN);
                    break;
                case ESFAND:
                    monthName = context.getString(R.string.ESFAND);
                    break;
                case FARVARDIN:
                default:
                    monthName = context.getString(R.string.FARVARDIN);
                    break;
            }
        }

        if (date.getClass().equals(IslamicDate.class)) {
            switch (ISLAMIC_MONTH_NAME_CODES.values()[monthOrdinal]) {
                case SAFAR:
                    monthName = context.getString(R.string.SAFAR);
                    break;
                case RABI_OL_AWAL:
                    monthName = context.getString(R.string.RABI_OL_AWAL);
                    break;
                case RABI_O_THANI:
                    monthName = context.getString(R.string.RABI_O_THANI);
                    break;
                case JAMADI_OL_AWLA:
                    monthName = context.getString(R.string.JAMADI_OL_AWLA);
                    break;
                case JAMADI_O_THANI:
                    monthName = context.getString(R.string.JAMADI_O_THANI);
                    break;
                case RAJAB:
                    monthName = context.getString(R.string.RAJAB);
                    break;
                case SHABAN:
                    monthName = context.getString(R.string.SHABAN);
                    break;
                case RAMADAN:
                    monthName = context.getString(R.string.RAMADAN);
                    break;
                case SHAWAL:
                    monthName = context.getString(R.string.SHAWAL);
                    break;
                case ZOLQADA:
                    monthName = context.getString(R.string.ZOLQADA);
                    break;
                case ZOLHAJJA:
                    monthName = context.getString(R.string.ZOLHAJJA);
                    break;
                case MUHARRAM:
                default:
                    monthName = context.getString(R.string.MUHARRAM);
                    break;
            }
        }

        if (date.getClass().equals(CivilDate.class)) {
            switch (CIVIL_MONTH_NAME_CODES.values()[monthOrdinal]) {
                case FEBRUARY:
                    monthName = context.getString(R.string.FEBRUARY);
                    break;
                case MARCH:
                    monthName = context.getString(R.string.MARCH);
                    break;
                case APRIL:
                    monthName = context.getString(R.string.APRIL);
                    break;
                case MAY:
                    monthName = context.getString(R.string.MAY);
                    break;
                case JUNE:
                    monthName = context.getString(R.string.JUNE);
                    break;
                case JULY:
                    monthName = context.getString(R.string.JULY);
                    break;
                case AUGUST:
                    monthName = context.getString(R.string.AUGUST);
                    break;
                case SEPTEMBER:
                    monthName = context.getString(R.string.SEPTEMBER);
                    break;
                case OCTOBER:
                    monthName = context.getString(R.string.OCTOBER);
                    break;
                case NOVEMBER:
                    monthName = context.getString(R.string.NOVEMBER);
                    break;
                case DECEMBER:
                    monthName = context.getString(R.string.DECEMBER);
                    break;
                case JANUARY:
                default:
                    monthName = context.getString(R.string.JANUARY);
                    break;
            }
        }

        return monthName;
    }
}
