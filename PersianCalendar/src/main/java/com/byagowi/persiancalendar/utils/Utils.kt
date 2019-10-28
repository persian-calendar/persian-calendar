package com.byagowi.persiancalendar.utils

import android.util.SparseArray
import androidx.annotation.StyleRes
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.*
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.*
import java.util.concurrent.TimeUnit

const val TAG = "Utils"
val twoSeconds = TimeUnit.SECONDS.toMillis(2)
val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
//
//
//
// Service
//
const val DAY_IN_SECOND: Long = 86400
const val CHANGE_DATE_TAG = "changeDate"
const val UPDATE_TAG = "update"
var sAllEnabledEvents: List<BaseEvent> = emptyList()
val emptyStringList: Array<String>? = arrayOf("", "", "", "", "", "", "", "", "", "", "", "", "")
var persianMonths = emptyStringList
var islamicMonths = emptyStringList
var gregorianMonths = emptyStringList
var weekDays = emptyStringList
var weekDaysInitials = emptyStringList
var preferredDigits = PERSIAN_DIGITS
var clockIn24 = DEFAULT_WIDGET_IN_24
var iranTime = DEFAULT_IRAN_TIME
var notifyInLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
var widgetClock = DEFAULT_WIDGET_CLOCK
var notifyDate = DEFAULT_NOTIFY_DATE
var notificationAthan = DEFAULT_NOTIFICATION_ATHAN
var selectedWidgetTextColor: String = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
var selectedWidgetBackgroundColor: String = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
//    static private String islamicOffset = DEFAULT_ISLAMIC_OFFSET;
var calculationMethod: String = DEFAULT_PRAY_TIME_METHOD
var language: String = DEFAULT_APP_LANGUAGE
var coordinate: Coordinate? = null
var mainCalendar = CalendarType.SHAMSI
var otherCalendars = arrayOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
var spacedComma = "، "
var showWeekOfYear: Boolean = false
var centerAlignWidgets: Boolean = false
var weekStartOffset: Int = 0
var weekEnds = booleanArrayOf()
var showDeviceCalendarEvents: Boolean = false
var whatToShowOnWidgets: Set<String> = emptySet()
var astronomicalFeaturesEnabled: Boolean = false
@StyleRes
var appTheme = R.style.LightTheme
var talkBackEnabled = false
var prayTimes: PrayTimes? = null
val irCodeOrder = listOf("zz", "ir", "af", "iq")
val afCodeOrder = listOf("zz", "af", "ir", "iq")
val arCodeOrder = listOf("zz", "iq", "ir", "af")
var cachedCityKey = ""
var cachedCity: CityItem? = null
var sPersianCalendarEvents = SparseArray<ArrayList<PersianCalendarEvent>>()
var sIslamicCalendarEvents = SparseArray<ArrayList<IslamicCalendarEvent>>()
var sGregorianCalendarEvents = SparseArray<ArrayList<GregorianCalendarEvent>>()
var sShiftWorkTitles: MutableMap<String, String> = HashMap()
var sShiftWorkStartingJdn: Long = -1
var sShiftWorkRecurs = true
var sShiftWorks: List<ShiftWorkRecord> = emptyList()
var sIsIranHolidaysEnabled = true
var sShiftWorkPeriod = 0
var sAM = DEFAULT_AM
var sPM = DEFAULT_PM
var latestToastShowTime: Long = -1
var numericalDatePreferred = false
var calendarTypesTitleAbbr = arrayOf<String>()
//    private static List<Reminder> sReminderDetails = Collections.emptyList();
//