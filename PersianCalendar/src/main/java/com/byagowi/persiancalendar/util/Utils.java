package com.byagowi.persiancalendar.util;

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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.azizhuss.arabicreshaper.ArabicShaping;
import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.entity.EventEntity;
import com.byagowi.persiancalendar.enums.SeasonEnum;
import com.byagowi.persiancalendar.locale.LocaleUtils;
import com.byagowi.persiancalendar.service.BroadcastReceivers;
import com.github.praytimes.CalculationMethod;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;
import com.github.twaddington.TypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.IslamicDate;
import calendar.LocaleData;
import calendar.PersianDate;

/**
 * Common utilities that needed for this calendar
 *
 * @author ebraminio
 */

public class Utils {
    private final String TAG = Utils.class.getName();
    private static Utils myInstance;
    private LocaleUtils localeUtils;
    private Context context;
    private Typeface typeface;
    private SharedPreferences prefs;

    private List<EventEntity> events;
    private PrayTimesCalculator prayTimesCalculator;
    private Map<PrayTime, Clock> prayTimes;

    private String cachedCityKey = "";
    private CityEntity cachedCity;

    private Utils(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateStoredPreference();
    }

    public static Utils getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new Utils(context.getApplicationContext());
        }
        return myInstance;
    }

    /**
     * Text shaping is a essential thing on supporting Arabic script text on older Android versions.
     * It converts normal Arabic character to their presentation forms according to their position
     * on the text.
     *
     * @param text Arabic string
     * @return Shaped text
     */
    public String shape(String text) {
        return (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
                ? ArabicShaping.shape(text)
                : text;
    }

    public String getString(String key) {
        return localeUtils == null
                ? ""
                : localeUtils.getString(key);
    }

    public String programVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Name not found on PersianCalendarUtils.programVersion");
            return "";
        }
    }

    private void initTypeface() {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), Constants.FONT_PATH);
        }
    }

    public void setFont(TextView textView) {
        initTypeface();
        textView.setTypeface(typeface);
    }

    public void setFontAndShape(TextView textView) {
        setFont(textView);
        textView.setText(shape(textView.getText().toString()));
    }

    public void setFontAndShape(PreferenceViewHolder holder) {
        // See android.support.v7.preference.Preference#onBindViewHolder
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        if (titleView != null) {
            setFontAndShape(titleView);
        }
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            setFontAndShape(summaryView);
        }
    }

    public void setActivityTitleAndSubtitle(Activity activity, String title, String subtitle) {
        if (title == null || subtitle == null) {
            return;
        }
        initTypeface();

        //noinspection ConstantConditions
        ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();

        if (supportActionBar == null) {
            return;
        }

        SpannableString titleSpan = new SpannableString(shape(title));
        titleSpan.setSpan(new TypefaceSpan(typeface), 0, titleSpan.length(), 0);
        titleSpan.setSpan(new RelativeSizeSpan(0.8f), 0, titleSpan.length(), 0);
        supportActionBar.setTitle(titleSpan);

        SpannableString subtitleSpan = new SpannableString(shape(subtitle));
        subtitleSpan.setSpan(new TypefaceSpan(typeface), 0, subtitleSpan.length(), 0);
        subtitleSpan.setSpan(new RelativeSizeSpan(0.8f), 0, subtitleSpan.length(), 0);
        supportActionBar.setSubtitle(subtitleSpan);
    }

    public CalculationMethod getCalculationMethod() {
        // It seems Iran is using Jafari method
        return CalculationMethod.valueOf(prefs.getString(
                Constants.PREF_PRAY_TIME_METHOD,
                Constants.DEFAULT_PRAY_TIME_METHOD));
    }

    public int getIslamicOffset() {
        return Integer.parseInt(prefs.getString(
                Constants.PREF_ISLAMIC_OFFSET,
                Constants.DEFAULT_ISLAMIC_OFFSET).replace("+", ""));
    }

    public Coordinate getCoordinate() {
        CityEntity cityEntity = getCityFromPreference();
        if (cityEntity != null) {
            return cityEntity.getCoordinate();
        }

        try {
            Coordinate coord = new Coordinate(
                    Double.parseDouble(prefs.getString(
                            Constants.PREF_LATITUDE,
                            Constants.DEFAULT_LATITUDE)),

                    Double.parseDouble(prefs.getString(
                            Constants.PREF_LONGITUDE,
                            Constants.DEFAULT_LONGITUDE)),

                    Double.parseDouble(prefs.getString(
                            Constants.PREF_ALTITUDE,
                            Constants.DEFAULT_ALTITUDE))
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

    private char[] preferredDigits;
    private boolean clockIn24;
    public boolean iranTime;

    public void updateStoredPreference() {
        preferredDigits = isPersianDigitSelected()
                ? Constants.PERSIAN_DIGITS
                : Constants.ARABIC_DIGITS;

        clockIn24 = prefs.getBoolean(Constants.PREF_WIDGET_IN_24, Constants.DEFAULT_WIDGET_IN_24);
        iranTime = prefs.getBoolean(Constants.PREF_IRAN_TIME, Constants.DEFAULT_IRAN_TIME);
    }

    public boolean isPersianDigitSelected() {
        return prefs.getBoolean(Constants.PREF_PERSIAN_DIGITS, Constants.DEFAULT_PERSIAN_DIGITS);
    }


    public void setTheme(Context context) {
        String key = prefs.getString(Constants.PREF_THEME, "");

        int theme = R.style.LightTheme; // default theme

        if (key.equals(Constants.LIGHT_THEME)) {
            theme = R.style.LightTheme;
        } else if (key.equals(Constants.DARK_THEME)) {
            theme = R.style.DarkTheme;
        }

        context.setTheme(theme);
    }


    public boolean isWidgetClock() {
        return prefs.getBoolean(Constants.PREF_WIDGET_CLOCK, Constants.DEFAULT_WIDGET_CLOCK);
    }

    public boolean isNotifyDate() {
        return prefs.getBoolean(Constants.PREF_NOTIFY_DATE, Constants.DEFAULT_NOTIFY_DATE);
    }

    public int getAthanVolume() {
        return prefs.getInt(Constants.PREF_ATHAN_VOLUME, Constants.DEFAULT_ATHAN_VOLUME);
    }

    public String getAppLanguage() {
        String language = prefs.getString(
                Constants.PREF_APP_LANGUAGE,
                Constants.DEFAULT_APP_LANGUAGE);
        // If is empty for whatever reason (pref dialog bug, etc), return Persian at least
        return TextUtils.isEmpty(language) ? Constants.DEFAULT_APP_LANGUAGE : language;
    }

    public String getTheme() {
        return prefs.getString(Constants.PREF_THEME, Constants.LIGHT_THEME);
    }

    public String getSelectedWidgetTextColor() {
        return prefs.getString(
                Constants.PREF_SELECTED_WIDGET_TEXT_COLOR,
                Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR);
    }

    public PersianDate getToday() {
        CivilDate civilDate = new CivilDate();
        return DateConverter.civilToPersian(civilDate);
    }

    public Calendar makeCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (iranTime) {
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return calendar;
    }

    public String clockToString(int hour, int minute) {
        return formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute));
    }

    public String getNextOghatTime(Clock clock, boolean changeDate) {
        Coordinate coordinate = getCoordinate();

        if (coordinate != null) {
            if (prayTimesCalculator == null) {
                prayTimesCalculator = new PrayTimesCalculator(getCalculationMethod());
                changeDate = true;
            }

            if (changeDate) {
                prayTimes = prayTimesCalculator.calculate(new Date(), coordinate);
            }

            if (prayTimes.get(PrayTime.IMSAK).getInt() > clock.getInt()) {
                return context.getString(R.string.azan1) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK));

            } else if (prayTimes.get(PrayTime.SUNRISE).getInt() > clock.getInt()) {
                return context.getString(R.string.aftab1) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE));

            } else if (prayTimes.get(PrayTime.DHUHR).getInt() > clock.getInt()) {
                return context.getString(R.string.azan2) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR));

            } else if (prayTimes.get(PrayTime.ASR).getInt() > clock.getInt()) {
                return context.getString(R.string.azan3) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.ASR));

            } else if (prayTimes.get(PrayTime.SUNSET).getInt() > clock.getInt()) {
                return context.getString(R.string.aftab2) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET));

            } else if (prayTimes.get(PrayTime.MAGHRIB).getInt() > clock.getInt()) {
                return context.getString(R.string.azan4) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB));

            } else if (prayTimes.get(PrayTime.ISHA).getInt() > clock.getInt()) {
                return context.getString(R.string.azan5) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.ISHA));

            } else if (prayTimes.get(PrayTime.MIDNIGHT).getInt() > clock.getInt()) {
                return context.getString(R.string.aftab3) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT));

            } else {
                return context.getString(R.string.azan1) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK)); //this is today & not tomorrow
            }

        } else return null;
    }

    public String getPersianFormattedClock(Clock clock) {
        String timeText = null;

        int hour = clock.getHour();
        if (!clockIn24) {
            if (hour >= 12) {
                timeText = Constants.PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = Constants.AM_IN_PERSIAN;
            }
        }

        String result = clockToString(hour, clock.getMinute());
        if (!clockIn24) {
            result = result + " " + timeText;
        }
        return result;
    }

    public String getPersianFormattedClock(Calendar calendar) {
        String timeText = null;

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (!clockIn24) {
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                timeText = Constants.PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = Constants.AM_IN_PERSIAN;
            }
        }

        String result = clockToString(hour, calendar.get(Calendar.MINUTE));
        if (!clockIn24) {
            result = result + " " + timeText;
        }
        return result;
    }

    public String formatNumber(int number) {
        return formatNumber(Integer.toString(number));
    }

    public String formatNumber(String number) {
        if (preferredDigits == Constants.ARABIC_DIGITS)
            return number;

        StringBuilder sb = new StringBuilder();
        for (char i : number.toCharArray()) {
            if (Character.isDigit(i)) {
                sb.append(preferredDigits[Integer.parseInt(i + "")]);
            } else {
                sb.append(i);
            }
        }
        return sb.toString();
    }

    public String dateToString(AbstractDate date) {
        return formatNumber(date.getDayOfMonth()) + ' '
                + getMonthName(date) + ' '
                + formatNumber(date.getYear());
    }

    public String dayTitleSummary(PersianDate persianDate) {
        return getWeekDayName(persianDate) + Constants.PERSIAN_COMMA + " "
                + dateToString(persianDate);
    }

    public String getMonthName(AbstractDate date) {
        String monthName = "";
        // zero based
        int month = date.getMonth() - 1;

        if (date.getClass().equals(PersianDate.class)) {
            LocaleData.PersianMonthNames monthNameCode = LocaleData.PersianMonthNames.values()[month];
            monthName = getString(String.valueOf(monthNameCode));
        } else if (date.getClass().equals(CivilDate.class)) {
            LocaleData.CivilMonthNames monthNameCode = LocaleData.CivilMonthNames.values()[month];
            monthName = getString(String.valueOf(monthNameCode));
        } else if (date.getClass().equals(IslamicDate.class)) {
            LocaleData.IslamicMonthNames monthNameCode = LocaleData.IslamicMonthNames.values()[month];
            monthName = getString(String.valueOf(monthNameCode));
        }

        return monthName;
    }

    public List<String> getMonthsNamesListWithOrdinal(AbstractDate date) {
        AbstractDate dateClone = date.clone();
        List<String> monthNameList = new ArrayList<>();
        dateClone.setDayOfMonth(1);
        for (int month = 1; month <= 12; ++month) {
            dateClone.setMonth(month);
            monthNameList.add(getMonthName(dateClone) + " / " + formatNumber(month));
        }
        return monthNameList;
    }

    public String getWeekDayName(AbstractDate date) {
        CivilDate civilDate;
        if (date.getClass().equals(PersianDate.class)) {
            civilDate = DateConverter.persianToCivil((PersianDate) date);
        } else if (date.getClass().equals(IslamicDate.class)) {
            civilDate = DateConverter.islamicToCivil((IslamicDate) date);
        } else {
            civilDate = (CivilDate) date;
        }

        // zero based
        int dayOfWeek = civilDate.getDayOfWeek() - 1;
        LocaleData.WeekDayNames weekDayNameCode = LocaleData.WeekDayNames.values()[dayOfWeek];

        return getString(weekDayNameCode.toString());
    }

    public void quickToast(String message) {
        Toast.makeText(context, shape(message), Toast.LENGTH_SHORT).show();
    }

    public int getDayIconResource(int day) {
        try {
            return Constants.DAYS_ICONS[day];
        } catch (IndexOutOfBoundsException e) {
            Log.e("com.byagowi.calendar", "No such field is available");
            return 0;
        }
    }

    private String convertStreamToString(InputStream is) {
        // http://stackoverflow.com/a/5445161
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String persianStringToArabic(String text) {
        return text
                .replaceAll("ی", "ي")
                .replaceAll("ک", "ك")
                .replaceAll("گ", "كی")
                .replaceAll("ژ", "زی")
                .replaceAll("چ", "جی")
                .replaceAll("پ", "بی");
    }

    private <T> Iterable<T> iteratorToIterable(final Iterator<T> iterator) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }


    public List<CityEntity> getAllCities(boolean needsSort) {
        List<CityEntity> result = new ArrayList<>();
        try {
            JSONObject countries = new JSONObject(convertStreamToString(
                    context.getResources().openRawResource(R.raw.cities)));

            for (String countryCode : iteratorToIterable(countries.keys())) {
                JSONObject country = countries.getJSONObject(countryCode);

                String countryEn = country.getString("en");
                String countryFa = country.getString("fa");

                JSONObject cities = country.getJSONObject("cities");

                for (String key : iteratorToIterable(cities.keys())) {
                    JSONObject city = cities.getJSONObject(key);

                    String en = city.getString("en");
                    String fa = city.getString("fa");

                    Coordinate coordinate = new Coordinate(
                            city.getDouble("latitude"),
                            city.getDouble("longitude"),
                            0 // city.getDouble("elevation")
                    );

                    result.add(new CityEntity(key, en, fa, countryCode, countryEn, countryFa, coordinate));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        if (!needsSort) {
            return result;
        }

        final String locale = getAppLanguage();

        CityEntity[] cities = result.toArray(new CityEntity[result.size()]);
        // Sort first by country code then city
        Arrays.sort(cities, new Comparator<CityEntity>() {
            @Override
            public int compare(CityEntity l, CityEntity r) {
                if (l.getKey().equals("CUSTOM")) {
                    return -1;
                }
                if (r.getKey().equals("CUSTOM")) {
                    return 1;
                }
                int compare = r.getCountryCode().compareTo(l.getCountryCode());
                if (compare != 0) return compare;
                if (locale.equals("en")) {
                    return l.getEn().compareTo(r.getEn());
                } else {
                    return persianStringToArabic(l.getFa())
                            .compareTo(persianStringToArabic(r.getFa()));
                }
            }
        });

        return Arrays.asList(cities);
    }

    public CityEntity getCityFromPreference() {
        String key = prefs.getString("Location", "");

        if (TextUtils.isEmpty(key) || key.equals("CUSTOM")) {
            return null;
        }

        if (key.equals(cachedCityKey)) {
            return cachedCity;
        }

        // cache last query even if no city avialable under the key, useful in case invalid
        // value is somehow inserted on the preference
        cachedCityKey = key;

        for (CityEntity cityEntity : getAllCities(false))
            if (cityEntity.getKey().equals(key)) {
                cachedCity = cityEntity;
                return cityEntity;
            }

        cachedCity = null;
        return null;
    }

    private List<EventEntity> readEventsFromJSON(InputStream is) {
        List<EventEntity> result = new ArrayList<>();
        try {
            JSONArray days = new JSONObject(convertStreamToString(is)).getJSONArray("events");

            int length = days.length();
            for (int i = 0; i < length; ++i) {
                JSONObject event = days.getJSONObject(i);

                int year = event.getInt("year");
                int month = event.getInt("month");
                int day = event.getInt("day");
                String title = event.getString("title");
                boolean holiday = event.getBoolean("holiday");

                result.add(new EventEntity(new PersianDate(year, month, day), title, holiday));
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

    public List<EventEntity> getEvents(PersianDate day) {
        if (events == null) {
            events = readEventsFromJSON(context.getResources().openRawResource(R.raw.events));
        }

        List<EventEntity> result = new ArrayList<>();
        for (EventEntity eventEntity : events) {
            if (eventEntity.getDate().equals(day)) {
                result.add(eventEntity);
            }
        }
        return result;
    }

    public String getEventsTitle(PersianDate day, boolean holiday) {
        String titles = "";
        boolean first = true;
        List<EventEntity> dayEvents = getEvents(day);

        for (EventEntity event : dayEvents) {
            if (event.isHoliday() == holiday) {
                if (first) {
                    first = false;

                } else {
                    titles = titles + "\n";
                }
                titles = titles + event.getTitle();
            }
        }
        return titles;
    }

    public void loadApp() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 1);
        Intent intent = new Intent(context, BroadcastReceivers.class);
        intent.setAction(Constants.BROADCAST_RESTART_APP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC, startTime.getTimeInMillis(), pendingIntent);
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void loadAlarms() {
        Log.d(TAG, "reading and loading all alarms from prefs");
        String prefString = prefs.getString(Constants.PREF_ATHAN_ALARM, "");
        CalculationMethod calculationMethod = getCalculationMethod();
        Coordinate coordinate = getCoordinate();

        if (calculationMethod != null && coordinate != null && !TextUtils.isEmpty(prefString)) {
            PrayTimesCalculator calculator = new PrayTimesCalculator(calculationMethod);
            Map<PrayTime, Clock> prayTimes = calculator.calculate(new Date(), coordinate);

            String[] alarmTimesNames = TextUtils.split(prefString, ",");
            for (int i = 0; i < alarmTimesNames.length; i++) {

                Clock alarmTime = prayTimes.get(PrayTime.valueOf(alarmTimesNames[i]));

                if (alarmTime != null) {
                    setAlarm(PrayTime.valueOf(alarmTimesNames[i]), alarmTime, i);
                }
            }
        }
    }

    public void setAlarm(PrayTime prayTime, Clock clock, int id) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, clock.getHour());
        triggerTime.set(Calendar.MINUTE, clock.getMinute());
        setAlarm(prayTime, triggerTime.getTimeInMillis(), id);
    }

    public void setAlarm(PrayTime prayTime, long timeInMillis, int id) {
        String valAthanGap = prefs.getString(Constants.PREF_ATHAN_GAP, "0");
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
            intent.setAction(Constants.BROADCAST_ALARM);
            intent.putExtra(Constants.KEY_EXTRA_PRAYER_KEY, prayTime.name());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SetExactAlarm.setExactAlarm(alarmManager,
                        AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private static class SetExactAlarm {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public static void setExactAlarm(AlarmManager alarmManager,
                                         int type, long triggerAtMillis, PendingIntent pendingIntent) {
            alarmManager.setExact(type, triggerAtMillis, pendingIntent);
        }
    }

    public Uri getAthanUri() {
        String defaultSoundUri = "android.resource://" + context.getPackageName() + "/" + R.raw.abdulbasit;
        return Uri.parse(defaultSoundUri);
    }

    public void changeAppLanguage(String localeCode) {
        Locale locale = TextUtils.isEmpty(localeCode) ? Locale.getDefault() : new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = context.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void changeCalendarLanguage(String localeCode) {
        if (localeUtils == null) {
            localeUtils = LocaleUtils.getInstance(context, localeCode);
        }

        localeUtils.changeLocale(localeCode);
    }

    public String loadLanguageFromSettings() {
        // set app language
        String locale = getAppLanguage();
        changeAppLanguage(locale.replaceAll("-(IR|AF)", ""));
        changeCalendarLanguage(locale);
        return locale;
    }

    public void copyToClipboard(View view) {
        // if it is older than this, the view is also shaped which is not good for copying, so just
        // nvm about backup solution for older Androids
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            CharSequence text = ((TextView) view).getText();
            CopyToClipboard.copyToCliboard(text, context);
            quickToast("«" + text + "»\n" + context.getString(R.string.date_copied_clipboard));
        }
    }

    private static class CopyToClipboard {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public static void copyToCliboard(CharSequence text, Context context) {
            ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("converted date", text));
        }
    }

    public SeasonEnum getSeason() {
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

    public List<DayEntity> getDays(int offset) {
        List<DayEntity> days = new ArrayList<>();
        PersianDate persianDate = getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        int dayOfWeek = DateConverter.persianToCivil(persianDate).getDayOfWeek() % 7;

        try {
            PersianDate today = getToday();
            for (int i = 1; i <= 31; i++) {
                persianDate.setDayOfMonth(i);

                DayEntity dayEntity = new DayEntity();
                dayEntity.setNum(formatNumber(i));
                dayEntity.setDayOfWeek(dayOfWeek);

                if (dayOfWeek == 6 || !TextUtils.isEmpty(getEventsTitle(persianDate, true))) {
                    dayEntity.setHoliday(true);
                }

                if (getEvents(persianDate).size() > 0) {
                    dayEntity.setEvent(true);
                }

                dayEntity.setPersianDate(persianDate.clone());

                if (persianDate.equals(today)) {
                    dayEntity.setToday(true);
                }

                days.add(dayEntity);
                dayOfWeek++;
                if (dayOfWeek == 7) {
                    dayOfWeek = 0;
                }
            }
        } catch (DayOutOfRangeException e) {
            // okay, it was expected
        }

        return days;
    }
}
