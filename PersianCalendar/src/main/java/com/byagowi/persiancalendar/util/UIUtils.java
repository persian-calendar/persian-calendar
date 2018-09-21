package com.byagowi.persiancalendar.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.github.praytimes.Clock;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.byagowi.persiancalendar.Constants.AM_IN_CKB;
import static com.byagowi.persiancalendar.Constants.AM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.BLUE_THEME;
import static com.byagowi.persiancalendar.Constants.CLASSIC_THEME;
import static com.byagowi.persiancalendar.Constants.DARK_THEME;
import static com.byagowi.persiancalendar.Constants.LANG_CKB;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.PM_IN_CKB;
import static com.byagowi.persiancalendar.Constants.PM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;

public class UIUtils {
    static public void setActivityTitleAndSubtitle(Activity activity, String title, String subtitle) {
        //noinspection ConstantConditions
        ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
            supportActionBar.setSubtitle(subtitle);
        }
    }

    public static void askForCalendarPermission(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.calendar_access)
                .setMessage(R.string.phone_calendar_required)
                .setPositiveButton(R.string.resume, (dialog, id) -> activity.requestPermissions(new String[]{
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
                .setPositiveButton(R.string.resume, (dialog, id) -> activity.requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        Constants.LOCATION_PERMISSION_REQUEST_CODE))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel()).show();
    }

    public static void toggleShowCalendarOnPreference(Context context, boolean enable) {
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

    public static String baseClockToString(Clock clock) {
        return baseClockToString(clock.getHour(), clock.getMinute());
    }


    public static String baseClockToString(int hour, int minute) {
        return Utils.formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute));
    }

    public static boolean isRTL(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    static public String getFormattedClock(Clock clock) {
        String timeText = null;

        int hour = clock.getHour();
        if (!Utils.isClockIn24()) {
            if (hour >= 12) {
                timeText = Utils.getAppLanguage().equals(LANG_CKB) ? PM_IN_CKB : PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = Utils.getAppLanguage().equals(LANG_CKB) ? AM_IN_CKB : AM_IN_PERSIAN;
            }
        }

        String result = baseClockToString(hour, clock.getMinute());
        if (!Utils.isClockIn24()) {
            result = result + " " + timeText;
        }
        return result;
    }

    static public @StringRes
    int getPrayTimeText(String athanKey) {
        switch (athanKey) {
            case "FAJR":
                return R.string.azan1;

            case "DHUHR":
                return R.string.azan2;

            case "ASR":
                return R.string.azan3;

            case "MAGHRIB":
                return R.string.azan4;

            case "ISHA":
            default:
                return R.string.azan5;
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

            case CLASSIC_THEME:
                return R.style.ClassicTheme;

            case BLUE_THEME:
                return R.style.BlueTheme;

            default:
            case LIGHT_THEME:
                return R.style.LightTheme;
        }
    }

    // https://stackoverflow.com/a/27788209
    private static Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID));
    }

    static public Uri getDefaultAthanUri(Context context) {
        return resourceToUri(context, R.raw.abdulbasit);
    }

    static String getOnlyLanguage(String string) {
        return string.replaceAll("-(IR|AF|US)", "");
    }

    private static long latestToastShowTime = -1;
    private static final long twoSeconds = TimeUnit.SECONDS.toMillis(2);
    private static AudioManager audioManager = null;

    public static void a11yShowToastWithClick(Context context, @StringRes int resId) {
        if (!Utils.isTalkBackEnabled()) return;

        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        long now = System.currentTimeMillis();
        if (now - latestToastShowTime > twoSeconds) {
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
            // https://stackoverflow.com/a/29423018
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
            latestToastShowTime = now;
        }
    }
}
