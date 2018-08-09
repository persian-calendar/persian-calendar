package com.byagowi.persiancalendar.util;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.CalendarsCardBinding;
import com.byagowi.persiancalendar.databinding.SelectdayFragmentBinding;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.github.praytimes.Clock;

import java.util.Locale;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

import static com.byagowi.persiancalendar.Constants.AM_IN_CKB;
import static com.byagowi.persiancalendar.Constants.AM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.DARK_THEME;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.PM_IN_CKB;
import static com.byagowi.persiancalendar.Constants.PM_IN_PERSIAN;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;

public class UIUtils {
    static public void setActivityTitleAndSubtitle(Activity activity, String title, String subtitle) {
        //noinspection ConstantConditions
        ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
            supportActionBar.setSubtitle(subtitle);
        }
    }

    public static void fillCalendarsCard(Context context, long jdn,
                                         CalendarsCardBinding binding, boolean isToday) {
        PersianDate persianDate = DateConverter.jdnToPersian(jdn);
        CivilDate civilDate = DateConverter.jdnToCivil(jdn);
        IslamicDate hijriDate = DateConverter.civilToIslamic(civilDate, Utils.getIslamicOffset());

        binding.weekDayName.setText(Utils.getWeekDayName(civilDate));
        binding.shamsiDateLinear.setText(CalendarUtils.toLinearDate(persianDate));
        binding.shamsiDateDay.setText(Utils.formatNumber(persianDate.getDayOfMonth()));
        binding.shamsiDate.setText(CalendarUtils.getMonthName(persianDate) + "\n" + Utils.formatNumber(persianDate.getYear()));

        binding.gregorianDateLinear.setText(CalendarUtils.toLinearDate(civilDate));
        binding.gregorianDateDay.setText(Utils.formatNumber(civilDate.getDayOfMonth()));
        binding.gregorianDate.setText(CalendarUtils.getMonthName(civilDate) + "\n" + Utils.formatNumber(civilDate.getYear()));

        binding.islamicDateLinear.setText(CalendarUtils.toLinearDate(hijriDate));
        binding.islamicDateDay.setText(Utils.formatNumber(hijriDate.getDayOfMonth()));
        binding.islamicDate.setText(CalendarUtils.getMonthName(hijriDate) + "\n" + Utils.formatNumber(hijriDate.getYear()));

        if (isToday) {
            binding.today.setVisibility(View.GONE);
            binding.todayIcon.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                binding.weekDayName.setText(binding.weekDayName.getText() + " (" + context.getString(R.string.iran_time) + ")");
            }
        } else {
            binding.today.setVisibility(View.VISIBLE);
            binding.todayIcon.setVisibility(View.VISIBLE);
        }
    }

    static public int fillSelectdaySpinners(Context context, SelectdayFragmentBinding binding) {
        AbstractDate date = CalendarUtils.getTodayOfCalendar(CalendarUtils.calendarTypeFromPosition(
                binding.calendarTypeSpinner.getSelectedItemPosition()));

        // years spinner init.
        String[] years = new String[200];
        int startingYearOnYearSpinner = date.getYear() - years.length / 2;
        for (int i = 0; i < years.length; ++i) {
            years[i] = Utils.formatNumber(i + startingYearOnYearSpinner);
        }
        binding.yearSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, years));
        binding.yearSpinner.setSelection(years.length / 2);
        //

        // month spinner init.
        String[] months = Utils.monthsNamesOfCalendar(date).clone();
        for (int i = 0; i < months.length; ++i) {
            months[i] = months[i] + " / " + Utils.formatNumber(i + 1);
        }
        binding.monthSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, months));
        binding.monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        String[] days = new String[31];
        for (int i = 0; i < days.length; ++i) {
            days[i] = Utils.formatNumber(i + 1);
        }
        binding.daySpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, days));
        binding.daySpinner.setSelection(date.getDayOfMonth() - 1);
        //

        return startingYearOnYearSpinner;
    }

    public static void askForCalendarPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                            Manifest.permission.READ_CALENDAR
                    },
                    Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE);
        }
    }

    static public String formatDeviceCalendarEventTitle(DeviceCalendarEvent event) {
        String desc = event.getDescription();
        String title = event.getTitle();
        if (!TextUtils.isEmpty(desc))
            title += " (" + Html.fromHtml(event.getDescription()).toString().trim() + ")";

        return title.replaceAll("\\n", " ").trim();
    }

    static String clockToString(int hour, int minute) {
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
                timeText = Utils.getAppLanguage().equals("ckb")
                        ? PM_IN_CKB
                        : PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = Utils.getAppLanguage().equals("ckb")
                        ? AM_IN_CKB
                        : AM_IN_PERSIAN;
            }
        }

        String result = clockToString(hour, clock.getMinute());
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

    static public void copyToClipboard(Context context, CharSequence text) {
        ClipboardManager clipboardService =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardService != null) {
            clipboardService.setPrimaryClip(ClipData.newPlainText("converted date", text));
            Toast.makeText(context, "«" + text + "»\n" + context.getString(R.string.date_copied_clipboard), Toast.LENGTH_SHORT).show();
        }
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
}
