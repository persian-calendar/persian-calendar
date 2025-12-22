package com.byagowi.persiancalendar.global

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.VisibleForTesting
import androidx.collection.LongSet
import androidx.collection.emptyLongSet
import androidx.collection.longSetOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_AM
import com.byagowi.persiancalendar.DEFAULT_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VIBRATION
import com.byagowi.persiancalendar.DEFAULT_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_BOLD_FONT
import com.byagowi.persiancalendar.DEFAULT_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.DEFAULT_DREAM_NOISE
import com.byagowi.persiancalendar.DEFAULT_DYNAMIC_ICON_ENABLED
import com.byagowi.persiancalendar.DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH
import com.byagowi.persiancalendar.DEFAULT_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.DEFAULT_HOLIDAY
import com.byagowi.persiancalendar.DEFAULT_IRAN_TIME
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_LARGE_ICON_ON_NOTIFICATION
import com.byagowi.persiancalendar.DEFAULT_LOCAL_NUMERAL
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.DEFAULT_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.DEFAULT_PM
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.DEFAULT_RED_HOLIDAYS
import com.byagowi.persiancalendar.DEFAULT_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.DEFAULT_SHOW_MOON_IN_SCORPIO
import com.byagowi.persiancalendar.DEFAULT_THEME_CYBERPUNK
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.DEFAULT_WALLPAPER_ALTERNATIVE
import com.byagowi.persiancalendar.DEFAULT_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.DEFAULT_WALLPAPER_DARK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CLOCK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.DEFAULT_WIDGET_IN_24
import com.byagowi.persiancalendar.DEFAULT_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_VIBRATION
import com.byagowi.persiancalendar.PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_BOLD_FONT
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_AS_HOLIDAY
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_TO_EXCLUDE
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_CUSTOM_FONT_NAME
import com.byagowi.persiancalendar.PREF_CUSTOM_IMAGE_NAME
import com.byagowi.persiancalendar.PREF_DREAM_NOISE
import com.byagowi.persiancalendar.PREF_DYNAMIC_ICON_ENABLED
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LARGE_DAY_NUMBER_ON_NOTIFICATION
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LOCAL_NUMERAL
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_RED_HOLIDAYS
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_MOON_IN_SCORPIO
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_SWIPE_DOWN_ACTION
import com.byagowi.persiancalendar.PREF_SWIPE_UP_ACTION
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_CYBERPUNK
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_WALLPAPER_ALTERNATIVE
import com.byagowi.persiancalendar.PREF_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.PREF_WALLPAPER_DARK
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.PREF_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.entities.WeekDay
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.calendar.SwipeDownAction
import com.byagowi.persiancalendar.ui.calendar.SwipeUpAction
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.isHighLatitude
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.isOldEra
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.byagowi.persiancalendar.utils.splitFilterNotEmpty
import com.byagowi.persiancalendar.utils.supportsDynamicIcon
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
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
private var persianMonths = monthNameEmptyList
private var oldEraPersianMonths = monthNameEmptyList
private var islamicMonths = monthNameEmptyList
private var gregorianMonths = monthNameEmptyList
private var nepaliMonths = monthNameEmptyList
private val weekDaysEmptyList = List(7) { "" }
var weekDaysTitles = weekDaysEmptyList
    private set
var weekDaysTitlesInitials = weekDaysEmptyList
    private set

private val numeral_ = mutableStateOf(Numeral.PERSIAN)
val numeral by numeral_

private val localNumeralPreference_ = mutableStateOf(DEFAULT_LOCAL_NUMERAL)
val localNumeralPreference by localNumeralPreference_

private val clockIn24_ = mutableStateOf(DEFAULT_WIDGET_IN_24)
val clockIn24 by clockIn24_

var isDynamicIconEverEnabled = false
    private set

private val isDynamicIconEnabled_ = mutableStateOf(DEFAULT_DYNAMIC_ICON_ENABLED)
val isDynamicIconEnabled by isDynamicIconEnabled_

private val isForcedIranTimeEnabled_ = mutableStateOf(DEFAULT_IRAN_TIME)
val isForcedIranTimeEnabled by isForcedIranTimeEnabled_

private val isNotifyDateOnLockScreen_ = mutableStateOf(DEFAULT_NOTIFY_DATE_LOCK_SCREEN)
val isNotifyDateOnLockScreen by isNotifyDateOnLockScreen_

private val isLargeDayNumberOnNotification_ = mutableStateOf(DEFAULT_LARGE_ICON_ON_NOTIFICATION)
val isLargeDayNumberOnNotification by isLargeDayNumberOnNotification_


private val prefersWidgetsDynamicColors_ = mutableStateOf(false)

@delegate:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val prefersWidgetsDynamicColors by prefersWidgetsDynamicColors_

private val isWidgetClock_ = mutableStateOf(DEFAULT_WIDGET_CLOCK)
val isWidgetClock by isWidgetClock_

private val isNotifyDate_ = mutableStateOf(DEFAULT_NOTIFY_DATE)
val isNotifyDate by isNotifyDate_

private val notificationAthan_ = mutableStateOf(isNotifyDate)
val notificationAthan by notificationAthan_
private val athanVibration_ = mutableStateOf(DEFAULT_ATHAN_VIBRATION)
val athanVibration by athanVibration_
private val ascendingAthan_ = mutableStateOf(DEFAULT_ASCENDING_ATHAN_VOLUME)
val ascendingAthan by ascendingAthan_

private val calculationMethod_ =
    mutableStateOf(CalculationMethod.valueOf(DEFAULT_PRAY_TIME_METHOD))
val calculationMethod by calculationMethod_

private val athanSoundName_ = mutableStateOf<String?>(null)
val athanSoundName by athanSoundName_

private val midnightMethod_ = mutableStateOf(calculationMethod.defaultMidnight)
val midnightMethod by midnightMethod_

private val highLatitudesMethod_ = mutableStateOf(HighLatitudesMethod.NightMiddle)
val highLatitudesMethod by highLatitudesMethod_

private val asrMethod_ = mutableStateOf(AsrMethod.Standard)
val asrMethod by asrMethod_

// Just to use in the settings
private val islamicCalendarOffset_ = mutableIntStateOf(DEFAULT_ISLAMIC_OFFSET)
val islamicCalendarOffset by islamicCalendarOffset_

private val language_ = mutableStateOf(Language.FA)
val language by language_

// Don't use this just to detect dark mode as it's invalid in system default
private val userSetTheme_ = mutableStateOf(Theme.SYSTEM_DEFAULT)
val userSetTheme by userSetTheme_

private val systemDarkTheme_ = mutableStateOf(Theme.SYSTEM_DEFAULT)
val systemDarkTheme by systemDarkTheme_

private val systemLightTheme_ = mutableStateOf(Theme.SYSTEM_DEFAULT)
val systemLightTheme by systemLightTheme_

private val isGradient_ = mutableStateOf(DEFAULT_THEME_GRADIENT)
val isGradient by isGradient_

private val isCyberpunk_ = mutableStateOf(DEFAULT_THEME_CYBERPUNK)
val isCyberpunk by isCyberpunk_

private val isRedHolidays_ = mutableStateOf(DEFAULT_RED_HOLIDAYS)
val isRedHolidays by isRedHolidays_

private val isBoldFont_ = mutableStateOf(DEFAULT_BOLD_FONT)
val isBoldFont by isBoldFont_

private val customFontName_ = mutableStateOf<String?>(null)
val customFontName by customFontName_

private val customImageName_ = mutableStateOf<String?>(null)
val customImageName by customImageName_


private val englishGregorianPersianMonths_ =
    mutableStateOf(DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS)
val englishGregorianPersianMonths by englishGregorianPersianMonths_

private val easternGregorianArabicMonths_ =
    mutableStateOf(DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS)
val easternGregorianArabicMonths by easternGregorianArabicMonths_

private val alternativePersianMonthsInAzeri_ =
    mutableStateOf(DEFAULT_AZERI_ALTERNATIVE_PERSIAN_MONTHS)
val alternativePersianMonthsInAzeri by alternativePersianMonthsInAzeri_

private val englishWeekDaysInIranEnglish_ =
    mutableStateOf(DEFAULT_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH)
val englishWeekDaysInIranEnglish by englishWeekDaysInIranEnglish_

private var coordinates_ = mutableStateOf<Coordinates?>(null)
val coordinates by coordinates_

private var cityName_ = mutableStateOf<String?>(null)
val cityName by cityName_

private val widgetTransparency_ = mutableFloatStateOf(DEFAULT_WIDGET_TRANSPARENCY)
val widgetTransparency by widgetTransparency_

var enabledCalendars = listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
    private set
val mainCalendar inline get() = enabledCalendars.getOrNull(0) ?: Calendar.SHAMSI
val mainCalendarNumeral
    get() = when {
        secondaryCalendar == null -> numeral
        numeral.isArabic || !language.canHaveLocalNumeral -> Numeral.ARABIC
        else -> mainCalendar.preferredNumeral
    }
val secondaryCalendar
    get() = if (secondaryCalendarEnabled) enabledCalendars.getOrNull(1) else null

private val isCenterAlignWidgets_ = mutableStateOf(DEFAULT_CENTER_ALIGN_WIDGETS)
val isCenterAlignWidgets by isCenterAlignWidgets_

private val weekStart_ = mutableStateOf(Language.FA.defaultWeekStart)
val weekStart by weekStart_

// mutableStateSetOf do exist but atomic updates and prohibition of out of class write is more important
private val weekEnds_ = mutableStateOf<Set<WeekDay>>(emptySet())
val weekEnds by weekEnds_

private val isShowWeekOfYearEnabled_ = mutableStateOf(false)
val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled_

private val dreamNoise_ = mutableStateOf(DEFAULT_DREAM_NOISE)
val dreamNoise by dreamNoise_

private val wallpaperDark_ = mutableStateOf(DEFAULT_WALLPAPER_DARK)
val wallpaperDark by wallpaperDark_

private val wallpaperAutomatic_ = mutableStateOf(DEFAULT_WALLPAPER_AUTOMATIC)
val wallpaperAutomatic by wallpaperAutomatic_

private val wallpaperAlternative_ = MutableStateFlow(DEFAULT_WALLPAPER_ALTERNATIVE)
val wallpaperAlternative: StateFlow<Boolean> get() = wallpaperAlternative_

private val preferredSwipeUpAction_ = MutableStateFlow(SwipeUpAction.entries[0])
val preferredSwipeUpAction: StateFlow<SwipeUpAction> get() = preferredSwipeUpAction_

private val preferredSwipeDownAction_ = MutableStateFlow(SwipeDownAction.entries[0])
val preferredSwipeDownAction: StateFlow<SwipeDownAction> get() = preferredSwipeDownAction_

private val isShowDeviceCalendarEvents_ = MutableStateFlow(false)
val isShowDeviceCalendarEvents: StateFlow<Boolean> get() = isShowDeviceCalendarEvents_

private val eventCalendarsIdsToExclude_ = MutableStateFlow(emptyLongSet())
val eventCalendarsIdsToExclude: StateFlow<LongSet> get() = eventCalendarsIdsToExclude_

private val eventCalendarsIdsAsHoliday_ = MutableStateFlow(emptyLongSet())
val eventCalendarsIdsAsHoliday: StateFlow<LongSet> get() = eventCalendarsIdsAsHoliday_

private val whatToShowOnWidgets_ = MutableStateFlow(emptySet<String>())
val whatToShowOnWidgets get() = whatToShowOnWidgets_

private val isAstronomicalExtraFeaturesEnabled_ = MutableStateFlow(DEFAULT_ASTRONOMICAL_FEATURES)
val isAstronomicalExtraFeaturesEnabled: StateFlow<Boolean> get() = isAstronomicalExtraFeaturesEnabled_

private val showMoonInScorpio_ = MutableStateFlow(DEFAULT_SHOW_MOON_IN_SCORPIO)
val showMoonInScorpio: StateFlow<Boolean> get() = showMoonInScorpio_

private val isTalkBackEnabled_ = MutableStateFlow(false)
val isTalkBackEnabled: StateFlow<Boolean> get() = isTalkBackEnabled_

private val isHighTextContrastEnabled_ = MutableStateFlow(false)
val isHighTextContrastEnabled: StateFlow<Boolean> get() = isHighTextContrastEnabled_

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

// Otherwise WidgetService might use untranslated messages
var prayTimesTitles: Map<PrayTime, String> = emptyMap()
    private set
var nothingScheduledString = ""
    private set
var holidayString = DEFAULT_HOLIDAY
    private set

private val numericalDatePreferred_ = MutableStateFlow(DEFAULT_NUMERICAL_DATE_PREFERRED)
val numericalDatePreferred: StateFlow<Boolean> get() = numericalDatePreferred_

private val calendarsTitlesAbbr_ = MutableStateFlow(emptyMap<Calendar, String>())
val calendarsTitlesAbbr: StateFlow<Map<Calendar, String>> get() = calendarsTitlesAbbr_

private val eventsRepository_ = MutableStateFlow(EventsRepository.empty())
val eventsRepository: StateFlow<EventsRepository> get() = eventsRepository_

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
    val preferences = context.preferences
    IslamicDate.islamicOffset = getIslamicCalendarOffset(preferences)
    eventsRepository_.value = EventsRepository(preferences, language)
}

private fun getIslamicCalendarOffset(preferences: SharedPreferences): Int {
    return if (preferences.isIslamicOffsetExpired) 0
    else preferences.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET.toString())
        ?.toIntOrNull() ?: DEFAULT_ISLAMIC_OFFSET
}

fun yearMonthNameOfDate(date: AbstractDate): List<String> {
    return when (date) {
        is PersianDate -> if (date.isOldEra) oldEraPersianMonths else persianMonths
        is CivilDate -> gregorianMonths
        is IslamicDate -> islamicMonths
        is NepaliDate -> nepaliMonths
        else -> monthNameEmptyList
    }
}

fun loadLanguageResources(resources: Resources) {
    debugLog("Utils: loadLanguageResources is called")
    val language = language
    persianMonths = language.getPersianMonths(
        resources,
        alternativeMonthsInAzeri = alternativePersianMonthsInAzeri,
        afghanistanHolidaysIsEnable = eventsRepository_.value.afghanistanHolidays && !eventsRepository_.value.iranHolidays,
    )
    oldEraPersianMonths = when {
        language.isPersianOrDari -> Language.persianCalendarMonthsInDariOrPersianOldEra
        language == Language.EN_IR -> Language.persianCalendarMonthsInDariOrPersianOldEraTransliteration
        else -> persianMonths
    }
    islamicMonths = language.getIslamicMonths(resources)
    gregorianMonths = language.getGregorianMonths(
        resources,
        englishGregorianPersianMonths || easternGregorianArabicMonths,
    )
    nepaliMonths = language.getNepaliMonths()
    weekDaysTitles = if (englishWeekDaysInIranEnglish) Language.EN_US.getWeekDays(resources)
    else language.getWeekDays(resources)
    weekDaysTitlesInitials =
        if (englishWeekDaysInIranEnglish) Language.EN_US.getWeekDaysInitials(resources)
        else language.getWeekDaysInitials(resources)
    shiftWorkTitles = mapOf(
        "d" to resources.getString(R.string.shift_work_morning), // d -> day work, legacy key
        "r" to resources.getString(R.string.shift_work_off), // r -> rest, legacy key
        "e" to resources.getString(R.string.shift_work_evening),
        "n" to resources.getString(R.string.shift_work_night)
    )
    calendarsTitlesAbbr_.value =
        Calendar.entries.associateWith { resources.getString(it.shortTitle) }
    when {
        // This is mostly pointless except we want to make sure even on broken language resources state
        // which might happen in widgets updates we don't have wrong values for these important two
        language.isPersianOrDari -> {
            amString = DEFAULT_AM
            pmString = DEFAULT_PM
        }

        else -> {
            amString = resources.getString(R.string.am)
            pmString = resources.getString(R.string.pm)
        }
    }
    holidayString = when {
        language.isPersian -> DEFAULT_HOLIDAY
        language.isDari -> "رخصتی"
        else -> resources.getString(R.string.holiday)
    }
    nothingScheduledString = resources.getString(R.string.nothing_scheduled)
    prayTimesTitles = PrayTime.entries.associateWith { resources.getString(it.stringRes) }
    spacedOr = resources.getString(R.string.spaced_or)
    spacedAndInDates = if (language.languagePrefersHalfSpaceAndInDates) " "
    else resources.getString(R.string.spaced_and)
    spacedColon = resources.getString(R.string.spaced_colon)
    spacedComma = resources.getString(R.string.spaced_comma)
}

fun updateStoredPreference(context: Context) {
    debugLog("Utils: updateStoredPreference is called")
    val preferences = context.preferences
    val language =
        preferences.getString(PREF_APP_LANGUAGE, null)?.let(Language::valueOfLanguageCode)
            ?: Language.getPreferredDefaultLanguage(context)

    language_.value = language
    userSetTheme_.value = run {
        val key = preferences.getString(PREF_THEME, null)
        Theme.entries.find { it.key == key }
    } ?: Theme.SYSTEM_DEFAULT
    systemDarkTheme_.value = run {
        val key = preferences.getString(PREF_SYSTEM_DARK_THEME, null)
        Theme.entries.find { it.key == key }.takeIf { it != Theme.SYSTEM_DEFAULT }
    } ?: Theme.DARK
    systemLightTheme_.value = run {
        val key = preferences.getString(PREF_SYSTEM_LIGHT_THEME, null)
        Theme.entries.find { it.key == key }.takeIf { it != Theme.SYSTEM_DEFAULT }
    } ?: Theme.LIGHT
    isGradient_.value = preferences.getBoolean(PREF_THEME_GRADIENT, DEFAULT_THEME_GRADIENT)
    isCyberpunk_.value = preferences.getBoolean(PREF_THEME_CYBERPUNK, DEFAULT_THEME_CYBERPUNK)
    isRedHolidays_.value = preferences.getBoolean(PREF_RED_HOLIDAYS, DEFAULT_RED_HOLIDAYS)
    isBoldFont_.value = preferences.getBoolean(PREF_BOLD_FONT, DEFAULT_BOLD_FONT)
    customFontName_.value = preferences.getString(PREF_CUSTOM_FONT_NAME, null)
    customImageName_.value = preferences.getString(PREF_CUSTOM_IMAGE_NAME, null)
    englishGregorianPersianMonths_.value = language.isPersian && preferences.getBoolean(
        PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS, DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
    )
    easternGregorianArabicMonths_.value = language.isArabic && preferences.getBoolean(
        PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
    )
    alternativePersianMonthsInAzeri_.value = language == Language.AZB && preferences.getBoolean(
        PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS, DEFAULT_AZERI_ALTERNATIVE_PERSIAN_MONTHS
    )
    englishWeekDaysInIranEnglish_.value = language == Language.EN_IR && preferences.getBoolean(
        PREF_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH, DEFAULT_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH
    )

    prefersWidgetsDynamicColors_.value = userSetTheme.isDynamicColors && preferences.getBoolean(
        PREF_WIDGETS_PREFER_SYSTEM_COLORS,
        true,
    )

    localNumeralPreference_.value =
        preferences.getBoolean(PREF_LOCAL_NUMERAL, DEFAULT_LOCAL_NUMERAL)
    numeral_.value = when {
        !language.canHaveLocalNumeral -> Numeral.ARABIC
        !localNumeralPreference -> Numeral.ARABIC
        else -> language.preferredNumeral
    }

    clockIn24_.value = preferences.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled_.value = language.showIranTimeOption && preferences.getBoolean(
        PREF_IRAN_TIME, DEFAULT_IRAN_TIME
    ) && TimeZone.getDefault().id != IRAN_TIMEZONE_ID
    isDynamicIconEverEnabled = PREF_DYNAMIC_ICON_ENABLED in preferences
    isDynamicIconEnabled_.value = preferences.getBoolean(
        PREF_DYNAMIC_ICON_ENABLED, DEFAULT_DYNAMIC_ICON_ENABLED
    ) && supportsDynamicIcon(mainCalendar, language)
    isNotifyDateOnLockScreen_.value = preferences.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN, DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isLargeDayNumberOnNotification_.value = preferences.getBoolean(
        PREF_LARGE_DAY_NUMBER_ON_NOTIFICATION, DEFAULT_LARGE_ICON_ON_NOTIFICATION
    )
    isWidgetClock_.value = preferences.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate_.value = preferences.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan_.value =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || preferences.getBoolean(
            PREF_NOTIFICATION_ATHAN,
            isNotifyDate,
        )
    athanVibration_.value = preferences.getBoolean(PREF_ATHAN_VIBRATION, DEFAULT_ATHAN_VIBRATION)
    ascendingAthan_.value =
        preferences.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME)
    isCenterAlignWidgets_.value =
        preferences.getBoolean(PREF_CENTER_ALIGN_WIDGETS, DEFAULT_CENTER_ALIGN_WIDGETS)

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod_.value = CalculationMethod.valueOf(
        preferences.getString(PREF_PRAY_TIME_METHOD, null) ?: DEFAULT_PRAY_TIME_METHOD
    )
    asrMethod_.value = if (calculationMethod.isJafari || !preferences.getBoolean(
            PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority
        )
    ) AsrMethod.Standard else AsrMethod.Hanafi
    islamicCalendarOffset_.intValue = getIslamicCalendarOffset(preferences)
    midnightMethod_.value =
        preferences.getString(PREF_MIDNIGHT_METHOD, null)?.let(MidnightMethod::valueOf)
            ?.takeIf { !it.isJafariOnly || calculationMethod.isJafari }
            ?: calculationMethod.defaultMidnight
    highLatitudesMethod_.value = HighLatitudesMethod.valueOf(
        if (coordinates?.isHighLatitude != true) DEFAULT_HIGH_LATITUDES_METHOD
        else preferences.getString(PREF_HIGH_LATITUDES_METHOD, null)
            ?: DEFAULT_HIGH_LATITUDES_METHOD
    )
    athanSoundName_.value = preferences.getString(PREF_ATHAN_NAME, null)

    dreamNoise_.value = preferences.getBoolean(PREF_DREAM_NOISE, DEFAULT_DREAM_NOISE)
    wallpaperDark_.value = preferences.getBoolean(PREF_WALLPAPER_DARK, DEFAULT_WALLPAPER_DARK)
    wallpaperAutomatic_.value =
        preferences.getBoolean(PREF_WALLPAPER_AUTOMATIC, DEFAULT_WALLPAPER_AUTOMATIC)
    wallpaperAlternative_.value =
        preferences.getBoolean(PREF_WALLPAPER_ALTERNATIVE, DEFAULT_WALLPAPER_ALTERNATIVE)

    preferredSwipeUpAction_.value = SwipeUpAction.entries.firstOrNull {
        it.name == preferences.getString(PREF_SWIPE_UP_ACTION, null)
    } ?: SwipeUpAction.entries[0]
    preferredSwipeDownAction_.value = SwipeDownAction.entries.firstOrNull {
        it.name == preferences.getString(PREF_SWIPE_DOWN_ACTION, null)
    } ?: SwipeDownAction.entries[0]

    val storedCity = preferences.getString(PREF_SELECTED_LOCATION, null)
        ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY }?.let { citiesStore[it] }
    coordinates_.value = storedCity?.coordinates ?: run {
        listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE).map {
            preferences.getString(it, null)?.toDoubleOrNull() ?: .0
        }.takeIf { coords -> coords.any { it != .0 } } // if all were zero preference isn't set yet
            ?.let { (lat, lng, alt) -> Coordinates(lat, lng, alt) }
    }
    cityName_.value = storedCity?.let(language::getCityName) ?: preferences.getString(
        PREF_GEOCODED_CITYNAME,
        null,
    )?.takeIf { it.isNotEmpty() }

    widgetTransparency_.floatValue =
        preferences.getFloat(PREF_WIDGET_TRANSPARENCY, DEFAULT_WIDGET_TRANSPARENCY)

    runCatching {
        val mainCalendar = Calendar.valueOf(
            preferences.getString(PREF_MAIN_CALENDAR_KEY, null) ?: language.defaultCalendars[0].name
        )
        val otherCalendars = (preferences.getString(PREF_OTHER_CALENDARS_KEY, null)
            ?: language.defaultCalendars.drop(1).joinToString(",") { it.name }).splitFilterNotEmpty(
            ","
        ).map(Calendar::valueOf)
        enabledCalendars = (listOf(mainCalendar) + otherCalendars).distinct()
        secondaryCalendarEnabled = preferences.getBoolean(
            PREF_SECONDARY_CALENDAR_IN_TABLE, DEFAULT_SECONDARY_CALENDAR_IN_TABLE
        )
    }.onFailure(logException).onFailure {
        // This really shouldn't happen, just in case
        enabledCalendars = listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
        secondaryCalendarEnabled = false
    }.getOrNull().debugAssertNotNull

    isShowWeekOfYearEnabled_.value = preferences.getBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false)
    weekStart_.value = preferences.getString(PREF_WEEK_START, null)?.toIntOrNull()?.let {
        WeekDay.entries[it]
    } ?: language.defaultWeekStart

    weekEnds_.value = preferences.getStringSet(PREF_WEEK_ENDS, null)?.let {
        buildSet { it.forEach { x -> x.toIntOrNull()?.let { i -> add(WeekDay.entries[i]) } } }
    } ?: language.defaultWeekEnds

    isShowDeviceCalendarEvents_.value =
        preferences.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    eventCalendarsIdsToExclude_.value = if (isShowDeviceCalendarEvents_.value) longSetOf(
        *(preferences.getString(PREF_CALENDARS_IDS_TO_EXCLUDE, null).orEmpty()).splitFilterNotEmpty(
            ","
        ).mapNotNull { it.toLongOrNull() }.toLongArray()
    ) else emptyLongSet()
    eventCalendarsIdsAsHoliday_.value = if (isShowDeviceCalendarEvents_.value) longSetOf(
        *(preferences.getString(PREF_CALENDARS_IDS_AS_HOLIDAY, null).orEmpty()).splitFilterNotEmpty(
            ","
        ).mapNotNull { it.toLongOrNull() }.toLongArray()
    ) else emptyLongSet()

    whatToShowOnWidgets_.value =
        preferences.getStringSet(PREF_WHAT_TO_SHOW_WIDGETS, null) ?: DEFAULT_WIDGET_CUSTOMIZATIONS

    isAstronomicalExtraFeaturesEnabled_.value =
        preferences.getBoolean(PREF_ASTRONOMICAL_FEATURES, DEFAULT_ASTRONOMICAL_FEATURES)
    showMoonInScorpio_.value = isAstronomicalExtraFeaturesEnabled_.value && preferences.getBoolean(
        PREF_SHOW_MOON_IN_SCORPIO,
        // It's true for the older users but the moment user enables astronomical features it disables this
        true
    )
    numericalDatePreferred_.value =
        preferences.getBoolean(PREF_NUMERICAL_DATE_PREFERRED, DEFAULT_NUMERICAL_DATE_PREFERRED)

    // TODO: probably can be done in applyAppLanguage itself?
    if (language.language != context.getString(R.string.code)) applyAppLanguage(context)

    shiftWorks =
        (preferences.getString(PREF_SHIFT_WORK_SETTING, null).orEmpty()).splitFilterNotEmpty(",")
            .map { it.splitFilterNotEmpty("=") }.filter { it.size == 2 }
            .map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) }
    shiftWorkPeriod = shiftWorks.sumOf { it.length }
    shiftWorkStartingJdn = preferences.getJdnOrNull(PREF_SHIFT_WORK_STARTING_JDN)
    shiftWorkRecurs = preferences.getBoolean(PREF_SHIFT_WORK_RECURS, true)

    context.getSystemService<AccessibilityManager>()?.updateAccessibilityFlows()
}

fun AccessibilityManager.updateAccessibilityFlows() {
    isTalkBackEnabled_.value = isEnabled && isTouchExplorationEnabled

    // https://stackoverflow.com/a/61599809
    isHighTextContrastEnabled_.value = runCatching {
        if (Build.VERSION.SDK_INT >= 36) {
            isHighContrastTextEnabled
        } else this@updateAccessibilityFlows.javaClass.getMethod("isHighTextContrastEnabled")
            .invoke(this) as? Boolean
    }.onFailure(logException).getOrNull() == true
}

@VisibleForTesting
fun changeWeekDaysForTest(weekEnds: Set<WeekDay>, action: () -> Unit) {
    val originalWeekEnds = weekEnds_.value
    weekEnds_.value = weekEnds
    action()
    weekEnds_.value = originalWeekEnds
}

@VisibleForTesting
fun initiateMonthNamesForTest() {
    oldEraPersianMonths = Language.persianCalendarMonthsInDariOrPersianOldEra
}

// A very special case to trig coordinates mechanism in saveLocation
fun overrideCoordinatesGlobalVariable(coordinates: Coordinates) {
    coordinates_.value = coordinates
}
