package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.text.Html
import android.util.Log
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.*
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
var sAllEnabledEvents: List<CalendarEvent<*>> = emptyList()
var sPersianCalendarEvents = SparseArray<ArrayList<PersianCalendarEvent>>()
var sIslamicCalendarEvents = SparseArray<ArrayList<IslamicCalendarEvent>>()
var sGregorianCalendarEvents = SparseArray<ArrayList<GregorianCalendarEvent>>()

fun isIranTime(): Boolean = iranTime

fun isWeekEnd(dayOfWeek: Int): Boolean = weekEnds[dayOfWeek]

fun isWeekOfYearEnabled(): Boolean = showWeekOfYear

fun isShowDeviceCalendarEvents(): Boolean = showDeviceCalendarEvents

fun hasAnyHolidays(dayEvents: List<CalendarEvent<*>>): Boolean = dayEvents.any { it.isHoliday }

fun fixDayOfWeek(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun fixDayOfWeekReverse(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

fun getAllEnabledEvents(): List<CalendarEvent<*>> = sAllEnabledEvents

fun getWeekDayName(position: Int): String? = weekDays.let { it[position % 7] }

fun getAmString(): String = sAM

fun getPmString(): String = sPM

fun getDayOfWeekFromJdn(jdn: Long): Int =
    civilDateToCalendar(CivilDate(jdn)).get(Calendar.DAY_OF_WEEK) % 7

fun formatDayAndMonth(day: Int, month: String): String =
    String.format(if (language == LANG_CKB) "%sی %s" else "%s %s", formatNumber(day), month)

fun dayTitleSummary(date: AbstractDate): String =
    getWeekDayName(date) + getSpacedComma() + formatDate(date)

fun civilDateToCalendar(civilDate: CivilDate): Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, civilDate.year)
    set(Calendar.MONTH, civilDate.month - 1)
    set(Calendar.DAY_OF_MONTH, civilDate.dayOfMonth)
}

fun getInitialOfWeekDay(position: Int): String =
    weekDaysInitials[position % 7]

fun getWeekDayName(date: AbstractDate): String {
    val civilDate = if (date is CivilDate)
        date
    else
        CivilDate(date)

    return weekDays[civilDateToCalendar(civilDate).get(Calendar.DAY_OF_WEEK) % 7]
}

fun calculateWeekOfYear(jdn: Long, startOfYearJdn: Long): Int {
    val dayOfYear = jdn - startOfYearJdn
    return ceil(1 + (dayOfYear - fixDayOfWeekReverse(getDayOfWeekFromJdn(jdn))) / 7.0).toInt()
}

fun getMonthName(date: AbstractDate): String =
    monthsNamesOfCalendar(date)[date.month - 1]

fun getMonthLength(calendar: CalendarType, year: Int, month: Int): Int {
    val yearOfNextMonth = if (month == 12) year + 1 else year
    val nextMonth = if (month == 12) 1 else month + 1
    return (getDateOfCalendar(calendar, yearOfNextMonth, nextMonth, 1).toJdn() - getDateOfCalendar(
        calendar,
        year,
        month,
        1
    ).toJdn()).toInt()
}

fun monthsNamesOfCalendar(date: AbstractDate): List<String> = when (date) {
    is PersianDate -> persianMonths
    is IslamicDate -> islamicMonths
    else -> gregorianMonths
}

fun getCalendarTypeFromDate(date: AbstractDate): CalendarType = when (date) {
    is IslamicDate -> CalendarType.ISLAMIC
    is CivilDate -> CalendarType.GREGORIAN
    else -> CalendarType.SHAMSI
}

fun getDateOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): AbstractDate =
    when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(year, month, day)
        CalendarType.GREGORIAN -> CivilDate(year, month, day)
        CalendarType.SHAMSI -> PersianDate(year, month, day)
    }

// Extra helpers
fun getA11yDaySummary(
    context: Context, jdn: Long, isToday: Boolean,
    deviceCalendarEvents: SparseArray<ArrayList<DeviceCalendarEvent>>?,
    withZodiac: Boolean, withOtherCalendars: Boolean, withTitle: Boolean
): String {
    // It has some expensive calculations, lets not do that when not needed
    if (!isTalkBackEnabled()) return ""

    val result = StringBuilder()

    if (isToday) {
        result.append(context.getString(R.string.today))
        result.append("\n")
    }

    val mainDate = getDateFromJdnOfCalendar(getMainCalendar(), jdn)

    if (withTitle) {
        result.append("\n")
        result.append(dayTitleSummary(mainDate))
    }

    val shift = getShiftWorkTitle(jdn, false)
    if (shift.isNotEmpty()) {
        result.append("\n")
        result.append(shift)
    }

    if (withOtherCalendars) {
        val otherCalendars = dateStringOfOtherCalendars(jdn, getSpacedComma())
        if (otherCalendars.isNotEmpty()) {
            result.append("\n")
            result.append("\n")
            result.append(context.getString(R.string.equivalent_to))
            result.append(" ")
            result.append(otherCalendars)
        }
    }

    val events = getEvents(jdn, deviceCalendarEvents)
    val holidays = getEventsTitle(
        events, true,
        compact = true,
        showDeviceCalendarEvents = true,
        insertRLM = false
    )
    if (holidays.isNotEmpty()) {
        result.append("\n")
        result.append("\n")
        result.append(context.getString(R.string.holiday_reason))
        result.append("\n")
        result.append(holidays)
    }

    val nonHolidays = getEventsTitle(
        events,
        holiday = false,
        compact = true,
        showDeviceCalendarEvents = true,
        insertRLM = false
    )
    if (nonHolidays.isNotEmpty()) {
        result.append("\n")
        result.append("\n")
        result.append(context.getString(R.string.events))
        result.append("\n")
        result.append(nonHolidays)
    }

    if (isWeekOfYearEnabled()) {
        val startOfYearJdn = getDateOfCalendar(
            getMainCalendar(),
            mainDate.year, 1, 1
        ).toJdn()
        val weekOfYearStart = calculateWeekOfYear(jdn, startOfYearJdn)
        result.append("\n")
        result.append("\n")
        result.append(
            String.format(
                context.getString(R.string.nth_week_of_year),
                formatNumber(weekOfYearStart)
            )
        )
    }

    if (withZodiac) {
        val zodiac = getZodiacInfo(context, jdn, false)
        if (zodiac.isNotEmpty()) {
            result.append("\n")
            result.append("\n")
            result.append(zodiac)
        }
    }

    return result.toString()
}

private fun <T : AbstractDate> holidayAwareEqualCheck(event: T, date: T): Boolean =
    (event.dayOfMonth == date.dayOfMonth && event.month == date.month && (event.year == -1 || event.year == date.year))

fun getEvents(
    jdn: Long,
    deviceCalendarEvents: SparseArray<ArrayList<DeviceCalendarEvent>>?
): List<CalendarEvent<*>> {
    val persian = PersianDate(jdn)
    val civil = CivilDate(jdn)
    val islamic = IslamicDate(jdn)

    val result = ArrayList<CalendarEvent<*>>()

    val persianList = sPersianCalendarEvents.get(persian.month * 100 + persian.dayOfMonth)
    if (persianList != null)
        for (persianCalendarEvent in persianList)
            if (holidayAwareEqualCheck(persianCalendarEvent.date, persian))
                result.add(persianCalendarEvent)

    var islamicList: List<IslamicCalendarEvent>? =
        sIslamicCalendarEvents.get(islamic.month * 100 + islamic.dayOfMonth)
    if (islamicList != null)
        for (islamicCalendarEvent in islamicList)
            if (holidayAwareEqualCheck(islamicCalendarEvent.date, islamic))
                result.add(islamicCalendarEvent)

    // Special case Imam Reza and Imam Mohammad Taqi martyrdom event on Hijri as it is a holiday and so vital to have
    if ((islamic.month == 2 || islamic.month == 11)
        && islamic.dayOfMonth == 29
        && getMonthLength(CalendarType.ISLAMIC, islamic.year, islamic.month) == 29
    ) {
        val alternativeDate = IslamicDate(islamic.year, islamic.month, 30)

        islamicList =
            sIslamicCalendarEvents.get(alternativeDate.month * 100 + alternativeDate.dayOfMonth)
        if (islamicList != null)
            for (islamicCalendarEvent in islamicList)
                if (holidayAwareEqualCheck(islamicCalendarEvent.date, alternativeDate))
                    result.add(islamicCalendarEvent)
    }

    val gregorianList = sGregorianCalendarEvents.get(civil.month * 100 + civil.dayOfMonth)
    if (gregorianList != null)
        for (gregorianCalendarEvent in gregorianList)
            if (holidayAwareEqualCheck(gregorianCalendarEvent.date, civil))
                result.add(gregorianCalendarEvent)

    // This one is passed by caller
    if (deviceCalendarEvents != null) {
        val deviceEventList = deviceCalendarEvents.get(civil.month * 100 + civil.dayOfMonth)
        if (deviceEventList != null)
            for (deviceCalendarEvent in deviceEventList)
            // holidayAwareEqualCheck is not needed as they won't have -1 on year field
                if (deviceCalendarEvent.date == civil)
                    result.add(deviceCalendarEvent)
    }

    return result
}

fun getIslamicOffset(context: Context): Int =
    try {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET).run {
            if (this == null) 0
            else Integer.parseInt(replace("+", ""))
        }
    } catch (ignore: Exception) {
        0
    }

fun loadEvents(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, null) ?: setOf("iran_holidays")

    val afghanistanHolidays = enabledTypes.contains("afghanistan_holidays")
    val afghanistanOthers = enabledTypes.contains("afghanistan_others")
    val iranHolidays = enabledTypes.contains("iran_holidays")
    val iranIslamic = enabledTypes.contains("iran_islamic")
    val iranAncient = enabledTypes.contains("iran_ancient")
    val iranOthers = enabledTypes.contains("iran_others")
    val international = enabledTypes.contains("international")

    sIsIranHolidaysEnabled = iranHolidays

    IslamicDate.useUmmAlQura = false
    if (!iranHolidays) {
        if (afghanistanHolidays) {
            IslamicDate.useUmmAlQura = true
        }
        when (getAppLanguage()) {
            LANG_FA_AF, LANG_PS, LANG_UR, LANG_AR, LANG_CKB, LANG_EN_US, LANG_JA -> IslamicDate.useUmmAlQura =
                true
        }
    }
    // Now that we are configuring converter's algorithm above, lets set the offset also
    IslamicDate.islamicOffset = getIslamicOffset(context)

    val persianCalendarEvents = SparseArray<ArrayList<PersianCalendarEvent>>()
    val islamicCalendarEvents = SparseArray<ArrayList<IslamicCalendarEvent>>()
    val gregorianCalendarEvents = SparseArray<ArrayList<GregorianCalendarEvent>>()
    val allEnabledEvents = ArrayList<CalendarEvent<*>>()

    try {
        val allTheEvents = JSONObject(readRawResource(context, R.raw.events))

        // https://stackoverflow.com/a/36188796
        operator fun JSONArray.iterator(): Iterator<JSONObject> =
            (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

        for (event in allTheEvents.getJSONArray("Persian Calendar")) {
            val month = event.getInt("month")
            val day = event.getInt("day")
            val year = if (event.has("year")) event.getInt("year") else -1
            var title = event.getString("title")
            var holiday = event.getBoolean("holiday")

            var addOrNot = false
            val type = event.getString("type")

            if (holiday && iranHolidays && (type == "Islamic Iran" ||
                        type == "Iran" || type == "Ancient Iran")
            )
                addOrNot = true

            if (!iranHolidays && type == "Islamic Iran")
                holiday = false

            if (iranIslamic && type == "Islamic Iran")
                addOrNot = true

            if (iranAncient && type == "Ancient Iran")
                addOrNot = true

            if (iranOthers && type == "Iran")
                addOrNot = true

            if (afghanistanHolidays && type == "Afghanistan" && holiday)
                addOrNot = true

            if (!afghanistanHolidays && type == "Afghanistan")
                holiday = false

            if (afghanistanOthers && type == "Afghanistan")
                addOrNot = true

            if (addOrNot) {
                title += " ("
                if (holiday && afghanistanHolidays && iranHolidays) {
                    if (type == "Islamic Iran" || type == "Iran")
                        title += "ایران، "
                    else if (type == "Afghanistan")
                        title += "افغانستان، "
                }
                title += formatDayAndMonth(day, persianMonths[month - 1]) + ")"

                var list: ArrayList<PersianCalendarEvent>? =
                    persianCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    persianCalendarEvents.put(month * 100 + day, list)
                }
                val ev = PersianCalendarEvent(PersianDate(year, month, day), title, holiday)
                list.add(ev)
                allEnabledEvents.add(ev)
            }
        }

        for (event in allTheEvents.getJSONArray("Hijri Calendar")) {
            val month = event.getInt("month")
            val day = event.getInt("day")
            var title = event.getString("title")
            var holiday = event.getBoolean("holiday")

            var addOrNot = false
            val type = event.getString("type")

            if (afghanistanHolidays && holiday && type == "Islamic Afghanistan")
                addOrNot = true

            if (!afghanistanHolidays && type == "Islamic Afghanistan")
                holiday = false

            if (afghanistanOthers && type == "Islamic Afghanistan")
                addOrNot = true

            if (iranHolidays && holiday && type == "Islamic Iran")
                addOrNot = true

            if (!iranHolidays && type == "Islamic Iran")
                holiday = false

            if (iranIslamic && type == "Islamic Iran")
                addOrNot = true

            if (iranOthers && type == "Islamic Iran")
                addOrNot = true

            if (addOrNot) {
                title += " ("
                if (holiday && afghanistanHolidays && iranHolidays) {
                    if (type == "Islamic Iran")
                        title += "ایران، "
                    else if (type == "Islamic Afghanistan")
                        title += "افغانستان، "
                }
                title += formatDayAndMonth(day, islamicMonths[month - 1]) + ")"

                var list: ArrayList<IslamicCalendarEvent>? =
                    islamicCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    islamicCalendarEvents.put(month * 100 + day, list)
                }
                val ev = IslamicCalendarEvent(IslamicDate(-1, month, day), title, holiday)
                list.add(ev)
                allEnabledEvents.add(ev)
            }
        }

        for (event in allTheEvents.getJSONArray("Gregorian Calendar")) {
            val month = event.getInt("month")
            val day = event.getInt("day")
            var title = event.getString("title")

            if (international) {
                title += " (" + formatDayAndMonth(day, gregorianMonths[month - 1]) + ")"

                var list: ArrayList<GregorianCalendarEvent>? =
                    gregorianCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    gregorianCalendarEvents.put(month * 100 + day, list)
                }
                val ev = GregorianCalendarEvent(CivilDate(-1, month, day), title, false)
                list.add(ev)
                allEnabledEvents.add(ev)
            }
        }

    } catch (ignore: JSONException) {
    }

    sPersianCalendarEvents = persianCalendarEvents
    sIslamicCalendarEvents = islamicCalendarEvents
    sGregorianCalendarEvents = gregorianCalendarEvents
    sAllEnabledEvents = allEnabledEvents
}

fun getAllEnabledAppointments(context: Context): List<DeviceCalendarEvent> {
    val startingDate = Calendar.getInstance()
    startingDate.add(Calendar.YEAR, -1)
    val deviceCalendarEvent = SparseArray<ArrayList<DeviceCalendarEvent>>()
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents(
        context, deviceCalendarEvent, allEnabledAppointments, startingDate,
        TimeUnit.DAYS.toMillis((365 * 2).toLong())
    )
    return allEnabledAppointments
}

private fun baseFormatClock(hour: Int, minute: Int): String =
    formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute))

fun getFormattedClock(clock: Clock, forceIn12: Boolean): String {
    val in12 = !clockIn24 || forceIn12
    if (!in12) return baseFormatClock(clock.hour, clock.minute)

    var hour = clock.hour
    val suffix: String
    if (hour >= 12) {
        suffix = getPmString()
        hour -= 12
    } else
        suffix = getAmString()

    return baseFormatClock(hour, clock.minute) + " " + suffix
}

fun calendarToCivilDate(calendar: Calendar): CivilDate {
    return CivilDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun makeCalendarFromDate(date: Date): Calendar {
    val calendar = Calendar.getInstance()
    if (isIranTime())
        calendar.timeZone = TimeZone.getTimeZone("Asia/Tehran")

    calendar.time = date
    return calendar
}

private fun readDeviceEvents(
    context: Context,
    deviceCalendarEvents: SparseArray<ArrayList<DeviceCalendarEvent>>,
    allEnabledAppointments: ArrayList<DeviceCalendarEvent>,
    startingDate: Calendar,
    rangeInMillis: Long
) {
    if (!isShowDeviceCalendarEvents()) return

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    try {
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startingDate.timeInMillis - DAY_IN_MILLIS)
        ContentUris.appendId(builder, startingDate.timeInMillis + rangeInMillis + DAY_IN_MILLIS)

        context.contentResolver.query(
            builder.build(),
            arrayOf(
                CalendarContract.Instances.EVENT_ID, // 0
                CalendarContract.Instances.TITLE, // 1
                CalendarContract.Instances.DESCRIPTION, // 2
                CalendarContract.Instances.BEGIN, // 3
                CalendarContract.Instances.END, // 4
                CalendarContract.Instances.VISIBLE, // 5
                CalendarContract.Instances.ALL_DAY, // 6
                CalendarContract.Instances.EVENT_COLOR // 7
            ), null, null, null
        )?.use {
            var i = 0
            while (it.moveToNext()) {
                if (it.getString(5) != "1")
                    continue

                val startDate = Date(it.getLong(3))
                val endDate = Date(it.getLong(4))
                val startCalendar = makeCalendarFromDate(startDate)
                val endCalendar = makeCalendarFromDate(endDate)

                val civilDate = calendarToCivilDate(startCalendar)

                val month = civilDate.month
                val day = civilDate.dayOfMonth

                var list: ArrayList<DeviceCalendarEvent>? =
                    deviceCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    deviceCalendarEvents.put(month * 100 + day, list)
                }

                var title = it.getString(1) ?: ""
                if (it.getString(6) == "1")
                    title = "\uD83D\uDCC5 $title"
                else {
                    title = "\uD83D\uDD53 $title"
                    title += " (" + baseFormatClock(
                        startCalendar.get(Calendar.HOUR_OF_DAY),
                        startCalendar.get(Calendar.MINUTE)
                    )

                    if (it.getLong(3) != it.getLong(4) && it.getLong(4) != 0L)
                        title += "-" + baseFormatClock(
                            endCalendar.get(Calendar.HOUR_OF_DAY),
                            endCalendar.get(Calendar.MINUTE)
                        )

                    title += ")"
                }
                val event = DeviceCalendarEvent(
                    id = it.getInt(0),
                    title = title,
                    description = it.getString(2) ?: "",
                    start = startDate,
                    end = endDate,
                    date = civilDate,
                    color = it.getString(7) ?: "",
                    isHoliday = false
                )
                list.add(event)
                allEnabledAppointments.add(event)

                // Don't go more than 1k events on any case
                if (++i == 1000) break
            }
        }
    } catch (e: Exception) {
        // We don't like crash addition from here, just catch all of exceptions
        Log.e("", "Error on device calendar events read", e)
    }

}

fun readDayDeviceEvents(context: Context, jdn: Long): SparseArray<ArrayList<DeviceCalendarEvent>> {
    val startingDate = civilDateToCalendar(CivilDate(if (jdn == -1L) getTodayJdn() else jdn))
    val deviceCalendarEvent = SparseArray<ArrayList<DeviceCalendarEvent>>()
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents(
        context,
        deviceCalendarEvent,
        allEnabledAppointments,
        startingDate,
        DAY_IN_MILLIS
    )
    return deviceCalendarEvent
}

fun readMonthDeviceEvents(
    context: Context,
    jdn: Long
): SparseArray<ArrayList<DeviceCalendarEvent>> {
    val startingDate = civilDateToCalendar(CivilDate(jdn))
    val deviceCalendarEvent = SparseArray<ArrayList<DeviceCalendarEvent>>()
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents(
        context,
        deviceCalendarEvent,
        allEnabledAppointments,
        startingDate,
        32L * DAY_IN_MILLIS
    )
    return deviceCalendarEvent
}

fun formatDeviceCalendarEventTitle(event: DeviceCalendarEvent): String {
    val desc = event.description
    var title = event.title
    if (desc.isNotEmpty())
        title += " (" + Html.fromHtml(event.description).toString().trim() + ")"

    return title.replace("\n", " ").trim()
}

fun getEventsTitle(
    dayEvents: List<CalendarEvent<*>>, holiday: Boolean,
    compact: Boolean, showDeviceCalendarEvents: Boolean,
    insertRLM: Boolean
): String {
    val titles = StringBuilder()
    var first = true

    for (event in dayEvents)
        if (event.isHoliday == holiday) {
            var title = event.title
            if (insertRLM)
                title = RLM + title

            if (event is DeviceCalendarEvent) {
                if (!showDeviceCalendarEvents)
                    continue

                if (!compact) {
                    title = formatDeviceCalendarEventTitle(event)
                }
            } else {
                if (compact)
                    title = title.replace("(.*) \\(.*?$".toRegex(), "$1")
            }

            if (first)
                first = false
            else
                titles.append("\n")
            titles.append(title)
        }

    return titles.toString()
}
