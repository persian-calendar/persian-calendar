package com.byagowi.persiancalendar.utils

import androidx.annotation.StyleRes
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.concurrent.TimeUnit

internal const val TAG = "Utils"
internal val twoSeconds = TimeUnit.SECONDS.toMillis(2)
//
//
//
// Service
//
internal const val DAY_IN_SECOND: Long = 86400
internal const val CHANGE_DATE_TAG = "changeDate"
internal const val UPDATE_TAG = "update"
internal val monthNameEmptyList = (1..12).map { "" }.toList()
internal var persianMonths = monthNameEmptyList
internal var islamicMonths = monthNameEmptyList
internal var gregorianMonths = monthNameEmptyList
internal val weekDaysEmptyList = (1..7).map { "" }.toList()
internal var weekDays = weekDaysEmptyList
internal var weekDaysInitials = weekDaysEmptyList
internal var preferredDigits = PERSIAN_DIGITS
internal var clockIn24 = DEFAULT_WIDGET_IN_24
internal var iranTime = DEFAULT_IRAN_TIME
internal var isNotifyDateOnLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
internal var isWidgetClock = DEFAULT_WIDGET_CLOCK
internal var isNotifyDate = DEFAULT_NOTIFY_DATE
internal var notificationAthan = DEFAULT_NOTIFICATION_ATHAN
internal var selectedWidgetTextColor: String = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
internal var selectedWidgetBackgroundColor: String = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
//    static private String islamicOffset = DEFAULT_ISLAMIC_OFFSET;
internal var calculationMethod: String = DEFAULT_PRAY_TIME_METHOD
internal var language: String = DEFAULT_APP_LANGUAGE
    get() = if (field.isEmpty()) DEFAULT_APP_LANGUAGE else field
internal var coordinate: Coordinate? = null
internal var mainCalendar = CalendarType.SHAMSI
internal var otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
internal var spacedComma = "، "
internal var showWeekOfYear: Boolean = false
internal var isCenterAlignWidgets: Boolean = false
internal var weekStartOffset: Int = 0
internal var weekEnds = BooleanArray(7)
internal var showDeviceCalendarEvents: Boolean = false
internal var whatToShowOnWidgets: Set<String> = emptySet()
internal var isAstronomicalFeaturesEnabled: Boolean = false
@StyleRes
internal var appTheme = R.style.LightTheme
internal var isTalkBackEnabled = false
internal var prayTimes: PrayTimes? = null
internal var cachedCityKey = ""
internal var cachedCity: CityItem? = null
internal var sShiftWorkTitles: Map<String, String> = emptyMap()
internal var sShiftWorkStartingJdn: Long = -1
internal var sShiftWorkRecurs = true
internal var sShiftWorks: List<ShiftWorkRecord> = emptyList()
internal var sIsIranHolidaysEnabled = true
internal var sShiftWorkPeriod = 0
internal var sAM = DEFAULT_AM
internal var sPM = DEFAULT_PM
internal var latestToastShowTime: Long = -1
internal var numericalDatePreferred = false
internal var calendarTypesTitleAbbr = emptyList<String>()