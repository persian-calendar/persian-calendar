package com.byagowi.persiancalendar.utils

import android.util.SparseArray
import androidx.annotation.StyleRes
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.*
import com.byagowi.persiancalendar.praytimes.Coordinate
import com.byagowi.persiancalendar.praytimes.PrayTimes
import java.util.*
import java.util.concurrent.TimeUnit

@kotlin.jvm.JvmField
val TAG = Utils::class.java.name
@kotlin.jvm.JvmField
val twoSeconds = TimeUnit.SECONDS.toMillis(2)
@kotlin.jvm.JvmField
val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
//
//
//
// Service
//
const val DAY_IN_SECOND: Long = 86400
const val CHANGE_DATE_TAG = "changeDate"
const val UPDATE_TAG = "update"
val emptyStringList: Array<String?> = arrayOf("", "", "", "", "", "", "", "", "", "", "", "", "")
@kotlin.jvm.JvmField
var persianMonths = emptyStringList
@kotlin.jvm.JvmField
var islamicMonths = emptyStringList
@kotlin.jvm.JvmField
var gregorianMonths = emptyStringList
@kotlin.jvm.JvmField
var weekDays = emptyStringList
@kotlin.jvm.JvmField
var weekDaysInitials = emptyStringList
@kotlin.jvm.JvmField
var preferredDigits = PERSIAN_DIGITS
@kotlin.jvm.JvmField
var clockIn24 = DEFAULT_WIDGET_IN_24
@kotlin.jvm.JvmField
var iranTime = DEFAULT_IRAN_TIME
@kotlin.jvm.JvmField
var notifyInLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
@kotlin.jvm.JvmField
var widgetClock = DEFAULT_WIDGET_CLOCK
@kotlin.jvm.JvmField
var notifyDate = DEFAULT_NOTIFY_DATE
@kotlin.jvm.JvmField
var notificationAthan = DEFAULT_NOTIFICATION_ATHAN
@kotlin.jvm.JvmField
var selectedWidgetTextColor: String = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
@kotlin.jvm.JvmField
var selectedWidgetBackgroundColor: String = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
//    static private String islamicOffset = DEFAULT_ISLAMIC_OFFSET;
@kotlin.jvm.JvmField
var calculationMethod: String? = DEFAULT_PRAY_TIME_METHOD
@kotlin.jvm.JvmField
var language: String? = DEFAULT_APP_LANGUAGE
@kotlin.jvm.JvmField
var coordinate: Coordinate? = null
@kotlin.jvm.JvmField
var mainCalendar = CalendarType.SHAMSI
@kotlin.jvm.JvmField
var otherCalendars = arrayOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
@kotlin.jvm.JvmField
var spacedComma = "، "
@kotlin.jvm.JvmField
var showWeekOfYear: Boolean = false
@kotlin.jvm.JvmField
var centerAlignWidgets: Boolean = false
@kotlin.jvm.JvmField
var weekStartOffset: Int = 0
@kotlin.jvm.JvmField
var weekEnds = booleanArrayOf()
@kotlin.jvm.JvmField
var showDeviceCalendarEvents: Boolean = false
@kotlin.jvm.JvmField
var whatToShowOnWidgets: Set<String> = emptySet()
@kotlin.jvm.JvmField
var astronomicalFeaturesEnabled: Boolean = false
@kotlin.jvm.JvmField
@StyleRes
var appTheme = R.style.LightTheme
@kotlin.jvm.JvmField
var talkBackEnabled = false
@kotlin.jvm.JvmField
var prayTimes: PrayTimes? = null
@kotlin.jvm.JvmField
val irCodeOrder = listOf("zz", "ir", "af", "iq")
@kotlin.jvm.JvmField
val afCodeOrder = listOf("zz", "af", "ir", "iq")
@kotlin.jvm.JvmField
val arCodeOrder = listOf("zz", "iq", "ir", "af")
@kotlin.jvm.JvmField
var cachedCityKey = ""
@kotlin.jvm.JvmField
var cachedCity: CityItem? = null
@kotlin.jvm.JvmField
var sPersianCalendarEvents = SparseArray<ArrayList<PersianCalendarEvent>>()
@kotlin.jvm.JvmField
var sIslamicCalendarEvents = SparseArray<ArrayList<IslamicCalendarEvent>>()
@kotlin.jvm.JvmField
var sGregorianCalendarEvents = SparseArray<ArrayList<GregorianCalendarEvent>>()
@kotlin.jvm.JvmField
var sShiftWorkTitles: MutableMap<String, String> = HashMap()
@kotlin.jvm.JvmField
var sShiftWorkStartingJdn: Long = -1
@kotlin.jvm.JvmField
var sShiftWorkRecurs = true
@kotlin.jvm.JvmField
var sShiftWorks: List<ShiftWorkRecord> = emptyList()
@kotlin.jvm.JvmField
var sIsIranHolidaysEnabled = true
@kotlin.jvm.JvmField
var sShiftWorkPeriod = 0
@kotlin.jvm.JvmField
var sAM = DEFAULT_AM
@kotlin.jvm.JvmField
var sPM = DEFAULT_PM
@kotlin.jvm.JvmField
var latestToastShowTime: Long = -1
@kotlin.jvm.JvmField
var numericalDatePreferred = false
@kotlin.jvm.JvmField
var calendarTypesTitleAbbr = arrayOf<String>()
//    private static List<Reminder> sReminderDetails = Collections.emptyList();
//