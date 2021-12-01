package com.byagowi.persiancalendar.global

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_AM
import com.byagowi.persiancalendar.DEFAULT_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.DEFAULT_HOLIDAY
import com.byagowi.persiancalendar.DEFAULT_IRAN_TIME
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_LOCAL_DIGITS
import com.byagowi.persiancalendar.DEFAULT_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.DEFAULT_PM
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CLOCK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.DEFAULT_WIDGET_IN_24
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_NEW_INTERFACE
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
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
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.Variants.debugLog
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.utils.canEnableNewInterface
import com.byagowi.persiancalendar.utils.ARABIC_DIGITS
import com.byagowi.persiancalendar.utils.EnabledHolidays
import com.byagowi.persiancalendar.utils.PERSIAN_DIGITS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.enableHighLatitudesConfiguration
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.loadEvents
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.byagowi.persiancalendar.utils.splitIgnoreEmpty
import com.byagowi.persiancalendar.utils.storedCity
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod

private val monthNameEmptyList = List(12) { "" }
var persianMonths = monthNameEmptyList
    private set
var islamicMonths = monthNameEmptyList
    private set
var gregorianMonths = monthNameEmptyList
    private set
var nepaliMonths = monthNameEmptyList
    private set
private val weekDaysEmptyList = List(7) { "" }
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
var calculationMethod = CalculationMethod.valueOf(DEFAULT_PRAY_TIME_METHOD)
    private set
var asrMethod = AsrMethod.Standard
    private set
var highLatitudesMethod = HighLatitudesMethod.NightMiddle
    private set
var enableNewInterface = false
    private set
var language = Language.FA
    private set
var easternGregorianArabicMonths = false
    private set
var coordinates: Coordinates? = null
    private set
var mainCalendar = CalendarType.SHAMSI
    private set
var otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    private set
var isShowWeekOfYearEnabled = false
    private set
var isCenterAlignWidgets = true
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
var isTalkBackEnabled = false
    private set
var isHighTextContrastEnabled = false
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
var spacedAnd = " و "
    private set
var spacedColon = ": "
    private set
var spacedComma = "، "
    private set
var holidayString = DEFAULT_HOLIDAY
    private set
var numericalDatePreferred = false
    private set
var calendarTypesTitleAbbr = emptyList<String>()
    private set
// Some more are in EventsUtils

// This should be called before any use of Utils on the activity and services
fun initGlobal(context: Context) {
    debugLog("Utils: initGlobal is called")
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResources(context)
    scheduleAlarms(context)
    configureCalendarsAndLoadEvents(context)
}

fun configureCalendarsAndLoadEvents(context: Context) {
    debugLog("Utils: configureCalendarsAndLoadEvents is called")
    val appPrefs = context.appPrefs

    IslamicDate.islamicOffset = if (appPrefs.isIslamicOffsetExpired) 0 else
        appPrefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)?.toIntOrNull() ?: 0

    val enabledHolidays = EnabledHolidays(appPrefs)
    isIranHolidaysEnabled = enabledHolidays.iranHolidays
    loadEvents(enabledHolidays, language)
}

fun loadLanguageResources(context: Context) {
    debugLog("Utils: loadLanguageResources is called")
    persianMonths = language.getPersianMonths(context)
    islamicMonths = language.getIslamicMonths(context)
    gregorianMonths = language.getGregorianMonths(context, easternGregorianArabicMonths)
    nepaliMonths = language.getNepaliMonths()
    weekDays = language.getWeekDays(context)
    weekDaysInitials = language.getWeekDaysInitials(context)
}

fun updateStoredPreference(context: Context) {
    debugLog("Utils: updateStoredPreference is called")
    val prefs = context.appPrefs

    language = prefs.getString(PREF_APP_LANGUAGE, null)?.let(Language::valueOfLanguageCode)
        ?: Language.preferredDefaultLanguage
    easternGregorianArabicMonths = prefs.getBoolean(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, false)

    enableNewInterface = canEnableNewInterface &&
            prefs.getBoolean(PREF_NEW_INTERFACE, false) //shouldEnableNewInterface)

    preferredDigits =
        if (!prefs.getBoolean(PREF_LOCAL_DIGITS, DEFAULT_LOCAL_DIGITS) ||
            !language.canHaveLocalDigits
        ) ARABIC_DIGITS
        else language.preferredDigits

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled =
        language.isIranExclusive && prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
    isCenterAlignWidgets = prefs.getBoolean(PREF_CENTER_ALIGN_WIDGETS, true)

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod = CalculationMethod
        .valueOf(prefs.getString(PREF_PRAY_TIME_METHOD, null) ?: DEFAULT_PRAY_TIME_METHOD)
    asrMethod =
        if (calculationMethod.isJafari ||
            !prefs.getBoolean(PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority)
        ) AsrMethod.Standard else AsrMethod.Hanafi
    highLatitudesMethod = HighLatitudesMethod.valueOf(
        if (!enableHighLatitudesConfiguration) DEFAULT_HIGH_LATITUDES_METHOD
        else prefs.getString(PREF_HIGH_LATITUDES_METHOD, null) ?: DEFAULT_HIGH_LATITUDES_METHOD
    )


    coordinates = prefs.storedCity?.coordinates ?: run {
        listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE)
            .map { prefs.getString(it, null)?.toDoubleOrNull() ?: .0 }
            .takeIf { coords -> coords.any { it != .0 } } // if all were zero preference isn't set yet
            ?.let { (lat, lng, alt) -> Coordinates(lat, lng, alt) }
    }
    runCatching {
        mainCalendar = CalendarType.valueOf(
            prefs.getString(PREF_MAIN_CALENDAR_KEY, null) ?: language.defaultMainCalendar
        )
        otherCalendars =
            (prefs.getString(PREF_OTHER_CALENDARS_KEY, null) ?: language.defaultOtherCalendars)
                .splitIgnoreEmpty(",").map(CalendarType::valueOf)
    }.onFailure(logException).onFailure {
        // This really shouldn't happen, just in case
        mainCalendar = CalendarType.SHAMSI
        otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    }.getOrNull().debugAssertNotNull

    isShowWeekOfYearEnabled = prefs.getBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, null) ?: language.defaultWeekStart).toIntOrNull() ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, null) ?: language.defaultWeekEnds)
        .mapNotNull(String::toIntOrNull).forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets = prefs.getStringSet(PREF_WHAT_TO_SHOW_WIDGETS, null)
        ?: DEFAULT_WIDGET_CUSTOMIZATIONS

    isAstronomicalFeaturesEnabled = prefs.getBoolean(PREF_ASTRONOMICAL_FEATURES, false)
    numericalDatePreferred = prefs.getBoolean(PREF_NUMERICAL_DATE_PREFERRED, false)

    // TODO: probably can be done in applyAppLanguage itself?
    if (language.language != resources.getString(R.string.code)) applyAppLanguage(context)

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

    when {
        // This is mostly pointless except we want to make sure even on broken language resources state
        // which might happen in widgets updates we don't have wrong values for these important two
        language.isPersian -> {
            amString = DEFAULT_AM
            pmString = DEFAULT_PM
        }
        else -> {
            amString = context.getString(R.string.am)
            pmString = context.getString(R.string.pm)
        }
    }
    holidayString = if (language.isDari) "رخصتی" else context.getString(R.string.holiday)
    spacedAnd = context.getString(R.string.spaced_and)
    spacedColon = context.getString(R.string.spaced_colon)
    spacedComma = context.getString(R.string.spaced_comma)

    isTalkBackEnabled = context.getSystemService<AccessibilityManager>()?.let {
        it.isEnabled && it.isTouchExplorationEnabled
    } ?: false

    // https://stackoverflow.com/a/61599809
    isHighTextContrastEnabled = runCatching {
        context.getSystemService<AccessibilityManager>()?.let {
            it.javaClass.getMethod("isHighTextContrastEnabled").invoke(it) as? Boolean
        }
    }.onFailure(logException).getOrNull() ?: false
}
