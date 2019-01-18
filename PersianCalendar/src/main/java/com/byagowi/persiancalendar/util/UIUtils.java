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
import com.byagowi.persiancalendar.praytimes.Clock;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

import static com.byagowi.persiancalendar.Constants.BLUE_THEME;
import static com.byagowi.persiancalendar.Constants.DARK_THEME;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.MODERN_THEME;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;

public class UIUtils {
    private static final long twoSeconds = TimeUnit.SECONDS.toMillis(2);
    private static long latestToastShowTime = -1;
    private static AudioManager audioManager = null;

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

    public static void toggleShowDeviceCalendarOnPreference(Context context, boolean enable) {
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

    public static String baseFormatClock(int hour, int minute) {
        return Utils.formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute));
    }

    public static boolean isRTL(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    static public String getFormattedClock(Clock clock, boolean forceIn12) {
        boolean in12 = Utils.isClockIn12() || forceIn12;
        if (!in12) return baseFormatClock(clock.getHour(), clock.getMinute());

        int hour = clock.getHour();
        String suffix;
        if (hour >= 12) {
            suffix = Utils.getAmString();
            hour -= 12;
        } else {
            suffix = Utils.getPmString();
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
    static public Uri getDefaultAthanUri(Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(R.raw.abdulbasit) + '/' +
                context.getResources().getResourceTypeName(R.raw.abdulbasit) + '/' +
                context.getResources().getResourceEntryName(R.raw.abdulbasit));
    }

    static String getOnlyLanguage(String string) {
        return string.replaceAll("-(IR|AF|US)", "");
    }

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
