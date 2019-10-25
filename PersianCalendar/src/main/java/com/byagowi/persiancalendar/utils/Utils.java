package com.byagowi.persiancalendar.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entities.AbstractEvent;
import com.byagowi.persiancalendar.entities.CityItem;
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entities.ShiftWorkRecord;
import io.github.persiancalendar.praytimes.Coordinate;
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
import java.util.concurrent.TimeUnit;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.byagowi.persiancalendar.ConstantsKt.*;
import static com.byagowi.persiancalendar.utils.FunctionsKt.*;
import static com.byagowi.persiancalendar.utils.UtilsKt.*;

public class Utils {

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

}
