package com.byagowi.persiancalendar.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.calendar.CivilDate;
import com.byagowi.persiancalendar.calendar.IslamicDate;
import com.byagowi.persiancalendar.calendar.PersianDate;
import com.byagowi.persiancalendar.entities.AbstractEvent;
import com.byagowi.persiancalendar.entities.CalendarTypeItem;
import com.byagowi.persiancalendar.entities.CityItem;
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entities.GregorianCalendarEvent;
import com.byagowi.persiancalendar.entities.IslamicCalendarEvent;
import com.byagowi.persiancalendar.entities.PersianCalendarEvent;
import com.byagowi.persiancalendar.entities.ShiftWorkRecord;
import com.byagowi.persiancalendar.equinox.Equinox;
import com.byagowi.persiancalendar.praytimes.CalculationMethod;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.Coordinate;
import com.byagowi.persiancalendar.praytimes.PrayTimes;
import com.byagowi.persiancalendar.praytimes.PrayTimesCalculator;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.service.AthanNotification;
import com.byagowi.persiancalendar.service.BroadcastReceivers;
import com.byagowi.persiancalendar.service.UpdateWorker;
import com.byagowi.persiancalendar.ui.AthanActivity;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.byagowi.persiancalendar.Constants.ALARMS_BASE_ID;
import static com.byagowi.persiancalendar.Constants.ARABIC_DIGITS;
import static com.byagowi.persiancalendar.Constants.ARABIC_INDIC_DIGITS;
import static com.byagowi.persiancalendar.Constants.BLUE_THEME;
import static com.byagowi.persiancalendar.Constants.BROADCAST_ALARM;
import static com.byagowi.persiancalendar.Constants.BROADCAST_RESTART_APP;
import static com.byagowi.persiancalendar.Constants.BROADCAST_UPDATE_APP;
import static com.byagowi.persiancalendar.Constants.CJK_DIGITS;
import static com.byagowi.persiancalendar.Constants.DARK_THEME;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS_AR;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS_CKB;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ALTITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_AM;
import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ATHAN_VOLUME;
import static com.byagowi.persiancalendar.Constants.DEFAULT_CITY;
import static com.byagowi.persiancalendar.Constants.DEFAULT_IRAN_TIME;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ISLAMIC_OFFSET;
import static com.byagowi.persiancalendar.Constants.DEFAULT_LATITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_LONGITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFICATION_ATHAN;
import static com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE_LOCK_SCREEN;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PM;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PRAY_TIME_METHOD;
import static com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WEEK_ENDS;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WEEK_START;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_CLOCK;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_IN_24;
import static com.byagowi.persiancalendar.Constants.KEY_EXTRA_PRAYER_KEY;
import static com.byagowi.persiancalendar.Constants.LANG_AR;
import static com.byagowi.persiancalendar.Constants.LANG_AZB;
import static com.byagowi.persiancalendar.Constants.LANG_CKB;
import static com.byagowi.persiancalendar.Constants.LANG_EN_IR;
import static com.byagowi.persiancalendar.Constants.LANG_EN_US;
import static com.byagowi.persiancalendar.Constants.LANG_FA;
import static com.byagowi.persiancalendar.Constants.LANG_FA_AF;
import static com.byagowi.persiancalendar.Constants.LANG_GLK;
import static com.byagowi.persiancalendar.Constants.LANG_JA;
import static com.byagowi.persiancalendar.Constants.LANG_PS;
import static com.byagowi.persiancalendar.Constants.LANG_UR;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.LOAD_APP_ID;
import static com.byagowi.persiancalendar.Constants.MODERN_THEME;
import static com.byagowi.persiancalendar.Constants.PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_ALTITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_ALARM;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_GAP;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_URI;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_VOLUME;
import static com.byagowi.persiancalendar.Constants.PREF_GEOCODED_CITYNAME;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;
import static com.byagowi.persiancalendar.Constants.PREF_IRAN_TIME;
import static com.byagowi.persiancalendar.Constants.PREF_ISLAMIC_OFFSET;
import static com.byagowi.persiancalendar.Constants.PREF_LATITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_LONGITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_MAIN_CALENDAR_KEY;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFICATION_ATHAN;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE_LOCK_SCREEN;
import static com.byagowi.persiancalendar.Constants.PREF_OTHER_CALENDARS_KEY;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_PRAY_TIME_METHOD;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_LOCATION;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.PREF_SHIFT_WORK_RECURS;
import static com.byagowi.persiancalendar.Constants.PREF_SHIFT_WORK_SETTING;
import static com.byagowi.persiancalendar.Constants.PREF_SHIFT_WORK_STARTING_JDN;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;
import static com.byagowi.persiancalendar.Constants.PREF_WEEK_ENDS;
import static com.byagowi.persiancalendar.Constants.PREF_WEEK_START;
import static com.byagowi.persiancalendar.Constants.PREF_WIDGET_CLOCK;
import static com.byagowi.persiancalendar.Constants.PREF_WIDGET_IN_24;
import static com.byagowi.persiancalendar.Constants.THREE_HOURS_APP_ID;
import static com.byagowi.persiancalendar.Constants.ZWJ;

//import com.byagowi.persiancalendar.entities.Reminder;

/**
 * Common utilities that needed for this calendar
 *
 * @author ebraminio
 */

public class Utils {
    static private final String TAG = Utils.class.getName();
    private static final long twoSeconds = TimeUnit.SECONDS.toMillis(2);
    private final static long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    //
    //
    //
    // Service
    //
    private static final long DAY_IN_SECOND = 86400;
    private static final String CHANGE_DATE_TAG = "changeDate";
    private static final String UPDATE_TAG = "update";
    static private String[] emptyStringList = {"", "", "", "", "", "", "", "", "", "", "", "", ""};
    static private String[] persianMonths = emptyStringList;
    static private String[] islamicMonths = emptyStringList;
    static private String[] gregorianMonths = emptyStringList;
    static private String[] weekDays = emptyStringList;
    static private String[] weekDaysInitials = emptyStringList;
    static private char[] preferredDigits = PERSIAN_DIGITS;
    static private boolean clockIn24 = DEFAULT_WIDGET_IN_24;
    static private boolean iranTime = DEFAULT_IRAN_TIME;
    static private boolean notifyInLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN;
    static private boolean widgetClock = DEFAULT_WIDGET_CLOCK;
    static private boolean notifyDate = DEFAULT_NOTIFY_DATE;
    static private boolean notificationAthan = DEFAULT_NOTIFICATION_ATHAN;
    static private String selectedWidgetTextColor = DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
    //    static private String islamicOffset = DEFAULT_ISLAMIC_OFFSET;
    static private String calculationMethod = DEFAULT_PRAY_TIME_METHOD;
    static private String language = DEFAULT_APP_LANGUAGE;
    static private Coordinate coordinate;
    static private CalendarType mainCalendar = CalendarType.SHAMSI;
    static private CalendarType[] otherCalendars =
            new CalendarType[]{CalendarType.GREGORIAN, CalendarType.ISLAMIC};
    static private String spacedComma = "، ";
    static private boolean showWeekOfYear;
    static private boolean centerAlignWidgets;
    static private int weekStartOffset;
    static private boolean[] weekEnds = {};
    static private boolean showDeviceCalendarEvents;
    static private Set<String> whatToShowOnWidgets = Collections.emptySet();
    static private boolean astronomicalFeaturesEnabled;
    @StyleRes
    static private int appTheme = R.style.LightTheme;
    private static boolean talkBackEnabled = false;
    static private PrayTimes prayTimes;
    static private List<String> irCodeOrder = Arrays.asList("zz", "ir", "af", "iq");
    static private List<String> afCodeOrder = Arrays.asList("zz", "af", "ir", "iq");
    static private List<String> arCodeOrder = Arrays.asList("zz", "iq", "ir", "af");
    static private String cachedCityKey = "";
    static private CityItem cachedCity;
    static private SparseArray<List<PersianCalendarEvent>> sPersianCalendarEvents = new SparseArray<>();
    static private SparseArray<List<IslamicCalendarEvent>> sIslamicCalendarEvents = new SparseArray<>();
    static private SparseArray<List<GregorianCalendarEvent>> sGregorianCalendarEvents = new SparseArray<>();
    static private List<AbstractEvent> sAllEnabledEvents = new ArrayList<>();
    static private Map<String, String> sShiftWorkTitles = new HashMap<>();
    static private long sShiftWorkStartingJdn = -1;
    static private boolean sShiftWorkRecurs = true;
    static private List<ShiftWorkRecord> sShiftWorks = Collections.emptyList();
    private static boolean sIsIranHolidaysEnabled = true;
    static private int sShiftWorkPeriod = 0;
    static private String sAM = DEFAULT_AM;
    static private String sPM = DEFAULT_PM;
    private static long latestToastShowTime = -1;
    private static boolean numericalDatePreferred = false;
    private static String[] calendarTypesTitleAbbr = {};
//    private static List<Reminder> sReminderDetails = Collections.emptyList();
//
//    public static List<Reminder> getReminderDetails() {
//        return sReminderDetails;
//    }

    static public int getMaxSupportedYear() {
        return 1398;
    }

    static public ArrayList<ShiftWorkRecord> getShiftWorks() {
        return new ArrayList<>(sShiftWorks);
    }

    private static String getAmString() {
        return sAM;
    }

    private static String getPmString() {
        return sPM;
    }

    static public long getShiftWorkStartingJdn() {
        return sShiftWorkStartingJdn;
    }

    static public boolean getShiftWorkRecurs() {
        return sShiftWorkRecurs;
    }

    static public Map<String, String> getShiftWorkTitles() {
        return sShiftWorkTitles;
    }

    // This should be called before any use of Utils on the activity and services
    static public void initUtils(@NonNull Context context) {
        updateStoredPreference(context);
        applyAppLanguage(context);
        loadLanguageResource(context);
        loadAlarms(context);
        loadEvents(context);
    }

    @Nullable
    static public Coordinate getCoordinate(@NonNull Context context) {
        CityItem cityEntity = getCityFromPreference(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (cityEntity != null) {
            return cityEntity.getCoordinate();
        }

        try {
            String latitudeString = prefs.getString(PREF_LATITUDE, DEFAULT_LATITUDE);
            if (latitudeString == null) latitudeString = DEFAULT_LATITUDE;
            String longtitudeString = prefs.getString(PREF_LONGITUDE, DEFAULT_LONGITUDE);
            if (longtitudeString == null) longtitudeString = DEFAULT_LONGITUDE;
            String altitudeString = prefs.getString(PREF_ALTITUDE, DEFAULT_ALTITUDE);
            if (altitudeString == null) altitudeString = DEFAULT_ALTITUDE;

            Coordinate coord = new Coordinate(
                    Double.parseDouble(latitudeString),
                    Double.parseDouble(longtitudeString),
                    Double.parseDouble(altitudeString)
            );

            // If latitude or longitude is zero probably preference is not set yet
            if (coord.getLatitude() == 0 && coord.getLongitude() == 0) {
                return null;
            }

            return coord;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static public void updateStoredPreference(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        language = prefs.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE);
        preferredDigits = prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS)
                ? PERSIAN_DIGITS
                : ARABIC_DIGITS;
        if ((language.equals(LANG_AR) || language.equals(LANG_CKB)) && preferredDigits == PERSIAN_DIGITS)
            preferredDigits = ARABIC_INDIC_DIGITS;
        if (language.equals(LANG_JA) && preferredDigits == PERSIAN_DIGITS)
            preferredDigits = CJK_DIGITS;

        clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24);
        iranTime = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME);
        notifyInLockScreen = prefs.getBoolean(PREF_NOTIFY_DATE_LOCK_SCREEN,
                DEFAULT_NOTIFY_DATE_LOCK_SCREEN);
        widgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK);
        notifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE);
        notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN);
        centerAlignWidgets = prefs.getBoolean("CenterAlignWidgets", false);
        selectedWidgetTextColor = prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR,
                DEFAULT_SELECTED_WIDGET_TEXT_COLOR);
        // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
        // so switched to "Tehran" method as default calculation algorithm
        calculationMethod = prefs.getString(PREF_PRAY_TIME_METHOD, DEFAULT_PRAY_TIME_METHOD);
        coordinate = getCoordinate(context);
        try {
            mainCalendar = CalendarType.valueOf(prefs.getString(PREF_MAIN_CALENDAR_KEY, "SHAMSI"));
            String otherCalendarsString = prefs.getString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC");
            if (otherCalendarsString == null) otherCalendarsString = "GREGORIAN,ISLAMIC";
            otherCalendarsString = otherCalendarsString.trim();
            if (TextUtils.isEmpty(otherCalendarsString)) {
                otherCalendars = new CalendarType[0];
            } else {
                String[] calendars = otherCalendarsString.split(",");
                otherCalendars = new CalendarType[calendars.length];
                for (int i = 0; i < calendars.length; ++i) {
                    otherCalendars[i] = CalendarType.valueOf(calendars[i]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Fail on parsing calendar preference", e);
            mainCalendar = CalendarType.SHAMSI;
            otherCalendars = new CalendarType[]{CalendarType.GREGORIAN, CalendarType.ISLAMIC};
        }
        spacedComma = isNonArabicScriptSelected() ? ", " : "، ";
        showWeekOfYear = prefs.getBoolean("showWeekOfYearNumber", false);

        String weekStart = prefs.getString(PREF_WEEK_START, DEFAULT_WEEK_START);
        if (weekStart == null) weekStart = DEFAULT_WEEK_START;
        weekStartOffset = Integer.parseInt(weekStart);
        weekEnds = new boolean[7];
        Set<String> weekEndsSet = prefs.getStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS);
        if (weekEndsSet == null) weekEndsSet = DEFAULT_WEEK_ENDS;
        for (String s : weekEndsSet)
            weekEnds[Integer.parseInt(s)] = true;

        showDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false);
        Resources resources = context.getResources();
        whatToShowOnWidgets = prefs.getStringSet("what_to_show",
                new HashSet<>(Arrays.asList(resources.getStringArray(R.array.what_to_show_default))));
        astronomicalFeaturesEnabled = prefs.getBoolean("astronomicalFeatures", false);
        numericalDatePreferred = prefs.getBoolean("numericalDatePreferred", false);

        if (!getOnlyLanguage(getAppLanguage()).equals(resources.getString(R.string.code)))
            applyAppLanguage(context);

        calendarTypesTitleAbbr = context.getResources().getStringArray(R.array.calendar_type_abbr);

        sShiftWorkTitles = new HashMap<>();
        try {
            sShiftWorks = new ArrayList<>();
            String shiftWork = prefs.getString(PREF_SHIFT_WORK_SETTING, "");
            if (shiftWork == null) shiftWork = "";
            String[] parts = shiftWork.split(",");
            for (String p : parts) {
                String[] v = p.split("=");
                if (v.length != 2) continue;
                sShiftWorks.add(new ShiftWorkRecord(v[0], Integer.valueOf(v[1])));
            }
            sShiftWorkStartingJdn = prefs.getLong(PREF_SHIFT_WORK_STARTING_JDN, -1);

            sShiftWorkPeriod = 0;
            for (ShiftWorkRecord shift : sShiftWorks) sShiftWorkPeriod += shift.getLength();

            sShiftWorkRecurs = prefs.getBoolean(PREF_SHIFT_WORK_RECURS, true);

            String[] titles = resources.getStringArray(R.array.shift_work);
            String[] keys = resources.getStringArray(R.array.shift_work_keys);
            for (int i = 0; i < titles.length; ++i)
                sShiftWorkTitles.put(keys[i], titles[i]);
        } catch (Exception e) {
            e.printStackTrace();
            sShiftWorks = Collections.emptyList();
            sShiftWorkStartingJdn = -1;

            sShiftWorkPeriod = 0;
            sShiftWorkRecurs = true;
        }

//        sReminderDetails = updateSavedReminders(context);

        switch (getAppLanguage()) {
            case LANG_FA:
            case LANG_FA_AF:
            case LANG_EN_IR:
                sAM = DEFAULT_AM;
                sPM = DEFAULT_PM;
                break;
            default:
                sAM = context.getString(R.string.am);
                sPM = context.getString(R.string.pm);
        }

        try {
            appTheme = getThemeFromName(getThemeFromPreference(context, prefs));
        } catch (Exception e) {
            e.printStackTrace();
            appTheme = R.style.LightTheme;
        }

        AccessibilityManager a11y = (AccessibilityManager) context.getSystemService(ACCESSIBILITY_SERVICE);
        talkBackEnabled = a11y != null && a11y.isEnabled() && a11y.isTouchExplorationEnabled();
    }

    private static String getCalendarNameAbbr(AbstractDate date) {
        if (calendarTypesTitleAbbr.length < 3) return "";

        // It should match with calendar_type array
        if (date instanceof PersianDate) return calendarTypesTitleAbbr[0];
        else if (date instanceof IslamicDate) return calendarTypesTitleAbbr[1];
        else if (date instanceof CivilDate) return calendarTypesTitleAbbr[2];
        else return "";
    }

    static boolean isNightModeEnabled(@NonNull Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    public static String getThemeFromPreference(@NonNull Context context, SharedPreferences prefs) {
        String result = prefs.getString(PREF_THEME, LIGHT_THEME);
        result = result == null ? LIGHT_THEME : result;
        if (result.equals(LIGHT_THEME) && isNightModeEnabled(context))
            result = DARK_THEME;
        return result;
    }

    @StyleRes
    public static int getAppTheme() {
        return appTheme;
    }

    private static int getIslamicOffset(@NonNull Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String islamicOffset = prefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET);
            if (islamicOffset == null) islamicOffset = DEFAULT_ISLAMIC_OFFSET;
            return Integer.parseInt(islamicOffset.replace("+", ""));
        } catch (Exception ignore) {
            return 0;
        }
    }

    static boolean isAstronomicalFeaturesEnabled() {
        return astronomicalFeaturesEnabled;
    }

    static public List<CalendarType> getEnabledCalendarTypes() {
        List<CalendarType> result = new ArrayList<>();
        result.add(getMainCalendar());
        result.addAll(Arrays.asList(otherCalendars));
        return result;
    }

    static public List<CalendarType> getOrderedCalendarTypes() {
        List<CalendarType> enabledCalendarTypes = getEnabledCalendarTypes();

        List<CalendarType> result = new ArrayList<>(enabledCalendarTypes);
        for (CalendarType key : CalendarType.values()) {
            if (!enabledCalendarTypes.contains(key)) {
                result.add(key);
            }
        }

        return result;
    }

    static public List<CalendarTypeItem> getOrderedCalendarEntities(@NonNull Context context) {
        applyAppLanguage(context);

        String[] values = context.getResources().getStringArray(R.array.calendar_values);
        String[] titles = context.getResources().getStringArray(R.array.calendar_type);

        // TODO: Can be done without Map
        Map<CalendarType, String> typeTitleMap = new HashMap<>();
        for (int i = 0; i < titles.length; ++i) {
            typeTitleMap.put(CalendarType.valueOf(values[i]), titles[i]);
        }

        List<CalendarTypeItem> result = new ArrayList<>();
        for (CalendarType type : getOrderedCalendarTypes()) {
            result.add(new CalendarTypeItem(type, typeTitleMap.get(type)));
        }

        return result;
    }

    static boolean isShownOnWidgets(String infoType) {
        return whatToShowOnWidgets.contains(infoType);
    }

    public static boolean isShowDeviceCalendarEvents() {
        return showDeviceCalendarEvents;
    }

    public static boolean isWeekEnd(int dayOfWeek) {
        return weekEnds[dayOfWeek];
    }

    static public boolean isIranTime() {
        return iranTime;
    }

    static public boolean isArabicDigitSelected() {
        return preferredDigits == ARABIC_DIGITS;
    }

    static boolean isWidgetClock() {
        return widgetClock;
    }

    static boolean isNotifyDate() {
        return notifyDate;
    }

    static public boolean isWeekOfYearEnabled() {
        return showWeekOfYear;
    }

    static public int getAthanVolume(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME);
    }

    static boolean isNotifyDateOnLockScreen() {
        return notifyInLockScreen;
    }

    static public CalculationMethod getCalculationMethod() {
        return CalculationMethod.valueOf(calculationMethod);
    }

    static public String getAppLanguage() {
        return TextUtils.isEmpty(language) ? DEFAULT_APP_LANGUAGE : language;
    }

    static public boolean isNonArabicScriptSelected() {
        switch (getAppLanguage()) {
            case LANG_EN_US:
            case LANG_JA:
                return true;
            default:
                return false;
        }
    }

    static public boolean isLocaleRTL() {
        // en-US and ja are our only real LTR locales for now
        switch (getAppLanguage()) {
            case LANG_EN_US:
            case LANG_JA:
                return false;
            default:
                return true;
        }
    }

    static String getSelectedWidgetTextColor() {
        return selectedWidgetTextColor;
    }

    static public CalendarType getMainCalendar() {
        return mainCalendar;
    }

    @StringRes
    static int getNextOwghatTimeId(Clock current, boolean dateHasChanged) {
        if (coordinate == null) return 0;

        if (prayTimes == null || dateHasChanged) {
            prayTimes = PrayTimesCalculator.calculate(getCalculationMethod(), new Date(), coordinate);
        }

        int clock = current.toInt();

        //TODO We like to show Imsak only in Ramadan
        if (prayTimes.getFajrClock().toInt() > clock)
            return R.string.fajr;

        else if (prayTimes.getSunriseClock().toInt() > clock)
            return R.string.sunrise;

        else if (prayTimes.getDhuhrClock().toInt() > clock)
            return R.string.dhuhr;

        else if (prayTimes.getAsrClock().toInt() > clock)
            return R.string.asr;

        else if (prayTimes.getSunsetClock().toInt() > clock)
            return R.string.sunset;

        else if (prayTimes.getMaghribClock().toInt() > clock)
            return R.string.maghrib;

        else if (prayTimes.getIshaClock().toInt() > clock)
            return R.string.isha;

        else if (prayTimes.getMidnightClock().toInt() > clock)
            return R.string.midnight;

        else
            // TODO: this is today's, not tomorrow
            return R.string.fajr;
    }

    static Clock getClockFromStringId(@StringRes int stringId) {
        switch (stringId) {
            case R.string.imsak:
                return prayTimes.getImsakClock();
            case R.string.fajr:
                return prayTimes.getFajrClock();
            case R.string.sunrise:
                return prayTimes.getSunriseClock();
            case R.string.dhuhr:
                return prayTimes.getDhuhrClock();
            case R.string.asr:
                return prayTimes.getAsrClock();
            case R.string.sunset:
                return prayTimes.getSunsetClock();
            case R.string.maghrib:
                return prayTimes.getMaghribClock();
            case R.string.isha:
                return prayTimes.getIshaClock();
            case R.string.midnight:
                return prayTimes.getMidnightClock();
            default:
                return Clock.fromInt(0);
        }
    }

    static public String formatNumber(int number) {
        return formatNumber(Integer.toString(number));
    }

    static public String formatNumber(String number) {
        if (preferredDigits == ARABIC_DIGITS)
            return number;

        char[] result = number.toCharArray();
        for (int i = 0; i < result.length; ++i) {
            char c = number.charAt(i);
            if (Character.isDigit(c))
                result[i] = preferredDigits[Character.getNumericValue(c)];
        }
        return String.valueOf(result);
    }

    static public String getSpacedComma() {
        return spacedComma;
    }

    static public String[] monthsNamesOfCalendar(AbstractDate date) {
        if (date instanceof PersianDate)
            return persianMonths;
        else if (date instanceof IslamicDate)
            return islamicMonths;
        else
            return gregorianMonths;
    }

    static public String getWeekDayName(AbstractDate date) {
        CivilDate civilDate = date instanceof CivilDate
                ? (CivilDate) date
                : new CivilDate(date);
        if (weekDays == null) return "";
        return weekDays[civilDateToCalendar(civilDate).get(Calendar.DAY_OF_WEEK) % 7];
    }

    static public int getDayIconResource(int day) {
        try {
            if (preferredDigits == ARABIC_DIGITS)
                return DAYS_ICONS_AR[day];
            else if (preferredDigits == ARABIC_INDIC_DIGITS)
                return DAYS_ICONS_CKB[day];
            return DAYS_ICONS[day];
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "No such field is available");
            return 0;
        }
    }

    static private String readStream(InputStream is) {
        // http://stackoverflow.com/a/5445161
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String readRawResource(@NonNull Context context, @RawRes int res) {
        return readStream(context.getResources().openRawResource(res));
    }

    static private String prepareForArabicSort(String text) {
        return text
                .replaceAll("ی", "ي")
                .replaceAll("ک", "ك")
                .replaceAll("گ", "كی")
                .replaceAll("ژ", "زی")
                .replaceAll("چ", "جی")
                .replaceAll("پ", "بی")
                .replaceAll("ڕ", "ری")
                .replaceAll("ڵ", "لی")
                .replaceAll("ڤ", "فی")
                .replaceAll("ۆ", "وی")
                .replaceAll("ێ", "یی")
                .replaceAll("ھ", "نی")
                .replaceAll("ە", "هی");
    }

    static private int getCountryCodeOrder(String countryCode) {
        switch (language) {
            case LANG_FA_AF:
            case LANG_PS:
                return afCodeOrder.indexOf(countryCode);

            case LANG_AR:
                return arCodeOrder.indexOf(countryCode);

            case LANG_FA:
            case LANG_GLK:
            case LANG_AZB:
            default:
                return irCodeOrder.indexOf(countryCode);
        }
    }

    static private <T> Iterable<T> iteratorToIterable(final Iterator<T> iterator) {
        return () -> iterator;
    }

    static public List<CityItem> getAllCities(@NonNull Context context, boolean needsSort) {
        List<CityItem> result = new ArrayList<>();
        try {
            JSONObject countries = new JSONObject(readRawResource(context, R.raw.cities));

            for (String countryCode : iteratorToIterable(countries.keys())) {
                JSONObject country = countries.getJSONObject(countryCode);

                String countryEn = country.getString("en");
                String countryFa = country.getString("fa");
                String countryCkb = country.getString("ckb");
                String countryAr = country.getString("ar");

                JSONObject cities = country.getJSONObject("cities");

                for (String key : iteratorToIterable(cities.keys())) {
                    JSONObject city = cities.getJSONObject(key);

                    String en = city.getString("en");
                    String fa = city.getString("fa");
                    String ckb = city.getString("ckb");
                    String ar = city.getString("ar");

                    Coordinate coordinate = new Coordinate(
                            city.getDouble("latitude"),
                            city.getDouble("longitude"),
                            // Don't Consider elevation for Iran
                            countryCode.equals("ir") ? 0 : city.getDouble("elevation")
                    );

                    result.add(new CityItem(key, en, fa, ckb, ar, countryCode,
                            countryEn, countryFa, countryCkb, countryAr, coordinate));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        if (!needsSort) {
            return result;
        }

        CityItem[] cities = result.toArray(new CityItem[0]);
        // Sort first by country code then city
        Arrays.sort(cities, (l, r) -> {
            if (l.getKey().equals("")) {
                return -1;
            }
            if (r.getKey().equals(DEFAULT_CITY)) {
                return 1;
            }

            int compare = getCountryCodeOrder(l.getCountryCode()) -
                    getCountryCodeOrder(r.getCountryCode());
            if (compare != 0) return compare;

            switch (language) {
                case LANG_EN_US:
                case LANG_JA:
                case LANG_EN_IR:
                    return l.getEn().compareTo(r.getEn());
                case LANG_AR:
                    return l.getAr().compareTo(r.getAr());
                case LANG_CKB:
                    return prepareForArabicSort(l.getCkb())
                            .compareTo(prepareForArabicSort(r.getCkb()));
                default:
                    return prepareForArabicSort(l.getFa())
                            .compareTo(prepareForArabicSort(r.getFa()));
            }
        });

        return Arrays.asList(cities);
    }

    static private CityItem getCityFromPreference(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String key = prefs.getString(PREF_SELECTED_LOCATION, "");
        if (key == null) key = "";

        if (TextUtils.isEmpty(key) || key.equals(DEFAULT_CITY))
            return null;

        if (key.equals(cachedCityKey))
            return cachedCity;

        // cache last query even if no city available under the key, useful in case invalid
        // value is somehow inserted on the preference
        cachedCityKey = key;

        for (CityItem cityEntity : getAllCities(context, false))
            if (cityEntity.getKey().equals(key))
                return cachedCity = cityEntity;

        return cachedCity = null;
    }

    static public String formatCoordinate(@NonNull Context context, Coordinate coordinate, String separator) {
        return String.format(Locale.getDefault(), "%s: %.4f%s%s: %.4f",
                context.getString(R.string.latitude), coordinate.getLatitude(), separator,
                context.getString(R.string.longitude), coordinate.getLongitude());
    }

    static public String getCityName(@NonNull Context context, boolean fallbackToCoord) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        CityItem cityEntity = getCityFromPreference(context);
        if (cityEntity != null) {
            if (language.equals(LANG_EN_IR) || language.equals(LANG_EN_US) || language.equals(LANG_JA))
                return cityEntity.getEn();
            else if (language.equals(LANG_CKB))
                return cityEntity.getCkb();
            return cityEntity.getFa();
        }

        String geocodedCityName = prefs.getString(PREF_GEOCODED_CITYNAME, "");
        if (!TextUtils.isEmpty(geocodedCityName))
            return geocodedCityName;

        if (fallbackToCoord)
            if (coordinate != null)
                return formatCoordinate(context, coordinate, spacedComma);

        return "";
    }

    public static List<AbstractEvent> getAllEnabledEvents() {
        return sAllEnabledEvents;
    }

    static private String formatDayAndMonth(int day, String month) {
        return String.format(language.equals(LANG_CKB) ? "%sی %s" : "%s %s", formatNumber(day),
                month);
    }

    public static boolean isIranHolidaysEnabled() {
        return sIsIranHolidaysEnabled;
    }

    static private void loadEvents(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());

        if (enabledTypes == null || enabledTypes.isEmpty()) {
            switch (getAppLanguage()) {
                case LANG_FA:
                case LANG_GLK:
                case LANG_AZB:
                case LANG_EN_IR:
                case LANG_CKB:
                    enabledTypes = new HashSet<>(Collections.singletonList("iran_holidays"));
                    break;
                default:
                    enabledTypes = new HashSet<>();
            }
        }

        boolean afghanistanHolidays = enabledTypes.contains("afghanistan_holidays");
        boolean afghanistanOthers = enabledTypes.contains("afghanistan_others");
        boolean iranHolidays = enabledTypes.contains("iran_holidays");
        boolean iranIslamic = enabledTypes.contains("iran_islamic");
        boolean iranAncient = enabledTypes.contains("iran_ancient");
        boolean iranOthers = enabledTypes.contains("iran_others");
        boolean international = enabledTypes.contains("international");

        sIsIranHolidaysEnabled = iranHolidays;

        IslamicDate.useUmmAlQura = false;
        if (!iranHolidays) {
            if (afghanistanHolidays) {
                IslamicDate.useUmmAlQura = true;
            }
            switch (getAppLanguage()) {
                case LANG_FA_AF:
                case LANG_PS:
                case LANG_UR:
                case LANG_AR:
                case LANG_CKB:
                case LANG_EN_US:
                case LANG_JA:
                    IslamicDate.useUmmAlQura = true;
            }
        }
        // Now that we are configuring converter's algorithm above, lets set the offset also
        IslamicDate.islamicOffset = getIslamicOffset(context);

        SparseArray<List<PersianCalendarEvent>> persianCalendarEvents = new SparseArray<>();
        SparseArray<List<IslamicCalendarEvent>> islamicCalendarEvents = new SparseArray<>();
        SparseArray<List<GregorianCalendarEvent>> gregorianCalendarEvents = new SparseArray<>();
        ArrayList<AbstractEvent> allEnabledEvents = new ArrayList<>();

        try {
            JSONArray days;
            int length;
            JSONObject allTheEvents = new JSONObject(readRawResource(context, R.raw.events));

            days = allTheEvents.getJSONArray("Persian Calendar");
            length = days.length();
            for (int i = 0; i < length; ++i) {
                JSONObject event = days.getJSONObject(i);

                int month = event.getInt("month");
                int day = event.getInt("day");
                int year = event.has("year") ? event.getInt("year") : -1;
                String title = event.getString("title");
                boolean holiday = event.getBoolean("holiday");

                boolean addOrNot = false;
                String type = event.getString("type");

                if (holiday && iranHolidays && (type.equals("Islamic Iran") ||
                        type.equals("Iran") || type.equals("Ancient Iran")))
                    addOrNot = true;

                if (!iranHolidays && type.equals("Islamic Iran"))
                    holiday = false;

                if (iranIslamic && type.equals("Islamic Iran"))
                    addOrNot = true;

                if (iranAncient && type.equals("Ancient Iran"))
                    addOrNot = true;

                if (iranOthers && type.equals("Iran"))
                    addOrNot = true;

                if (afghanistanHolidays && type.equals("Afghanistan") && holiday)
                    addOrNot = true;

                if (!afghanistanHolidays && type.equals("Afghanistan"))
                    holiday = false;

                if (afghanistanOthers && type.equals("Afghanistan"))
                    addOrNot = true;

                if (addOrNot) {
                    title += " (";
                    if (holiday && afghanistanHolidays && iranHolidays) {
                        if (type.equals("Islamic Iran") || type.equals("Iran"))
                            title += "ایران، ";
                        else if (type.equals("Afghanistan"))
                            title += "افغانستان، ";
                    }
                    title += formatDayAndMonth(day, persianMonths[month - 1]) + ")";

                    List<PersianCalendarEvent> list = persianCalendarEvents.get(month * 100 + day);
                    if (list == null) {
                        list = new ArrayList<>();
                        persianCalendarEvents.put(month * 100 + day, list);
                    }
                    PersianCalendarEvent ev = new PersianCalendarEvent(new PersianDate(year, month, day), title, holiday);
                    list.add(ev);
                    allEnabledEvents.add(ev);
                }
            }

            days = allTheEvents.getJSONArray("Hijri Calendar");
            length = days.length();
            for (int i = 0; i < length; ++i) {
                JSONObject event = days.getJSONObject(i);

                int month = event.getInt("month");
                int day = event.getInt("day");
                String title = event.getString("title");
                boolean holiday = event.getBoolean("holiday");

                boolean addOrNot = false;
                String type = event.getString("type");

                if (afghanistanHolidays && holiday && type.equals("Islamic Afghanistan"))
                    addOrNot = true;

                if (!afghanistanHolidays && type.equals("Islamic Afghanistan"))
                    holiday = false;

                if (afghanistanOthers && type.equals("Islamic Afghanistan"))
                    addOrNot = true;

                if (iranHolidays && holiday && type.equals("Islamic Iran"))
                    addOrNot = true;

                if (!iranHolidays && type.equals("Islamic Iran"))
                    holiday = false;

                if (iranIslamic && type.equals("Islamic Iran"))
                    addOrNot = true;

                if (iranOthers && type.equals("Islamic Iran"))
                    addOrNot = true;

                if (addOrNot) {
                    title += " (";
                    if (holiday && afghanistanHolidays && iranHolidays) {
                        if (type.equals("Islamic Iran"))
                            title += "ایران، ";
                        else if (type.equals("Islamic Afghanistan"))
                            title += "افغانستان، ";
                    }
                    title += formatDayAndMonth(day, islamicMonths[month - 1]) + ")";
                    List<IslamicCalendarEvent> list = islamicCalendarEvents.get(month * 100 + day);
                    if (list == null) {
                        list = new ArrayList<>();
                        islamicCalendarEvents.put(month * 100 + day, list);
                    }
                    IslamicCalendarEvent ev = new IslamicCalendarEvent(new IslamicDate(-1, month, day), title, holiday);
                    list.add(ev);
                    allEnabledEvents.add(ev);
                }
            }

            days = allTheEvents.getJSONArray("Gregorian Calendar");
            length = days.length();
            for (int i = 0; i < length; ++i) {
                JSONObject event = days.getJSONObject(i);

                int month = event.getInt("month");
                int day = event.getInt("day");
                String title = event.getString("title");

                if (international) {
                    title += " (" + formatDayAndMonth(day, gregorianMonths[month - 1]) + ")";
                    List<GregorianCalendarEvent> list = gregorianCalendarEvents.get(month * 100 + day);
                    if (list == null) {
                        list = new ArrayList<>();
                        gregorianCalendarEvents.put(month * 100 + day, list);
                    }
                    GregorianCalendarEvent ev = new GregorianCalendarEvent(new CivilDate(-1, month, day), title, false);
                    list.add(ev);
                    allEnabledEvents.add(ev);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        sPersianCalendarEvents = persianCalendarEvents;
        sIslamicCalendarEvents = islamicCalendarEvents;
        sGregorianCalendarEvents = gregorianCalendarEvents;
        sAllEnabledEvents = allEnabledEvents;
    }

    private static <T extends AbstractDate> boolean holidayAwareEqualCheck(T event, T date) {
        return event.getDayOfMonth() == date.getDayOfMonth()
                && event.getMonth() == date.getMonth()
                && (event.getYear() == -1 || event.getYear() == date.getYear());
    }

    static public String getShiftWorkTitle(long jdn, boolean abbreviated) {
        if (sShiftWorkStartingJdn == -1 || jdn < sShiftWorkStartingJdn || sShiftWorkPeriod == 0)
            return "";

        long passedDays = jdn - sShiftWorkStartingJdn;
        if (!sShiftWorkRecurs && (passedDays >= sShiftWorkPeriod))
            return "";

        int dayInPeriod = (int) (passedDays % sShiftWorkPeriod);
        int accumulation = 0;
        for (ShiftWorkRecord shift : sShiftWorks) {
            accumulation += shift.getLength();
            if (accumulation > dayInPeriod) {
                // Skip rests on abbreviated mode
                if (sShiftWorkRecurs && abbreviated && shift.getType().equals("r")) return "";

                String title = sShiftWorkTitles.get(shift.getType());
                if (title == null) return "";
                return abbreviated ?
                        (title.substring(0, 1) +
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                                        && !language.equals(LANG_AR) ? ZWJ : "")) :
                        title;
            }
        }

        // Shouldn't be reached
        return "";
    }

    static public List<AbstractEvent> getEvents(long jdn,
                                                @Nullable SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents) {
        PersianDate persian = new PersianDate(jdn);
        CivilDate civil = new CivilDate(jdn);
        IslamicDate islamic = new IslamicDate(jdn);

        List<AbstractEvent> result = new ArrayList<>();

        List<PersianCalendarEvent> persianList =
                sPersianCalendarEvents.get(persian.getMonth() * 100 + persian.getDayOfMonth());
        if (persianList != null)
            for (PersianCalendarEvent persianCalendarEvent : persianList)
                if (holidayAwareEqualCheck(persianCalendarEvent.getDate(), persian))
                    result.add(persianCalendarEvent);

        List<IslamicCalendarEvent> islamicList =
                sIslamicCalendarEvents.get(islamic.getMonth() * 100 + islamic.getDayOfMonth());
        if (islamicList != null)
            for (IslamicCalendarEvent islamicCalendarEvent : islamicList)
                if (holidayAwareEqualCheck(islamicCalendarEvent.getDate(), islamic))
                    result.add(islamicCalendarEvent);

        // Special case Imam Reza and Imam Mohammad Taqi martyrdom event on Hijri as it is a holiday and so vital to have
        if ((islamic.getMonth() == 2 || islamic.getMonth() == 11) && islamic.getDayOfMonth() == 29
                && getMonthLength(CalendarType.ISLAMIC, islamic.getYear(), islamic.getMonth()) == 29) {
            IslamicDate alternativeDate = new IslamicDate(islamic.getYear(), islamic.getMonth(), 30);

            islamicList = sIslamicCalendarEvents.get(alternativeDate.getMonth() * 100 +
                    alternativeDate.getDayOfMonth());
            if (islamicList != null)
                for (IslamicCalendarEvent islamicCalendarEvent : islamicList)
                    if (holidayAwareEqualCheck(islamicCalendarEvent.getDate(), alternativeDate))
                        result.add(islamicCalendarEvent);
        }

        List<GregorianCalendarEvent> gregorianList =
                sGregorianCalendarEvents.get(civil.getMonth() * 100 + civil.getDayOfMonth());
        if (gregorianList != null)
            for (GregorianCalendarEvent gregorianCalendarEvent : gregorianList)
                if (holidayAwareEqualCheck(gregorianCalendarEvent.getDate(), civil))
                    result.add(gregorianCalendarEvent);

        // This one is passed by caller
        if (deviceCalendarEvents != null) {
            List<DeviceCalendarEvent> deviceEventList =
                    deviceCalendarEvents.get(civil.getMonth() * 100 + civil.getDayOfMonth());
            if (deviceEventList != null)
                for (DeviceCalendarEvent deviceCalendarEvent : deviceEventList)
                    // holidayAwareEqualCheck is not needed as they won't have -1 on year field
                    if (deviceCalendarEvent.getDate().equals(civil))
                        result.add(deviceCalendarEvent);
        }

        return result;
    }

    static public String getEventsTitle(List<AbstractEvent> dayEvents, boolean holiday,
                                        boolean compact, boolean showDeviceCalendarEvents,
                                        boolean insertRLM) {
        StringBuilder titles = new StringBuilder();
        boolean first = true;

        for (AbstractEvent event : dayEvents)
            if (event.isHoliday() == holiday) {
                String title = event.getTitle();
                if (insertRLM) {
                    title = Constants.RLM + title;
                }
                if (event instanceof DeviceCalendarEvent) {
                    if (!showDeviceCalendarEvents)
                        continue;

                    if (!compact) {
                        title = formatDeviceCalendarEventTitle((DeviceCalendarEvent) event);
                    }
                } else {
                    if (compact)
                        title = title.replaceAll("(.*) \\(.*?$", "$1");
                }

                if (first)
                    first = false;
                else
                    titles.append("\n");
                titles.append(title);
            }

        return titles.toString();
    }

    static void loadAlarms(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String prefString = prefs.getString(PREF_ATHAN_ALARM, "");
        Log.d(TAG, "reading and loading all alarms from prefs: " + prefString);
        CalculationMethod calculationMethod = getCalculationMethod();

        if (calculationMethod != null && coordinate != null && !TextUtils.isEmpty(prefString)) {
            long athanGap;
            try {
                String athanGapStr = prefs.getString(PREF_ATHAN_GAP, "0");
                if (athanGapStr == null) athanGapStr = "0";
                athanGap = (long) (Double.parseDouble(athanGapStr) * 60 * 1000);
            } catch (NumberFormatException e) {
                athanGap = 0;
            }

            PrayTimes prayTimes = PrayTimesCalculator.calculate(calculationMethod,
                    new Date(), coordinate);
            // convert spacedComma separated string to a set
            Set<String> alarmTimesSet = new HashSet<>(Arrays.asList(TextUtils.split(prefString, ",")));

            String[] alarmTimesNames = alarmTimesSet.toArray(new String[0]);
            for (int i = 0; i < alarmTimesNames.length; i++) {
                Clock alarmTime;
                switch (alarmTimesNames[i]) {
                    case "DHUHR":
                        alarmTime = prayTimes.getDhuhrClock();
                        break;

                    case "ASR":
                        alarmTime = prayTimes.getAsrClock();
                        break;

                    case "MAGHRIB":
                        alarmTime = prayTimes.getMaghribClock();
                        break;

                    case "ISHA":
                        alarmTime = prayTimes.getIshaClock();
                        break;

                    // a better to have default
                    default:
                    case "FAJR":
                        alarmTime = prayTimes.getFajrClock();
                }

                setAlarm(context, alarmTimesNames[i], alarmTime, i, athanGap);
            }
        }

//        for (Reminder event : Utils.getReminderDetails()) {
//            ReminderUtils.turnOn(context, event);
//        }
    }

    static private void setAlarm(@NonNull Context context, String alarmTimeName, Clock clock, int ord,
                                 long athanGap) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, clock.getHour());
        triggerTime.set(Calendar.MINUTE, clock.getMinute());
        setAlarm(context, alarmTimeName, triggerTime.getTimeInMillis(), ord, athanGap);
    }

    static private void setAlarm(@NonNull Context context, String alarmTimeName, long timeInMillis, int ord,
                                 long athanGap) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.setTimeInMillis(timeInMillis - athanGap);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // don't set an alarm in the past
        if (alarmManager != null && !triggerTime.before(Calendar.getInstance())) {
            Log.d(TAG, "setting alarm for: " + triggerTime.getTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    ALARMS_BASE_ID + ord,
                    new Intent(context, BroadcastReceivers.class)
                            .putExtra(KEY_EXTRA_PRAYER_KEY, alarmTimeName)
                            .setAction(BROADCAST_ALARM),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    static public Uri getCustomAthanUri(@NonNull Context context) {
        String uri = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_ATHAN_URI, "");
        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }

    static public void startAthan(Context context, String prayTimeKey) {
        if (notificationAthan) {
            // Is this needed?
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                ContextCompat.startForegroundService(context,
//                        new Intent(context, AthanNotification.class));

            context.startService(new Intent(context, AthanNotification.class)
                    .putExtra(Constants.KEY_EXTRA_PRAYER_KEY, prayTimeKey));
        } else {
            context.startActivity(new Intent(context, AthanActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Constants.KEY_EXTRA_PRAYER_KEY, prayTimeKey));
        }
    }

    static public int fixDayOfWeek(int dayOfWeek) {
        return (dayOfWeek + weekStartOffset) % 7;
    }

    static public int fixDayOfWeekReverse(int dayOfWeek) {
        return (dayOfWeek + 7 - weekStartOffset) % 7;
    }

    // Context preferably should be activity context not application
    static public void applyAppLanguage(@NonNull Context context) {
        String localeCode = getOnlyLanguage(language);
        // To resolve this issue, https://issuetracker.google.com/issues/128908783 (marked as fixed now)
        // if ((language.equals(LANG_GLK) || language.equals(LANG_AZB)) && Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
        //    localeCode = LANG_FA;
        // }
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (language.equals(LANG_AZB)) {
                locale = new Locale(LANG_FA);
            }
            config.setLayoutDirection(locale);
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    static private void loadLanguageResource(@NonNull Context context) {
        @RawRes int messagesFile;
        switch (language) {
            case LANG_FA_AF:
                messagesFile = R.raw.messages_fa_af;
                break;
            case LANG_PS:
                messagesFile = R.raw.messages_ps;
                break;
            case LANG_GLK:
                messagesFile = R.raw.messages_glk;
                break;
            case LANG_AR:
                messagesFile = R.raw.messages_ar;
                break;
            case LANG_CKB:
                messagesFile = R.raw.messages_ckb;
                break;
            case LANG_UR:
                messagesFile = R.raw.messages_ur;
                break;
            case LANG_EN_US:
                messagesFile = R.raw.messages_en;
                break;
            case LANG_JA:
                messagesFile = R.raw.messages_ja;
                break;
            case LANG_AZB:
                messagesFile = R.raw.messages_azb;
                break;
            case LANG_EN_IR:
            case LANG_FA:
            default:
                messagesFile = R.raw.messages_fa;
                break;
        }

        persianMonths = new String[12];
        islamicMonths = new String[12];
        gregorianMonths = new String[12];
        weekDays = new String[7];
        weekDaysInitials = new String[7];

        try {
            JSONObject messages = new JSONObject(readRawResource(context, messagesFile));

            JSONArray persianMonthsArray = messages.getJSONArray("PersianCalendarMonths");
            for (int i = 0; i < 12; ++i)
                persianMonths[i] = persianMonthsArray.getString(i);

            JSONArray islamicMonthsArray = messages.getJSONArray("IslamicCalendarMonths");
            for (int i = 0; i < 12; ++i)
                islamicMonths[i] = islamicMonthsArray.getString(i);

            JSONArray gregorianMonthsArray = messages.getJSONArray("GregorianCalendarMonths");
            for (int i = 0; i < 12; ++i)
                gregorianMonths[i] = gregorianMonthsArray.getString(i);

            JSONArray weekDaysArray = messages.getJSONArray("WeekDays");
            for (int i = 0; i < 7; ++i) {
                weekDays[i] = weekDaysArray.getString(i);
                if (language.equals(LANG_AR)) {
                    weekDaysInitials[i] = weekDays[i].substring(2, 4);
                } else if (language.equals(LANG_AZB)) {
                    weekDaysInitials[i] = weekDays[i].substring(0, 2);
                } else {
                    weekDaysInitials[i] = weekDays[i].substring(0, 1);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static long calculateDiffToChangeDate() {
        Date currentTime = Calendar.getInstance().getTime();
        long current = currentTime.getTime() / 1000;

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 1);

        long start = startTime.getTimeInMillis() / 1000 + DAY_IN_SECOND;

        return start - current;
    }

    public static void setChangeDateWorker() {
        long remainedSeconds = calculateDiffToChangeDate();
        OneTimeWorkRequest changeDateWorker =
                new OneTimeWorkRequest.Builder(UpdateWorker.class)
                        .setInitialDelay(remainedSeconds, TimeUnit.SECONDS)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance().beginUniqueWork(
                CHANGE_DATE_TAG,
                ExistingWorkPolicy.REPLACE,
                changeDateWorker).enqueue();
    }

    public static String getInitialOfWeekDay(int position) {
        if (weekDaysInitials == null) return "";
        return weekDaysInitials[position % 7];
    }

    static boolean goForWorker() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static String getWeekDayName(int position) {
        return weekDays[position % 7];
    }

    static public void loadApp(Context context) {
        if (goForWorker()) return;

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Calendar startTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 1);
            startTime.add(Calendar.DATE, 1);

            PendingIntent dailyPendingIntent = PendingIntent.getBroadcast(context, LOAD_APP_ID,
                    new Intent(context, BroadcastReceivers.class).setAction(BROADCAST_RESTART_APP),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC, startTime.getTimeInMillis(), dailyPendingIntent);

            // There are simpler triggers on older Androids like SCREEN_ON but they
            // are not available anymore, lets register an hourly alarm for >= Oreo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent threeHoursPendingIntent = PendingIntent.getBroadcast(context,
                        THREE_HOURS_APP_ID,
                        new Intent(context, BroadcastReceivers.class)
                                .setAction(BROADCAST_UPDATE_APP),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setInexactRepeating(AlarmManager.RTC,
                        // Start from one hour from now
                        Calendar.getInstance().getTimeInMillis() + TimeUnit.HOURS.toMillis(1),
                        TimeUnit.HOURS.toMillis(3), threeHoursPendingIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "loadApp fail", e);
        }
    }

    public static void startEitherServiceOrWorker(@NonNull Context context) {
        WorkManager workManager = WorkManager.getInstance();
        if (goForWorker()) {
            PeriodicWorkRequest.Builder updateBuilder = new PeriodicWorkRequest
                    .Builder(UpdateWorker.class, 1, TimeUnit.HOURS);

            PeriodicWorkRequest updateWork = updateBuilder.build();
            workManager.enqueueUniquePeriodicWork(
                    UPDATE_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    updateWork);
        } else {
            // Disable all the scheduled workers, just in case enabled before
            workManager.cancelAllWork();
            // Or,
            // workManager.cancelAllWorkByTag(UPDATE_TAG);
            // workManager.cancelUniqueWork(CHANGE_DATE_TAG);

            boolean alreadyRan = false;
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                try {
                    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                        if (ApplicationService.class.getName().equals(service.service.getClassName())) {
                            alreadyRan = true;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "startEitherServiceOrWorker service's first part fail", e);
                }
            }

            if (!alreadyRan) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        ContextCompat.startForegroundService(context,
                                new Intent(context, ApplicationService.class));

                    context.startService(new Intent(context, ApplicationService.class));
                } catch (Exception e) {
                    Log.e(TAG, "startEitherServiceOrWorker service's second part fail", e);
                }
            }
        }
    }

    static public String dateStringOfOtherCalendars(long jdn, String separator) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (CalendarType type : otherCalendars) {
            if (!first) result.append(separator);
            result.append(formatDate(getDateFromJdnOfCalendar(type, jdn)));
            first = false;
        }
        return result.toString();
    }

    public static void copyToClipboard(View view, CharSequence label, CharSequence text) {
        ClipboardManager clipboardService =
                (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardService == null || label == null || text == null) return;

        clipboardService.setPrimaryClip(ClipData.newPlainText(label, text));
        createAndShowShortSnackbar(view,
                String.format(view.getContext().getString(R.string.date_copied_clipboard), text));
    }

    public static void createAndShowSnackbar(@Nullable View view, String message, int duration) {
        if (view == null) return;

        Snackbar snackbar = Snackbar.make(view, message, duration);

        View snackbarView = snackbar.getView();
        snackbarView.setOnClickListener(v -> snackbar.dismiss());

        TextView text = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        text.setTextColor(Color.WHITE);
        text.setMaxLines(5);

        snackbar.show();
    }

    public static void createAndShowShortSnackbar(@Nullable View view, @StringRes int messageId) {
        if (view == null) return;
        Context context = view.getContext();
        if (context == null) return;

        createAndShowSnackbar(view, context.getString(messageId), Snackbar.LENGTH_SHORT);
    }

    public static void createAndShowShortSnackbar(@Nullable View view, String message) {
        if (view == null) return;

        createAndShowSnackbar(view, message, Snackbar.LENGTH_SHORT);
    }

    public static boolean isTalkBackEnabled() {
        return talkBackEnabled;
    }

    static boolean isCenterAlignWidgets() {
        return centerAlignWidgets;
    }

    static boolean isShiaPrayTimeCalculationSelected() {
        CalculationMethod calculationMethod = getCalculationMethod();
        return calculationMethod.equals(CalculationMethod.Tehran) ||
                calculationMethod.equals(CalculationMethod.Jafari);
    }

    public static void askForCalendarPermission(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.calendar_access)
                .setMessage(R.string.phone_calendar_required)
                .setPositiveButton(R.string.continue_button, (dialog, id) -> activity.requestPermissions(new String[]{
                                Manifest.permission.READ_CALENDAR
                        },
                        Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel()).show();
    }

    public static void askForLocationPermission(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.location_access)
                .setMessage(R.string.phone_location_required)
                .setPositiveButton(R.string.continue_button, (dialog, id) -> activity.requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        Constants.LOCATION_PERMISSION_REQUEST_CODE))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel()).show();
    }

    public static void toggleShowDeviceCalendarOnPreference(@NonNull Context context, boolean enable) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, enable);
        edit.apply();
    }

    static public String formatDeviceCalendarEventTitle(DeviceCalendarEvent event) {
        String desc = event.getDescription();
        String title = event.getTitle();
        if (!TextUtils.isEmpty(desc))
            title += " (" + Html.fromHtml(event.getDescription()).toString().trim() + ")";

        return title.replaceAll("\\n", " ").trim();
    }

    private static String baseFormatClock(int hour, int minute) {
        return formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute));
    }

    public static boolean isRTL(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    static public String getFormattedClock(Clock clock, boolean forceIn12) {
        boolean in12 = !clockIn24 || forceIn12;
        if (!in12) return baseFormatClock(clock.getHour(), clock.getMinute());

        int hour = clock.getHour();
        String suffix;
        if (hour >= 12) {
            suffix = getPmString();
            hour -= 12;
        } else {
            suffix = getAmString();
        }
        return baseFormatClock(hour, clock.getMinute()) + " " + suffix;
    }

    static public @StringRes
    int getPrayTimeText(String athanKey) {
        switch (athanKey) {
            case "FAJR":
                return R.string.fajr;

            case "DHUHR":
                return R.string.dhuhr;

            case "ASR":
                return R.string.asr;

            case "MAGHRIB":
                return R.string.maghrib;

            case "ISHA":
            default:
                return R.string.isha;
        }
    }

    static public @DrawableRes
    int getPrayTimeImage(String athanKey) {
        switch (athanKey) {
            case "FAJR":
                return R.drawable.fajr;

            case "DHUHR":
                return R.drawable.dhuhr;

            case "ASR":
                return R.drawable.asr;

            case "MAGHRIB":
                return R.drawable.maghrib;

            case "ISHA":
            default:
                return R.drawable.isha;
        }
    }

    @StyleRes
    public static int getThemeFromName(String name) {
        switch (name) {
            case DARK_THEME:
                return R.style.DarkTheme;

            case MODERN_THEME:
                return R.style.ModernTheme;

            case BLUE_THEME:
                return R.style.BlueTheme;

            default:
            case LIGHT_THEME:
                return R.style.LightTheme;
        }
    }

    // https://stackoverflow.com/a/27788209
    static public Uri getDefaultAthanUri(@NonNull Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(R.raw.abdulbasit) + '/' +
                context.getResources().getResourceTypeName(R.raw.abdulbasit) + '/' +
                context.getResources().getResourceEntryName(R.raw.abdulbasit));
    }

    private static String getOnlyLanguage(String string) {
        return string.replaceAll("-(IR|AF|US)", "");
    }

    public static void a11yAnnounceAndClick(View view, @StringRes int resId) {
        if (!isTalkBackEnabled()) return;

        Context context = view.getContext();
        if (context == null) return;

        long now = System.currentTimeMillis();
        if (now - latestToastShowTime > twoSeconds) {
            createAndShowShortSnackbar(view, resId);
            // https://stackoverflow.com/a/29423018
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null)
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
            latestToastShowTime = now;
        }
    }

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

    static public CalendarType getCalendarTypeFromDate(AbstractDate date) {
        if (date instanceof IslamicDate)
            return CalendarType.ISLAMIC;
        else if (date instanceof CivilDate)
            return CalendarType.GREGORIAN;
        else
            return CalendarType.SHAMSI;
    }

    static public int getMonthLength(CalendarType calendar, int year, int month) {
        int yearOfNextMonth = month == 12 ? year + 1 : year;
        int nextMonth = month == 12 ? 1 : month + 1;
        return (int) (getDateOfCalendar(calendar, yearOfNextMonth, nextMonth, 1).toJdn() -
                getDateOfCalendar(calendar, year, month, 1).toJdn());
    }

    static Calendar makeCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (isIranTime()) {
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return calendar;
    }

    static public Calendar getSpringEquinox(long jdn) {
        return makeCalendarFromDate(Equinox.northwardEquinox(new CivilDate(jdn).getYear()));
    }

    static public String toLinearDate(AbstractDate date) {
        return String.format("%s/%s/%s", formatNumber(date.getYear()),
                formatNumber(date.getMonth()), formatNumber(date.getDayOfMonth()));
    }

    static public String dayTitleSummary(AbstractDate date) {
        return getWeekDayName(date) + getSpacedComma() + formatDate(date);
    }

    static public String getMonthName(AbstractDate date) {
        String[] months = monthsNamesOfCalendar(date);
        if (months == null) return "";
        return months[date.getMonth() - 1];
    }

    static public int getDayOfWeekFromJdn(long jdn) {
        return civilDateToCalendar(new CivilDate(jdn)).get(Calendar.DAY_OF_WEEK) % 7;
    }

    static public long getTodayJdn() {
        return calendarToCivilDate(makeCalendarFromDate(new Date())).toJdn();
    }

    static public AbstractDate getTodayOfCalendar(CalendarType calendar) {
        return getDateFromJdnOfCalendar(calendar, getTodayJdn());
    }

    public static int calculateWeekOfYear(long jdn, long startOfYearJdn) {
        long dayOfYear = jdn - startOfYearJdn;
        return (int) Math.ceil(1 + (dayOfYear - fixDayOfWeekReverse(getDayOfWeekFromJdn(jdn))) / 7.);
    }

    static public String formatDate(AbstractDate date) {
        return numericalDatePreferred ? (toLinearDate(date) + " " + getCalendarNameAbbr(date)).trim() : (
                String.format(getAppLanguage().equals(LANG_CKB) ? "%sی %sی %s" : "%s %s %s",
                        formatNumber(date.getDayOfMonth()), getMonthName(date),
                        formatNumber(date.getYear())));
    }

    public static List<DeviceCalendarEvent> getAllEnabledAppointments(@NonNull Context context) {
        Calendar startingDate = Calendar.getInstance();
        startingDate.add(Calendar.YEAR, -1);
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvent = new SparseArray<>();
        List<DeviceCalendarEvent> allEnabledAppointments = new ArrayList<>();
        readDeviceEvents(context, deviceCalendarEvent, allEnabledAppointments, startingDate,
                TimeUnit.DAYS.toMillis(365 * 2));
        return allEnabledAppointments;
    }

    public static SparseArray<List<DeviceCalendarEvent>> readDayDeviceEvents(@NonNull Context context, long jdn) {
        if (jdn == -1) {
            jdn = getTodayJdn();
        }
        Calendar startingDate = civilDateToCalendar(new CivilDate(jdn));
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvent = new SparseArray<>();
        List<DeviceCalendarEvent> allEnabledAppointments = new ArrayList<>();
        readDeviceEvents(context, deviceCalendarEvent, allEnabledAppointments, startingDate, DAY_IN_MILLIS);
        return deviceCalendarEvent;
    }

    public static SparseArray<List<DeviceCalendarEvent>> readMonthDeviceEvents(@NonNull Context context, long jdn) {
        Calendar startingDate = civilDateToCalendar(new CivilDate(jdn));
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvent = new SparseArray<>();
        List<DeviceCalendarEvent> allEnabledAppointments = new ArrayList<>();
        readDeviceEvents(context, deviceCalendarEvent, allEnabledAppointments, startingDate, 32L * DAY_IN_MILLIS);
        return deviceCalendarEvent;
    }

    private static void readDeviceEvents(@NonNull Context context,
                                         SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents,
                                         List<DeviceCalendarEvent> allEnabledAppointments,
                                         Calendar startingDate,
                                         long rangeInMillis) {
        if (!isShowDeviceCalendarEvents()) {
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
                            CalendarContract.Instances.EVENT_COLOR     // 9
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
                Calendar startCalendar = makeCalendarFromDate(startDate);
                Calendar endCalendar = makeCalendarFromDate(endDate);

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
                    title += " (" + baseFormatClock(startCalendar.get(Calendar.HOUR_OF_DAY),
                            startCalendar.get(Calendar.MINUTE));

                    if (cursor.getLong(3) != cursor.getLong(4) && cursor.getLong(4) != 0) {
                        title += "-" + baseFormatClock(endCalendar.get(Calendar.HOUR_OF_DAY),
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

    // Extra helpers
    public static Calendar civilDateToCalendar(CivilDate civilDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, civilDate.getYear());
        cal.set(Calendar.MONTH, civilDate.getMonth() - 1);
        cal.set(Calendar.DAY_OF_MONTH, civilDate.getDayOfMonth());
        return cal;
    }

    public static CivilDate calendarToCivilDate(Calendar calendar) {
        return new CivilDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    static public String getA11yDaySummary(@NonNull Context context, long jdn, boolean isToday,
                                           SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents,
                                           boolean withZodiac, boolean withOtherCalendars, boolean withTitle) {
        // It has some expensive calculations, lets not do that when not needed
        if (!isTalkBackEnabled()) return "";

        StringBuilder result = new StringBuilder();

        if (isToday) {
            result.append(context.getString(R.string.today));
            result.append("\n");
        }

        AbstractDate mainDate = getDateFromJdnOfCalendar(getMainCalendar(), jdn);

        if (withTitle) {
            result.append("\n");
            result.append(dayTitleSummary(mainDate));
        }

        String shift = getShiftWorkTitle(jdn, false);
        if (!TextUtils.isEmpty(shift)) {
            result.append("\n");
            result.append(shift);
        }

        if (withOtherCalendars) {
            String otherCalendars = dateStringOfOtherCalendars(jdn, getSpacedComma());
            if (!TextUtils.isEmpty(otherCalendars)) {
                result.append("\n");
                result.append("\n");
                result.append(context.getString(R.string.equivalent_to));
                result.append(" ");
                result.append(otherCalendars);
            }
        }

        List<AbstractEvent> events = getEvents(jdn, deviceCalendarEvents);
        String holidays = getEventsTitle(events, true, true, true, false);
        if (!TextUtils.isEmpty(holidays)) {
            result.append("\n");
            result.append("\n");
            result.append(context.getString(R.string.holiday_reason));
            result.append("\n");
            result.append(holidays);
        }

        String nonHolidays = getEventsTitle(events, false, true, true, false);
        if (!TextUtils.isEmpty(nonHolidays)) {
            result.append("\n");
            result.append("\n");
            result.append(context.getString(R.string.events));
            result.append("\n");
            result.append(nonHolidays);
        }

        if (isWeekOfYearEnabled()) {
            long startOfYearJdn = getDateOfCalendar(getMainCalendar(),
                    mainDate.getYear(), 1, 1).toJdn();
            int weekOfYearStart = calculateWeekOfYear(jdn, startOfYearJdn);
            result.append("\n");
            result.append("\n");
            result.append(String.format(context.getString(R.string.nth_week_of_year),
                    formatNumber(weekOfYearStart)));
        }

        if (withZodiac) {
            String zodiac = AstronomicalUtils.INSTANCE.getZodiacInfo(context, jdn, false);
            if (!TextUtils.isEmpty(zodiac)) {
                result.append("\n");
                result.append("\n");
                result.append(zodiac);
            }
        }

        return result.toString();
    }

    public static boolean hasAnyHolidays(List<AbstractEvent> dayEvents) {
        for (AbstractEvent event : dayEvents) {
            if (event.isHoliday()) {
                return true;
            }
        }
        return false;
    }

//    private static List<Reminder> updateSavedReminders(Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String storedJson = prefs.getString(REMINDERS_STORE_KEY, "[]");
//        if (TextUtils.isEmpty(storedJson))
//            storedJson = "[]";
//
//        List<Reminder> reminders = new ArrayList<>();
//        try {
//            JSONArray jsonArray = new JSONArray(storedJson);
//            int length = jsonArray.length();
//            for (int i = 0; i < length; ++i) {
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                reminders.add(new Reminder(
//                        jsonObject.getInt("id"),
//                        jsonObject.getString("name"),
//                        jsonObject.getString("info"),
//                        ReminderUtils.timeUnitFromString(jsonObject.getString("unit")),
//                        jsonObject.getInt("quantity"),
//                        jsonObject.getLong("startTime")
//                ));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return reminders;
//    }
//
//    public static void storeReminders(Context context, List<Reminder> reminders) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        try {
//            JSONArray json = new JSONArray();
//            for (Reminder reminder : reminders) {
//                JSONObject object = new JSONObject();
//                object.put("id", reminder.id);
//                object.put("name", reminder.name);
//                object.put("info", reminder.info);
//                object.put("unit", ReminderUtils.timeUnitToString(reminder.unit));
//                object.put("quantity", reminder.quantity);
//                object.put("startTime", reminder.startTime);
//                json.put(object);
//            }
//
//            String serializedJson = json.toString();
//
//            // Just don't store huge objects
//            if (serializedJson.length() > 8000)
//                return;
//
//            SharedPreferences.Editor edit = prefs.edit();
//            edit.putString(REMINDERS_STORE_KEY, serializedJson);
//            edit.apply();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Nullable
//    public static Reminder getReminderById(int id) {
//        for (Reminder reminder : sReminderDetails) {
//            if (reminder.id == id) return reminder;
//        }
//        return null;
//    }
}
