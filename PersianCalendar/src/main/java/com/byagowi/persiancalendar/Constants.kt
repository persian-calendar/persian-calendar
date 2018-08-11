package com.byagowi.persiancalendar

object Constants {

  val LOCAL_INTENT_UPDATE_PREFERENCE = "update-preference"

  val LOCATION_PERMISSION_REQUEST_CODE = 23
  val ATHAN_RINGTONE_REQUEST_CODE = 19
  val CALENDAR_READ_PERMISSION_REQUEST_CODE = 55
  val CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE = 63


  val LANG_EN = "en"
  val LANG_EN_US = "en-US"
  val LANG_CKB = "ckb"
  val LANG_UR = "ur"

  val PREF_KEY_ATHAN = "Athan"
  val PREF_PRAY_TIME_METHOD = "SelectedPrayTimeMethod"
  val PREF_ISLAMIC_OFFSET = "islamicOffset"
  val PREF_LATITUDE = "Latitude"
  val PREF_LONGITUDE = "Longitude"
  val PREF_SELECTED_LOCATION = "Location"
  val PREF_GEOCODED_CITYNAME = "cityname"
  val PREF_ALTITUDE = "Altitude"
  val PREF_WIDGET_IN_24 = "WidgetIn24"
  val PREF_IRAN_TIME = "IranTime"
  val PREF_PERSIAN_DIGITS = "PersianDigits"
  val PREF_ATHAN_URI = "AthanURI"
  val PREF_ATHAN_NAME = "AthanName"
  val PREF_SHOW_DEVICE_CALENDAR_EVENTS = "showDeviceCalendarEvents"
  val PREF_WIDGET_CLOCK = "WidgetClock"
  val PREF_NOTIFY_DATE = "NotifyDate"
  val PREF_NOTIFICATION_ATHAN = "NotificationAthan"
  val PREF_NOTIFY_DATE_LOCK_SCREEN = "NotifyDateLockScreen"
  val PREF_ATHAN_VOLUME = "AthanVolume"
  val PREF_APP_LANGUAGE = "AppLanguage"
  val PREF_SELECTED_WIDGET_TEXT_COLOR = "SelectedWidgetTextColor"
  val PREF_ATHAN_ALARM = "AthanAlarm"
  val PREF_ATHAN_GAP = "AthanGap"
  val PREF_THEME = "Theme"
  val PREF_HOLIDAY_TYPES = "holiday_types"

  val DEFAULT_CITY = "CUSTOM"
  val DEFAULT_PRAY_TIME_METHOD = "Tehran"
  val DEFAULT_ISLAMIC_OFFSET = "0"
  val DEFAULT_LATITUDE = "0"
  val DEFAULT_LONGITUDE = "0"
  val DEFAULT_ALTITUDE = "0"
  val DEFAULT_APP_LANGUAGE = "fa"
  val DEFAULT_SELECTED_WIDGET_TEXT_COLOR = "#ffffffff"
  val DEFAULT_WIDGET_IN_24 = true
  val DEFAULT_IRAN_TIME = false
  val DEFAULT_PERSIAN_DIGITS = true
  val DEFAULT_WIDGET_CLOCK = true
  val DEFAULT_NOTIFY_DATE = true
  val DEFAULT_NOTIFICATION_ATHAN = false
  val DEFAULT_NOTIFY_DATE_LOCK_SCREEN = true
  val DEFAULT_ATHAN_VOLUME = 1

  val LIGHT_THEME = "LightTheme"
  val DARK_THEME = "DarkTheme"
  val CLASSIC_THEME = "ClassicTheme"

  val FAJR = "fajr"
  val SUNRISE = "sunrise"
  val DHUHR = "dhuhr"
  val ASR = "asr"
  val SUNSET = "sunset"
  val MAGHRIB = "maghrib"
  val ISHA = "isha"
  val MIDNIGHT = "midnight"

  val TODAY = "today"
  val EQUALS_WITH = "equals_with"
  val DAY = "day"
  val MONTH = "month"
  val YEAR = "year"

  val LOAD_APP_ID = 1000
  val THREE_HOURS_APP_ID = 1010
  val ALARMS_BASE_ID = 2000

  val OFFSET_ARGUMENT = "OFFSET_ARGUMENT"
  val BROADCAST_INTENT_TO_MONTH_FRAGMENT = "BROADCAST_INTENT_TO_MONTH_FRAGMENT"
  val BROADCAST_FIELD_TO_MONTH_FRAGMENT = "BROADCAST_FIELD_TO_MONTH_FRAGMENT"
  val BROADCAST_FIELD_SELECT_DAY_JDN = "BROADCAST_FIELD_SELECT_DAY_JDN"
  val BROADCAST_ALARM = "BROADCAST_ALARM"
  val BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP"
  val BROADCAST_UPDATE_APP = "BROADCAST_UPDATE_APP"
  val KEY_EXTRA_PRAYER_KEY = "prayer_name"
  val BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY = Integer.MAX_VALUE
  val FONT_PATH = "fonts/NotoNaskhArabic-Regular.ttf"

  val RLM = '\u200F'
  val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
  val ARABIC_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
  val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
  val AM_IN_PERSIAN = "ق.ظ"
  val AM_IN_CKB = "ب.ن"
  val PM_IN_PERSIAN = "ب.ظ"
  val PM_IN_CKB = "د.ن"

  val DAYS_ICONS = intArrayOf(0, R.drawable.day1, R.drawable.day2, R.drawable.day3, R.drawable.day4, R.drawable.day5, R.drawable.day6, R.drawable.day7, R.drawable.day8, R.drawable.day9, R.drawable.day10, R.drawable.day11, R.drawable.day12, R.drawable.day13, R.drawable.day14, R.drawable.day15, R.drawable.day16, R.drawable.day17, R.drawable.day18, R.drawable.day19, R.drawable.day20, R.drawable.day21, R.drawable.day22, R.drawable.day23, R.drawable.day24, R.drawable.day25, R.drawable.day26, R.drawable.day27, R.drawable.day28, R.drawable.day29, R.drawable.day30, R.drawable.day31)

  val DAYS_ICONS_AR = intArrayOf(0, R.drawable.day1_ar, R.drawable.day2_ar, R.drawable.day3_ar, R.drawable.day4_ar, R.drawable.day5_ar, R.drawable.day6_ar, R.drawable.day7_ar, R.drawable.day8_ar, R.drawable.day9_ar, R.drawable.day10_ar, R.drawable.day11_ar, R.drawable.day12_ar, R.drawable.day13_ar, R.drawable.day14_ar, R.drawable.day15_ar, R.drawable.day16_ar, R.drawable.day17_ar, R.drawable.day18_ar, R.drawable.day19_ar, R.drawable.day20_ar, R.drawable.day21_ar, R.drawable.day22_ar, R.drawable.day23_ar, R.drawable.day24_ar, R.drawable.day25_ar, R.drawable.day26_ar, R.drawable.day27_ar, R.drawable.day28_ar, R.drawable.day29_ar, R.drawable.day30_ar, R.drawable.day31_ar)

  val DAYS_ICONS_CKB = intArrayOf(0, R.drawable.day1, R.drawable.day2, R.drawable.day3, R.drawable.day4_ckb, R.drawable.day5_ckb, R.drawable.day6_ckb, R.drawable.day7, R.drawable.day8, R.drawable.day9, R.drawable.day10, R.drawable.day11, R.drawable.day12, R.drawable.day13, R.drawable.day14_ckb, R.drawable.day15_ckb, R.drawable.day16_ckb, R.drawable.day17, R.drawable.day18, R.drawable.day19, R.drawable.day20, R.drawable.day21, R.drawable.day22, R.drawable.day23, R.drawable.day24_ckb, R.drawable.day25_ckb, R.drawable.day26_ckb, R.drawable.day27, R.drawable.day28, R.drawable.day29, R.drawable.day30, R.drawable.day31)
}
