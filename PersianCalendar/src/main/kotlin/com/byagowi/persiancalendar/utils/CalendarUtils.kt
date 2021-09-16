package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.praytimes.Clock
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

val supportedYearOfIranCalendar: Int get() = IranianIslamicDateConverter.latestSupportedYearOfIran

fun isWeekEnd(dayOfWeek: Int) = weekEnds[dayOfWeek]

fun applyWeekStartOffsetToWeekDay(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

fun revertWeekStartOffsetFromWeekDay(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun getWeekDayName(position: Int) = weekDays[position % 7]

fun dayTitleSummary(jdn: Jdn, date: AbstractDate, calendarNameInLinear: Boolean = true): String =
    jdn.dayOfWeekName + spacedComma + formatDate(date, calendarNameInLinear)

fun getInitialOfWeekDay(position: Int) = weekDaysInitials[position % 7]

// 1 means Saturday on it and 7 means Friday
fun CalendarType.getLastWeekDayOfMonth(year: Int, month: Int, dayOfWeek: Int): Int {
    val monthLength = this.getMonthLength(year, month)
    val endOfMonthJdn = Jdn(this, year, month, monthLength)
    return monthLength - ((endOfMonthJdn.value - dayOfWeek + 3L) % 7).toInt()
}

val AbstractDate.monthName get() = this.calendarType.monthsNames.getOrNull(month - 1) ?: ""

val CalendarType.monthsNames: List<String>
    get() = when (this) {
        CalendarType.SHAMSI -> persianMonths
        CalendarType.ISLAMIC -> islamicMonths
        CalendarType.GREGORIAN -> gregorianMonths
    }

// Generating text used in TalkBack / Voice Assistant
fun getA11yDaySummary(
    context: Context, jdn: Jdn, isToday: Boolean, deviceCalendarEvents: DeviceCalendarEventsStore,
    withZodiac: Boolean, withOtherCalendars: Boolean, withTitle: Boolean
): String = buildString {
    // It has some expensive calculations, lets not do that when not needed
    if (!isTalkBackEnabled) return@buildString

    if (isToday) appendLine(context.getString(R.string.today))

    val mainDate = jdn.toCalendar(mainCalendar)

    if (withTitle) appendLine().append(dayTitleSummary(jdn, mainDate))

    val shift = getShiftWorkTitle(jdn, false)
    if (shift.isNotEmpty()) appendLine().append(shift)

    if (withOtherCalendars) {
        val otherCalendars = dateStringOfOtherCalendars(jdn, spacedComma)
        if (otherCalendars.isNotEmpty()) {
            appendLine().appendLine()
                .append(context.getString(R.string.equivalent_to))
                .append(" ")
                .append(otherCalendars)
        }
    }

    val events = getEvents(jdn, deviceCalendarEvents)
    val holidays = getEventsTitle(
        events, true,
        compact = true, showDeviceCalendarEvents = true, insertRLM = false, addIsHoliday = false
    )
    if (holidays.isNotEmpty()) {
        appendLine().appendLine()
            .appendLine(context.getString(R.string.holiday_reason, holidays))
    }

    val nonHolidays = getEventsTitle(
        events, false,
        compact = true, showDeviceCalendarEvents = true, insertRLM = false, addIsHoliday = false
    )
    if (nonHolidays.isNotEmpty()) {
        appendLine().appendLine()
            .appendLine(context.getString(R.string.events))
            .append(nonHolidays)
    }

    if (isShowWeekOfYearEnabled) {
        val startOfYearJdn = Jdn(mainCalendar, mainDate.year, 1, 1)
        val weekOfYearStart = jdn.getWeekOfYear(startOfYearJdn)
        appendLine().appendLine()
            .append(context.getString(R.string.nth_week_of_year, formatNumber(weekOfYearStart)))
    }

    if (withZodiac) {
        val zodiac = getZodiacInfo(context, jdn, withEmoji = false, short = false)
        if (zodiac.isNotEmpty()) appendLine().appendLine().append(zodiac)
    }
}

private fun baseFormatClock(hour: Int, minute: Int): String =
    formatNumber("%d:%02d".format(Locale.ENGLISH, hour, minute))

fun Clock.toFormattedString(forcedIn12: Boolean = false) =
    if (clockIn24 && !forcedIn12) baseFormatClock(hour, minute)
    else baseFormatClock((hour % 12).takeIf { it != 0 } ?: 12, minute) + " " +
            if (hour >= 12) pmString else amString

fun Clock.asRemainingTime(context: Context, short: Boolean = false): String {
    val pairs = listOf(R.string.n_hours to hour, R.string.n_minutes to minute)
        .filter { (_, n) -> n != 0 }
    if (pairs.size == 2 && short) // if both present special casing the short form makes sense
        return context.getString(R.string.n_hours_minutes, formatNumber(hour), formatNumber(minute))
    return pairs.joinToString(spacedAnd) { (stringId, n) ->
        context.getString(stringId, formatNumber(n))
    }
}

fun Calendar.toCivilDate() =
    CivilDate(this[Calendar.YEAR], this[Calendar.MONTH] + 1, this[Calendar.DAY_OF_MONTH])

fun Date.toJavaCalendar(forceLocalTime: Boolean = false): Calendar = Calendar.getInstance().also {
    if (!forceLocalTime && isForcedIranTimeEnabled)
        it.timeZone = TimeZone.getTimeZone("Asia/Tehran")
    it.time = this
}

fun Jdn.toJavaCalendar(): Calendar = Calendar.getInstance().also {
    val gregorian = this.toGregorianCalendar()
    it.set(gregorian.year, gregorian.month - 1, gregorian.dayOfMonth)
}

// Google Meet generates weird and ugly descriptions with lines having such patterns, let's get rid of them
private val descriptionCleaningPattern = Regex("^-::~[:~]+:-$", RegexOption.MULTILINE)

private fun readDeviceEvents(
    context: Context, startingDate: Calendar, rangeInMillis: Long
): List<CalendarEvent.DeviceCalendarEvent> = if (!isShowDeviceCalendarEvents ||
    ActivityCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALENDAR
    ) != PackageManager.PERMISSION_GRANTED
) emptyList() else runCatching {
    context.contentResolver.query(
        CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, startingDate.timeInMillis - DAY_IN_MILLIS)
            ContentUris.appendId(this, startingDate.timeInMillis + rangeInMillis + DAY_IN_MILLIS)
        }.build(), arrayOf(
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
            val startCalendar = startDate.toJavaCalendar()
            val endCalendar = endDate.toJavaCalendar()
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
                description = it.getString(2)?.replace(descriptionCleaningPattern, "") ?: "",
                start = startDate,
                end = endDate,
                date = startCalendar.toCivilDate(),
                color = it.getString(7) ?: "",
                isHoliday = false
            )
        }.take(1000 /* let's put some limitation */).toList()
    }
}.onFailure(logException).getOrNull() ?: emptyList()

fun Context.readDayDeviceEvents(jdn: Jdn) =
    DeviceCalendarEventsStore(readDeviceEvents(this, jdn.toJavaCalendar(), DAY_IN_MILLIS))

fun Context.readMonthDeviceEvents(jdn: Jdn) =
    DeviceCalendarEventsStore(readDeviceEvents(this, jdn.toJavaCalendar(), 32L * DAY_IN_MILLIS))

fun Context.getAllEnabledAppointments() = readDeviceEvents(
    this, Calendar.getInstance().apply { add(Calendar.YEAR, -1) },
    365L * 2L * DAY_IN_MILLIS // all the events of previous and next year from today
)

fun CalendarEvent.DeviceCalendarEvent.formatTitle(): String =
    (title + if (description.isNotBlank())
        " (${HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()})"
    else "").replace("\n", " ").trim()


fun getEventsTitle(
    dayEvents: List<CalendarEvent<*>>, holiday: Boolean, compact: Boolean,
    showDeviceCalendarEvents: Boolean, insertRLM: Boolean, addIsHoliday: Boolean
) = dayEvents
    .filter { it.isHoliday == holiday && (it !is CalendarEvent.DeviceCalendarEvent || showDeviceCalendarEvents) }
    .map {
        val title = when {
            it is CalendarEvent.DeviceCalendarEvent && !compact -> it.formatTitle()
            compact -> it.title.replace(Regex(" \\([^)]+\\)$"), "")
            else -> it.title
        }

        if (addIsHoliday && it.isHoliday) "$title ($holidayString)" else title
    }
    .joinToString("\n") { if (insertRLM) RLM + it else it }

val AbstractDate.calendarType: CalendarType
    get() = when (this) {
        is IslamicDate -> CalendarType.ISLAMIC
        is CivilDate -> CalendarType.GREGORIAN
        else -> CalendarType.SHAMSI
    }

fun CalendarType.getMonthLength(year: Int, month: Int): Int {
    val nextMonthYear = if (month == 12) year + 1 else year
    val nextMonthMonth = if (month == 12) 1 else month + 1
    val nextMonthStartingDay = Jdn(this, nextMonthYear, nextMonthMonth, 1)
    val thisMonthStartingDay = Jdn(this, year, month, 1)
    return nextMonthStartingDay - thisMonthStartingDay
}

fun calculateDaysDifference(resources: Resources, jdn: Jdn): String {
    val daysAbsoluteDistance = abs(Jdn.today - jdn)
    val civilBase = CivilDate(2000, 1, 1)
    val civilOffset = CivilDate(civilBase.toJdn() + daysAbsoluteDistance)
    val yearsDifference = civilOffset.year - civilBase.year
    val monthsDifference = civilOffset.month - civilBase.month
    val daysOfMonthDifference = civilOffset.dayOfMonth - civilBase.dayOfMonth
    val days = resources.getString(R.string.n_days, formatNumber(daysAbsoluteDistance))
    return if (monthsDifference == 0 && yearsDifference == 0) days else ("$days (~" + listOf(
        R.string.n_years to yearsDifference,
        R.string.n_months to monthsDifference,
        R.string.n_days to daysOfMonthDifference
    ).filter { (_, n) -> n != 0 }.joinToString(spacedAnd) { (stringId, n) ->
        resources.getString(stringId, formatNumber(n))
    } + ")")
}

fun Jdn.getWeekOfYear(startOfYear: Jdn): Int {
    val dayOfYear = this - startOfYear
    return ceil(1 + (dayOfYear - applyWeekStartOffsetToWeekDay(this.dayOfWeek)) / 7.0).toInt()
}

val Jdn.dayOfWeekName: String get() = weekDays[this.dayOfWeek]

fun getEvents(jdn: Jdn, deviceEvents: DeviceCalendarEventsStore) = listOf(
    persianCalendarEvents.getEvents(jdn.toPersianCalendar()),
    islamicCalendarEvents.getEvents(jdn.toIslamicCalendar()),
    gregorianCalendarEvents.getEvents(jdn.toGregorianCalendar(), deviceEvents)
).flatten()

fun formatDate(
    date: AbstractDate, calendarNameInLinear: Boolean = true, forceNonNumerical: Boolean = false
): String = if (numericalDatePreferred && !forceNonNumerical)
    (toLinearDate(date) + if (calendarNameInLinear) (" " + getCalendarNameAbbr(date)) else "").trim()
else language.dmy.format(formatNumber(date.dayOfMonth), date.monthName, formatNumber(date.year))

fun toLinearDate(date: AbstractDate): String = "%s/%s/%s".format(
    formatNumber(date.year), formatNumber(date.month), formatNumber(date.dayOfMonth)
)

private fun getCalendarNameAbbr(date: AbstractDate) =
    calendarTypesTitleAbbr.getOrNull(date.calendarType.ordinal) ?: ""

fun dateStringOfOtherCalendars(jdn: Jdn, separator: String) =
    otherCalendars.joinToString(separator) { formatDate(jdn.toCalendar(it)) }

fun CivilDate.getSpringEquinox() = Equinox.northwardEquinox(this.year).toJavaCalendar()
