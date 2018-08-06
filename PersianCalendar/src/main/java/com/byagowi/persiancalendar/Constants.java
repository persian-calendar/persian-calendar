package com.byagowi.persiancalendar;

public class Constants {

    public static final String LOCAL_INTENT_UPDATE_PREFERENCE = "update-preference";

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 23;
    public static final int ATHAN_RINGTONE_REQUEST_CODE = 19;
    public static final int CALENDAR_READ_PERMISSION_REQUEST_CODE = 55;
    public static final int CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE = 63;


    public static final String LANG_EN = "en";
    public static final String LANG_EN_US = "en-US";
    public static final String LANG_CKB = "ckb";
    public static final String LANG_UR = "ur";

    public static final String PREF_KEY_ATHAN = "Athan";
    public static final String PREF_PRAY_TIME_METHOD = "SelectedPrayTimeMethod";
    public static final String PREF_ISLAMIC_OFFSET = "islamicOffset";
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

    public static final String LIGHT_THEME = "LightTheme";
    public static final String DARK_THEME = "DarkTheme";
    public static final String CLASSIC_THEME = "ClassicTheme";

    public static final String FAJR = "fajr";
    public static final String SUNRISE = "sunrise";
    public static final String DHUHR = "dhuhr";
    public static final String ASR = "asr";
    public static final String SUNSET = "sunset";
    public static final String MAGHRIB = "maghrib";
    public static final String ISHA = "isha";
    public static final String MIDNIGHT = "midnight";

    public static final String TODAY = "today";
    public static final String EQUALS_WITH = "equals_with";
    public static final String DAY = "day";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    public static final int LOAD_APP_ID = 1000;
    public static final int THREE_HOURS_APP_ID = 1010;
    public static final int ALARMS_BASE_ID = 2000;

    public static final String OFFSET_ARGUMENT = "OFFSET_ARGUMENT";
    public static final String BROADCAST_INTENT_TO_MONTH_FRAGMENT = "BROADCAST_INTENT_TO_MONTH_FRAGMENT";
    public static final String BROADCAST_FIELD_TO_MONTH_FRAGMENT = "BROADCAST_FIELD_TO_MONTH_FRAGMENT";
    public static final String BROADCAST_FIELD_SELECT_DAY_JDN = "BROADCAST_FIELD_SELECT_DAY_JDN";
    public static final String BROADCAST_ALARM = "BROADCAST_ALARM";
    public static final String BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP";
    public static final String BROADCAST_UPDATE_APP = "BROADCAST_UPDATE_APP";
    public static final String KEY_EXTRA_PRAYER_KEY = "prayer_name";
    public static final int BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY = Integer.MAX_VALUE;
    public static final String FONT_PATH = "fonts/NotoNaskhArabic-Regular.ttf";

    public static final char RLM = '\u200F';
    public static final char[] ARABIC_INDIC_DIGITS = {'٠', '١', '٢', '٣', '٤', '٥',
            '٦', '٧', '٨', '٩'};
    public static final char[] ARABIC_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'};
    public static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶',
            '۷', '۸', '۹'};
    public static final String AM_IN_PERSIAN = "ق.ظ";
    public static final String AM_IN_CKB = "ب.ن";
    public static final String PM_IN_PERSIAN = "ب.ظ";
    public static final String PM_IN_CKB = "د.ن";

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
