package com.byagowi.persiancalendar.locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import calendar.LocaleData;

import static com.byagowi.persiancalendar.Constants.ASR;
import static com.byagowi.persiancalendar.Constants.DAY;
import static com.byagowi.persiancalendar.Constants.DHUHR;
import static com.byagowi.persiancalendar.Constants.EQUALS_WITH;
import static com.byagowi.persiancalendar.Constants.FAJR;
import static com.byagowi.persiancalendar.Constants.GEORGIAN;
import static com.byagowi.persiancalendar.Constants.HIJRI_QAMARI;
import static com.byagowi.persiancalendar.Constants.HIJRI_SHAMSI;
import static com.byagowi.persiancalendar.Constants.ISHA;
import static com.byagowi.persiancalendar.Constants.MAGHRIB;
import static com.byagowi.persiancalendar.Constants.MIDNIGHT;
import static com.byagowi.persiancalendar.Constants.MONTH;
import static com.byagowi.persiancalendar.Constants.SUNRISE;
import static com.byagowi.persiancalendar.Constants.SUNSET;
import static com.byagowi.persiancalendar.Constants.TODAY;
import static com.byagowi.persiancalendar.Constants.YEAR;

public class LocaleUtils {
    private static final String TAG = LocaleUtils.class.getName();
    private static LocaleUtils instance;
    private static final String CALENDAR_BUNDLE = "CalendarBundle";

    private Context context;
    private Map<String, String> cache = new HashMap<>();

    private LocaleUtils() {
    }

    private LocaleUtils(Context context, String localeCode) {
        this.context = context;
    }

    public static LocaleUtils getInstance(Context context, String code) {
        if (instance == null) {
            instance = new LocaleUtils(context, code);
        }
        return instance;
    }

    public void changeLocale(String localeCode) {
        // We used "prs" instead "fa-AF" as it seems Android Studio doesn't recognize it
        if (localeCode.equals("fa-AF")) {
            localeCode = "prs";
        }
        // These are locales that have extra file named like CalendarBundle_*.properties
        String fileSuffix = (!TextUtils.isEmpty(localeCode) && localeCode.matches("ps|prs")) ?
                ("_" + localeCode) :
                "";

        try {
            InputStream pis = context.getAssets().open("locale/" + CALENDAR_BUNDLE + fileSuffix + ".properties");
            ResourceBundle bundle = new PropertyResourceBundle(pis);

            // cache strings for later use
            for (LocaleData.PersianMonthNames name : LocaleData.PersianMonthNames.values()) {
                String stringName = name.toString();
                cache.put(stringName, getUTF8(bundle.getString(stringName)));
            }

            for (LocaleData.CivilMonthNames name : LocaleData.CivilMonthNames.values()) {
                String stringName = name.toString();
                cache.put(stringName, getUTF8(bundle.getString(stringName)));
            }

            for (LocaleData.IslamicMonthNames name : LocaleData.IslamicMonthNames.values()) {
                String stringName = name.toString();
                cache.put(stringName, getUTF8(bundle.getString(stringName)));
            }

            for (LocaleData.WeekDayNames name : LocaleData.WeekDayNames.values()) {
                String stringName = name.toString();
                cache.put(stringName, getUTF8(bundle.getString(stringName)));
            }

            cache.put(FAJR, getUTF8(bundle.getString(FAJR)));
            cache.put(SUNRISE, getUTF8(bundle.getString(SUNRISE)));
            cache.put(DHUHR, getUTF8(bundle.getString(DHUHR)));
            cache.put(ASR, getUTF8(bundle.getString(ASR)));
            cache.put(SUNSET, getUTF8(bundle.getString(SUNSET)));
            cache.put(MAGHRIB, getUTF8(bundle.getString(MAGHRIB)));
            cache.put(ISHA, getUTF8(bundle.getString(ISHA)));
            cache.put(MIDNIGHT, getUTF8(bundle.getString(MIDNIGHT)));
            cache.put(TODAY, getUTF8(bundle.getString(TODAY)));
            cache.put(EQUALS_WITH, getUTF8(bundle.getString(EQUALS_WITH)));
            cache.put(DAY, getUTF8(bundle.getString(DAY)));
            cache.put(MONTH, getUTF8(bundle.getString(MONTH)));
            cache.put(YEAR, getUTF8(bundle.getString(YEAR)));
            cache.put(HIJRI_SHAMSI, getUTF8(bundle.getString(HIJRI_SHAMSI)));
            cache.put(HIJRI_QAMARI, getUTF8(bundle.getString(HIJRI_QAMARI)));
            cache.put(GEORGIAN, getUTF8(bundle.getString(GEORGIAN)));
        } catch (IOException e) {
            Log.e(TAG, "COULDN'T LOAD PROPERTIES FILES", e);
        }
    }

    public String getString(String key) {
        return cache.get(key);
    }

    private String getUTF8(String input) {
        String output = "";
        try {
            output = new String(input.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "couldn't convert string to utf-8: " + input, e);
        }
        return output;
    }
}
