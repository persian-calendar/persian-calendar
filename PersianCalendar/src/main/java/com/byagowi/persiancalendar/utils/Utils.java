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

    static public String formatDeviceCalendarEventTitle(DeviceCalendarEvent event) {
        String desc = event.getDescription();
        String title = event.getTitle();
        if (!TextUtils.isEmpty(desc))
            title += " (" + Html.fromHtml(event.getDescription()).toString().trim() + ")";

        return title.replaceAll("\\n", " ").trim();
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

}
