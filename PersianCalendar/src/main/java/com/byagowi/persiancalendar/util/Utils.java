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
import android.support.annotation.IdRes;
import android.support.annotation.RawRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.azizhuss.arabicreshaper.ArabicShaping;
import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.ShapedArrayAdapter;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.entity.EventEntity;
import com.byagowi.persiancalendar.enums.CalendarTypeEnum;
import com.byagowi.persiancalendar.enums.SeasonEnum;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.IslamicDate;
import calendar.PersianDate;

import static com.byagowi.persiancalendar.Constants.AM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.ARABIC_DIGITS;
import static com.byagowi.persiancalendar.Constants.BROADCAST_ALARM;
import static com.byagowi.persiancalendar.Constants.BROADCAST_RESTART_APP;
import static com.byagowi.persiancalendar.Constants.DARK_THEME;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS;
import static com.byagowi.persiancalendar.Constants.DAYS_ICONS_AR;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ALTITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ATHAN_VOLUME;
import static com.byagowi.persiancalendar.Constants.DEFAULT_CITY;
import static com.byagowi.persiancalendar.Constants.DEFAULT_IRAN_TIME;
import static com.byagowi.persiancalendar.Constants.DEFAULT_ISLAMIC_OFFSET;
import static com.byagowi.persiancalendar.Constants.DEFAULT_LATITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_LONGITUDE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.DEFAULT_PRAY_TIME_METHOD;
import static com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_CLOCK;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_IN_24;
import static com.byagowi.persiancalendar.Constants.FONT_PATH;
import static com.byagowi.persiancalendar.Constants.KEY_EXTRA_PRAYER_KEY;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.PERSIAN_COMMA;
import static com.byagowi.persiancalendar.Constants.PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.PREF_ALTITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_ALARM;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_GAP;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_VOLUME;
import static com.byagowi.persiancalendar.Constants.PREF_GEOCODED_CITYNAME;
import static com.byagowi.persiancalendar.Constants.PREF_IRAN_TIME;
import static com.byagowi.persiancalendar.Constants.PREF_ISLAMIC_OFFSET;
import static com.byagowi.persiancalendar.Constants.PREF_LATITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_LONGITUDE;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_PRAY_TIME_METHOD;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_LOCATION;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;
import static com.byagowi.persiancalendar.Constants.PREF_WIDGET_CLOCK;
import static com.byagowi.persiancalendar.Constants.PREF_WIDGET_IN_24;

/**
 * Common utilities that needed for this calendar
 *
 * @author ebraminio
 */

public class Utils {

    private final String TAG = Utils.class.getName();
    private Context context;
    private Typeface typeface;
    private SharedPreferences prefs;

    private List<EventEntity> events;
    private PrayTimesCalculator prayTimesCalculator;
    private Map<PrayTime, Clock> prayTimes;

    private String[] persianMonths;
    private String[] islamicMonths;
    private String[] gregorianMonths;
    private String[] weekDays;

    private String cachedCityKey = "";
    private CityEntity cachedCity;

    private Utils(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateStoredPreference();
    }

    private static WeakReference<Utils> myWeakInstance;

    public static Utils getInstance(Context context) {
        if (myWeakInstance == null || myWeakInstance.get() == null) {
            myWeakInstance = new WeakReference<>(new Utils(context.getApplicationContext()));
        }
        return myWeakInstance.get();
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
            typeface = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
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

    public void setFontShapeAndGravity(TextView textView) {
        setFontAndShape(textView);
        textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
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
        // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
        // so switched to "Tehran" method as default calculation algorithm
        return CalculationMethod.valueOf(prefs.getString(PREF_PRAY_TIME_METHOD,
                DEFAULT_PRAY_TIME_METHOD));
    }

    public int getIslamicOffset() {
        return Integer.parseInt(prefs.getString(
                PREF_ISLAMIC_OFFSET,
                DEFAULT_ISLAMIC_OFFSET).replace("+", ""));
    }

    public Coordinate getCoordinate() {
        CityEntity cityEntity = getCityFromPreference();
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

    private char[] preferredDigits;
    private boolean clockIn24;
    public boolean iranTime;

    public void updateStoredPreference() {
        preferredDigits = isPersianDigitSelected()
                ? PERSIAN_DIGITS
                : ARABIC_DIGITS;

        clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24);
        iranTime = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME);
    }

    public boolean isPersianDigitSelected() {
        return prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS);
    }


    public void setTheme(Context context) {
        String key = prefs.getString(PREF_THEME, "");

        int theme = R.style.LightTheme; // default theme

        if (key.equals(LIGHT_THEME)) {
            theme = R.style.LightTheme;
        } else if (key.equals(DARK_THEME)) {
            theme = R.style.DarkTheme;
        }

        context.setTheme(theme);
    }


    public boolean isWidgetClock() {
        return prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK);
    }

    public boolean isNotifyDate() {
        return prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE);
    }

    public int getAthanVolume() {
        return prefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME);
    }

    public String getAppLanguage() {
        String language = prefs.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE);
        // If is empty for whatever reason (pref dialog bug, etc), return Persian at least
        return TextUtils.isEmpty(language) ? DEFAULT_APP_LANGUAGE : language;
    }

    public String getTheme() {
        return prefs.getString(PREF_THEME, LIGHT_THEME);
    }

    public String getSelectedWidgetTextColor() {
        return prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, DEFAULT_SELECTED_WIDGET_TEXT_COLOR);
    }

    public PersianDate getToday() {
        return DateConverter.civilToPersian(new CivilDate(makeCalendarFromDate(new Date())));
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

            if (prayTimes.get(PrayTime.FAJR).getInt() > clock.getInt()) {
                return context.getString(R.string.azan1) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.FAJR));

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
                return context.getString(R.string.azan1) + ": " + getPersianFormattedClock(prayTimes.get(PrayTime.FAJR)); //this is today & not tomorrow
            }

        } else return null;
    }

    public String getPersianFormattedClock(Clock clock) {
        String timeText = null;

        int hour = clock.getHour();
        if (!clockIn24) {
            if (hour >= 12) {
                timeText = PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = AM_IN_PERSIAN;
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
                timeText = PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = AM_IN_PERSIAN;
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

    public String dateToString(AbstractDate date) {
        return formatNumber(date.getDayOfMonth()) + ' ' + getMonthName(date) + ' ' +
                formatNumber(date.getYear());
    }

    public String dayTitleSummary(PersianDate persianDate) {
        return getWeekDayName(persianDate) + PERSIAN_COMMA + " " + dateToString(persianDate);
    }

    public String[] monthsNamesOfCalendar(AbstractDate date) {
        // the next step would be using them so lets check if they have initialized already
        if (persianMonths == null || gregorianMonths == null || islamicMonths == null)
            loadLanguageResource();

        if (date instanceof PersianDate)
            return persianMonths.clone();
        else if (date instanceof IslamicDate)
            return islamicMonths.clone();
        else
            return gregorianMonths.clone();
    }

    public String getMonthName(AbstractDate date) {
        return monthsNamesOfCalendar(date)[date.getMonth() - 1];
    }

    public String getWeekDayName(AbstractDate date) {
        if (date instanceof IslamicDate)
            date = DateConverter.islamicToCivil((IslamicDate) date);
        else if (date instanceof PersianDate)
            date = DateConverter.persianToCivil((PersianDate) date);

        if (weekDays == null)
            loadLanguageResource();

        return weekDays[date.getDayOfWeek() % 7];
    }

    public void quickToast(String message) {
        Toast.makeText(context, shape(message), Toast.LENGTH_SHORT).show();
    }

    public void longToast(String message) {
        Toast.makeText(context, shape(message), Toast.LENGTH_LONG).show();
    }

    public int getDayIconResource(int day) {
        try {
            return preferredDigits == ARABIC_DIGITS ? DAYS_ICONS_AR[day] : DAYS_ICONS[day];
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "No such field is available");
            return 0;
        }
    }

    private String readStream(InputStream is) {
        // http://stackoverflow.com/a/5445161
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public String readRawResource(@RawRes int res) {
        return readStream(context.getResources().openRawResource(res));
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
            JSONObject countries = new JSONObject(readRawResource(R.raw.cities));

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
                if (l.getKey().equals("")) {
                    return -1;
                }
                if (r.getKey().equals(DEFAULT_CITY)) {
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

    private CityEntity getCityFromPreference() {
        String key = prefs.getString(PREF_SELECTED_LOCATION, "");

        if (TextUtils.isEmpty(key) || key.equals(DEFAULT_CITY))
            return null;

        if (key.equals(cachedCityKey))
            return cachedCity;

        // cache last query even if no city available under the key, useful in case invalid
        // value is somehow inserted on the preference
        cachedCityKey = key;

        for (CityEntity cityEntity : getAllCities(false))
            if (cityEntity.getKey().equals(key))
                return cachedCity = cityEntity;

        return cachedCity = null;
    }

    public String formatCoordinate(Coordinate coordinate, String separator) {
        return String.format(Locale.getDefault(), "%s: %.4f%s%s: %.4f",
                context.getString(R.string.latitude), coordinate.getLatitude(), separator,
                context.getString(R.string.longitude), coordinate.getLongitude());
    }

    public String getCityName(boolean fallbackToCoord) {
        CityEntity cityEntity = getCityFromPreference();
        if (cityEntity != null)
            return getAppLanguage().equals("en") ? cityEntity.getEn() : cityEntity.getFa();

        String geocodedCityName = prefs.getString(PREF_GEOCODED_CITYNAME, "");
        if (!TextUtils.isEmpty(geocodedCityName))
            return geocodedCityName;

        if (fallbackToCoord) {
            Coordinate coordinate = getCoordinate();
            if (coordinate != null)
                return formatCoordinate(coordinate, PERSIAN_COMMA + " ");
        }

        return "";
    }

    private void loadEvents() {
        List<EventEntity> events = new ArrayList<>();
        try {
            JSONArray days = new JSONObject(readRawResource(R.raw.events)).getJSONArray("events");

            int length = days.length();
            for (int i = 0; i < length; ++i) {
                JSONObject event = days.getJSONObject(i);

                int year = event.getInt("year");
                int month = event.getInt("month");
                int day = event.getInt("day");
                String title = event.getString("title");
                boolean holiday = event.getBoolean("holiday");

                events.add(new EventEntity(new PersianDate(year, month, day), title, holiday));
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        this.events = events;
    }

    private int maxSupportedYear = -1;
    private int minSupportedYear = -1;
    private boolean isYearWarnGivenOnce = false;

    public void checkYearAndWarnIfNeeded(int selectedYear) {
        // once is enough, see #clearYearWarnFlag() also
        if (isYearWarnGivenOnce)
            return;

        if (maxSupportedYear == -1 || minSupportedYear == -1)
            loadMinMaxSupportedYear();

        if (selectedYear < minSupportedYear) {
            longToast(context.getString(R.string.holidaysIncompletenessWarning));

            isYearWarnGivenOnce = true;
        }

        if (selectedYear > maxSupportedYear) {
            longToast(context.getString(getToday().getYear() > maxSupportedYear
                    ? R.string.shouldBeUpdated
                    : R.string.holidaysIncompletenessWarning));

            isYearWarnGivenOnce = true;
        }
    }

    // called from CalendarFragment to make it once per calendar view
    public void clearYearWarnFlag() {
        isYearWarnGivenOnce = false;
    }

    private void loadMinMaxSupportedYear() {
        if (events == null) {
            loadEvents();
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (EventEntity eventEntity : events) {
            int year = eventEntity.getDate().getYear();

            if (min > year && year != -1) {
                min = year;
            }

            if (max < year) {
                max = year;
            }
        }

        minSupportedYear = min;
        maxSupportedYear = max;
    }

    public List<EventEntity> getEvents(PersianDate day) {
        if (events == null) {
            loadEvents();
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
        intent.setAction(BROADCAST_RESTART_APP);
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

    public String setToCommaSeparated(Set<String> set) {
        return TextUtils.join(",", set);
    }

    public Set<String> commaSeparatedToSet(String commaSeparated) {
        Set<String> result = new HashSet<>();
        result.addAll(Arrays.asList(TextUtils.split(commaSeparated, ",")));
        return result;
    }

    public void loadAlarms() {
        String prefString = prefs.getString(PREF_ATHAN_ALARM, "");
        Log.d(TAG, "reading and loading all alarms from prefs: " + prefString);
        CalculationMethod calculationMethod = getCalculationMethod();
        Coordinate coordinate = getCoordinate();

        if (calculationMethod != null && coordinate != null && !TextUtils.isEmpty(prefString)) {
            PrayTimesCalculator calculator = new PrayTimesCalculator(calculationMethod);
            Map<PrayTime, Clock> prayTimes = calculator.calculate(new Date(), coordinate);

            Set<String> alarmTimesSet = commaSeparatedToSet(prefString);
            // in the past IMSAK was used but now we figured out FAJR was what we wanted
            if (alarmTimesSet.remove("IMSAK")) {
                alarmTimesSet.add("FAJR");
            }

            String[] alarmTimesNames = alarmTimesSet.toArray(new String[alarmTimesSet.size()]);
            for (int i = 0; i < alarmTimesNames.length; i++) {
                PrayTime prayTime = PrayTime.valueOf(alarmTimesNames[i]);

                Clock alarmTime = prayTimes.get(prayTime);

                if (alarmTime != null) {
                    setAlarm(prayTime, alarmTime, i);
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

    // Context preferably should be activity context not application
    public void changeAppLanguage(Context context) {
        String localeCode = getAppLanguage().replaceAll("-(IR|AF)", "");
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

    public void loadLanguageResource() {
        @RawRes int messagesFile;
        String lang = getAppLanguage();

        if (lang.equals("fa-AF"))
            messagesFile = R.raw.messages_fa_af;
        else if (lang.equals("ps"))
            messagesFile = R.raw.messages_ps;
        else
            messagesFile = R.raw.messages_fa;

        persianMonths = new String[12];
        islamicMonths = new String[12];
        gregorianMonths = new String[12];
        weekDays = new String[7];

        try {
            JSONObject messages = new JSONObject(readRawResource(messagesFile));

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
            for (int i = 0; i < 7; ++i)
                weekDays[i] = weekDaysArray.getString(i);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void copyToClipboard(View view) {
        // if it is older than this, the view is also shaped which is not good for copying, so just
        // nvm about backup solution for older Androids
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            CharSequence text = ((TextView) view).getText();
            CopyToClipboard.copyToClipboard(text, context);
            quickToast("«" + text + "»\n" + context.getString(R.string.date_copied_clipboard));
        }
    }

    private static class CopyToClipboard {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public static void copyToClipboard(CharSequence text, Context context) {
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

    // based on R.array.calendar_type order
    public CalendarTypeEnum calendarTypeFromPosition(int position) {
        if (position == 0)
            return CalendarTypeEnum.SHAMSI;
        else if (position == 1)
            return CalendarTypeEnum.ISLAMIC;
        else
            return CalendarTypeEnum.GREGORIAN;
    }

    @IdRes
    public final static int DROPDOWN_LAYOUT = R.layout.select_dialog_item;

    public int fillYearMonthDaySpinners(Context context, Spinner calendarTypeSpinner,
                                        Spinner yearSpinner, Spinner monthSpinner,
                                        Spinner daySpinner) {
        AbstractDate date;
        PersianDate newDatePersian = getToday();
        CivilDate newDateCivil = DateConverter.persianToCivil(newDatePersian);
        IslamicDate newDateIslamic = DateConverter.persianToIslamic(newDatePersian);

        date = newDateCivil;
        switch (calendarTypeFromPosition(calendarTypeSpinner.getSelectedItemPosition())) {
            case GREGORIAN:
                date = newDateCivil;
                break;

            case ISLAMIC:
                date = newDateIslamic;
                break;

            case SHAMSI:
                date = newDatePersian;
                break;
        }

        // years spinner init.
        String[] years = new String[200];
        int startingYearOnYearSpinner = date.getYear() - years.length / 2;
        for (int i = 0; i < years.length; ++i) {
            years[i] = formatNumber(i + startingYearOnYearSpinner);
        }
        yearSpinner.setAdapter(new ShapedArrayAdapter<>(context, DROPDOWN_LAYOUT, years));
        yearSpinner.setSelection(years.length / 2);
        //

        // month spinner init.
        String[] months = monthsNamesOfCalendar(date);
        for (int i = 0; i < months.length; ++i) {
            months[i] = months[i] + " / " + formatNumber(i + 1);
        }
        monthSpinner.setAdapter(new ShapedArrayAdapter<>(context, DROPDOWN_LAYOUT, months));
        monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        String[] days = new String[31];
        for (int i = 0; i < days.length; ++i) {
            days[i] = formatNumber(i + 1);
        }
        daySpinner.setAdapter(new ShapedArrayAdapter<>(context, DROPDOWN_LAYOUT, days));
        daySpinner.setSelection(date.getDayOfMonth() - 1);
        //

        return startingYearOnYearSpinner;
    }
}
