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
import com.byagowi.persiancalendar.databinding.CalendarsTabContentBinding;
import com.byagowi.persiancalendar.databinding.SelectdayFragmentBinding;
import com.byagowi.persiancalendar.entity.CalendarTypeEntity;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.github.praytimes.Clock;

import java.util.List;
import java.util.Locale;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.CivilDate;
import calendar.DateConverter;

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
                                         CalendarsTabContentBinding binding,
                                         CalendarType calendarType,
                                         List<CalendarType> calendars) {
        AbstractDate firstCalendar,
                secondCalendar = null,
                thirdCalendar = null;
        firstCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendars.get(0), jdn);
        if (calendars.size() > 1) {
            secondCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendars.get(1), jdn);
        }
        if (calendars.size() > 2) {
            thirdCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendars.get(2), jdn);
        }

        binding.weekDayName.setText(Utils.getWeekDayName(firstCalendar));

        binding.firstCalendarDateLinear.setText(CalendarUtils.toLinearDate(firstCalendar));
        binding.firstCalendarDateDay.setText(Utils.formatNumber(firstCalendar.getDayOfMonth()));
        binding.firstCalendarDate.setText(CalendarUtils.getMonthName(firstCalendar) + "\n" + Utils.formatNumber(firstCalendar.getYear()));

        if (secondCalendar == null) {
            binding.secondCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.secondCalendarDateLinear.setText(CalendarUtils.toLinearDate(secondCalendar));
            binding.secondCalendarDateDay.setText(Utils.formatNumber(secondCalendar.getDayOfMonth()));
            binding.secondCalendarDate.setText(CalendarUtils.getMonthName(secondCalendar) + "\n" + Utils.formatNumber(secondCalendar.getYear()));
        }

        if (thirdCalendar == null) {
            binding.thirdCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.thirdCalendarDateLinear.setText(CalendarUtils.toLinearDate(thirdCalendar));
            binding.thirdCalendarDateDay.setText(Utils.formatNumber(thirdCalendar.getDayOfMonth()));
            binding.thirdCalendarDate.setText(CalendarUtils.getMonthName(thirdCalendar) + "\n" + Utils.formatNumber(thirdCalendar.getYear()));
        }

        long diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn);

        if (diffDays == 0) {
            binding.today.setVisibility(View.GONE);
            binding.todayIcon.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                binding.weekDayName.setText(binding.weekDayName.getText() + " (" + context.getString(R.string.iran_time) + ")");
            }
            binding.today.setVisibility(View.GONE);
            binding.todayIcon.setVisibility(View.GONE);
            binding.diffDate.setVisibility(View.GONE);
        } else {
            binding.today.setVisibility(View.VISIBLE);
            binding.todayIcon.setVisibility(View.VISIBLE);
            binding.diffDate.setVisibility(View.VISIBLE);

            CivilDate civilBase = new CivilDate(2000, 1, 1);
            CivilDate civilOffset = DateConverter.jdnToCivil(diffDays + DateConverter.civilToJdn(civilBase));
            int yearDiff = civilOffset.getYear() - 2000;
            int monthDiff = civilOffset.getMonth() - 1;
            int dayOfMonthDiff = civilOffset.getDayOfMonth() - 1;
            String text = String.format(context.getString(R.string.date_diff_text),
                    Utils.formatNumber((int) diffDays),
                    Utils.formatNumber(yearDiff),
                    Utils.formatNumber(monthDiff),
                    Utils.formatNumber(dayOfMonthDiff));
            if (diffDays <= 30) {
                text = text.split("\\(")[0];
            }
            binding.diffDate.setText(text);
        }

        {
            AbstractDate mainDate = CalendarUtils.getDateFromJdnOfCalendar(calendarType, jdn);
            AbstractDate startOfYear = CalendarUtils.getDateOfCalendar(calendarType,
                    mainDate.getYear(), 1, 1);
            AbstractDate startOfNextYear = CalendarUtils.getDateOfCalendar(
                    calendarType, mainDate.getYear() + 1, 1, 1);
            long startOfYearJdn = CalendarUtils.getJdnDate(startOfYear);
            long endOfYearJdn = CalendarUtils.getJdnDate(startOfNextYear) - 1;
            int currentWeek = CalendarUtils.calculateWeekOfYear(jdn, startOfYearJdn);
            int weeksCount = CalendarUtils.calculateWeekOfYear(endOfYearJdn, startOfYearJdn);

            binding.startAndEndOfYearDiff.setText(
                    String.format(context.getString(R.string.start_of_year_diff) + "\n" +
                                    context.getString(R.string.end_of_year_diff),
                            Utils.formatNumber((int) (jdn - startOfYearJdn)),
                            Utils.formatNumber(currentWeek),
                            Utils.formatNumber(mainDate.getMonth()),
                            Utils.formatNumber((int) (endOfYearJdn - jdn)),
                            Utils.formatNumber(weeksCount - currentWeek),
                            Utils.formatNumber(12 - mainDate.getMonth())));
        }
    }

    static public int fillSelectdaySpinners(Context context, SelectdayFragmentBinding binding, long jdn) {
        if (jdn == -1) {
            jdn = CalendarUtils.getTodayJdn();
        }

        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(
                ((CalendarTypeEntity) binding.calendarTypeSpinner.getSelectedItem()).getType(),
                jdn);

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

    public static void askforExternalStoragePermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    Constants.WRITE_EXTERNAL_STORAGE);
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
