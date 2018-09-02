package com.byagowi.persiancalendar.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding;
import com.byagowi.persiancalendar.databinding.SelectdayFragmentBinding;
import com.byagowi.persiancalendar.entity.CalendarTypeEntity;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entity.FormattedIntEntity;
import com.github.praytimes.Clock;

import java.util.ArrayList;
import java.util.Calendar;
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
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
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


    @StringRes
    final private static int[] YEARS_NAME = {
            R.string.year1, R.string.year2, R.string.year3,
            R.string.year4, R.string.year5, R.string.year6,
            R.string.year7, R.string.year8, R.string.year9,
            R.string.year10, R.string.year11, R.string.year12
    };

    @SuppressLint("SetTextI18n")
    public static void fillCalendarsCard(Context context, long jdn,
                                         CalendarsTabContentBinding binding, OwghatTabContentBinding owghatBinding,
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

        boolean applyLineMultiplier = !TypefaceUtils.isCustomFontEnabled();
        Typeface calendarFont = TypefaceUtils.getCalendarFragmentFont(context);

        binding.weekDayName.setText(Utils.getWeekDayName(firstCalendar));

        binding.firstCalendarDateLinear.setText(CalendarUtils.toLinearDate(firstCalendar));
        binding.firstCalendarDateDay.setText(Utils.formatNumber(firstCalendar.getDayOfMonth()));
        binding.firstCalendarDateDay.setTypeface(calendarFont);
        binding.firstCalendarDate.setText(String.format("%s\n%s",
                CalendarUtils.getMonthName(firstCalendar),
                Utils.formatNumber(firstCalendar.getYear())));
        binding.firstCalendarDate.setTypeface(calendarFont);
        if (applyLineMultiplier) binding.firstCalendarDate.setLineSpacing(0, .6f);

        if (secondCalendar == null) {
            binding.secondCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.secondCalendarDateLinear.setText(CalendarUtils.toLinearDate(secondCalendar));
            binding.secondCalendarDateDay.setText(Utils.formatNumber(secondCalendar.getDayOfMonth()));
            binding.secondCalendarDateDay.setTypeface(calendarFont);
            binding.secondCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(secondCalendar),
                    Utils.formatNumber(secondCalendar.getYear())));
            binding.secondCalendarDate.setTypeface(calendarFont);
            if (applyLineMultiplier) binding.secondCalendarDate.setLineSpacing(0, .6f);
        }

        if (thirdCalendar == null) {
            binding.thirdCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.thirdCalendarDateLinear.setText(CalendarUtils.toLinearDate(thirdCalendar));
            binding.thirdCalendarDateDay.setText(Utils.formatNumber(thirdCalendar.getDayOfMonth()));
            binding.thirdCalendarDateDay.setTypeface(calendarFont);
            binding.thirdCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(thirdCalendar),
                    Utils.formatNumber(thirdCalendar.getYear())));
            binding.thirdCalendarDate.setTypeface(calendarFont);
            if (applyLineMultiplier) binding.thirdCalendarDate.setLineSpacing(0, .6f);
        }

        long diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn);

        if (diffDays == 0) {
            binding.today.setVisibility(View.GONE);
            binding.todayIcon.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                binding.weekDayName.setText(String.format("%s (%s)",
                        binding.weekDayName.getText(),
                        context.getString(R.string.iran_time)));
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

        // Based on Mehdi's work
        {
            CivilDate civilDate = DateConverter.jdnToCivil(jdn);
            int year = civilDate.getYear();
            int month = civilDate.getMonth();
            int day = civilDate.getDayOfMonth();
            int week = civilDate.getDayOfWeek();

            @StringRes
            int monthName, monthEmoji;
            if ((month == 12 && day >= 22 && day <= 31) || (month == 1 && day >= 1 && day <= 19)) {
                monthName = R.string.capricorn;
                monthEmoji = R.string.capricorn_emoji;
            } else if ((month == 1 && day >= 20 && day <= 31) || (month == 2 && day >= 1 && day <= 17)) {
                monthName = R.string.aquarius;
                monthEmoji = R.string.aquarius_emoji;
            } else if ((month == 2 && day >= 18 && day <= 29) || (month == 3 && day >= 1 && day <= 19)) {
                monthName = R.string.pisces;
                monthEmoji = R.string.pisces_emoji;
            } else if ((month == 3 && day >= 20 && day <= 31) || (month == 4 && day >= 1 && day <= 19)) {
                monthName = R.string.aries;
                monthEmoji = R.string.aries_emoji;
            } else if ((month == 4 && day >= 20 && day <= 30) || (month == 5 && day >= 1 && day <= 20)) {
                monthName = R.string.taurus;
                monthEmoji = R.string.taurus_emoji;
            } else if ((month == 5 && day >= 21 && day <= 31) || (month == 6 && day >= 1 && day <= 20)) {
                monthName = R.string.gemini;
                monthEmoji = R.string.gemini_emoji;
            } else if ((month == 6 && day >= 21 && day <= 30) || (month == 7 && day >= 1 && day <= 22)) {
                monthName = R.string.cancer;
                monthEmoji = R.string.cancer_emoji;
            } else if ((month == 7 && day >= 23 && day <= 31) || (month == 8 && day >= 1 && day <= 22)) {
                monthName = R.string.leo;
                monthEmoji = R.string.leo_emoji;
            } else if ((month == 8 && day >= 23 && day <= 31) || (month == 9 && day >= 1 && day <= 22)) {
                monthName = R.string.virgo;
                monthEmoji = R.string.virgo_emoji;
            } else if ((month == 9 && day >= 23 && day <= 30) || (month == 10 && day >= 1 && day <= 22)) {
                monthName = R.string.libra;
                monthEmoji = R.string.libra_emoji;
            } else if ((month == 10 && day >= 23 && day <= 31) || (month == 11 && day >= 1 && day <= 21)) {
                monthName = R.string.scorpio;
                monthEmoji = R.string.scorpio_emoji;
            } else if ((month == 11 && day >= 22 && day <= 30) || (month == 12 && day >= 1 && day <= 21)) {
                monthName = R.string.sagittarius;
                monthEmoji = R.string.sagittarius_emoji;
            } else {
                monthName = R.string.sagittarius; // this never should happen
                monthEmoji = R.string.sagittarius_emoji;
            }

            binding.zodiac.setText(String.format("%s: %s\n%s: %s %s",
                    context.getString(R.string.year_name),
                    context.getString(YEARS_NAME[year % 12]),
                    context.getString(R.string.zodiac),
                    context.getString(monthEmoji), context.getString(monthName)));

            switch (week) {
                case Calendar.SATURDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrOne));
                    break;
                case Calendar.SUNDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrTwo));
                    break;
                case Calendar.MONDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrThree));
                    break;
                case Calendar.TUESDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrFour));
                    break;
                case Calendar.WEDNESDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrFive));
                    break;
                case Calendar.THURSDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrSix));
                    break;
                case Calendar.FRIDAY:
                    owghatBinding.todayDhikr.setText(context.getString(R.string.todaydhikr) + " : " + context.getString(R.string.dhikrSeven));
                    break;
                default:
                    break;
            }

        }
    }

    static public void fillSelectDaySpinners(Context context, SelectdayFragmentBinding binding,
                                             long jdn) {
        if (jdn == -1) {
            jdn = CalendarUtils.getTodayJdn();
        }

        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(
                ((CalendarTypeEntity) binding.calendarTypeSpinner.getSelectedItem()).getType(),
                jdn);

        // years spinner init.
        List<FormattedIntEntity> years = new ArrayList<>();
        final int YEARS = 200;
        int startingYearOnYearSpinner = date.getYear() - YEARS / 2;
        for (int i = 0; i < YEARS; ++i) {
            years.add(new FormattedIntEntity(i + startingYearOnYearSpinner,
                    Utils.formatNumber(i + startingYearOnYearSpinner)));
        }
        binding.yearSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, years));
        binding.yearSpinner.setSelection(YEARS / 2);
        //

        // month spinner init.
        List<FormattedIntEntity> months = new ArrayList<>();
        String[] monthsTitle = Utils.monthsNamesOfCalendar(date);
        for (int i = 1; i <= 12; ++i) {
            months.add(new FormattedIntEntity(i,
                    monthsTitle[i - 1] + " / " + Utils.formatNumber(i)));
        }
        binding.monthSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, months));
        binding.monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        List<FormattedIntEntity> days = new ArrayList<>();
        for (int i = 1; i <= 31; ++i) {
            days.add(new FormattedIntEntity(i, Utils.formatNumber(i)));
        }
        binding.daySpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, days));
        binding.daySpinner.setSelection(date.getDayOfMonth() - 1);
    }

    public static void askForCalendarPermission(Activity activity) {
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                            Manifest.permission.READ_CALENDAR
                    },
                    Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE);
        }
    }

    public static void askForLocationPermission(Activity activity) {
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
        }
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
