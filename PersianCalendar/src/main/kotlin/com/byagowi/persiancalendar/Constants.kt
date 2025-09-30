package com.byagowi.persiancalendar

import android.graphics.Color
import android.os.Build

/**
 * Centralized app constants and default preference keys/values used across the project.
 *
 * Kept intentionally simple — only constants, no runtime logic — to avoid initialization side-effects.
 */

const val LOG_TAG = "PersianCalendar"

// --- General keys -----------------------------------------------------------
const val LAST_CHOSEN_TAB_KEY = "LastChosenTab"
const val EXPANDED_TIME_STATE_KEY = "ExpandedTimeState"

// --- Preferences keys ------------------------------------------------------
const val PREF_MAIN_CALENDAR_KEY = "mainCalendarType"
const val PREF_OTHER_CALENDARS_KEY = "otherCalendarTypes"
const val PREF_SECONDARY_CALENDAR_IN_TABLE = "secondaryCalendarShown"
const val PREF_PRAY_TIME_METHOD = "SelectedPrayTimeMethod"
const val PREF_HIGH_LATITUDES_METHOD = "SelectedHighLatitudesMethod"
const val PREF_MIDNIGHT_METHOD = "SelectedMidnightMethod"
const val PREF_ASR_HANAFI_JURISTIC = "AsrHanafiJuristic"
const val PREF_ISLAMIC_OFFSET = "islamic_offset"
const val PREF_ISLAMIC_OFFSET_SET_DATE = "islamic_offset_set_date"
const val PREF_LATITUDE = "Latitude"
const val PREF_LONGITUDE = "Longitude"
const val PREF_SELECTED_LOCATION = "Location"
const val PREF_GEOCODED_CITYNAME = "cityname"
const val PREF_ALTITUDE = "Altitude"
const val PREF_WIDGET_IN_24 = "WidgetIn24"
const val PREF_IRAN_TIME = "IranTime"
const val PREF_LOCAL_DIGITS = "PersianDigits"
const val PREF_ATHAN_URI = "AthanURI"
const val PREF_ATHAN_NAME = "AthanName"
const val PREF_SHOW_DEVICE_CALENDAR_EVENTS = "showDeviceCalendarEvents"
const val PREF_CALENDARS_IDS_TO_EXCLUDE = "calendarsIdsToExclude"
const val PREF_CALENDARS_IDS_AS_HOLIDAY = "calendarsIdsAsHoliday"
const val PREF_WIDGET_CLOCK = "WidgetClock"
const val PREF_CENTER_ALIGN_WIDGETS = "CenterAlignWidgets"
const val PREF_WHAT_TO_SHOW_WIDGETS = "what_to_show"
const val PREF_WIDGETS_PREFER_SYSTEM_COLORS = "PrefersSystemColorInWidgets"
const val PREF_NUMERICAL_DATE_PREFERRED = "numericalDatePreferred"
const val PREF_ASTRONOMICAL_FEATURES = "astronomicalFeatures"
const val PREF_SHOW_WEEK_OF_YEAR_NUMBER = "showWeekOfYearNumber"
const val PREF_NOTIFY_DATE = "NotifyDate"
const val PREF_NOTIFY_IGNORED = "NotifyIgnored"
const val PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT = "BatteryOptimizationIgnoredCount"
const val PREF_NOTIFICATION_ATHAN = "NotificationAthan"
const val PREF_ATHAN_VIBRATION = "NotificationAthanVibration"
const val PREF_NOTIFY_DATE_LOCK_SCREEN = "NotifyDateLockScreen"
const val PREF_ATHAN_VOLUME = "athanVolume"
const val PREF_ASCENDING_ATHAN_VOLUME = "AscendingAthanVolume"
const val PREF_APP_LANGUAGE = "AppLanguage"
const val PREF_EASTERN_GREGORIAN_ARABIC_MONTHS = "EasternGregorianArabicMonths"
const val PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS = "EnglishGregorianPersianMonths"
const val PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS = "AzeriAlternativePersianMonths"
const val PREF_SELECTED_WIDGET_TEXT_COLOR = "SelectedWidgetTextColor"
const val PREF_SELECTED_WIDGET_BACKGROUND_COLOR = "SelectedWidgetBackgroundColor"
const val PREF_SELECTED_DATE_AGE_WIDGET = "SelectedDateForAgeWidget"
const val PREF_SELECTED_DATE_AGE_WIDGET_START = "SelectedDateForAgeWidgetStart"
const val PREF_TITLE_AGE_WIDGET = "TitleForAgeWidget"
const val PREF_ATHAN_ALARM = "AthanAlarm"
const val PREF_ATHAN_GAP = "AthanGap"
const val PREF_THEME = "Theme"
const val PREF_SYSTEM_DARK_THEME = "SystemDarkTheme"
const val PREF_SYSTEM_LIGHT_THEME = "SystemLightTheme"
const val PREF_THEME_GRADIENT = "ThemeGradient"
const val PREF_RED_HOLIDAYS = "RedHolidays"
const val PREF_VAZIR_ENABLED = "VazirEnabled"
const val PREF_HOLIDAY_TYPES = "holiday_types"
const val PREF_WEEK_START = "WeekStart"
const val PREF_WEEK_ENDS = "WeekEnds"
const val PREF_SHIFT_WORK_STARTING_JDN = "ShiftWorkJdn"
const val PREF_SHIFT_WORK_SETTING = "ShiftWorkSetting"
const val PREF_SHIFT_WORK_RECURS = "ShiftWorkRecurs"
const val PREF_DISABLE_OWGHAT = "DisableOwghat"
const val PREF_LAST_APP_VISIT_VERSION = "LastAppVisitVersion"
const val PREF_SHOW_QIBLA_IN_COMPASS = "showQibla"
const val PREF_COMPASS_SET_LOCATION_IGNORED = "compassSetLocationIgnored"
const val PREF_TRUE_NORTH_IN_COMPASS = "trueNorth"
const val PREF_WALLPAPER_DARK = "WallpaperDark"
const val PREF_WALLPAPER_AUTOMATIC = "WallpaperAutomatic"
const val PREF_DREAM_NOISE = "DreamNoise"
const val PREF_WIDGET_TRANSPARENCY = "WidgetTransparency"
const val PREF_SWIPE_UP_ACTION = "SwipeUpAction"
const val PREF_SWIPE_DOWN_ACTION = "SwipeDownAction"
const val PREF_ATHAN_CHANNEL_ID = "AthanChannelId"

// --- Additional (new) preferences ------------------------------------------
const val PREF_AUTO_BACKUP_ENABLED = "AutoBackupEnabled"
const val PREF_AUTO_BACKUP_FREQUENCY = "AutoBackupFrequency"
const val PREF_CUSTOM_NOTIFICATIONS = "CustomNotifications"
const val PREF_DEFAULT_VIEW_MODE = "DefaultViewMode"
const val PREF_FAVORITE_LOCATIONS = "FavoriteLocations"
const val PREF_ENABLE_WIDGET_ANIMATIONS = "EnableWidgetAnimations"
const val PREF_SHOW_ISLAMIC_EVENTS = "ShowIslamicEvents"
const val PREF_SHOW_SOLAR_EVENTS = "ShowSolarEvents"
const val PREF_SHOW_GREGORIAN_EVENTS = "ShowGregorianEvents"
const val PREF_ALLOW_HAPTIC_FEEDBACK = "AllowHapticFeedback"
const val PREF_BACKUP_PATH = "BackupPath"
const val PREF_EVENT_REMINDERS = "EventReminders"
const val PREF_THEME_CUSTOM_COLOR = "ThemeCustomColor"
const val PREF_WIDGET_CORNER_RADIUS = "WidgetCornerRadius"

// --- Default values --------------------------------------------------------
const val DEFAULT_CITY = "CUSTOM"
const val DEFAULT_PRAY_TIME_METHOD = "Tehran"
const val DEFAULT_HIGH_LATITUDES_METHOD = "NightMiddle"
const val DEFAULT_SELECTED_WIDGET_TEXT_COLOR = Color.WHITE
const val DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR = Color.TRANSPARENT
const val DEFAULT_WIDGET_IN_24 = false
const val DEFAULT_IRAN_TIME = false
const val DEFAULT_LOCAL_DIGITS = true
const val DEFAULT_WIDGET_CLOCK = true

// NOTE: this depends on runtime SDK so it must be a 'val' not a 'const'
val DEFAULT_NOTIFY_DATE = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

const val DEFAULT_ASCENDING_ATHAN_VOLUME = false
const val DEFAULT_ATHAN_VIBRATION = true
const val DEFAULT_NOTIFY_DATE_LOCK_SCREEN = true
const val DEFAULT_ATHAN_VOLUME = 3
const val DEFAULT_ISLAMIC_OFFSET = "0"
const val DEFAULT_SECONDARY_CALENDAR_IN_TABLE = false
const val DEFAULT_THEME_GRADIENT = true
const val DEFAULT_RED_HOLIDAYS = false
const val DEFAULT_VAZIR_ENABLED = false
const val DEFAULT_WALLPAPER_DARK = true
const val DEFAULT_WALLPAPER_AUTOMATIC = false
const val DEFAULT_DREAM_NOISE = false
const val DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS = false
const val DEFAULT_AZERI_ALTERNATIVE_PERSIAN_MONTHS = false
const val DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS = false
const val DEFAULT_WIDGET_TRANSPARENCY = 0f
const val DEFAULT_ATHAN_CHANNEL_ID = 3005

// New default values
const val DEFAULT_AUTO_BACKUP_ENABLED = true
const val DEFAULT_AUTO_BACKUP_FREQUENCY = "Weekly"
const val DEFAULT_CUSTOM_NOTIFICATIONS = false
const val DEFAULT_DEFAULT_VIEW_MODE = "Month"
val DEFAULT_FAVORITE_LOCATIONS: List<String> = emptyList()
const val DEFAULT_ENABLE_WIDGET_ANIMATIONS = true
const val DEFAULT_SHOW_ISLAMIC_EVENTS = true
const val DEFAULT_SHOW_SOLAR_EVENTS = true
const val DEFAULT_SHOW_GREGORIAN_EVENTS = true
const val DEFAULT_ALLOW_HAPTIC_FEEDBACK = true
const val DEFAULT_BACKUP_PATH = "/storage/emulated/0/PersianCalendar/backup/"
const val DEFAULT_EVENT_REMINDERS = true
const val DEFAULT_THEME_CUSTOM_COLOR = Color.BLUE
const val DEFAULT_WIDGET_CORNER_RADIUS = 16

const val ALARMS_BASE_ID = 2000

// --- Broadcasts ------------------------------------------------------------
const val BROADCAST_ALARM = "BROADCAST_ALARM"
const val ADD_EVENT = "ADD_EVENT"
const val MONTH_RESET_COMMAND = "MONTH_RESET_COMMAND"
const val MONTH_PREV_COMMAND = "MONTH_PREV_COMMAND"
const val MONTH_NEXT_COMMAND = "MONTH_NEXT_COMMAND"
const val BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP"
const val BROADCAST_UPDATE_APP = "BROADCAST_UPDATE_APP"
const val KEY_EXTRA_PRAYER = "prayer_name"
const val KEY_EXTRA_PRAYER_TIME = "prayer_time"

// --- Unicode helpers --------------------------------------------------------
const val NBSP = "\u00A0"
const val ZWNJ = "\u200C"
const val ZWJ = "\u200D"
const val LRM = "\u200E"
const val RLM = "\u200F"

const val EN_DASH = "–"

const val DEFAULT_AM = "ق.ظ"
const val DEFAULT_PM = "ب.ظ"
const val DEFAULT_HOLIDAY = "تعطیل"

// --- WorkManager tags -----------------------------------------------------
const val CHANGE_DATE_TAG = "changeDate"
const val ALARM_TAG = "alarmTag"
const val UPDATE_TAG = "update"

const val LAST_PLAYED_ATHAN_KEY = "LAST_PLAYED_ATHAN_KEY"
const val LAST_PLAYED_ATHAN_JDN = "LAST_PLAYED_ATHAN_JDN"

// --- Misc keys -------------------------------------------------------------
const val OTHER_CALENDARS_KEY = "other_calendars"
const val NON_HOLIDAYS_EVENTS_KEY = "non_holiday_events"
const val OWGHAT_KEY = "owghat"
const val OWGHAT_LOCATION_KEY = "owghat_location"

val DEFAULT_WIDGET_CUSTOMIZATIONS = setOf(
    OTHER_CALENDARS_KEY, NON_HOLIDAYS_EVENTS_KEY, OWGHAT_KEY, OWGHAT_LOCATION_KEY
)

// --- Qibla coordinates -----------------------------------------------------
const val QIBLA_LATITUDE = 21.422522
const val QIBLA_LONGITUDE = 39.826181

// --- Astronomy -------------------------------------------------------------
const val AU_IN_KM = 149597871L // astronomical unit, ~earth/sun distance

// --- Timezones -------------------------------------------------------------
const val IRAN_TIMEZONE_ID = "Asia/Tehran"
const val AFGHANISTAN_TIMEZONE_ID = "Asia/Kabul"
const val NEPAL_TIMEZONE_ID = "Asia/Kathmandu"

// --- Shared content keys --------------------------------------------------
const val SHARED_CONTENT_KEY_CARD = "card"
const val SHARED_CONTENT_KEY_CARD_CONTENT = "cardContent"
const val SHARED_CONTENT_KEY_STOP = "stop"
const val SHARED_CONTENT_KEY_OPEN_DRAWER = "openDrawer"
const val SHARED_CONTENT_KEY_EVENTS = "events"
const val SHARED_CONTENT_KEY_THREE_DOTS_MENU = "threeDots"
const val SHARED_CONTENT_KEY_SHARE_BUTTON = "shareButton"
const val SHARED_CONTENT_KEY_MAP = "map"
const val SHARED_CONTENT_KEY_TIME_BAR = "time"
const val SHARED_CONTENT_KEY_LEVEL = "level"
const val SHARED_CONTENT_KEY_COMPASS = "compass"
const val SHARED_CONTENT_KEY_MOON = "moon"
const val SHARED_CONTENT_KEY_TIME = "time"
const val SHARED_CONTENT_KEY_DAYS_SCREEN_SURFACE_CONTENT = "daysScreenSurfaceContent"
const val SHARED_CONTENT_KEY_DAYS_SCREEN_ICON = "daysScreenIcon"
 
