package com.byagowi.persiancalendar.utils

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.AppLocalesData
import com.byagowi.persiancalendar.DEFAULT_AM
import com.byagowi.persiancalendar.DEFAULT_APP_LANGUAGE
import com.byagowi.persiancalendar.DEFAULT_HOLIDAY
import com.byagowi.persiancalendar.DEFAULT_IRAN_TIME
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.DEFAULT_PERSIAN_DIGITS
import com.byagowi.persiancalendar.DEFAULT_PM
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.DEFAULT_WEEK_ENDS
import com.byagowi.persiancalendar.DEFAULT_WEEK_START
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CLOCK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.DEFAULT_WIDGET_IN_24
import com.byagowi.persiancalendar.LANG_AR
import com.byagowi.persiancalendar.LANG_AZB
import com.byagowi.persiancalendar.LANG_CKB
import com.byagowi.persiancalendar.LANG_EN_IR
import com.byagowi.persiancalendar.LANG_EN_US
import com.byagowi.persiancalendar.LANG_ES
import com.byagowi.persiancalendar.LANG_FA
import com.byagowi.persiancalendar.LANG_FA_AF
import com.byagowi.persiancalendar.LANG_FR
import com.byagowi.persiancalendar.LANG_GLK
import com.byagowi.persiancalendar.LANG_JA
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.logDebug
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.utils.getThemeFromName
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*

val monthNameEmptyList = List(12) { "" }
var persianMonths = monthNameEmptyList
    private set
var islamicMonths = monthNameEmptyList
    private set
var gregorianMonths = monthNameEmptyList
    private set
val weekDaysEmptyList = List(7) { "" }
var weekDays = weekDaysEmptyList
    private set
var weekDaysInitials = weekDaysEmptyList
    private set
var preferredDigits = PERSIAN_DIGITS
    private set
var clockIn24 = DEFAULT_WIDGET_IN_24
    private set
var isForcedIranTimeEnabled = DEFAULT_IRAN_TIME
    private set
var isNotifyDateOnLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    private set
var isWidgetClock = DEFAULT_WIDGET_CLOCK
    private set
var isNotifyDate = DEFAULT_NOTIFY_DATE
    private set
var notificationAthan = DEFAULT_NOTIFICATION_ATHAN
    private set
var selectedWidgetTextColor = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    private set
var selectedWidgetBackgroundColor = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
    private set
var calculationMethod = CalculationMethod.valueOf(DEFAULT_PRAY_TIME_METHOD)
    private set
var language = DEFAULT_APP_LANGUAGE
    private set
    get() = if (field.isEmpty()) DEFAULT_APP_LANGUAGE else field
var easternGregorianArabicMonths = false
    private set
var coordinates: Coordinate? = null
    private set
var mainCalendar = CalendarType.SHAMSI
    private set
var otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    private set
var spacedComma = "، "
    private set
var spacedColon = ": "
    private set
var isShowWeekOfYearEnabled = false
    private set
var isCenterAlignWidgets = false
    private set
var weekStartOffset = 0
    private set
var weekEnds = BooleanArray(7)
    private set
var isShowDeviceCalendarEvents = false
    private set
var whatToShowOnWidgets = emptySet<String>()
    private set
var isAstronomicalFeaturesEnabled = false
    private set

@StyleRes
var appTheme = R.style.LightTheme
    private set
var isTalkBackEnabled = false
    private set
var isHighTextContrastEnabled = false
    private set
var prayTimes: PrayTimes? = null
    private set
var shiftWorkTitles = emptyMap<String, String>()
    private set
var shiftWorkStartingJdn: Jdn? = null
    private set
var shiftWorkRecurs = true
    private set
var shiftWorks = emptyList<ShiftWorkRecord>()
    private set
var shiftWorkPeriod = 0
    private set
var isIranHolidaysEnabled = true
    private set
var amString = DEFAULT_AM
    private set
var pmString = DEFAULT_PM
    private set
var holidayString = DEFAULT_HOLIDAY
    private set
var numericalDatePreferred = false
    private set
var calendarTypesTitleAbbr = emptyList<String>()
    private set
// Some more are in EventsUtils

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    logDebug("Utils", "initUtils is called")
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResources()
    scheduleAlarms(context)
    configureCalendarsAndLoadEvents(context)
}

fun configureCalendarsAndLoadEvents(context: Context) {
    logDebug("Utils", "configureCalendarsAndLoadEvents is called")
    IslamicDate.islamicOffset = context.appPrefs
        .getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)?.toIntOrNull() ?: 0
    val enabledHolidays = EnabledHolidays(context.appPrefs)
    isIranHolidaysEnabled = enabledHolidays.iranHolidays
    loadEvents(enabledHolidays, language)
}

fun loadLanguageResources() {
    logDebug("Utils", "loadLanguageResources is called")
    val language = language
    persianMonths = AppLocalesData.getPersianCalendarMonths(language)
    islamicMonths = AppLocalesData.getIslamicCalendarMonths(language)
    gregorianMonths =
        AppLocalesData.getGregorianCalendarMonths(language, easternGregorianArabicMonths)
    weekDays = AppLocalesData.getWeekDays(language)
    weekDaysInitials = AppLocalesData.getWeekDaysInitials(language)
}

@StringRes
fun getNextOwghatTimeId(current: Clock, dateHasChanged: Boolean): Int {
    coordinates ?: return 0

    if (prayTimes == null || dateHasChanged)
        prayTimes = PrayTimesCalculator.calculate(calculationMethod, Date(), coordinates)

    val clock = current.toInt()

    return prayTimes?.let {
        //TODO We like to show Imsak only in Ramadan
        when {
            it.fajrClock.toInt() > clock -> R.string.fajr
            it.sunriseClock.toInt() > clock -> R.string.sunrise
            it.dhuhrClock.toInt() > clock -> R.string.dhuhr
            it.asrClock.toInt() > clock -> R.string.asr
            it.sunsetClock.toInt() > clock -> R.string.sunset
            it.maghribClock.toInt() > clock -> R.string.maghrib
            it.ishaClock.toInt() > clock -> R.string.isha
            it.midnightClock.toInt() > clock -> R.string.midnight
            // TODO: this is today's, not tomorrow
            else -> R.string.fajr
        }
    } ?: 0
}

fun getOwghatTimeOfStringId(@StringRes stringId: Int): Clock {
    if (prayTimes == null && coordinates != null)
        prayTimes = PrayTimesCalculator.calculate(calculationMethod, Date(), coordinates)

    return prayTimes?.getFromStringId(stringId) ?: Clock.fromInt(0)
}

private fun getOnlyLanguage(string: String): String = string.replace(Regex("-(IR|AF|US)"), "")

fun updateStoredPreference(context: Context) {
    logDebug("Utils", "updateStoredPreference is called")
    val prefs = context.appPrefs

    language = prefs.getString(PREF_APP_LANGUAGE, null) ?: DEFAULT_APP_LANGUAGE
    easternGregorianArabicMonths = prefs.getBoolean(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, false)

    preferredDigits = when (language) {
        LANG_EN_US, LANG_JA, LANG_FR, LANG_ES -> ARABIC_DIGITS
        else -> when {
            prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS) -> when (language) {
                LANG_AR, LANG_CKB -> ARABIC_INDIC_DIGITS
                else -> PERSIAN_DIGITS
            }
            else -> ARABIC_DIGITS
        }
    }

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
    isCenterAlignWidgets = prefs.getBoolean(PREF_CENTER_ALIGN_WIDGETS, false)

    selectedWidgetTextColor = prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, null)
        ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    selectedWidgetBackgroundColor = prefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR, null)
        ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod = CalculationMethod
        .valueOf(prefs.getString(PREF_PRAY_TIME_METHOD, null) ?: DEFAULT_PRAY_TIME_METHOD)

    coordinates = prefs.getStoredCity()?.coordinate ?: run {
        listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE)
            .map { prefs.getString(it, null)?.toDoubleOrNull() ?: .0 }
            .takeIf { coords -> coords.any { it != .0 } } // if all were zero preference isn't set yet
            ?.let { (lat, lng, alt) -> Coordinate(lat, lng, alt) }
    }
    runCatching {
        mainCalendar =
            CalendarType.valueOf(prefs.getString(PREF_MAIN_CALENDAR_KEY, null) ?: "SHAMSI")

        otherCalendars = (prefs.getString(PREF_OTHER_CALENDARS_KEY, null) ?: "GREGORIAN,ISLAMIC")
            .splitIgnoreEmpty(",").map(CalendarType::valueOf)
    }.onFailure(logException).onFailure {
        mainCalendar = CalendarType.SHAMSI
        otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    }

    spacedComma = when {
        language == LANG_JA -> "、"
        isNonArabicScriptSelected -> ", "
        else -> "، "
    }
    spacedColon = when (language) {
        LANG_JA -> "："
        else -> ": "
    }
    isShowWeekOfYearEnabled = prefs.getBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, null) ?: DEFAULT_WEEK_START).toIntOrNull() ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, null) ?: DEFAULT_WEEK_ENDS)
        .mapNotNull(String::toIntOrNull).forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets = prefs.getStringSet(PREF_WHAT_TO_SHOW_WIDGETS, null)
        ?: DEFAULT_WIDGET_CUSTOMIZATIONS

    isAstronomicalFeaturesEnabled = prefs.getBoolean(PREF_ASTRONOMICAL_FEATURES, false)
    numericalDatePreferred = prefs.getBoolean(PREF_NUMERICAL_DATE_PREFERRED, false)

    if (getOnlyLanguage(language) != resources.getString(R.string.code))
        applyAppLanguage(context)

    calendarTypesTitleAbbr = CalendarType.values().map { context.getString(it.shortTitle) }

    shiftWorks = (prefs.getString(PREF_SHIFT_WORK_SETTING, null) ?: "")
        .splitIgnoreEmpty(",")
        .map { it.splitIgnoreEmpty("=") }
        .filter { it.size == 2 }
        .map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) }
    shiftWorkPeriod = shiftWorks.sumOf { it.length }
    shiftWorkStartingJdn = prefs.getJdnOrNull(PREF_SHIFT_WORK_STARTING_JDN)
    shiftWorkRecurs = prefs.getBoolean(PREF_SHIFT_WORK_RECURS, true)
    shiftWorkTitles = mapOf(
        "d" to context.getString(R.string.shift_work_morning), // d -> day work, legacy key
        "r" to context.getString(R.string.shift_work_off), // r -> rest, legacy key
        "e" to context.getString(R.string.shift_work_evening),
        "n" to context.getString(R.string.shift_work_night)
    )

    when (language) {
        LANG_FA, LANG_FA_AF, LANG_EN_IR -> {
            amString = DEFAULT_AM
            pmString = DEFAULT_PM
            holidayString = if (language == LANG_FA_AF) "رخصتی" else DEFAULT_HOLIDAY
        }
        else -> {
            amString = context.getString(R.string.am)
            pmString = context.getString(R.string.pm)
            holidayString = context.getString(R.string.holiday)
        }
    }

    appTheme = runCatching {
        getThemeFromName(getThemeFromPreference(context, prefs))
    }.onFailure(logException).getOrDefault(R.style.LightTheme)

    isTalkBackEnabled = context.getSystemService<AccessibilityManager>()?.let {
        it.isEnabled && it.isTouchExplorationEnabled
    } ?: false

    // https://stackoverflow.com/a/61599809
    isHighTextContrastEnabled = runCatching {
        context.getSystemService<AccessibilityManager>()?.let {
            (it.javaClass.getMethod("isHighTextContrastEnabled").invoke(it) as? Boolean)
        }
    }.onFailure(logException).getOrNull() ?: false
}

// Context preferably should be activity context not application
fun applyAppLanguage(context: Context) {
    logDebug("Utils", "applyAppLanguage is called")
    val localeCode = getOnlyLanguage(language)
    val locale = Locale(localeCode)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(
        when (localeCode) {
            LANG_AZB, LANG_GLK -> Locale(LANG_FA)
            else -> locale
        }
    )
    resources.updateConfiguration(config, resources.displayMetrics)
}

//fun Context.withLocale(): Context {
//    val config = resources.configuration
//    val locale = Locale(getOnlyLanguage(language))
//    Locale.setDefault(locale)
//    config.setLocale(locale)
//    config.setLayoutDirection(when (language) {
//        LANG_AZB, LANG_GLK -> Locale(LANG_FA)
//        else -> locale
//    })
//    return createConfigurationContext(config)
//}
