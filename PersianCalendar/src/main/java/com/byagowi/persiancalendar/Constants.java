package com.byagowi.persiancalendar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Constants {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 23;
    public static final int ATHAN_RINGTONE_REQUEST_CODE = 19;
    public static final int CALENDAR_READ_PERMISSION_REQUEST_CODE = 55;
    public static final int CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE = 63;

    public static final String REMINDERS_STORE_KEY = "REMINDERS_STORE";
    public static final String REMINDERS_COUNT_KEY = "REMINDER_%d";
    public static final String REMINDER_ID = "reminder_id";
    public static final int SIGNAL_PAUSE = 5;

    public static final String LANG_FA = "fa";
    public static final String LANG_FA_AF = "fa-AF";
    public static final String LANG_PS = "ps";
    public static final String LANG_GLK = "glk";
    public static final String LANG_AR = "ar";
    public static final String LANG_EN_IR = "en";
    public static final String LANG_EN_US = "en-US";
    public static final String LANG_JA = "ja";
    public static final String LANG_CKB = "ckb";
    public static final String LANG_UR = "ur";

    public static final int CALENDARS_TAB = 0;
    public static final int EVENT_TAB = 1;
    public static final int OWGHAT_TAB = 2;

    public static final String LAST_CHOSEN_TAB_KEY = "LastChosenTab";

    public static final String PREF_MAIN_CALENDAR_KEY = "mainCalendarType";
    public static final String PREF_OTHER_CALENDARS_KEY = "otherCalendarTypes";
    public static final String PREF_KEY_ATHAN = "Athan";
    public static final String PREF_PRAY_TIME_METHOD = "SelectedPrayTimeMethod";
    public static final String PREF_ISLAMIC_OFFSET = "islamic_offset";
    public static final String PREF_LATITUDE = "Latitude";
    public static final String PREF_LONGITUDE = "Longitude";
    public static final String PREF_SELECTED_LOCATION = "Location";
    public static final String PREF_GEOCODED_CITYNAME = "cityname";
    public static final String PREF_ALTITUDE = "Altitude";
    public static final String PREF_WIDGET_IN_24 = "WidgetIn24";
    public static final String PREF_IRAN_TIME = "IranTime";
    public static final String PREF_PERSIAN_DIGITS = "PersianDigits";
    public static final String PREF_ATHAN_URI = "AthanURI";
    public static final String PREF_ATHAN_NAME = "AthanName";
    public static final String PREF_SHOW_DEVICE_CALENDAR_EVENTS = "showDeviceCalendarEvents";
    public static final String PREF_WIDGET_CLOCK = "WidgetClock";
    public static final String PREF_NOTIFY_DATE = "NotifyDate";
    public static final String PREF_NOTIFICATION_ATHAN = "NotificationAthan";
    public static final String PREF_NOTIFY_DATE_LOCK_SCREEN = "NotifyDateLockScreen";
    public static final String PREF_ATHAN_VOLUME = "AthanVolume";
    public static final String PREF_APP_LANGUAGE = "AppLanguage";
    public static final String PREF_SELECTED_WIDGET_TEXT_COLOR = "SelectedWidgetTextColor";
    public static final String PREF_ATHAN_ALARM = "AthanAlarm";
    public static final String PREF_ATHAN_GAP = "AthanGap";
    public static final String PREF_THEME = "Theme";
    public static final String PREF_HOLIDAY_TYPES = "holiday_types";
    public static final String PREF_WEEK_START = "WeekStart";
    public static final String PREF_WEEK_ENDS = "WeekEnds";
    public static final String PREF_SHIFT_WORK_STARTING_JDN = "ShiftWorkJdn";
    public static final String PREF_SHIFT_WORK_SETTING = "ShiftWorkSetting";
    public static final String PREF_SHIFT_WORK_RECURS = "ShiftWorkRecurs";

    public static final String CHANGE_LANGUAGE_IS_PROMOTED_ONCE = "CHANGE_LANGUAGE_IS_PROMOTED_ONCE";

    public static final String DEFAULT_CITY = "CUSTOM";
    public static final String DEFAULT_PRAY_TIME_METHOD = "Tehran";
    public static final String DEFAULT_ISLAMIC_OFFSET = "0";
    public static final String DEFAULT_LATITUDE = "0";
    public static final String DEFAULT_LONGITUDE = "0";
    public static final String DEFAULT_ALTITUDE = "0";
    public static final String DEFAULT_APP_LANGUAGE = "fa";
    public static final String DEFAULT_SELECTED_WIDGET_TEXT_COLOR = "#ffffffff";
    public static final boolean DEFAULT_WIDGET_IN_24 = true;
    public static final boolean DEFAULT_IRAN_TIME = false;
    public static final boolean DEFAULT_PERSIAN_DIGITS = true;
    public static final boolean DEFAULT_WIDGET_CLOCK = true;
    public static final boolean DEFAULT_NOTIFY_DATE = true;
    public static final boolean DEFAULT_NOTIFICATION_ATHAN = false;
    public static final boolean DEFAULT_NOTIFY_DATE_LOCK_SCREEN = true;
    public static final int DEFAULT_ATHAN_VOLUME = 1;
    public static final String DEFAULT_WEEK_START = "0";
    // WeekEnds, 6 means Friday
    public static final Set<String> DEFAULT_WEEK_ENDS = new HashSet<>(Collections.singletonList("6"));

    public static final String LIGHT_THEME = "LightTheme";
    public static final String DARK_THEME = "DarkTheme";
    public static final String BLUE_THEME = "BlueTheme";
    public static final String MODERN_THEME = "ClassicTheme"; // don't change it, for legacy reasons

    public static final int LOAD_APP_ID = 1000;
    public static final int THREE_HOURS_APP_ID = 1010;
    public static final int ALARMS_BASE_ID = 2000;
    public static final int REMINDERS_BASE_ID = 10000; // reserved till 20000
    public static final int REMINDERS_MAX_ID = 10000;

    public static final String OFFSET_ARGUMENT = "OFFSET_ARGUMENT";
    public static final String BROADCAST_ALARM = "BROADCAST_ALARM";
    public static final String BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP";
    public static final String BROADCAST_UPDATE_APP = "BROADCAST_UPDATE_APP";
    public static final String KEY_EXTRA_PRAYER_KEY = "prayer_name";
    public static final int BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY = Integer.MAX_VALUE;
    public static final String FONT_PATH = "fonts/NotoNaskhArabic-Regular.ttf";

    public static final char RLM = '\u200F';
    public static final String ZWJ = "\u200D";
    public static final char[] ARABIC_INDIC_DIGITS = {'٠', '١', '٢', '٣', '٤', '٥',
            '٦', '٧', '٨', '٩'};
    public static final char[] ARABIC_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'};
    public static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶',
            '۷', '۸', '۹'};

    // for now
    public static final char[] CJK_DIGITS = ARABIC_DIGITS;
//    public static final char[] CJK_DIGITS = {'０', '１', '２', '３', '４', '５', '６',
//        '７', '８', '９'};

    public static final String DEFAULT_AM = "ق.ظ";
    public static final String DEFAULT_PM = "ب.ظ";

    public static final int[] DAYS_ICONS = {0,
            R.drawable.day1, R.drawable.day2, R.drawable.day3,
            R.drawable.day4, R.drawable.day5, R.drawable.day6,
            R.drawable.day7, R.drawable.day8, R.drawable.day9,
            R.drawable.day10, R.drawable.day11, R.drawable.day12,
            R.drawable.day13, R.drawable.day14, R.drawable.day15,
            R.drawable.day16, R.drawable.day17, R.drawable.day18,
            R.drawable.day19, R.drawable.day20, R.drawable.day21,
            R.drawable.day22, R.drawable.day23, R.drawable.day24,
            R.drawable.day25, R.drawable.day26, R.drawable.day27,
            R.drawable.day28, R.drawable.day29, R.drawable.day30,
            R.drawable.day31};

    public static final int[] DAYS_ICONS_AR = {0,
            R.drawable.day1_ar, R.drawable.day2_ar, R.drawable.day3_ar,
            R.drawable.day4_ar, R.drawable.day5_ar, R.drawable.day6_ar,
            R.drawable.day7_ar, R.drawable.day8_ar, R.drawable.day9_ar,
            R.drawable.day10_ar, R.drawable.day11_ar, R.drawable.day12_ar,
            R.drawable.day13_ar, R.drawable.day14_ar, R.drawable.day15_ar,
            R.drawable.day16_ar, R.drawable.day17_ar, R.drawable.day18_ar,
            R.drawable.day19_ar, R.drawable.day20_ar, R.drawable.day21_ar,
            R.drawable.day22_ar, R.drawable.day23_ar, R.drawable.day24_ar,
            R.drawable.day25_ar, R.drawable.day26_ar, R.drawable.day27_ar,
            R.drawable.day28_ar, R.drawable.day29_ar, R.drawable.day30_ar,
            R.drawable.day31_ar};

    public static final int[] DAYS_ICONS_CKB = {0,
            R.drawable.day1, R.drawable.day2, R.drawable.day3,
            R.drawable.day4_ckb, R.drawable.day5_ckb, R.drawable.day6_ckb,
            R.drawable.day7, R.drawable.day8, R.drawable.day9,
            R.drawable.day10, R.drawable.day11, R.drawable.day12,
            R.drawable.day13, R.drawable.day14_ckb, R.drawable.day15_ckb,
            R.drawable.day16_ckb, R.drawable.day17, R.drawable.day18,
            R.drawable.day19, R.drawable.day20, R.drawable.day21,
            R.drawable.day22, R.drawable.day23, R.drawable.day24_ckb,
            R.drawable.day25_ckb, R.drawable.day26_ckb, R.drawable.day27,
            R.drawable.day28, R.drawable.day29, R.drawable.day30,
            R.drawable.day31};
}
