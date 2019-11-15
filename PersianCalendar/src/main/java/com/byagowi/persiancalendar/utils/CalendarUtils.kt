package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.text.Html
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import java.util.*
import kotlin.math.ceil

fun isWeekEnd(dayOfWeek: Int): Boolean = weekEnds[dayOfWeek]

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
    set(civilDate.year, civilDate.month - 1, civilDate.dayOfMonth)
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

    if (isShowWeekOfYearEnabled) {
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
        addAll(persianCalendarEvents.getEvents(PersianDate(jdn)))
        val islamic = IslamicDate(jdn)
        addAll(islamicCalendarEvents.getEvents(islamic))
        // Special case Islamic events happening in 30th day but the month has only 29 days
        if (islamic.dayOfMonth == 29 &&
            getMonthLength(CalendarType.ISLAMIC, islamic.year, islamic.month) == 29
        ) addAll(islamicCalendarEvents.getEvents(IslamicDate(islamic.year, islamic.month, 30)))
        val civil = CivilDate(jdn)
        addAll(deviceCalendarEvents.getDeviceEvents(civil)) // Passed by caller
        addAll(gregorianCalendarEvents.getEvents(civil))
    }

fun getIslamicOffset(context: Context): Int =
    PreferenceManager.getDefaultSharedPreferences(context)?.getString(
        PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET
    )?.replace("+", "")?.toIntOrNull() ?: 0

private fun baseFormatClock(hour: Int, minute: Int): String =
    formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute))

fun getFormattedClock(clock: Clock, forceIn12: Boolean): String {
    val in12 = !clockIn24 || forceIn12
    if (!in12) return baseFormatClock(clock.hour, clock.minute)

    var hour = clock.hour
    val suffix: String
    if (hour >= 12) {
        suffix = pmString
        hour -= 12
    } else suffix = amString

    return baseFormatClock(hour, clock.minute) + " " + suffix
}

fun calendarToCivilDate(calendar: Calendar) = CivilDate(
    calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH]
)

fun makeCalendarFromDate(date: Date): Calendar = Calendar.getInstance().apply {
    if (isIranTime)
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
    time = date
}

private fun readDeviceEvents(
    context: Context,
    startingDate: Calendar,
    rangeInMillis: Long
): List<DeviceCalendarEvent> = if (!isShowDeviceCalendarEvents ||
    ActivityCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALENDAR
    ) != PackageManager.PERMISSION_GRANTED
) emptyList() else try {
    context.contentResolver.query(
        CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, startingDate.timeInMillis - DAY_IN_MILLIS)
            ContentUris.appendId(this, startingDate.timeInMillis + rangeInMillis + DAY_IN_MILLIS)
        }.build(),
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
        var i = 0 // Don't go more than 1k events on any case
        generateSequence { if (it.moveToNext() && (++i) < 1000) it else null }.mapNotNull {
            if (it.getString(5) != "1") return@mapNotNull null
            val startDate = Date(it.getLong(3))
            val endDate = Date(it.getLong(4))
            val startCalendar = makeCalendarFromDate(startDate)
            val endCalendar = makeCalendarFromDate(endDate)
            fun Calendar.clock() = baseFormatClock(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
            DeviceCalendarEvent(
                id = it.getInt(0),
                title =
                if (it.getString(6) == "1") "\uD83D\uDCC5 ${it.getString(1) ?: ""}"
                else "\uD83D\uDD53 ${it.getString(1) ?: ""} (${startCalendar.clock()}${(
                        if (it.getLong(3) != it.getLong(4) && it.getLong(4) != 0L)
                            "-${endCalendar.clock()}"
                        else "")})",
                description = it.getString(2) ?: "",
                start = startDate,
                end = endDate,
                date = calendarToCivilDate(startCalendar),
                color = it.getString(7) ?: "",
                isHoliday = false
            )
        }.toList()
    } ?: emptyList()
} catch (e: Exception) {
    e.printStackTrace()
    emptyList<DeviceCalendarEvent>()
}

fun readDayDeviceEvents(ctx: Context, jdn: Long) = readDeviceEvents(
    ctx,
    civilDateToCalendar(CivilDate(if (jdn == -1L) getTodayJdn() else jdn)),
    DAY_IN_MILLIS
).toDeviceEventsStore()

fun readMonthDeviceEvents(ctx: Context, jdn: Long) = readDeviceEvents(
    ctx,
    civilDateToCalendar(CivilDate(jdn)),
    32L * DAY_IN_MILLIS
).toDeviceEventsStore()

fun getAllEnabledAppointments(ctx: Context) = readDeviceEvents(
    ctx,
    Calendar.getInstance().apply { add(Calendar.YEAR, -1) },
    365L * 2L * DAY_IN_MILLIS // all the events of previous and next year from today
)

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