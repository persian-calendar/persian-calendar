package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.text.Html
import androidx.core.app.ActivityCompat
import com.byagowi.persiancalendar.LANG_CKB
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.entities.CalendarEvent
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import java.util.*
import kotlin.math.abs

fun isWeekEnd(dayOfWeek: Int) = weekEnds[dayOfWeek]

fun applyWeekStartOffsetToWeekDay(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

fun revertWeekStartOffsetFromWeekDay(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun getWeekDayName(position: Int) = weekDays[position % 7]

fun formatDayAndMonth(day: Int, month: String): String = when (language) {
    LANG_CKB -> "%sی %s"
    else -> "%s %s"
}.format(formatNumber(day), month)

fun dayTitleSummary(date: AbstractDate, calendarNameInLinear: Boolean = true): String =
    getWeekDayName(Jdn(date)) + spacedComma + formatDate(date, calendarNameInLinear)

fun CivilDate.toCalendar(): Calendar =
    Calendar.getInstance().apply { set(year, month - 1, dayOfMonth) }

fun getInitialOfWeekDay(position: Int) = weekDaysInitials[position % 7]

fun getWeekDayName(jdn: Jdn) = weekDays[jdn.dayOfWeek]

fun getMonthName(date: AbstractDate) = monthsNamesOfCalendar(date).getOrNull(date.month - 1) ?: ""

fun monthsNamesOfCalendar(date: AbstractDate): List<String> = when (date) {
    is PersianDate -> persianMonths
    is IslamicDate -> islamicMonths
    else -> gregorianMonths
}

// Generating text used in TalkBack / Voice Assistant
fun getA11yDaySummary(
    context: Context, jdn: Jdn, isToday: Boolean,
    deviceCalendarEvents: DeviceCalendarEventsStore,
    withZodiac: Boolean, withOtherCalendars: Boolean, withTitle: Boolean
): String {
    // It has some expensive calculations, lets not do that when not needed
    if (!isTalkBackEnabled) return ""

    val result = StringBuilder()

    if (isToday) {
        result.append(context.getString(R.string.today))
            .append("\n")
    }

    val mainDate = jdn.toCalendar(mainCalendar)

    if (withTitle) {
        result.append("\n")
            .append(dayTitleSummary(mainDate))
    }

    val shift = getShiftWorkTitle(jdn, false)
    if (shift.isNotEmpty()) {
        result.append("\n")
            .append(shift)
    }

    if (withOtherCalendars) {
        val otherCalendars = dateStringOfOtherCalendars(jdn, spacedComma)
        if (otherCalendars.isNotEmpty()) {
            result.append("\n\n")
                .append(context.getString(R.string.equivalent_to))
                .append(" ")
                .append(otherCalendars)
        }
    }

    val events = getEvents(jdn, deviceCalendarEvents)
    val holidays = getEventsTitle(
        events, true,
        compact = true,
        showDeviceCalendarEvents = true,
        insertRLM = false,
        addIsHoliday = false
    )
    if (holidays.isNotEmpty()) {
        result.append("\n\n")
            .append(context.getString(R.string.holiday_reason))
            .append("\n")
            .append(holidays)
    }

    val nonHolidays = getEventsTitle(
        events,
        holiday = false,
        compact = true,
        showDeviceCalendarEvents = true,
        insertRLM = false,
        addIsHoliday = false
    )
    if (nonHolidays.isNotEmpty()) {
        result.append("\n\n")
            .append(context.getString(R.string.events))
            .append("\n")
            .append(nonHolidays)
    }

    if (isShowWeekOfYearEnabled) {
        val startOfYearJdn = Jdn(mainCalendar, mainDate.year, 1, 1)
        val weekOfYearStart = jdn.getWeekOfYear(startOfYearJdn)
        result.append("\n\n")
            .append(
                context.getString(R.string.nth_week_of_year).format(formatNumber(weekOfYearStart))
            )
    }

    if (withZodiac) {
        val zodiac = getZodiacInfo(context, jdn, withEmoji = false, short = false)
        if (zodiac.isNotEmpty()) {
            result.append("\n\n").append(zodiac)
        }
    }

    return result.toString()
}

fun getEvents(jdn: Jdn, deviceCalendarEvents: DeviceCalendarEventsStore): List<CalendarEvent<*>> =
    ArrayList<CalendarEvent<*>>().apply {
        addAll(persianCalendarEvents.getEvents(jdn.toPersianCalendar()))
        val islamic = jdn.toIslamicCalendar()
        addAll(islamicCalendarEvents.getEvents(islamic))
        // Special case Islamic events happening in 30th day but the month has only 29 days
        if (islamic.dayOfMonth == 29 &&
            getMonthLength(CalendarType.ISLAMIC, islamic.year, islamic.month) == 29
        ) addAll(islamicCalendarEvents.getEvents(IslamicDate(islamic.year, islamic.month, 30)))
        val civil = jdn.toGregorianCalendar()
        addAll(deviceCalendarEvents.getEvents(civil)) // Passed by caller
        addAll(gregorianCalendarEvents.getEvents(civil))
    }

private fun baseFormatClock(hour: Int, minute: Int): String =
    formatNumber("%d:%02d".format(Locale.ENGLISH, hour, minute))

fun Clock.toFormattedString(forcedIn12: Boolean = false) =
    if (clockIn24 && !forcedIn12) baseFormatClock(hour, minute)
    else baseFormatClock((hour % 12).takeIf { it != 0 } ?: 12, minute) + " " +
            if (hour >= 12) pmString else amString

fun calendarToCivilDate(calendar: Calendar) = CivilDate(
    calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH]
)

fun makeCalendarFromDate(date: Date, forceLocalTime: Boolean = false): Calendar =
    Calendar.getInstance().apply {
        if (!forceLocalTime && isForcedIranTimeEnabled)
            timeZone = TimeZone.getTimeZone("Asia/Tehran")
        time = date
    }

private fun readDeviceEvents(
    context: Context,
    startingDate: Calendar,
    rangeInMillis: Long
): List<CalendarEvent.DeviceCalendarEvent> = if (!isShowDeviceCalendarEvents ||
    ActivityCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALENDAR
    ) != PackageManager.PERMISSION_GRANTED
) emptyList() else runCatching {
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
        generateSequence { if (it.moveToNext()) it else null }.filter {
            it.getString(5) == "1" // is visible
        }.map {
            val startDate = Date(it.getLong(3))
            val endDate = Date(it.getLong(4))
            val startCalendar = makeCalendarFromDate(startDate)
            val endCalendar = makeCalendarFromDate(endDate)
            fun Calendar.clock() = baseFormatClock(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
            CalendarEvent.DeviceCalendarEvent(
                id = it.getInt(0),
                title =
                if (it.getString(6) == "1") "\uD83D\uDCC5 ${it.getString(1) ?: ""}"
                else "\uD83D\uDD53 ${it.getString(1) ?: ""} (${startCalendar.clock()}${
                    (if (it.getLong(3) != it.getLong(4) && it.getLong(4) != 0L)
                        "-${endCalendar.clock()}"
                    else "")
                })",
                description = it.getString(2) ?: "",
                start = startDate,
                end = endDate,
                date = calendarToCivilDate(startCalendar),
                color = it.getString(7) ?: "",
                isHoliday = false
            )
        }.take(1000 /* let's put some limitation */).toList()
    }
}.onFailure(logException).getOrNull() ?: emptyList()

fun readDayDeviceEvents(ctx: Context, jdn: Jdn) = readDeviceEvents(
    ctx, CivilDate(jdn.value.takeIf { it != -1L } ?: Jdn.today.value).toCalendar(), DAY_IN_MILLIS
).toEventsStore()

fun readMonthDeviceEvents(ctx: Context, jdn: Jdn) = readDeviceEvents(
    ctx, jdn.toGregorianCalendar().toCalendar(), 32L * DAY_IN_MILLIS
).toEventsStore()

fun getAllEnabledAppointments(ctx: Context) = readDeviceEvents(
    ctx, Calendar.getInstance().apply { add(Calendar.YEAR, -1) },
    365L * 2L * DAY_IN_MILLIS // all the events of previous and next year from today
)

fun formatDeviceCalendarEventTitle(event: CalendarEvent.DeviceCalendarEvent): String =
    (event.title + if (event.description.isNotBlank())
        " (" + Html.fromHtml(event.description).toString().trim() + ")"
    else "").replace("\n", " ").trim()

// Move this to strings or somewhere
fun addIsHoliday(title: String) = "$title (تعطیل)"

fun getEventsTitle(
    dayEvents: List<CalendarEvent<*>>, holiday: Boolean, compact: Boolean,
    showDeviceCalendarEvents: Boolean, insertRLM: Boolean, addIsHoliday: Boolean
) = dayEvents
    .filter { it.isHoliday == holiday && (it !is CalendarEvent.DeviceCalendarEvent || showDeviceCalendarEvents) }
    .map {
        val title = when {
            it is CalendarEvent.DeviceCalendarEvent && !compact -> formatDeviceCalendarEventTitle(it)
            compact -> it.title.replace(Regex(" \\([^)]+\\)$"), "")
            else -> it.title
        }

        if (addIsHoliday && it.isHoliday)
            addIsHoliday(title)
        else
            title
    }
    .joinToString("\n") { if (insertRLM) RLM + it else it }

fun getCalendarTypeFromDate(date: AbstractDate): CalendarType = when (date) {
    is IslamicDate -> CalendarType.ISLAMIC
    is CivilDate -> CalendarType.GREGORIAN
    else -> CalendarType.SHAMSI
}

fun getMonthLength(calendar: CalendarType, year: Int, month: Int): Int {
    val nextMonthYear = if (month == 12) year + 1 else year
    val nextMonthMonth = if (month == 12) 1 else month + 1
    val nextMonthStartingDay = Jdn(calendar, nextMonthYear, nextMonthMonth, 1)
    val thisMonthStartingDay = Jdn(calendar, year, month, 1)
    return nextMonthStartingDay - thisMonthStartingDay
}

fun calculateDaysDifference(jdn: Jdn, messageToFormat: String): String {
    val selectedDayAbsoluteDistance = abs(Jdn.today - jdn)
    val civilBase = CivilDate(2000, 1, 1)
    val civilOffset = CivilDate(civilBase.toJdn() + selectedDayAbsoluteDistance)
    val yearDiff = civilOffset.year - 2000
    val monthDiff = civilOffset.month - 1
    val dayOfMonthDiff = civilOffset.dayOfMonth - 1

    val result = messageToFormat.format(
        formatNumber(selectedDayAbsoluteDistance.toInt()),
        formatNumber(yearDiff),
        formatNumber(monthDiff),
        formatNumber(dayOfMonthDiff)
    )
    return if (selectedDayAbsoluteDistance <= 31) result.split(" (")[0] else result
}
