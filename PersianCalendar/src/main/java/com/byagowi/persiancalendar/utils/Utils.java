package com.byagowi.persiancalendar.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.byagowi.persiancalendar.R;
import io.github.persiancalendar.calendar.AbstractDate;
import io.github.persiancalendar.calendar.CivilDate;
import io.github.persiancalendar.calendar.IslamicDate;
import io.github.persiancalendar.calendar.PersianDate;
import com.byagowi.persiancalendar.entities.AbstractEvent;
import com.byagowi.persiancalendar.entities.CityItem;
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entities.GregorianCalendarEvent;
import com.byagowi.persiancalendar.entities.IslamicCalendarEvent;
import com.byagowi.persiancalendar.entities.PersianCalendarEvent;
import com.byagowi.persiancalendar.entities.ShiftWorkRecord;
import com.byagowi.persiancalendar.praytimes.CalculationMethod;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.Coordinate;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.service.UpdateWorker;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.github.persiancalendar.Equinox;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.byagowi.persiancalendar.ConstantsKt.*;
import static com.byagowi.persiancalendar.utils.FunctionsKt.*;
import static com.byagowi.persiancalendar.utils.UtilsKt.*;

//import com.byagowi.persiancalendar.entities.Reminder;


public class Utils {

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
        selectedWidgetBackgroundColor = prefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
                DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR);
        // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
        // so switched to "Tehran" method as default calculation algorithm
        calculationMethod = prefs.getString(PREF_PRAY_TIME_METHOD, DEFAULT_PRAY_TIME_METHOD);
        coordinate = getCoordinate(context);
        try {
            mainCalendar = CalendarType.valueOf(prefs.getString(PREF_MAIN_CALENDAR_KEY, "SHAMSI"));
            String otherCalendarsString = prefs.getString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC");
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
        weekStartOffset = Integer.parseInt(weekStart);
        weekEnds = new boolean[7];
        Set<String> weekEndsSet = prefs.getStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS);
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
        } catch (JSONException ignore) {
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

    private static <T extends AbstractDate> boolean holidayAwareEqualCheck(T event, T date) {
        return event.getDayOfMonth() == date.getDayOfMonth()
                && event.getMonth() == date.getMonth()
                && (event.getYear() == -1 || event.getYear() == date.getYear());
    }

    static public @NonNull
    String getShiftWorkTitle(long jdn, boolean abbreviated) {
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
                if (sShiftWorkRecurs && abbreviated &&
                        (shift.getType().equals("r") || shift.getType().equals(sShiftWorkTitles.get("r"))))
                    return "";

                String title = sShiftWorkTitles.get(shift.getType());
                if (title == null) title = shift.getType();
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

    static public @NonNull
    String getEventsTitle(List<AbstractEvent> dayEvents, boolean holiday,
                          boolean compact, boolean showDeviceCalendarEvents,
                          boolean insertRLM) {
        StringBuilder titles = new StringBuilder();
        boolean first = true;

        for (AbstractEvent event : dayEvents)
            if (event.isHoliday() == holiday) {
                String title = event.getTitle();
                if (insertRLM) {
                    title = RLM + title;
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

    public static void setChangeDateWorker(@NonNull Context context) {
        long remainedSeconds = calculateDiffToChangeDate();
        OneTimeWorkRequest changeDateWorker =
                new OneTimeWorkRequest.Builder(UpdateWorker.class)
                        .setInitialDelay(remainedSeconds, TimeUnit.SECONDS)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(context).beginUniqueWork(
                CHANGE_DATE_TAG,
                ExistingWorkPolicy.REPLACE,
                changeDateWorker).enqueue();
    }

    public static void startEitherServiceOrWorker(@NonNull Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
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

    static public @NonNull
    String dateStringOfOtherCalendars(long jdn, String separator) {
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
                        CALENDAR_READ_PERMISSION_REQUEST_CODE))
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
                        LOCATION_PERMISSION_REQUEST_CODE))
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
