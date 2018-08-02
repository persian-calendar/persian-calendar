package com.byagowi.persiancalendar.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entity.GregorianCalendarEvent;
import com.byagowi.persiancalendar.entity.IslamicCalendarEvent;
import com.byagowi.persiancalendar.entity.PersianCalendarEvent;
import com.byagowi.persiancalendar.enums.CalendarTypeEnum;
import com.byagowi.persiancalendar.enums.SeasonEnum;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.service.BroadcastReceivers;
import com.github.praytimes.CalculationMethod;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RawRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

import static com.byagowi.persiancalendar.Constants.AM_IN_CKB;
import static com.byagowi.persiancalendar.Constants.AM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.ARABIC_DIGITS;
import static com.byagowi.persiancalendar.Constants.ARABIC_INDIC_DIGITS;
import static com.byagowi.persiancalendar.Constants.BROADCAST_ALARM;
import static com.byagowi.persiancalendar.Constants.BROADCAST_RESTART_APP;
import static com.byagowi.persiancalendar.Constants.DARK_THEME;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS_AR;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS_CKB;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ALTITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ATHAN_VOLUME;
import static com.byagowi.persiancalendar.Constants.DEFAULT_CITY;
import static com.byagowi.persiancalendar.Constants.DEFAULT_IRAN_TIME;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ISLAMIC_OFFSET;
import static com.byagowi.persiancalendar.Constants.DEFAULT_LATITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_LONGITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE_LOCK_SCREEN;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PRAY_TIME_METHOD;
import static com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_CLOCK;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_IN_24;
import static com.byagowi.persiancalendar.Constants.KEY_EXTRA_PRAYER_KEY;
import static com.byagowi.persiancalendar.Constants.LANG_CKB;
import static com.byagowi.persiancalendar.Constants.LANG_EN;
import static com.byagowi.persiancalendar.Constants.LANG_EN_US;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PM_IN_CKB;
import static com.byagowi.persiancalendar.Constants.PM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.PREF_ALTITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_ALARM;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_GAP;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_VOLUME;
import static com.byagowi.persiancalendar.Constants.PREF_GEOCODED_CITYNAME;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;
import static com.byagowi.persiancalendar.Constants.PREF_IRAN_TIME;
import static com.byagowi.persiancalendar.Constants.PREF_ISLAMIC_OFFSET;
import static com.byagowi.persiancalendar.Constants.PREF_LATITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_LONGITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE_LOCK_SCREEN;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_PRAY_TIME_METHOD;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_LOCATION;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;
import static com.byagowi.persiancalendar.Constants.PREF_WIDGET_CLOCK;
import static com.byagowi.persiancalendar.Constants.PREF_WIDGET_IN_24;

//import com.byagowi.persiancalendar.service.UpdateWorker;
//import androidx.work.ExistingPeriodicWorkPolicy;
//import androidx.work.ExistingWorkPolicy;
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;

/**
 * Common utilities that needed for this calendar
 *
 * @author ebraminio
 */

public class Utils {

    static private final String TAG = Utils.class.getName();

    // This should be called before any use of Utils on the activity and services
    static public void initUtils(Context context) {
        updateStoredPreference(context);
        changeAppLanguage(context);
        loadLanguageResource(context);
        loadAlarms(context);
        loadEvents(context);
    }

    static private String[] persianMonths;
    static private String[] islamicMonths;
    static private String[] gregorianMonths;
    static private String[] weekDays;
    static private String[] weekDaysInitials;

    static public void setActivityTitleAndSubtitle(Activity activity, String title, String subtitle) {
        //noinspection ConstantConditions
        ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
            supportActionBar.setSubtitle(subtitle);
        }
    }

    static public Coordinate getCoordinate(Context context) {
        CityEntity cityEntity = getCityFromPreference(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (cityEntity != null) {
            return cityEntity.getCoordinate();
        }

        try {
            Coordinate coord = new Coordinate(
                    Double.parseDouble(prefs.getString(PREF_LATITUDE, DEFAULT_LATITUDE)),
                    Double.parseDouble(prefs.getString(PREF_LONGITUDE, DEFAULT_LONGITUDE)),
                    Double.parseDouble(prefs.getString(PREF_ALTITUDE, DEFAULT_ALTITUDE))
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

    static private char[] preferredDigits = PERSIAN_DIGITS;
    static private boolean clockIn24 = DEFAULT_WIDGET_IN_24;
    static private boolean iranTime = DEFAULT_IRAN_TIME;
    static private boolean notifyInLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN;
    static private boolean widgetClock = DEFAULT_WIDGET_CLOCK;
    static private boolean notifyDate = DEFAULT_NOTIFY_DATE;
	static private boolean notificationAthan = DEFAULT_NOTIFICATION_ATHAN;
    static private String selectedWidgetTextColor = DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
    static private String islamicOffset = DEFAULT_ISLAMIC_OFFSET;
    static private String calculationMethod = DEFAULT_PRAY_TIME_METHOD;
    static private String language = DEFAULT_APP_LANGUAGE;
    static private Coordinate coordinate;
    static private CalendarTypeEnum mainCalendar;
    static private String comma;
    static private boolean showWeekOfYear;
    static private int weekStartOffset;
    static private boolean[] weekEnds;
    static private boolean showDeviceCalendarEvents;
    static private Set<String> whatToShowOnWidgets;

    static public void updateStoredPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        language = prefs.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE);
        preferredDigits = prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS)
                ? PERSIAN_DIGITS
                : ARABIC_DIGITS;
        if (language.equals("ckb") && preferredDigits == PERSIAN_DIGITS)
            preferredDigits = ARABIC_INDIC_DIGITS;

        clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24);
        iranTime = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME);
        notifyInLockScreen = prefs.getBoolean(PREF_NOTIFY_DATE_LOCK_SCREEN,
                DEFAULT_NOTIFY_DATE_LOCK_SCREEN);
        widgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK);
        notifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE);
        notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN);
        selectedWidgetTextColor = prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR,
                DEFAULT_SELECTED_WIDGET_TEXT_COLOR);
        islamicOffset = prefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET);
        // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
        // so switched to "Tehran" method as default calculation algorithm
        calculationMethod = prefs.getString(PREF_PRAY_TIME_METHOD, DEFAULT_PRAY_TIME_METHOD);
        coordinate = getCoordinate(context);
        mainCalendar = CalendarTypeEnum.valueOf(prefs.getString("mainCalendarType", "SHAMSI"));
        comma = language.equals(LANG_EN_US) ? "," : "،";
        showWeekOfYear = prefs.getBoolean("showWeekOfYearNumber", false);

        weekStartOffset = Integer.parseInt(prefs.getString("WeekStart", "0"));
        // WeekEnds, 6 means Friday
        weekEnds = new boolean[7];
        for (String s : prefs.getStringSet("WeekEnds", new HashSet<>(Arrays.asList("6"))))
            weekEnds[Integer.parseInt(s)] = true;

        showDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false);
        whatToShowOnWidgets = prefs.getStringSet("what_to_show",
                new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.what_to_show_default))));
    }

    public static boolean isShownOnWidgets(String infoType) {
        return whatToShowOnWidgets.contains(infoType);
    }

    public static boolean isShowDeviceCalendarEvents() {
        return showDeviceCalendarEvents;
    }

    public static boolean isWeekEnd(int dayOfWeek) {
        return weekEnds[dayOfWeek];
    }

    public static boolean isRTL(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    static public boolean isIranTime() {
        return iranTime;
    }

    static public boolean isArabicDigitSelected() {
        return preferredDigits == ARABIC_DIGITS;
    }

    static public boolean isWidgetClock() {
        return widgetClock;
    }

    static public boolean isNotifyDate() {
        return notifyDate;
    }
	
    static public boolean isNotificationAthan() {
        return notificationAthan;
    }

    static public boolean isWeekOfYearEnabled() {
        return showWeekOfYear;
    }

    static public int getAthanVolume(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME);
    }

    static public boolean isNotifyDateOnLockScreen() {
        return notifyInLockScreen;
    }

    static public CalculationMethod getCalculationMethod() {
        return CalculationMethod.valueOf(calculationMethod);
    }

    static public int getIslamicOffset() {
        return Integer.parseInt(islamicOffset.replace("+", ""));
    }

    static public String getAppLanguage() {
        // If is empty for whatever reason (pref dialog bug, etc), return Persian at least
        return TextUtils.isEmpty(language) ? DEFAULT_APP_LANGUAGE : language;
    }

    static public String getSelectedWidgetTextColor() {
        return selectedWidgetTextColor;
    }

    static public PersianDate getToday() {
        return DateConverter.civilToPersian(new CivilDate(makeCalendarFromDate(new Date())));
    }

    static public long getTodayJdn() {
        return DateConverter.civilToJdn(new CivilDate(makeCalendarFromDate(new Date())));
    }

    static public IslamicDate getIslamicToday() {
        return DateConverter.civilToIslamic(new CivilDate(makeCalendarFromDate(new Date())), getIslamicOffset());
    }

    static public CivilDate getGregorianToday() {
        return new CivilDate(makeCalendarFromDate(new Date()));
    }

    static public CalendarTypeEnum getMainCalendar() {
        return mainCalendar;
    }

    static public AbstractDate getTodayOfCalendar(CalendarTypeEnum calendar) {
        switch (calendar) {
            case ISLAMIC:
                return getIslamicToday();
            case GREGORIAN:
                return getGregorianToday();
            case SHAMSI:
            default:
                return getToday();
        }
    }

    static public AbstractDate getDateOfCalendar(CalendarTypeEnum calendar, int year, int month, int day) {
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

    static public long getJdnOfCalendar(CalendarTypeEnum calendar, int year, int month, int day) {
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

    static public AbstractDate getDateFromJdnOfCalendar(CalendarTypeEnum calendar, long jdn) {
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
        if (iranTime) {
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return calendar;
    }

    static public String toLinearDate(AbstractDate date) {
        return String.format("%s/%s/%s", formatNumber(date.getYear()),
                formatNumber(date.getMonth()), formatNumber(date.getDayOfMonth()));
    }

    static private String clockToString(int hour, int minute) {
        return formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute));
    }

    static private Map<PrayTime, Clock> prayTimes;

    static public String getNextOwghatTime(Context context, Clock clock, boolean dateHasChanged) {
        if (coordinate == null) return null;

        if (prayTimes == null || dateHasChanged) {
            prayTimes = new PrayTimesCalculator(getCalculationMethod())
                    .calculate(new Date(), coordinate);
        }

        if (prayTimes.get(PrayTime.FAJR).getInt() > clock.getInt()) {
            return context.getString(R.string.azan1) + ": " + getFormattedClock(prayTimes.get(PrayTime.FAJR));

        } else if (prayTimes.get(PrayTime.SUNRISE).getInt() > clock.getInt()) {
            return context.getString(R.string.aftab1) + ": " + getFormattedClock(prayTimes.get(PrayTime.SUNRISE));

        } else if (prayTimes.get(PrayTime.DHUHR).getInt() > clock.getInt()) {
            return context.getString(R.string.azan2) + ": " + getFormattedClock(prayTimes.get(PrayTime.DHUHR));

        } else if (prayTimes.get(PrayTime.ASR).getInt() > clock.getInt()) {
            return context.getString(R.string.azan3) + ": " + getFormattedClock(prayTimes.get(PrayTime.ASR));

        } else if (prayTimes.get(PrayTime.SUNSET).getInt() > clock.getInt()) {
            return context.getString(R.string.aftab2) + ": " + getFormattedClock(prayTimes.get(PrayTime.SUNSET));

        } else if (prayTimes.get(PrayTime.MAGHRIB).getInt() > clock.getInt()) {
            return context.getString(R.string.azan4) + ": " + getFormattedClock(prayTimes.get(PrayTime.MAGHRIB));

        } else if (prayTimes.get(PrayTime.ISHA).getInt() > clock.getInt()) {
            return context.getString(R.string.azan5) + ": " + getFormattedClock(prayTimes.get(PrayTime.ISHA));

        } else if (prayTimes.get(PrayTime.MIDNIGHT).getInt() > clock.getInt()) {
            return context.getString(R.string.aftab3) + ": " + getFormattedClock(prayTimes.get(PrayTime.MIDNIGHT));

        } else {
            return context.getString(R.string.azan1) + ": " + getFormattedClock(prayTimes.get(PrayTime.FAJR)); //this is today & not tomorrow
        }
    }

    static public String getFormattedClock(Clock clock) {
        String timeText = null;

        int hour = clock.getHour();
        if (!clockIn24) {
            if (hour >= 12) {
                timeText = language.equals("ckb")
                        ? PM_IN_CKB
                        : PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = language.equals("ckb")
                        ? AM_IN_CKB
                        : AM_IN_PERSIAN;
            }
        }

        String result = clockToString(hour, clock.getMinute());
        if (!clockIn24) {
            result = result + " " + timeText;
        }
        return result;
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

    static public String dateToString(AbstractDate date) {
        return formatNumber(date.getDayOfMonth()) + ' ' + getMonthName(date) + ' ' +
                formatNumber(date.getYear());
    }

    static public String dateStringOfOtherCalendar(CalendarTypeEnum calendar, long jdn) {
        switch (calendar) {
            case ISLAMIC:
                return Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
                        comma + " " +
                        Utils.dateToString(DateConverter.jdnToCivil(jdn));
            case GREGORIAN:
                return Utils.dateToString(DateConverter.jdnToPersian(jdn)) +
                        comma + " " +
                        Utils.dateToString(DateConverter.civilToIslamic(
                                DateConverter.jdnToCivil(jdn), getIslamicOffset()));
            case SHAMSI:
            default:
                return Utils.dateToString(DateConverter.jdnToCivil(jdn)) +
                        comma + " " +
                        Utils.dateToString(DateConverter.civilToIslamic(
                                DateConverter.jdnToCivil(jdn), getIslamicOffset()));
        }
    }

    static public String dayTitleSummary(AbstractDate date) {
        return getWeekDayName(date) + comma + " " + dateToString(date);
    }

    static public String getComma() {
        return comma;
    }

    static private String[] monthsNamesOfCalendar(AbstractDate date) {
        if (date instanceof PersianDate)
            return persianMonths;
        else if (date instanceof IslamicDate)
            return islamicMonths;
        else
            return gregorianMonths;
    }

    static public String getMonthName(AbstractDate date) {
        return monthsNamesOfCalendar(date)[date.getMonth() - 1];
    }

    static public String getWeekDayName(AbstractDate date) {
        if (date instanceof IslamicDate)
            date = DateConverter.islamicToCivil((IslamicDate) date);
        else if (date instanceof PersianDate)
            date = DateConverter.persianToCivil((PersianDate) date);

        return weekDays[date.getDayOfWeek() % 7];
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

    static public String readRawResource(Context context, @RawRes int res) {
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

    static private <T> Iterable<T> iteratorToIterable(final Iterator<T> iterator) {
        return () -> iterator;
    }

    static public List<CityEntity> getAllCities(Context context, boolean needsSort) {
        List<CityEntity> result = new ArrayList<>();
        try {
            JSONObject countries = new JSONObject(readRawResource(context, R.raw.cities));

            for (String countryCode : iteratorToIterable(countries.keys())) {
                JSONObject country = countries.getJSONObject(countryCode);

                String countryEn = country.getString("en");
                String countryFa = country.getString("fa");
                String countryCkb = country.getString("ckb");

                JSONObject cities = country.getJSONObject("cities");

                for (String key : iteratorToIterable(cities.keys())) {
                    JSONObject city = cities.getJSONObject(key);

                    String en = city.getString("en");
                    String fa = city.getString("fa");
                    String ckb = city.getString("ckb");

                    Coordinate coordinate = new Coordinate(
                            city.getDouble("latitude"),
                            city.getDouble("longitude"),
                            0 // city.getDouble("elevation")
                    );

                    result.add(new CityEntity(key, en, fa, ckb, countryCode,
                            countryEn, countryFa, countryCkb, coordinate));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        if (!needsSort) {
            return result;
        }

        CityEntity[] cities = result.toArray(new CityEntity[result.size()]);
        // Sort first by country code then city
        Arrays.sort(cities, (l, r) -> {
            if (l.getKey().equals("")) {
                return -1;
            }
            if (r.getKey().equals(DEFAULT_CITY)) {
                return 1;
            }
            int compare = r.getCountryCode().compareTo(l.getCountryCode());
            if (compare != 0) return compare;
            if (language.equals(LANG_EN))
                return l.getEn().compareTo(r.getEn());
            else if (language.equals(LANG_CKB))
                return prepareForArabicSort(l.getCkb())
                        .compareTo(prepareForArabicSort(r.getCkb()));
            else
                return prepareForArabicSort(l.getFa())
                        .compareTo(prepareForArabicSort(r.getFa()));
        });

        return Arrays.asList(cities);
    }

    static private String cachedCityKey = "";
    static private CityEntity cachedCity;

    static private CityEntity getCityFromPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String key = prefs.getString(PREF_SELECTED_LOCATION, "");

        if (TextUtils.isEmpty(key) || key.equals(DEFAULT_CITY))
            return null;

        if (key.equals(cachedCityKey))
            return cachedCity;

        // cache last query even if no city available under the key, useful in case invalid
        // value is somehow inserted on the preference
        cachedCityKey = key;

        for (CityEntity cityEntity : getAllCities(context, false))
            if (cityEntity.getKey().equals(key))
                return cachedCity = cityEntity;

        return cachedCity = null;
    }

    static public String formatCoordinate(Context context, Coordinate coordinate, String separator) {
        return String.format(Locale.getDefault(), "%s: %.4f%s%s: %.4f",
                context.getString(R.string.latitude), coordinate.getLatitude(), separator,
                context.getString(R.string.longitude), coordinate.getLongitude());
    }

    static public String getCityName(Context context, boolean fallbackToCoord) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        CityEntity cityEntity = getCityFromPreference(context);
        if (cityEntity != null) {
            if (language.equals(LANG_EN))
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
                return formatCoordinate(context, coordinate, comma + " ");

        return "";
    }

    static private SparseArray<List<PersianCalendarEvent>> persianCalendarEvents;
    static private SparseArray<List<IslamicCalendarEvent>> islamicCalendarEvents;
    static private SparseArray<List<GregorianCalendarEvent>> gregorianCalendarEvents;
    static private SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents;
    static public List<Object> allEnabledEvents;
    static public List<String> allEnabledEventsTitles;

    static private void loadEvents(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());

        if (enabledTypes.isEmpty())
            enabledTypes = new HashSet<>(Arrays.asList("iran_holidays"));

        boolean afghanistanHolidays = enabledTypes.contains("afghanistan_holidays");
        boolean afghanistanOthers = enabledTypes.contains("afghanistan_others");
        boolean iranHolidays = enabledTypes.contains("iran_holidays");
        boolean iranIslamic = enabledTypes.contains("iran_islamic");
        boolean iranAncient = enabledTypes.contains("iran_ancient");
        boolean iranOthers = enabledTypes.contains("iran_others");
        boolean international = enabledTypes.contains("international");

        SparseArray<List<PersianCalendarEvent>> persianCalendarEvents = new SparseArray<>();
        SparseArray<List<IslamicCalendarEvent>> islamicCalendarEvents = new SparseArray<>();
        SparseArray<List<GregorianCalendarEvent>> gregorianCalendarEvents = new SparseArray<>();
        ArrayList<Object> allEnabledEvents = new ArrayList<>();
        ArrayList<String> allEnabledEventsTitles = new ArrayList<>();

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
                    title += formatNumber(day) + " " + persianMonths[month - 1] + ")";

                    List<PersianCalendarEvent> list = persianCalendarEvents.get(month * 100 + day);
                    if (list == null) {
                        list = new ArrayList<>();
                        persianCalendarEvents.put(month * 100 + day, list);
                    }
                    PersianCalendarEvent ev = new PersianCalendarEvent(new PersianDate(year, month, day), title, holiday);
                    list.add(ev);
                    allEnabledEvents.add(ev);
                    allEnabledEventsTitles.add(title);
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
                    title += formatNumber(day) + " " + islamicMonths[month - 1] + ")";
                    List<IslamicCalendarEvent> list = islamicCalendarEvents.get(month * 100 + day);
                    if (list == null) {
                        list = new ArrayList<>();
                        islamicCalendarEvents.put(month * 100 + day, list);
                    }
                    IslamicCalendarEvent ev = new IslamicCalendarEvent(new IslamicDate(-1, month, day), title, holiday);
                    list.add(ev);
                    allEnabledEvents.add(ev);
                    allEnabledEventsTitles.add(title);
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
                    title += " (" + formatNumber(day) + " " + gregorianMonths[month - 1] + ")";
                    List<GregorianCalendarEvent> list = gregorianCalendarEvents.get(month * 100 + day);
                    if (list == null) {
                        list = new ArrayList<>();
                        gregorianCalendarEvents.put(month * 100 + day, list);
                    }
                    GregorianCalendarEvent ev = new GregorianCalendarEvent(new CivilDate(-1, month, day), title, false);
                    list.add(ev);
                    allEnabledEvents.add(ev);
                    allEnabledEventsTitles.add(title);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        Utils.persianCalendarEvents = persianCalendarEvents;
        Utils.islamicCalendarEvents = islamicCalendarEvents;
        Utils.gregorianCalendarEvents = gregorianCalendarEvents;
        Utils.allEnabledEvents = allEnabledEvents;
        Utils.allEnabledEventsTitles = allEnabledEventsTitles;

        readDeviceCalendarEvents(context);
    }

    public static void askForCalendarPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                            Manifest.permission.READ_CALENDAR
                    },
                    Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE);
        }
    }

    private static void readDeviceCalendarEvents(Context context) {
        SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents = new SparseArray<>();
        Utils.deviceCalendarEvents = deviceCalendarEvents;

        if (!showDeviceCalendarEvents) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            if (context instanceof AppCompatActivity) {
                askForCalendarPermission((AppCompatActivity) context);
            }
            return;
        }

        try {
            Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                    new String[]{
                            CalendarContract.Events._ID,            // 0
                            CalendarContract.Events.TITLE,          // 1
                            CalendarContract.Events.DESCRIPTION,    // 2
                            CalendarContract.Events.DTSTART,        // 3
                            CalendarContract.Events.DTEND,          // 4
                            CalendarContract.Events.EVENT_LOCATION, // 5
                            CalendarContract.Events.RRULE,          // 6
                            CalendarContract.Events.VISIBLE,        // 7
                            CalendarContract.Events.ALL_DAY,        // 8
                            CalendarContract.Events.DELETED         // 9
                    }, null, null, null);

            if (cursor == null) {
                return;
            }

            while (cursor.moveToNext()) {
                if (!cursor.getString(7).equals("1") || cursor.getString(9).equals("1"))
                    continue;

                boolean allDay = false;
                if (cursor.getString(8).equals("1"))
                    allDay = true;

                Date startDate = new Date(cursor.getLong(3));
                Date endDate = new Date(cursor.getLong(4));
                Calendar startCalendar = makeCalendarFromDate(startDate);
                Calendar endCalendar = makeCalendarFromDate(endDate);

                CivilDate civilDate = new CivilDate(startCalendar);

                int month = civilDate.getMonth();
                int day = civilDate.getDayOfMonth();

                String repeatRule = cursor.getString(6);
                if (repeatRule != null && repeatRule.contains("FREQ=YEARLY"))
                    civilDate.setYear(-1);

                List<DeviceCalendarEvent> list = deviceCalendarEvents.get(month * 100 + day);
                if (list == null) {
                    list = new ArrayList<>();
                    deviceCalendarEvents.put(month * 100 + day, list);
                }

                String title = cursor.getString(1);
                if (allDay) {
                    if (civilDate.getYear() == -1) {
                        title = "\uD83C\uDF89 " + title;
                    } else {
                        title = "\uD83D\uDCC5 " + title;
                    }
                } else {
                    title = "\uD83D\uDD53 " + title;
                    title += " (" + clockToString(startCalendar.get(Calendar.HOUR_OF_DAY),
                            startCalendar.get(Calendar.MINUTE));

                    if (cursor.getLong(3) != cursor.getLong(4) && cursor.getLong(4) != 0) {
                        title += "-" + clockToString(endCalendar.get(Calendar.HOUR_OF_DAY),
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
                        civilDate
                );
                list.add(event);
                allEnabledEvents.add(event);
                allEnabledEventsTitles.add(title);
            }
            cursor.close();
        } catch (Exception e) {
            // We don't like crash addition from here, just catch all of exceptions
            Log.e(TAG, "Error on device calendar events read", e);
        }
    }

    static public List<AbstractEvent> getEvents(long jdn) {
        PersianDate day = DateConverter.jdnToPersian(jdn);
        CivilDate civil = DateConverter.jdnToCivil(jdn);
        IslamicDate islamic = DateConverter.jdnToIslamic(jdn);

        List<AbstractEvent> result = new ArrayList<>();

        List<PersianCalendarEvent> persianList =
                persianCalendarEvents.get(day.getMonth() * 100 + day.getDayOfMonth());
        if (persianList != null)
            for (PersianCalendarEvent persianCalendarEvent : persianList)
                if (persianCalendarEvent.getDate().equals(day))
                    result.add(persianCalendarEvent);

        List<IslamicCalendarEvent> islamicList =
                islamicCalendarEvents.get(islamic.getMonth() * 100 + islamic.getDayOfMonth());
        if (islamicList != null)
            for (IslamicCalendarEvent islamicCalendarEvent : islamicList)
                if (islamicCalendarEvent.getDate().equals(islamic))
                    result.add(islamicCalendarEvent);

        List<GregorianCalendarEvent> gregorianList =
                gregorianCalendarEvents.get(civil.getMonth() * 100 + civil.getDayOfMonth());
        if (gregorianList != null)
            for (GregorianCalendarEvent gregorianCalendarEvent : gregorianList)
                if (gregorianCalendarEvent.getDate().equals(civil))
                    result.add(gregorianCalendarEvent);

        List<DeviceCalendarEvent> deviceEventList =
                deviceCalendarEvents.get(civil.getMonth() * 100 + civil.getDayOfMonth());
        if (deviceEventList != null)
            for (DeviceCalendarEvent deviceCalendarEvent : deviceEventList)
                if (deviceCalendarEvent.getCivilDate().equals(civil))
                    result.add(deviceCalendarEvent);

        return result;
    }

    static public String formatDeviceCalendarEventTitle(DeviceCalendarEvent event) {
        String desc = event.getDescription();
        String title = event.getTitle();
        if (!TextUtils.isEmpty(desc))
            title += " (" + event.getDescription() + ")";

        return title.replaceAll("\\n", " ").trim();
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
                        title = title.replaceAll(" \\(.*$", "");
                }

                if (first)
                    first = false;
                else
                    titles.append("\n");
                titles.append(title);
            }

        return titles.toString();
    }

    private static void loadAlarms(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String prefString = prefs.getString(PREF_ATHAN_ALARM, "");
        Log.d(TAG, "reading and loading all alarms from prefs: " + prefString);
        CalculationMethod calculationMethod = getCalculationMethod();

        if (calculationMethod != null && coordinate != null && !TextUtils.isEmpty(prefString)) {
            PrayTimesCalculator calculator = new PrayTimesCalculator(calculationMethod);
            Map<PrayTime, Clock> prayTimes = calculator.calculate(new Date(), coordinate);
            // convert comma separated string to a set
            Set<String> alarmTimesSet = new HashSet<>(Arrays.asList(TextUtils.split(prefString, ",")));
            // in the past IMSAK was used but now we figured out FAJR was what we wanted
            if (alarmTimesSet.remove("IMSAK"))
                alarmTimesSet.add("FAJR");

            String[] alarmTimesNames = alarmTimesSet.toArray(new String[alarmTimesSet.size()]);
            for (int i = 0; i < alarmTimesNames.length; i++) {
                PrayTime prayTime = PrayTime.valueOf(alarmTimesNames[i]);

                Clock alarmTime = prayTimes.get(prayTime);

                if (alarmTime != null)
                    setAlarm(context, prayTime, alarmTime, i);
            }
        }
    }

    static private void setAlarm(Context context, PrayTime prayTime, Clock clock, int id) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, clock.getHour());
        triggerTime.set(Calendar.MINUTE, clock.getMinute());
        setAlarm(context, prayTime, triggerTime.getTimeInMillis(), id);
    }

    static private void setAlarm(Context context, PrayTime prayTime, long timeInMillis, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String valAthanGap = prefs.getString(PREF_ATHAN_GAP, "0");
        long athanGap;
        try {
            athanGap = (long) (Double.parseDouble(valAthanGap) * 60);
        } catch (NumberFormatException e) {
            athanGap = 0;
        }

        Calendar triggerTime = Calendar.getInstance();
        triggerTime.setTimeInMillis(timeInMillis - TimeUnit.SECONDS.toMillis(athanGap));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // don't set an alarm in the past
        if (!triggerTime.before(Calendar.getInstance())) {
            Log.d(TAG, "setting alarm for: " + triggerTime.getTime());

            Intent intent = new Intent(context, BroadcastReceivers.class);
            intent.setAction(BROADCAST_ALARM);
            intent.putExtra(KEY_EXTRA_PRAYER_KEY, prayTime.name());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SetExactAlarm.setExactAlarm(alarmManager,
                        AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    static private class SetExactAlarm {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public static void setExactAlarm(AlarmManager alarmManager,
                                         int type, long triggerAtMillis, PendingIntent pendingIntent) {
            alarmManager.setExact(type, triggerAtMillis, pendingIntent);
        }
    }

    static public Uri getAthanUri(Context context) {
        String defaultSoundUri = "android.resource://" + context.getPackageName() + "/" + R.raw.abdulbasit;
        return Uri.parse(defaultSoundUri);
    }

    static public String getOnlyLanguage(String string) {
        return string.replaceAll("-(IR|AF|US)", "");
    }

    static public int getDayOfWeekFromJdn(long jdn) {
        return DateConverter.jdnToCivil(jdn).getDayOfWeek() % 7;
    }

    static public int fixDayOfWeek(int dayOfWeek) {
        return (dayOfWeek + weekStartOffset) % 7;
    }

    static public int fixDayOfWeekReverse(int dayOfWeek) {
        return (dayOfWeek + 7 - weekStartOffset) % 7;
    }

    // Context preferably should be activity context not application
    static public void changeAppLanguage(Context context) {
        String localeCode = getOnlyLanguage(language);
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(config.locale);
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static void setTheme(AppCompatActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        switch (prefs.getString(PREF_THEME, LIGHT_THEME)) {
            case DARK_THEME:
                activity.setTheme(R.style.DarkTheme);
                return;
//            case CLASSIC_THEME:
//                setTheme(R.style.ClassicTheme);
//                return;
            default:
            case LIGHT_THEME:
                activity.setTheme(R.style.LightTheme);
        }
    }

    static private void loadLanguageResource(Context context) {
        @RawRes int messagesFile;
        switch (language) {
            case "fa-AF":
                messagesFile = R.raw.messages_fa_af;
                break;
            case "ps":
                messagesFile = R.raw.messages_ps;
                break;
            case "ckb":
                messagesFile = R.raw.messages_ckb;
                break;
            case "ur":
                messagesFile = R.raw.messages_ur;
                break;
            case "en-US":
                messagesFile = R.raw.messages_en;
                break;
            case "en":
            case "fa":
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
                weekDaysInitials[i] = weekDays[i].substring(0, 1);
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static String getInitialOfWeekDay(int position) {
        return weekDaysInitials[position % 7];
    }

    static public void copyToClipboard(Context context, CharSequence text) {
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("converted date", text));
        Toast.makeText(context, "«" + text + "»\n" + context.getString(R.string.date_copied_clipboard), Toast.LENGTH_SHORT).show();
    }

    static public SeasonEnum getSeason() {
        int month = getToday().getMonth();

        if (month < 4) {
            return SeasonEnum.SPRING;

        } else if (month < 7) {
            return SeasonEnum.SUMMER;

        } else if (month < 10) {
            return SeasonEnum.FALL;

        } else {
            return SeasonEnum.WINTER;
        }
    }

    // based on R.array.calendar_type order
    static public CalendarTypeEnum calendarTypeFromPosition(int position) {
        switch (position) {
            case 0:
                return CalendarTypeEnum.SHAMSI;
            case 1:
                return CalendarTypeEnum.ISLAMIC;
            default:
                return CalendarTypeEnum.GREGORIAN;
        }
    }

    static public int positionFromCalendarType(CalendarTypeEnum calendar) {
        switch (calendar) {
            case SHAMSI:
                return 0;
            case ISLAMIC:
                return 1;
            default:
                return 2;
        }
    }

    static public int fillYearMonthDaySpinners(Context context, Spinner calendarTypeSpinner,
                                               Spinner yearSpinner, Spinner monthSpinner,
                                               Spinner daySpinner) {
        AbstractDate date = getTodayOfCalendar(calendarTypeFromPosition(
                calendarTypeSpinner.getSelectedItemPosition()));

        // years spinner init.
        String[] years = new String[200];
        int startingYearOnYearSpinner = date.getYear() - years.length / 2;
        for (int i = 0; i < years.length; ++i) {
            years[i] = formatNumber(i + startingYearOnYearSpinner);
        }
        yearSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, years));
        yearSpinner.setSelection(years.length / 2);
        //

        // month spinner init.
        String[] months = monthsNamesOfCalendar(date).clone();
        for (int i = 0; i < months.length; ++i) {
            months[i] = months[i] + " / " + formatNumber(i + 1);
        }
        monthSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, months));
        monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        String[] days = new String[31];
        for (int i = 0; i < days.length; ++i) {
            days[i] = formatNumber(i + 1);
        }
        daySpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, days));
        daySpinner.setSelection(date.getDayOfMonth() - 1);
        //

        return startingYearOnYearSpinner;
    }


    //
    //
    //
    // Service
    //
//    private static final long DAY_IN_SECOND = 86400;

//    private static long calculateDiffToChangeDate() {
//        Date currentTime = Calendar.getInstance().getTime();
//        long current = currentTime.getTime() / 1000;
//
//        Calendar startTime = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, 0);
//        startTime.set(Calendar.MINUTE, 0);
//        startTime.set(Calendar.SECOND, 1);
//
//        long start = startTime.getTimeInMillis() / 1000 + DAY_IN_SECOND;
//
//        return start - current;
//    }

//    private static final String CHANGE_DATE_TAG = "changeDate";
//    public static void setChangeDateWorker() {
//        long remainedSeconds = calculateDiffToChangeDate();
//        OneTimeWorkRequest changeDateWorker =
//                new OneTimeWorkRequest.Builder(UpdateWorker.class)
//                        .setInitialDelay(remainedSeconds, TimeUnit.SECONDS)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
//                        .build();
//
//        WorkManager.getInstance().beginUniqueWork(
//                CHANGE_DATE_TAG,
//                ExistingWorkPolicy.REPLACE,
//                changeDateWorker).enqueue();
//    }


    static public void loadApp(Context context) {
//        if (!goForWorker()) {
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 1);
        startTime.add(Calendar.DATE, 1);
        Intent intent = new Intent(context, BroadcastReceivers.class);
        intent.setAction(BROADCAST_RESTART_APP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.set(AlarmManager.RTC, startTime.getTimeInMillis(), pendingIntent);
//        }
    }

//    public static boolean goForWorker() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
//    }

    private static final String UPDATE_TAG = "update";

    public static void startEitherServiceOrWorker(Context context) {
//        WorkManager workManager = WorkManager.getInstance();
//        if (goForWorker()) {
//            PeriodicWorkRequest.Builder updateBuilder = new PeriodicWorkRequest
//                    .Builder(UpdateWorker.class, 1, TimeUnit.HOURS);
//
//            PeriodicWorkRequest updateWork = updateBuilder.build();
//            workManager.enqueueUniquePeriodicWork(
//                    UPDATE_TAG,
//                    ExistingPeriodicWorkPolicy.REPLACE,
//                    updateWork);
//        } else {
//            // Disable all the scheduled workers, just in case enabled before
//            workManager.cancelAllWork();
//            // Or,
//            // workManager.cancelAllWorkByTag(UPDATE_TAG);
//            // workManager.cancelUniqueWork(CHANGE_DATE_TAG);

        boolean alreadyRan = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (ApplicationService.class.getName().equals(service.service.getClassName())) {
                    alreadyRan = true;
                }
            }
        }

        if (!alreadyRan) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(new Intent(context, ApplicationService.class));
            context.startService(new Intent(context, ApplicationService.class));
        }
//        }
    }
}
