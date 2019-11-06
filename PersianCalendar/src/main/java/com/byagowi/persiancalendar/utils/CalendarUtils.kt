package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.text.Html
import android.util.Log
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
var sPersianCalendarEvents: PersianCalendarEventsStore = emptyMap()
var sIslamicCalendarEvents: IslamicCalendarEventsStore = emptyMap()
var sGregorianCalendarEvents: GregorianCalendarEventsStore = emptyMap()

fun isWeekEnd(dayOfWeek: Int): Boolean = weekEnds[dayOfWeek]

fun hasAnyHolidays(dayEvents: List<CalendarEvent<*>>): Boolean = dayEvents.any { it.isHoliday }

fun fixDayOfWeek(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun fixDayOfWeekReverse(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

fun getWeekDayName(position: Int): String? = weekDays.let { it[position % 7] }

fun getDayOfWeekFromJdn(jdn: Long): Int =
    civilDateToCalendar(CivilDate(jdn))[Calendar.DAY_OF_WEEK] % 7

fun formatDayAndMonth(day: Int, month: String): String =
    String.format(if (language == LANG_CKB) "%sی %s" else "%s %s", formatNumber(day), month)

fun dayTitleSummary(date: AbstractDate): String =
    getWeekDayName(date) + spacedComma + formatDate(date)

fun civilDateToCalendar(civilDate: CivilDate): Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, civilDate.year)
    set(Calendar.MONTH, civilDate.month - 1)
    set(Calendar.DAY_OF_MONTH, civilDate.dayOfMonth)
}

fun getInitialOfWeekDay(position: Int): String = weekDaysInitials[position % 7]

fun getWeekDayName(date: AbstractDate): String = weekDays[civilDateToCalendar(
    if (date is CivilDate) date else CivilDate(date)
)[Calendar.DAY_OF_WEEK] % 7]

fun calculateWeekOfYear(jdn: Long, startOfYearJdn: Long): Int {
    val dayOfYear = jdn - startOfYearJdn
    return ceil(1 + (dayOfYear - fixDayOfWeekReverse(getDayOfWeekFromJdn(jdn))) / 7.0).toInt()
}

fun getMonthName(date: AbstractDate): String = monthsNamesOfCalendar(date)[date.month - 1]

fun monthsNamesOfCalendar(date: AbstractDate): List<String> = when (date) {
    is PersianDate -> persianMonths
    is IslamicDate -> islamicMonths
    else -> gregorianMonths
}

// Extra helpers
fun getA11yDaySummary(
    context: Context, jdn: Long, isToday: Boolean,
    deviceCalendarEvents: DeviceCalendarEventsStore,
    withZodiac: Boolean, withOtherCalendars: Boolean, withTitle: Boolean
): String {
    // It has some expensive calculations, lets not do that when not needed
    if (!isTalkBackEnabled) return ""

    val result = StringBuilder()

    if (isToday) {
        result.append(context.getString(R.string.today))
        result.append("\n")
    }

    val mainDate = getDateFromJdnOfCalendar(mainCalendar, jdn)

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
        val otherCalendars = dateStringOfOtherCalendars(jdn, spacedComma)
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

    if (showWeekOfYear) {
        val startOfYearJdn = getDateOfCalendar(
            mainCalendar,
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

fun getEvents(jdn: Long, deviceCalendarEvents: DeviceCalendarEventsStore): List<CalendarEvent<*>> =
    ArrayList<CalendarEvent<*>>().apply {
        addAll(sPersianCalendarEvents.getEvents(PersianDate(jdn)))
        val islamic = IslamicDate(jdn)
        addAll(sIslamicCalendarEvents.getEvents(islamic))
        // Special case Islamic events happening in 30th day but the month has only 29 days
        if (islamic.dayOfMonth == 29 &&
            getMonthLength(CalendarType.ISLAMIC, islamic.year, islamic.month) == 29
        ) addAll(sIslamicCalendarEvents.getEvents(IslamicDate(islamic.year, islamic.month, 30)))
        val civil = CivilDate(jdn)
        addAll(deviceCalendarEvents.getDeviceEvents(civil)) // Passed by caller
        addAll(sGregorianCalendarEvents.getEvents(civil))
    }

fun getIslamicOffset(context: Context): Int =
    PreferenceManager.getDefaultSharedPreferences(context)?.getString(
        PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET
    )?.replace("+", "")?.toIntOrNull() ?: 0

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

    val persianCalendarEvents = HashMap<Int, ArrayList<PersianCalendarEvent>>()
    val islamicCalendarEvents = HashMap<Int, ArrayList<IslamicCalendarEvent>>()
    val gregorianCalendarEvents = HashMap<Int, ArrayList<GregorianCalendarEvent>>()
    val allEnabledEvents = ArrayList<CalendarEvent<*>>()

    try {
        val allTheEvents = JSONObject(readRawResource(context, R.raw.events))

        // https://stackoverflow.com/a/36188796
        operator fun JSONArray.iterator(): Iterator<JSONObject> =
            (0 until length()).map { get(it) as JSONObject }.iterator()

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

                val ev = PersianCalendarEvent(PersianDate(year, month, day), title, holiday)
                persianCalendarEvents.addToStore(ev)
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

                val ev = IslamicCalendarEvent(IslamicDate(-1, month, day), title, holiday)
                islamicCalendarEvents.addToStore(ev)
                allEnabledEvents.add(ev)
            }
        }

        for (event in allTheEvents.getJSONArray("Gregorian Calendar")) {
            val month = event.getInt("month")
            val day = event.getInt("day")
            var title = event.getString("title")

            if (international) {
                title += " (" + formatDayAndMonth(day, gregorianMonths[month - 1]) + ")"

                val ev = GregorianCalendarEvent(CivilDate(-1, month, day), title, false)
                gregorianCalendarEvents.addToStore(ev)
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

private fun baseFormatClock(hour: Int, minute: Int): String =
    formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute))

fun getFormattedClock(clock: Clock, forceIn12: Boolean): String {
    val in12 = !clockIn24 || forceIn12
    if (!in12) return baseFormatClock(clock.hour, clock.minute)

    var hour = clock.hour
    val suffix: String
    if (hour >= 12) {
        suffix = sPM
        hour -= 12
    } else
        suffix = sAM

    return baseFormatClock(hour, clock.minute) + " " + suffix
}

fun calendarToCivilDate(calendar: Calendar) = CivilDate(
    calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH]
)

fun makeCalendarFromDate(date: Date): Calendar = Calendar.getInstance().apply {
    if (iranTime)
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
    time = date
}

private fun readDeviceEvents(
    context: Context,
    allEnabledAppointments: ArrayList<DeviceCalendarEvent>,
    startingDate: Calendar,
    rangeInMillis: Long
): DeviceCalendarEventsStore {
    if (!showDeviceCalendarEvents ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) != PackageManager.PERMISSION_GRANTED
    ) return emptyMap()

    val result = HashMap<Int, ArrayList<DeviceCalendarEvent>>()
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
                    date = calendarToCivilDate(startCalendar),
                    color = it.getString(7) ?: "",
                    isHoliday = false
                )
                result.addToStore(event)
                allEnabledAppointments.add(event)

                // Don't go more than 1k events on any case
                if (++i == 1000) break
            }
        }
    } catch (e: Exception) {
        // We don't like crash addition from here, just catch all of exceptions
        Log.e("", "Error on device calendar events read", e)
    }
    return result
}

fun readDayDeviceEvents(context: Context, jdn: Long) = readDeviceEvents(
    context,
    ArrayList(), // intentionally ignored result
    civilDateToCalendar(CivilDate(if (jdn == -1L) getTodayJdn() else jdn)),
    DAY_IN_MILLIS
)

fun readMonthDeviceEvents(context: Context, jdn: Long) = readDeviceEvents(
    context,
    ArrayList(), // intentionally ignored result
    civilDateToCalendar(CivilDate(jdn)),
    32L * DAY_IN_MILLIS
)

fun getAllEnabledAppointments(context: Context): List<DeviceCalendarEvent> {
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents( // ignore main result this time
        context,
        allEnabledAppointments,
        Calendar.getInstance().apply { add(Calendar.YEAR, -1) },
        365L * 2L * DAY_IN_MILLIS // all the events of previous and next year from today
    )
    return allEnabledAppointments
}

fun formatDeviceCalendarEventTitle(event: DeviceCalendarEvent): String =
    (event.title + if (event.description.isNotBlank())
        " (" + Html.fromHtml(event.description).toString().trim() + ")"
    else "").replace("\n", " ").trim()

fun getEventsTitle(
    dayEvents: List<CalendarEvent<*>>, holiday: Boolean, compact: Boolean,
    showDeviceCalendarEvents: Boolean, insertRLM: Boolean
) = dayEvents
    .filter { it.isHoliday == holiday && (it !is DeviceCalendarEvent || showDeviceCalendarEvents) }
    .map {
        when {
            it is DeviceCalendarEvent && !compact -> formatDeviceCalendarEventTitle(it)
            compact -> it.title.split(" (")[0]
            else -> it.title
        }
    }.joinToString("\n") { if (insertRLM) RLM + it else it }

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

fun getMonthLength(calendar: CalendarType, year: Int, month: Int): Int = (getDateOfCalendar(
    calendar, if (month == 12) year + 1 else year, if (month == 12) 1 else month + 1, 1
).toJdn() - getDateOfCalendar(
    calendar, year, month, 1
).toJdn()).toInt()