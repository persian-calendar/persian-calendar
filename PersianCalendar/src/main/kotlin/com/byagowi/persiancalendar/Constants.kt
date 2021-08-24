package com.byagowi.persiancalendar

const val LOCATION_PERMISSION_REQUEST_CODE = 23
const val CALENDAR_READ_PERMISSION_REQUEST_CODE = 55

const val LANG_FA = "fa"
const val LANG_FA_AF = "fa-AF"
const val LANG_PS = "ps"
const val LANG_GLK = "glk"
const val LANG_AR = "ar"
const val LANG_EN_IR = "en"
const val LANG_FR = "fr"
const val LANG_ES = "es"
const val LANG_EN_US = "en-US"
const val LANG_JA = "ja"
const val LANG_AZB = "azb"
const val LANG_CKB = "ckb"
const val LANG_UR = "ur"

const val LAST_CHOSEN_TAB_KEY = "LastChosenTab"

const val PREF_MAIN_CALENDAR_KEY = "mainCalendarType"
const val PREF_OTHER_CALENDARS_KEY = "otherCalendarTypes"
const val PREF_PRAY_TIME_METHOD = "SelectedPrayTimeMethod"
const val PREF_ISLAMIC_OFFSET = "islamic_offset"
const val PREF_LATITUDE = "Latitude"
const val PREF_LONGITUDE = "Longitude"
const val PREF_SELECTED_LOCATION = "Location"
const val PREF_GEOCODED_CITYNAME = "cityname"
const val PREF_ALTITUDE = "Altitude"
const val PREF_WIDGET_IN_24 = "WidgetIn24"
const val PREF_IRAN_TIME = "IranTime"
const val PREF_PERSIAN_DIGITS = "PersianDigits"
const val PREF_ATHAN_URI = "AthanURI"
const val PREF_ATHAN_NAME = "AthanName"
const val PREF_SHOW_DEVICE_CALENDAR_EVENTS = "showDeviceCalendarEvents"
const val PREF_WIDGET_CLOCK = "WidgetClock"
const val PREF_CENTER_ALIGN_WIDGETS = "CenterAlignWidgets"
const val PREF_WHAT_TO_SHOW_WIDGETS = "what_to_show"
const val PREF_NUMERICAL_DATE_PREFERRED = "numericalDatePreferred"
const val PREF_ASTRONOMICAL_FEATURES = "astronomicalFeatures"
const val PREF_SHOW_WEEK_OF_YEAR_NUMBER = "showWeekOfYearNumber"
const val PREF_NOTIFY_DATE = "NotifyDate"
const val PREF_NOTIFICATION_ATHAN = "NotificationAthan"
const val PREF_NOTIFY_DATE_LOCK_SCREEN = "NotifyDateLockScreen"
const val PREF_ATHAN_VOLUME = "AthanVolume"
const val PREF_ASCENDING_ATHAN_VOLUME = "AscendingAthanVolume"
const val PREF_APP_LANGUAGE = "AppLanguage"
const val PREF_EASTERN_GREGORIAN_ARABIC_MONTHS = "EasternGregorianArabicMonths"
const val PREF_SELECTED_WIDGET_TEXT_COLOR = "SelectedWidgetTextColor"
const val PREF_SELECTED_WIDGET_BACKGROUND_COLOR = "SelectedWidgetBackgroundColor"
const val PREF_SELECTED_DATE_AGE_WIDGET = "SelectedDateForAgeWidget"
const val PREF_TITLE_AGE_WIDGET = "TitleForAgeWidget"
const val PREF_ATHAN_ALARM = "AthanAlarm"
const val PREF_ATHAN_GAP = "AthanGap"
const val PREF_THEME = "Theme"
const val PREF_HOLIDAY_TYPES = "holiday_types"
const val PREF_WEEK_START = "WeekStart"
const val PREF_WEEK_ENDS = "WeekEnds"
const val PREF_SHIFT_WORK_STARTING_JDN = "ShiftWorkJdn"
const val PREF_SHIFT_WORK_SETTING = "ShiftWorkSetting"
const val PREF_SHIFT_WORK_RECURS = "ShiftWorkRecurs"
const val PREF_DISABLE_OWGHAT = "DisableOwghat"

const val CHANGE_LANGUAGE_IS_PROMOTED_ONCE = "CHANGE_LANGUAGE_IS_PROMOTED_ONCE"

const val DEFAULT_CITY = "CUSTOM"
const val DEFAULT_PRAY_TIME_METHOD = "Tehran"
const val DEFAULT_APP_LANGUAGE = "fa"
const val DEFAULT_SELECTED_WIDGET_TEXT_COLOR = "#ffffffff"
const val DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR = "#00000000"
const val DEFAULT_WIDGET_IN_24 = true
const val DEFAULT_IRAN_TIME = false
const val DEFAULT_PERSIAN_DIGITS = true
const val DEFAULT_WIDGET_CLOCK = true
const val DEFAULT_NOTIFY_DATE = true
const val DEFAULT_NOTIFICATION_ATHAN = false
const val DEFAULT_NOTIFY_DATE_LOCK_SCREEN = true
const val DEFAULT_ATHAN_VOLUME = 1
const val DEFAULT_WEEK_START = "0"
const val DEFAULT_ISLAMIC_OFFSET = "0"

// WeekEnds, 6 means Friday
val DEFAULT_WEEK_ENDS = setOf("6")

const val LIGHT_THEME = "LightTheme"
const val DARK_THEME = "DarkTheme"
const val BLACK_THEME = "BlackTheme"
const val BLUE_THEME = "BlueTheme"
const val MODERN_THEME = "ClassicTheme" // don't change it, for legacy reasons
const val SYSTEM_DEFAULT_THEME = "SystemDefault"

const val LOAD_APP_ID = 1000
const val THREE_HOURS_APP_ID = 1010
const val ALARMS_BASE_ID = 2000

const val BROADCAST_ALARM = "BROADCAST_ALARM"
const val BROADCAST_RESTART_APP = "BROADCAST_RESTART_APP"
const val BROADCAST_UPDATE_APP = "BROADCAST_UPDATE_APP"
const val KEY_EXTRA_PRAYER = "prayer_name"
const val KEY_EXTRA_PRAYER_TIME = "prayer_time"
const val FONT_PATH = "fonts/NotoNaskhArabic-Regular.ttf"

const val RLM = "\u200F"
const val ZWJ = "\u200D"

const val DEFAULT_AM = "ق.ظ"
const val DEFAULT_PM = "ب.ظ"

// WorkManager tags, should be unique
const val CHANGE_DATE_TAG = "changeDate"
const val ALARM_TAG = "alarmTag"
const val UPDATE_TAG = "update"

const val OTHER_CALENDARS_KEY = "other_calendars"
const val NON_HOLIDAYS_EVENTS_KEY = "non_holiday_events"
const val OWGHAT_KEY = "owghat"
const val OWGHAT_LOCATION_KEY = "owghat_location"

// A new one can't be added and should be default off unfortunately as users might have set it already
val DEFAULT_WIDGET_CUSTOMIZATIONS = setOf(
    OTHER_CALENDARS_KEY, NON_HOLIDAYS_EVENTS_KEY, OWGHAT_KEY, OWGHAT_LOCATION_KEY
)
