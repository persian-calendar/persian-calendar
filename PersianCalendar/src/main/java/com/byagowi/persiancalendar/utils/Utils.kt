package com.byagowi.persiancalendar.utils

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.*
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

const val TAG = "Utils"
val twoSeconds = TimeUnit.SECONDS.toMillis(2)
//
//
//
// Service
//
const val DAY_IN_SECOND: Long = 86400
const val CHANGE_DATE_TAG = "changeDate"
const val UPDATE_TAG = "update"
val monthNameEmptyList = (1..12).map { "" }.toList()
var persianMonths = monthNameEmptyList
    private set
var islamicMonths = monthNameEmptyList
    private set
var gregorianMonths = monthNameEmptyList
    private set
val weekDaysEmptyList = (1..7).map { "" }.toList()
var weekDays = weekDaysEmptyList
    private set
var weekDaysInitials = weekDaysEmptyList
    private set
var preferredDigits = PERSIAN_DIGITS
    private set
var clockIn24 = DEFAULT_WIDGET_IN_24
    private set
var isIranTime = DEFAULT_IRAN_TIME
    private set
var isNotifyDateOnLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    private set
var isWidgetClock = DEFAULT_WIDGET_CLOCK
    private set
var isNotifyDate = DEFAULT_NOTIFY_DATE
    private set
var notificationAthan = DEFAULT_NOTIFICATION_ATHAN
    private set
var selectedWidgetTextColor: String = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    private set
var selectedWidgetBackgroundColor: String = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
    private set
//    static private String islamicOffset = DEFAULT_ISLAMIC_OFFSET;
var calculationMethod: String = DEFAULT_PRAY_TIME_METHOD
    private set
var language: String = DEFAULT_APP_LANGUAGE
    private set
    get() = if (field.isEmpty()) DEFAULT_APP_LANGUAGE else field
var coordinate: Coordinate? = null
    private set
var mainCalendar = CalendarType.SHAMSI
    private set
var otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    private set
var spacedComma = "، "
    private set
var isShowWeekOfYearEnabled: Boolean = false
    private set
var isCenterAlignWidgets: Boolean = false
    private set
var weekStartOffset: Int = 0
    private set
var weekEnds = BooleanArray(7)
    private set
var isShowDeviceCalendarEvents: Boolean = false
    private set
var whatToShowOnWidgets: Set<String> = emptySet()
    private set
var isAstronomicalFeaturesEnabled: Boolean = false
    private set
@StyleRes
var appTheme = R.style.LightTheme
    private set
var isTalkBackEnabled = false
    private set
var prayTimes: PrayTimes? = null
    private set
var cachedCityKey = ""
    private set
var cachedCity: CityItem? = null
    private set
var shiftWorkTitles: Map<String, String> = emptyMap()
    private set
var shiftWorkStartingJdn: Long = -1
    private set
var shiftWorkRecurs = true
    private set
var shiftWorks: List<ShiftWorkRecord> = emptyList()
    private set
var shiftWorkPeriod = 0
    private set
var isIranHolidaysEnabled = true
    private set
var amString = DEFAULT_AM
    private set
var pmString = DEFAULT_PM
    private set
var latestToastShowTime: Long = -1
    private set
var numericalDatePreferred = false
    private set
var calendarTypesTitleAbbr = emptyList<String>()
    private set
val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
var allEnabledEvents: List<CalendarEvent<*>> = emptyList()
    private set
var persianCalendarEvents: PersianCalendarEventsStore = emptyMap()
    private set
var islamicCalendarEvents: IslamicCalendarEventsStore = emptyMap()
    private set
var gregorianCalendarEvents: GregorianCalendarEventsStore = emptyMap()
    private set

fun loadEvents(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val enabledTypes = prefs?.getStringSet(PREF_HOLIDAY_TYPES, null) ?: setOf("iran_holidays")

    val afghanistanHolidays = enabledTypes.contains("afghanistan_holidays")
    val afghanistanOthers = enabledTypes.contains("afghanistan_others")
    val iranHolidays = enabledTypes.contains("iran_holidays")
    val iranIslamic = enabledTypes.contains("iran_islamic")
    val iranAncient = enabledTypes.contains("iran_ancient")
    val iranOthers = enabledTypes.contains("iran_others")
    val international = enabledTypes.contains("international")

    isIranHolidaysEnabled = iranHolidays

    IslamicDate.useUmmAlQura = false
    if (!iranHolidays) {
        if (afghanistanHolidays) {
            IslamicDate.useUmmAlQura = true
        }
        when (language) {
            LANG_FA_AF, LANG_PS, LANG_UR, LANG_AR, LANG_CKB, LANG_EN_US, LANG_JA ->
                IslamicDate.useUmmAlQura = true
        }
    }

    // Now that we are configuring converter's algorithm above, lets set the offset also
    IslamicDate.islamicOffset = getIslamicOffset(context)

    try {
        val allEnabledEventsBuilder = ArrayList<CalendarEvent<*>>()

        val allTheEvents = JSONObject(readRawResource(context, R.raw.events))

        // https://stackoverflow.com/a/36188796
        fun JSONObject.getArray(key: String): Sequence<JSONObject> =
            getJSONArray(key).run { (0 until length()).asSequence().map { get(it) as JSONObject } }

        persianCalendarEvents = allTheEvents.getArray("Persian Calendar").mapNotNull {
            val month = it.getInt("month")
            val day = it.getInt("day")
            val year = if (it.has("year")) it.getInt("year") else -1
            var title = it.getString("title")
            var holiday = it.getBoolean("holiday")

            var addOrNot = false
            val type = it.getString("type")

            if (holiday && iranHolidays &&
                (type == "Islamic Iran" || type == "Iran" || type == "Ancient Iran")
            ) addOrNot = true

            if (!iranHolidays && type == "Islamic Iran") holiday = false
            if (iranIslamic && type == "Islamic Iran") addOrNot = true
            if (iranAncient && type == "Ancient Iran") addOrNot = true
            if (iranOthers && type == "Iran") addOrNot = true
            if (afghanistanHolidays && type == "Afghanistan" && holiday) addOrNot = true
            if (!afghanistanHolidays && type == "Afghanistan") holiday = false
            if (afghanistanOthers && type == "Afghanistan") addOrNot = true

            if (addOrNot) {
                title += " ("
                if (holiday && afghanistanHolidays && iranHolidays) {
                    if (type == "Islamic Iran" || type == "Iran")
                        title += "ایران، "
                    else if (type == "Afghanistan")
                        title += "افغانستان، "
                }
                title += formatDayAndMonth(day, persianMonths[month - 1]) + ")"
                PersianCalendarEvent(PersianDate(year, month, day), title, holiday)
            } else null
        }.toList().also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

        islamicCalendarEvents = allTheEvents.getArray("Hijri Calendar").mapNotNull {
            val month = it.getInt("month")
            val day = it.getInt("day")
            var title = it.getString("title")
            var holiday = it.getBoolean("holiday")

            var addOrNot = false
            val type = it.getString("type")

            if (afghanistanHolidays && holiday && type == "Islamic Afghanistan") addOrNot = true
            if (!afghanistanHolidays && type == "Islamic Afghanistan") holiday = false
            if (afghanistanOthers && type == "Islamic Afghanistan") addOrNot = true
            if (iranHolidays && holiday && type == "Islamic Iran") addOrNot = true
            if (!iranHolidays && type == "Islamic Iran") holiday = false
            if (iranIslamic && type == "Islamic Iran") addOrNot = true
            if (iranOthers && type == "Islamic Iran") addOrNot = true

            if (addOrNot) {
                title += " ("
                if (holiday && afghanistanHolidays && iranHolidays) {
                    if (type == "Islamic Iran")
                        title += "ایران، "
                    else if (type == "Islamic Afghanistan")
                        title += "افغانستان، "
                }
                title += formatDayAndMonth(day, islamicMonths[month - 1]) + ")"

                IslamicCalendarEvent(IslamicDate(-1, month, day), title, holiday)
            } else null
        }.toList().also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

        gregorianCalendarEvents = allTheEvents.getArray("Gregorian Calendar").mapNotNull {
            val month = it.getInt("month")
            val day = it.getInt("day")
            var title = it.getString("title")

            if (international) {
                title += " (" + formatDayAndMonth(day, gregorianMonths[month - 1]) + ")"

                GregorianCalendarEvent(CivilDate(-1, month, day), title, false)
            } else null
        }.toList().also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

        allEnabledEvents = allEnabledEventsBuilder
    } catch (e: JSONException) {
        e.printStackTrace()
    }
}

fun loadLanguageResource(context: Context) {
    @RawRes val messagesFile: Int = when (language) {
        LANG_FA_AF -> R.raw.messages_fa_af
        LANG_PS -> R.raw.messages_ps
        LANG_GLK -> R.raw.messages_glk
        LANG_AR -> R.raw.messages_ar
        LANG_CKB -> R.raw.messages_ckb
        LANG_UR -> R.raw.messages_ur
        LANG_EN_US -> R.raw.messages_en
        LANG_JA -> R.raw.messages_ja
        LANG_AZB -> R.raw.messages_azb
        LANG_EN_IR, LANG_FA -> R.raw.messages_fa
        else -> R.raw.messages_fa
    }

    try {
        val messages = JSONObject(readRawResource(context, messagesFile))

        fun JSONArray.toStringList() = (0 until length()).map { getString(it) }

        persianMonths = messages.getJSONArray("PersianCalendarMonths").toStringList()
        islamicMonths = messages.getJSONArray("IslamicCalendarMonths").toStringList()
        gregorianMonths = messages.getJSONArray("GregorianCalendarMonths").toStringList()
        messages.getJSONArray("WeekDays").toStringList().run {
            weekDays = this
            weekDaysInitials = this.map {
                when (language) {
                    LANG_AR -> it.substring(2, 4)
                    LANG_AZB -> it.substring(0, 2)
                    else -> it.substring(0, 1)
                }
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        persianMonths = monthNameEmptyList
        islamicMonths = monthNameEmptyList
        gregorianMonths = monthNameEmptyList
        weekDays = weekDaysEmptyList
        weekDaysInitials = weekDaysEmptyList
    }
}

@StringRes
fun getNextOwghatTimeId(current: Clock, dateHasChanged: Boolean): Int {
    coordinate ?: return 0

    if (prayTimes == null || dateHasChanged)
        prayTimes = PrayTimesCalculator.calculate(getCalculationMethod(), Date(), coordinate)

    val clock = current.toInt()

    return prayTimes?.run {
        //TODO We like to show Imsak only in Ramadan
        when {
            fajrClock.toInt() > clock -> R.string.fajr
            sunriseClock.toInt() > clock -> R.string.sunrise
            dhuhrClock.toInt() > clock -> R.string.dhuhr
            asrClock.toInt() > clock -> R.string.asr
            sunsetClock.toInt() > clock -> R.string.sunset
            maghribClock.toInt() > clock -> R.string.maghrib
            ishaClock.toInt() > clock -> R.string.isha
            midnightClock.toInt() > clock -> R.string.midnight
            // TODO: this is today's, not tomorrow
            else -> R.string.fajr
        }
    } ?: 0
}

fun getCityFromPreference(context: Context): CityItem? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val key = prefs.getString(PREF_SELECTED_LOCATION, "") ?: ""
    if (key.isEmpty() || key == DEFAULT_CITY) return null

    if (key == cachedCityKey)
        return cachedCity

    // cache last query even if no city available under the key, useful in case invalid
    // value is somehow inserted on the preference
    cachedCityKey = key
    cachedCity = getAllCities(context, false).firstOrNull { it.key == key }
    return cachedCity
}

fun a11yAnnounceAndClick(view: View, @StringRes resId: Int) {
    if (!isTalkBackEnabled) return

    val context = view.context ?: return

    val now = System.currentTimeMillis()
    if (now - latestToastShowTime > twoSeconds) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show()
        // https://stackoverflow.com/a/29423018
        context.getSystemService<AudioManager>()?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        latestToastShowTime = now
    }
}

private fun getOnlyLanguage(string: String): String = string.replace("-(IR|AF|US)".toRegex(), "")

fun updateStoredPreference(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    language = prefs.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE) ?: DEFAULT_APP_LANGUAGE

    preferredDigits =
        if (prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS)) when (language) {
            LANG_AR, LANG_CKB -> ARABIC_INDIC_DIGITS
            LANG_JA -> CJK_DIGITS
            else -> PERSIAN_DIGITS
        }
        else ARABIC_DIGITS

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isIranTime = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
    isCenterAlignWidgets = prefs.getBoolean("CenterAlignWidgets", false)

    selectedWidgetTextColor =
        (prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, DEFAULT_SELECTED_WIDGET_TEXT_COLOR)
            ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR)

    selectedWidgetBackgroundColor =
        (prefs.getString(
            PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
            DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
        )) ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod =
        prefs.getString(PREF_PRAY_TIME_METHOD, DEFAULT_PRAY_TIME_METHOD) ?: DEFAULT_PRAY_TIME_METHOD

    coordinate = getCoordinate(context)
    try {
        mainCalendar = CalendarType.valueOf(
            prefs.getString(PREF_MAIN_CALENDAR_KEY, null) ?: "SHAMSI"
        )

        otherCalendars = (prefs.getString(PREF_OTHER_CALENDARS_KEY, null) ?: "GREGORIAN,ISLAMIC")
            .splitIgnoreEmpty(",").map(CalendarType::valueOf).toList()
    } catch (e: Exception) {
        Log.e(TAG, "Fail on parsing calendar preference", e)
        mainCalendar = CalendarType.SHAMSI
        otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    }

    spacedComma = if (isNonArabicScriptSelected()) ", " else "، "
    isShowWeekOfYearEnabled = prefs.getBoolean("showWeekOfYearNumber", false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, DEFAULT_WEEK_START) ?: DEFAULT_WEEK_START).toIntOrNull()
            ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, null) ?: DEFAULT_WEEK_ENDS)
        .mapNotNull(String::toIntOrNull)
        .forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets = prefs.getStringSet("what_to_show", null)
        ?: resources.getStringArray(R.array.what_to_show_default).toSet()

    isAstronomicalFeaturesEnabled = prefs.getBoolean("astronomicalFeatures", false)
    numericalDatePreferred = prefs.getBoolean("numericalDatePreferred", false)

    if (getOnlyLanguage(language) != resources.getString(R.string.code))
        applyAppLanguage(context)

    calendarTypesTitleAbbr = context.resources.getStringArray(R.array.calendar_type_abbr).toList()

    shiftWorks = (prefs.getString(PREF_SHIFT_WORK_SETTING, null) ?: "")
        .splitIgnoreEmpty(",")
        .map { it.splitIgnoreEmpty("=") }
        .filter { it.size == 2 }
        .map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) }
    shiftWorkPeriod = shiftWorks.map { it.length }.sum()
    shiftWorkStartingJdn = prefs.getLong(PREF_SHIFT_WORK_STARTING_JDN, -1)
    shiftWorkRecurs = prefs.getBoolean(PREF_SHIFT_WORK_RECURS, true)
    shiftWorkTitles = resources.getStringArray(R.array.shift_work_keys)
        .zip(resources.getStringArray(R.array.shift_work))
        .toMap()

    when (language) {
        LANG_FA, LANG_FA_AF, LANG_EN_IR -> {
            amString = DEFAULT_AM
            pmString = DEFAULT_PM
        }
        else -> {
            amString = context.getString(R.string.am)
            pmString = context.getString(R.string.pm)
        }
    }

    appTheme = try {
        getThemeFromName(getThemeFromPreference(context, prefs))
    } catch (e: Exception) {
        e.printStackTrace()
        R.style.LightTheme
    }

    isTalkBackEnabled = context.getSystemService<AccessibilityManager>()?.run {
        isEnabled && isTouchExplorationEnabled
    } ?: false
}

// Context preferably should be activity context not application
fun applyAppLanguage(context: Context) {
    val localeCode = getOnlyLanguage(language)
    // To resolve this issue, https://issuetracker.google.com/issues/128908783 (marked as fixed now)
    // if ((language.equals(LANG_GLK) || language.equals(LANG_AZB)) && Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
    //    localeCode = LANG_FA;
    // }
    var locale = Locale(localeCode)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.locale = locale
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        if (language == LANG_AZB) {
            locale = Locale(LANG_FA)
        }
        config.setLayoutDirection(locale)
    }
    resources.updateConfiguration(config, resources.displayMetrics)
}
