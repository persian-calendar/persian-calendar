package com.byagowi.persiancalendar.global

import android.content.Context
import android.content.res.Resources
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_AM
import com.byagowi.persiancalendar.DEFAULT_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
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
import com.byagowi.persiancalendar.DEFAULT_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.DEFAULT_THEME_CYBERPUNK
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CLOCK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.DEFAULT_WIDGET_IN_24
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_CYBERPUNK
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.enableHighLatitudesConfiguration
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.byagowi.persiancalendar.utils.splitFilterNotEmpty
import com.byagowi.persiancalendar.utils.storedCity
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.TimeZone

// Using global variable isn't really the best idea.
// Somehow it's a legacy thing for this now aged project.
// We have limited most of global variable to this package and
// are avoiding storing complicated things on it.

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
var preferredDigits = Language.PERSIAN_DIGITS
    private set
var clockIn24 = DEFAULT_WIDGET_IN_24
    private set

private val isForcedIranTimeEnabled_ = MutableStateFlow(DEFAULT_IRAN_TIME)
val isForcedIranTimeEnabled: StateFlow<Boolean> = isForcedIranTimeEnabled_

val isNotifyDateOnLockScreen_ = MutableStateFlow(DEFAULT_NOTIFY_DATE_LOCK_SCREEN)
val isNotifyDateOnLockScreen: StateFlow<Boolean> = isNotifyDateOnLockScreen_

private val prefersWidgetsDynamicColors_ = MutableStateFlow(false)
val prefersWidgetsDynamicColorsFlow: StateFlow<Boolean> = prefersWidgetsDynamicColors_

var isWidgetClock = DEFAULT_WIDGET_CLOCK
    private set

val isNotifyDate_ = MutableStateFlow(DEFAULT_NOTIFY_DATE)
val isNotifyDate: StateFlow<Boolean> = isNotifyDate_

private val notificationAthan_ = MutableStateFlow(DEFAULT_NOTIFICATION_ATHAN)
val notificationAthan: StateFlow<Boolean> = notificationAthan_
private val ascendingAthan_ = MutableStateFlow(DEFAULT_ASCENDING_ATHAN_VOLUME)
val ascendingAthan: StateFlow<Boolean> = ascendingAthan_

private val calculationMethod_ =
    MutableStateFlow(CalculationMethod.valueOf(DEFAULT_PRAY_TIME_METHOD))
val calculationMethod: StateFlow<CalculationMethod> = calculationMethod_

private val athanSoundName_ = MutableStateFlow("")
val athanSoundName: StateFlow<String> = athanSoundName_

var midnightMethod = calculationMethod.value.defaultMidnight
    private set

private val asrMethod_ = MutableStateFlow(AsrMethod.Standard)
val asrMethod: StateFlow<AsrMethod> = asrMethod_

var highLatitudesMethod = HighLatitudesMethod.NightMiddle
    private set

private val language_ = MutableStateFlow(Language.FA)
val language: StateFlow<Language> = language_

private val theme_ = MutableStateFlow(Theme.SYSTEM_DEFAULT)
val theme: StateFlow<Theme> = theme_

private val isCyberpunk_ = MutableStateFlow(DEFAULT_THEME_CYBERPUNK)
val isCyberpunk: StateFlow<Boolean> = isCyberpunk_

private val isGradient_ = MutableStateFlow(DEFAULT_THEME_GRADIENT)
val isGradient: StateFlow<Boolean> = isGradient_

private var alternativeGregorianMonths = false

private val coordinates_ = MutableStateFlow<Coordinates?>(null)
val coordinates: StateFlow<Coordinates?> = coordinates_

private val cityName_ = MutableStateFlow<String?>(null)
val cityName: StateFlow<String?> = cityName_

var enabledCalendars = listOf(CalendarType.SHAMSI, CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    private set
val mainCalendar inline get() = enabledCalendars.getOrNull(0) ?: CalendarType.SHAMSI
val mainCalendarDigits
    get() = when {
        secondaryCalendar == null -> preferredDigits
        preferredDigits === Language.ARABIC_DIGITS -> Language.ARABIC_DIGITS
        !language.value.canHaveLocalDigits -> Language.ARABIC_DIGITS
        else -> mainCalendar.preferredDigits
    }
val secondaryCalendar
    get() = if (secondaryCalendarEnabled) enabledCalendars.getOrNull(1) else null
val secondaryCalendarDigits
    get() = when {
        !language.value.canHaveLocalDigits -> Language.ARABIC_DIGITS
        preferredDigits === Language.ARABIC_DIGITS -> Language.ARABIC_DIGITS
        else -> secondaryCalendar?.preferredDigits ?: Language.ARABIC_DIGITS
    }
var isShowWeekOfYearEnabled = false
    private set
var isCenterAlignWidgets = true
    private set
var weekStartOffset = 0
    private set
var weekEnds = BooleanArray(7)
    private set

private val isShowDeviceCalendarEvents_ = MutableStateFlow(false)
val isShowDeviceCalendarEvents: StateFlow<Boolean> = isShowDeviceCalendarEvents_

var whatToShowOnWidgets = emptySet<String>()
    private set
var isAstronomicalExtraFeaturesEnabled = false
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
var spacedAndInDates = " و "
    private set
var spacedOr = " و "
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
var eventsRepository: EventsRepository? = null
    private set

private var secondaryCalendarEnabled = false

// This should be called before any use of Utils on the activity and services
fun initGlobal(context: Context) {
    debugLog("Utils: initGlobal is called")
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResources(context.resources)
    scheduleAlarms(context)
    configureCalendarsAndLoadEvents(context)
}

fun configureCalendarsAndLoadEvents(context: Context) {
    debugLog("Utils: configureCalendarsAndLoadEvents is called")
    val appPrefs = context.appPrefs

    IslamicDate.islamicOffset = if (appPrefs.isIslamicOffsetExpired) 0 else appPrefs.getString(
        PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET
    )?.toIntOrNull() ?: 0

    eventsRepository = EventsRepository(appPrefs, language.value)
    isIranHolidaysEnabled = eventsRepository?.iranHolidays ?: false
}

fun loadLanguageResources(resources: Resources) {
    debugLog("Utils: loadLanguageResources is called")
    val language = language.value
    persianMonths = language.getPersianMonths(resources)
    islamicMonths = language.getIslamicMonths(resources)
    gregorianMonths = language.getGregorianMonths(resources, alternativeGregorianMonths)
    nepaliMonths = language.getNepaliMonths()
    weekDays = language.getWeekDays(resources)
    weekDaysInitials = language.getWeekDaysInitials(resources)
}

fun updateStoredPreference(context: Context) {
    debugLog("Utils: updateStoredPreference is called")
    val prefs = context.appPrefs
    val language = language.value

    language_.value = prefs.getString(PREF_APP_LANGUAGE, null)?.let(Language::valueOfLanguageCode)
        ?: Language.getPreferredDefaultLanguage(context)
    theme_.value = run {
        val key = prefs.getString(PREF_THEME, null) ?: Theme.SYSTEM_DEFAULT.key
        Theme.entries.find { it.key == key } ?: Theme.SYSTEM_DEFAULT
    }
    isCyberpunk_.value = prefs.getBoolean(PREF_THEME_CYBERPUNK, DEFAULT_THEME_CYBERPUNK)
    isGradient_.value = prefs.getBoolean(PREF_THEME_GRADIENT, DEFAULT_THEME_GRADIENT)
    alternativeGregorianMonths = when {
        language.isPersian -> prefs.getBoolean(
            PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS, DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
        )

        language.isArabic -> prefs.getBoolean(
            PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
        )

        else -> false
    }

    prefersWidgetsDynamicColors_.value = theme.value.isDynamicColors() &&
            prefs.getBoolean(PREF_WIDGETS_PREFER_SYSTEM_COLORS, true)

    preferredDigits = if (!prefs.getBoolean(
            PREF_LOCAL_DIGITS, DEFAULT_LOCAL_DIGITS
        ) || !language.canHaveLocalDigits
    ) Language.ARABIC_DIGITS
    else language.preferredDigits

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled_.value = language.showIranTimeOption && prefs.getBoolean(
        PREF_IRAN_TIME, DEFAULT_IRAN_TIME
    ) && TimeZone.getDefault().id != IRAN_TIMEZONE_ID
    isNotifyDateOnLockScreen_.value = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN, DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate_.value = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan_.value = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
    ascendingAthan_.value =
        prefs.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME)
    isCenterAlignWidgets = prefs.getBoolean(PREF_CENTER_ALIGN_WIDGETS, true)

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod_.value = CalculationMethod.valueOf(
        prefs.getString(PREF_PRAY_TIME_METHOD, null) ?: DEFAULT_PRAY_TIME_METHOD
    )
    asrMethod_.value = if (calculationMethod.value.isJafari || !prefs.getBoolean(
            PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority
        )
    ) AsrMethod.Standard else AsrMethod.Hanafi
    midnightMethod =
        prefs.getString(PREF_MIDNIGHT_METHOD, null)?.let(MidnightMethod::valueOf)
            ?.takeIf { !it.isJafariOnly || calculationMethod.value.isJafari }
            ?: calculationMethod.value.defaultMidnight
    highLatitudesMethod = HighLatitudesMethod.valueOf(
        if (coordinates.value?.enableHighLatitudesConfiguration != true) DEFAULT_HIGH_LATITUDES_METHOD
        else prefs.getString(PREF_HIGH_LATITUDES_METHOD, null) ?: DEFAULT_HIGH_LATITUDES_METHOD
    )
    athanSoundName_.value =
        prefs.getString(PREF_ATHAN_NAME, null) ?: context.getString(R.string.default_athan)

    val storedCity = prefs.storedCity
    coordinates_.value = storedCity?.coordinates ?: run {
        listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE).map {
            prefs.getString(it, null)?.toDoubleOrNull() ?: .0
        }.takeIf { coords -> coords.any { it != .0 } } // if all were zero preference isn't set yet
            ?.let { (lat, lng, alt) -> Coordinates(lat, lng, alt) }
    }
    cityName_.value = storedCity?.let(language::getCityName)
        ?: prefs.getString(PREF_GEOCODED_CITYNAME, null)?.takeIf { it.isNotEmpty() }

    runCatching {
        val mainCalendar = CalendarType.valueOf(
            prefs.getString(PREF_MAIN_CALENDAR_KEY, null) ?: language.defaultMainCalendar
        )
        val otherCalendars = (prefs.getString(PREF_OTHER_CALENDARS_KEY, null)
            ?: language.defaultOtherCalendars).splitFilterNotEmpty(",").map(CalendarType::valueOf)
        enabledCalendars = (listOf(mainCalendar) + otherCalendars).distinct()
        secondaryCalendarEnabled = prefs.getBoolean(
            PREF_SECONDARY_CALENDAR_IN_TABLE, DEFAULT_SECONDARY_CALENDAR_IN_TABLE
        )
    }.onFailure(logException).onFailure {
        // This really shouldn't happen, just in case
        enabledCalendars = listOf(CalendarType.SHAMSI, CalendarType.GREGORIAN, CalendarType.ISLAMIC)
        secondaryCalendarEnabled = false
    }.getOrNull().debugAssertNotNull

    isShowWeekOfYearEnabled = prefs.getBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, null) ?: language.defaultWeekStart).toIntOrNull() ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, null)
        ?: language.defaultWeekEnds).mapNotNull(String::toIntOrNull).forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents_.value =
        prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets =
        prefs.getStringSet(PREF_WHAT_TO_SHOW_WIDGETS, null) ?: DEFAULT_WIDGET_CUSTOMIZATIONS

    isAstronomicalExtraFeaturesEnabled = prefs.getBoolean(PREF_ASTRONOMICAL_FEATURES, false)
    numericalDatePreferred = prefs.getBoolean(PREF_NUMERICAL_DATE_PREFERRED, false)

    // TODO: probably can be done in applyAppLanguage itself?
    if (language.language != resources.getString(R.string.code)) applyAppLanguage(context)

    calendarTypesTitleAbbr = CalendarType.entries.map { context.getString(it.shortTitle) }

    shiftWorks = (prefs.getString(PREF_SHIFT_WORK_SETTING, null) ?: "").splitFilterNotEmpty(",")
        .map { it.splitFilterNotEmpty("=") }.filter { it.size == 2 }
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
    holidayString = when {
        language.isPersian -> DEFAULT_HOLIDAY
        language.isDari -> "رخصتی"
        else -> context.getString(R.string.holiday)
    }
    spacedOr = context.getString(R.string.spaced_or)
    spacedAndInDates = if (language.languagePrefersHalfSpaceAndInDates) " "
    else context.getString(R.string.spaced_and)
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

// A very special case to trig coordinates mechanism in saveLocation
fun overrideCoordinatesGlobalVariable(coordinates: Coordinates) {
    coordinates_.value = coordinates
}
