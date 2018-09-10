package com.byagowi.persiancalendar.util;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.SparseArray;

import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.core.app.ActivityCompat;
import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.CivilDate;
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
                return new IslamicDate(year, month, day).toJdn();
            case GREGORIAN:
                return new CivilDate(year, month, day).toJdn();
            case SHAMSI:
            default:
                return new PersianDate(year, month, day).toJdn();
        }
    }

    static public int getMonthLength(CalendarType calendar, int year, int month) {
        switch (calendar) {
            case ISLAMIC:
                return (int) (new IslamicDate(month == 12 ? year + 1 : year, month == 12 ? 1 : month + 1, 1).toJdn() -
                        new IslamicDate(year, month, 1).toJdn());
            case GREGORIAN:
                return (int) (new CivilDate(month == 12 ? year + 1 : year, month == 12 ? 1 : month + 1, 1).toJdn() -
                        new CivilDate(year, month, 1).toJdn());
            case SHAMSI:
            default:
                return (int) (new PersianDate(month == 12 ? year + 1 : year, month == 12 ? 1 : month + 1, 1).toJdn() -
                        new PersianDate(year, month, 1).toJdn());
        }
    }

    static public AbstractDate getDateFromJdnOfCalendar(CalendarType calendar, long jdn) {
        switch (calendar) {
            case ISLAMIC:
                return new IslamicDate(jdn);
            case GREGORIAN:
                return new CivilDate(jdn);
            case SHAMSI:
            default:
                return new PersianDate(jdn);
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

    static public CivilDate getGregorianToday() {
        return calendarToCivilDate(makeCalendarFromDate(new Date()));
    }

    static public long getTodayJdn() {
        return getGregorianToday().toJdn();
    }

    static public PersianDate getPersianToday() {
        return new PersianDate(getTodayJdn());
    }

    static public IslamicDate getIslamicToday() {
        return new IslamicDate(getTodayJdn());
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

    static public String dayTitleSummary(AbstractDate date) {
        return Utils.getWeekDayName(date) + Utils.getComma() + " " + dateToString(date);
    }

    static public String getMonthName(AbstractDate date) {
        return Utils.monthsNamesOfCalendar(date)[date.getMonth() - 1];
    }

    static public int getDayOfWeekFromJdn(long jdn) {
        return civilDateToCalendar(new CivilDate(jdn)).get(Calendar.DAY_OF_WEEK) % 7;
    }

    public static int calculateWeekOfYear(long jdn, long startOfYearJdn) {
        long dayOfYear = jdn - startOfYearJdn;
        return (int) Math.ceil(1 + (dayOfYear - Utils.fixDayOfWeekReverse(getDayOfWeekFromJdn(jdn))) / 7.);
    }

    static public String dateToString(AbstractDate date) {
        return Utils.formatNumber(date.getDayOfMonth()) + ' ' + getMonthName(date) + ' ' +
                Utils.formatNumber(date.getYear());
    }

    public static List<DeviceCalendarEvent> getAllEnabledAppointments(Context context) {
        Calendar startingDate = Calendar.getInstance();
        startingDate.add(Calendar.YEAR, -1);
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvent = new SparseArray<>();
        List<DeviceCalendarEvent> allEnabledAppointments = new ArrayList<>();
        readDeviceEvents(context, deviceCalendarEvent, allEnabledAppointments, startingDate,
                TimeUnit.DAYS.toMillis(365 * 2));
        return allEnabledAppointments;
    }

    private final static long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    public static SparseArray<List<DeviceCalendarEvent>> readDayDeviceEvents(Context context, long jdn) {
        if (jdn == -1) {
            jdn = getTodayJdn();
        }
        Calendar startingDate = civilDateToCalendar(new CivilDate(jdn));
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvent = new SparseArray<>();
        List<DeviceCalendarEvent> allEnabledAppointments = new ArrayList<>();
        readDeviceEvents(context, deviceCalendarEvent, allEnabledAppointments, startingDate, DAY_IN_MILLIS);
        return deviceCalendarEvent;
    }

    public static SparseArray<List<DeviceCalendarEvent>> readMonthDeviceEvents(Context context, long jdn) {
        Calendar startingDate = civilDateToCalendar(new CivilDate(jdn));
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvent = new SparseArray<>();
        List<DeviceCalendarEvent> allEnabledAppointments = new ArrayList<>();
        readDeviceEvents(context, deviceCalendarEvent, allEnabledAppointments, startingDate, 32L * DAY_IN_MILLIS);
        return deviceCalendarEvent;
    }

    private static void readDeviceEvents(Context context,
                                         SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents,
                                         List<DeviceCalendarEvent> allEnabledAppointments,
                                         Calendar startingDate,
                                         long rangeInMillis) {
        if (!Utils.isShowDeviceCalendarEvents()) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, startingDate.getTimeInMillis() - DAY_IN_MILLIS);
            ContentUris.appendId(builder, startingDate.getTimeInMillis() + rangeInMillis + DAY_IN_MILLIS);

            Cursor cursor = context.getContentResolver().query(builder.build(),
                    new String[]{
                            CalendarContract.Instances.EVENT_ID,       // 0
                            CalendarContract.Instances.TITLE,          // 1
                            CalendarContract.Instances.DESCRIPTION,    // 2
                            CalendarContract.Instances.BEGIN,          // 3
                            CalendarContract.Instances.END,            // 4
                            CalendarContract.Instances.EVENT_LOCATION, // 5
                            CalendarContract.Instances.RRULE,          // 6
                            CalendarContract.Instances.VISIBLE,        // 7
                            CalendarContract.Instances.ALL_DAY,        // 8
                            CalendarContract.Instances.EVENT_COLOR     // 10
                    }, null, null, null);

            if (cursor == null) {
                return;
            }

            int i = 0;
            while (cursor.moveToNext()) {
                if (!cursor.getString(7).equals("1"))
                    continue;

                boolean allDay = false;
                if (cursor.getString(8).equals("1"))
                    allDay = true;

                Date startDate = new Date(cursor.getLong(3));
                Date endDate = new Date(cursor.getLong(4));
                Calendar startCalendar = CalendarUtils.makeCalendarFromDate(startDate);
                Calendar endCalendar = CalendarUtils.makeCalendarFromDate(endDate);

                CivilDate civilDate = calendarToCivilDate(startCalendar);

                int month = civilDate.getMonth();
                int day = civilDate.getDayOfMonth();

                List<DeviceCalendarEvent> list = deviceCalendarEvents.get(month * 100 + day);
                if (list == null) {
                    list = new ArrayList<>();
                    deviceCalendarEvents.put(month * 100 + day, list);
                }

                String title = cursor.getString(1);
                if (allDay) {
                    title = "\uD83D\uDCC5 " + title;
                } else {
                    title = "\uD83D\uDD53 " + title;
                    title += " (" + UIUtils.baseClockToString(startCalendar.get(Calendar.HOUR_OF_DAY),
                            startCalendar.get(Calendar.MINUTE));

                    if (cursor.getLong(3) != cursor.getLong(4) && cursor.getLong(4) != 0) {
                        title += "-" + UIUtils.baseClockToString(endCalendar.get(Calendar.HOUR_OF_DAY),
                                endCalendar.get(Calendar.MINUTE));
                    }

                    title += ")";
                }
                DeviceCalendarEvent event = new DeviceCalendarEvent(
                        cursor.getInt(0),
                        title,
                        cursor.getString(2),
                        startDate,
                        endDate,
                        cursor.getString(5),
                        civilDate,
                        cursor.getString(9)
                );
                list.add(event);
                allEnabledAppointments.add(event);

                // Don't go more than 1k events on any case
                if (++i == 1000) break;
            }
            cursor.close();
        } catch (Exception e) {
            // We don't like crash addition from here, just catch all of exceptions
            Log.e("", "Error on device calendar events read", e);
        }
    }

    // Based on Mehdi's work
    public static boolean isMoonInScorpio(PersianDate persianDate, IslamicDate islamicDate) {
        int res = (int) (((((float) (islamicDate.getDayOfMonth() + 1) * 12.2f) +
                (persianDate.getDayOfMonth() + 1)) / 30.f) + persianDate.getMonth());
        if (res > 12) res -= 12;
        return res == 8;
    }

    // Extra helpers
    public static Calendar civilDateToCalendar(CivilDate civilDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, civilDate.getYear());
        cal.set(Calendar.MONTH, civilDate.getMonth() - 1);
        cal.set(Calendar.DAY_OF_MONTH, civilDate.getDayOfMonth());
        return cal;
    }

    private static CivilDate calendarToCivilDate(Calendar calendar) {
        return new CivilDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }
}
