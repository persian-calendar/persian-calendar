package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.provider.CalendarContract
import androidx.annotation.PluralsRes
import androidx.core.app.ActivityCompat
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.entities.everyYear
import com.byagowi.persiancalendar.global.calendarsTitlesAbbr
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.eventCalendarsIdsAsHoliday
import com.byagowi.persiancalendar.global.eventCalendarsIdsToExclude
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.numericalDatePreferred
import com.byagowi.persiancalendar.global.showMoonInScorpio
import com.byagowi.persiancalendar.global.spacedAndInDates
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.spacedOr
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.global.yearMonthNameOfDate
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

val supportedYearOfIranCalendar: Int get() = IranianIslamicDateConverter.latestSupportedYearOfIran

fun dayTitleSummary(jdn: Jdn, date: AbstractDate, calendarNameInLinear: Boolean = true): String =
    jdn.weekDay.title + spacedComma + formatDate(date, calendarNameInLinear)

val AbstractDate.monthName get() = yearMonthNameOfDate(this).getOrNull(month - 1).orEmpty()

// Generating text used in TalkBack / Voice Assistant
fun getA11yDaySummary(
    resources: Resources,
    jdn: Jdn,
    isToday: Boolean,
    deviceCalendarEvents: DeviceCalendarEventsStore,
    withZodiac: Boolean,
    withOtherCalendars: Boolean,
    withTitle: Boolean,
    withWeekOfYear: Boolean = isShowWeekOfYearEnabled.value,
): String = buildString {
    // It has some expensive calculations, lets not do that when not needed
    if (!isTalkBackEnabled.value) return@buildString

    if (isToday) appendLine(resources.getString(R.string.today))

    val mainDate = jdn on mainCalendar

    if (withTitle) appendLine().append(dayTitleSummary(jdn, mainDate))

    val shift = getShiftWorkTitle(jdn)
    if (shift != null) appendLine().append(shift)

    if (withOtherCalendars) {
        val otherCalendars = dateStringOfOtherCalendars(jdn, spacedComma)
        if (otherCalendars.isNotEmpty()) {
            appendLine().appendLine().append(resources.getString(R.string.equivalent_to))
                .append(" ").append(otherCalendars)
        }
    }

    val events = eventsRepository.value.getEvents(jdn, deviceCalendarEvents)
    val holidays = getEventsTitle(
        events,
        true,
        showDeviceCalendarEvents = true,
        insertRLM = false,
        addIsHoliday = false,
    )
    if (holidays.isNotEmpty()) {
        appendLine().appendLine().appendLine(resources.getString(R.string.holiday_reason, holidays))
    }

    val nonHolidays = getEventsTitle(
        events,
        false,
        showDeviceCalendarEvents = true,
        insertRLM = false,
        addIsHoliday = false,
    )
    if (nonHolidays.isNotEmpty()) {
        appendLine().appendLine().appendLine(resources.getString(R.string.events))
            .append(nonHolidays)
    }

    if (withWeekOfYear) {
        val startOfYearJdn = Jdn(mainCalendar, mainDate.year, 1, 1)
        val weekOfYearStart = jdn.getWeekOfYear(startOfYearJdn, weekStart.value)
        appendLine().appendLine().append(
            resources.getString(
                R.string.nth_week_of_year, numeral.value.format(weekOfYearStart)
            )
        )
    }

    if (withZodiac && showMoonInScorpio.value) {
        appendLine().appendLine().appendLine(generateYearName(resources, jdn, withEmoji = false))
        if (isMoonInScorpio(jdn)) append(resources.getString(R.string.moon_in_scorpio))
    }
}

// Before 1304 different month names and lengths, and different animal year names were in use
// Every year events are excepted, not clean but unlikely be noticed
val PersianDate.isOldEra get() = year < 1304 && year != everyYear

fun GregorianCalendar.toCivilDate(): CivilDate {
    return CivilDate(
        this[GregorianCalendar.YEAR],
        this[GregorianCalendar.MONTH] + 1,
        this[GregorianCalendar.DAY_OF_MONTH]
    )
}

fun Date.toGregorianCalendar(forceLocalTime: Boolean = false): GregorianCalendar {
    val calendar = GregorianCalendar()
    if (!forceLocalTime && isForcedIranTimeEnabled.value) calendar.timeZone =
        TimeZone.getTimeZone(IRAN_TIMEZONE_ID)
    calendar.time = this
    return calendar
}

fun GregorianCalendar.formatDateAndTime(withWeekDay: Boolean = false): String {
    val jdn = Jdn(this.toCivilDate())
    val weekDayName = if (withWeekDay) jdn.weekDay.title + spacedComma else ""
    return language.value.timeAndDateFormat.format(
        Clock(this).toFormattedString(),
        weekDayName + formatDate(jdn on mainCalendar, forceNonNumerical = true)
    )
}

// Google Meet generates weird and ugly descriptions with lines having such patterns, let's get rid of them
private val descriptionCleaningPattern = Regex("^-::~[:~]+:-$", RegexOption.MULTILINE)

private fun readDeviceEvents(
    context: Context,
    startingDate: GregorianCalendar,
    duration: Duration,
    limit: Int,
    searchTerm: String? = null,
): List<CalendarEvent.DeviceCalendarEvent> {
    if (!isShowDeviceCalendarEvents.value || ActivityCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) != PackageManager.PERMISSION_GRANTED
    ) return emptyList()

    val selection: String?
    val selectionArgs: Array<String>?
    val boundaryRegex: Regex?
    if (!searchTerm.isNullOrBlank()) {
        selection =
            "(${CalendarContract.Instances.TITLE} LIKE ? OR " + "${CalendarContract.Instances.DESCRIPTION} LIKE ?)"
        selectionArgs = arrayOf("%$searchTerm%", "%$searchTerm%")
        boundaryRegex = createSearchRegex(searchTerm)
    } else {
        selection = null
        selectionArgs = null
        boundaryRegex = null
    }

    return runCatching {
        context.contentResolver.query(
            CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
                ContentUris.appendId(this, startingDate.timeInMillis - 1.days.inWholeMilliseconds)
                ContentUris.appendId(
                    this, startingDate.timeInMillis + (duration + 1.days).inWholeMilliseconds
                )
            }.build(),
            arrayOf(
                CalendarContract.Instances.EVENT_ID, // 0
                CalendarContract.Instances.TITLE, // 1
                CalendarContract.Instances.DESCRIPTION, // 2
                CalendarContract.Instances.BEGIN, // 3
                CalendarContract.Instances.END, // 4
                CalendarContract.Instances.VISIBLE, // 5
                CalendarContract.Instances.ALL_DAY, // 6
                CalendarContract.Instances.EVENT_COLOR, // 7
                CalendarContract.Instances.DISPLAY_COLOR, // 8
                CalendarContract.Instances.CALENDAR_ID, // 9
            ),
            selection,
            selectionArgs,
            CalendarContract.Instances.BEGIN + " ASC LIMIT ${limit * 3}",
        )?.use {
            generateSequence { if (it.moveToNext()) it else null }.filter {
                it.getString(5) == "1" && // is visible
                        it.getLong(9) !in eventCalendarsIdsToExclude.value && run {
                    boundaryRegex == null || boundaryRegex.containsMatchIn(
                        it.getString(1).orEmpty()
                    ) || boundaryRegex.containsMatchIn(it.getString(2).orEmpty())
                }
            }.map {
                val start = Date(it.getLong(3)).toGregorianCalendar()
                val end = Date(it.getLong(4)).toGregorianCalendar()
                CalendarEvent.DeviceCalendarEvent(
                    id = it.getLong(0),
                    title = it.getString(1),
                    time = if (it.getString(6/*ALL_DAY*/) == "1") null else Clock(start).toBasicFormatString() + (if (it.getLong(
                            3
                        ) != it.getLong(4) && it.getLong(4) != 0L
                    ) " $EN_DASH ${Clock(end).toBasicFormatString()}"
                    else ""),
                    start = start,
                    end = end,
                    description = it.getString(2)?.replace(descriptionCleaningPattern, "")
                        .orEmpty(),
                    date = start.toCivilDate(),
                    color = it.getString(7) ?: it.getString(8).orEmpty(),
                    isHoliday = it.getLong(9) in eventCalendarsIdsAsHoliday.value,
                    source = null,
                )
            }.take(limit).toList()
        }
    }.onFailure(logException).getOrNull() ?: emptyList()
}

fun createSearchRegex(searchTerm: String): Regex =
    Regex("\\b" + Regex.escape(searchTerm), RegexOption.IGNORE_CASE)

fun Context.readDaysDeviceEvents(
    jdn: Jdn,
    duration: Duration,
    limit: Int = 100,
) = DeviceCalendarEventsStore(
    readDeviceEvents(
        this,
        jdn.toGregorianCalendar(),
        duration,
        limit = limit,
    )
)

fun Context.readDayDeviceEvents(jdn: Jdn) = readDaysDeviceEvents(jdn, 1.days, 20)
fun Context.readWeekDeviceEvents(jdn: Jdn) = readDaysDeviceEvents(jdn, 7.days)
fun Context.readTwoWeekDeviceEvents(jdn: Jdn) = readDaysDeviceEvents(jdn, 14.days)
fun Context.readMonthDeviceEvents(jdn: Jdn) = readDaysDeviceEvents(jdn, 32.days)
fun Context.readYearDeviceEvents(jdn: Jdn) = readDaysDeviceEvents(jdn, 366.days)

fun createMonthEventsList(context: Context, date: AbstractDate): Map<Jdn, List<CalendarEvent<*>>> {
    val baseJdn = Jdn(date)
    val deviceEvents = context.readMonthDeviceEvents(baseJdn)
    return (0..<mainCalendar.getMonthLength(date.year, date.month)).map { baseJdn + it }
        .associateWith { eventsRepository.value.getEvents(it, deviceEvents) }
}

fun Context.searchDeviceCalendarEvents(searchTerm: String?): List<CalendarEvent.DeviceCalendarEvent> {
    val now = System.currentTimeMillis()
    val startDate = GregorianCalendar().apply { add(GregorianCalendar.YEAR, -1) }
    val duration = (365L * 2L).days // 1 year backwards + 1 year forward
    val events = readDeviceEvents(
        context = this,
        startingDate = startDate,
        duration = duration,
        searchTerm = searchTerm,
        limit = 100,
    )
    val (upcoming, past) = events.partition { it.start.timeInMillis >= now }
    val sortedUpcoming = upcoming.sortedBy { it.start.timeInMillis }
    val sortedPast = past.sortedByDescending { it.start.timeInMillis }
    return (sortedUpcoming + sortedPast).take(100)
}

fun getEventsTitle(
    dayEvents: List<CalendarEvent<*>>,
    holiday: Boolean,
    showDeviceCalendarEvents: Boolean,
    insertRLM: Boolean,
    addIsHoliday: Boolean
) =
    dayEvents.filter { it.isHoliday == holiday && (it !is CalendarEvent.DeviceCalendarEvent || showDeviceCalendarEvents) }
        .map {
            val title = when {
                it is CalendarEvent.DeviceCalendarEvent -> it.oneLinerTitleWithTime
                else -> it.title
            }

            if (addIsHoliday && it.isHoliday) "$title ($holidayString)" else title
        }.joinToString("\n") { if (insertRLM) RLM + it else it }

val AbstractDate.calendar: Calendar
    get() = when (this) {
        is IslamicDate -> Calendar.ISLAMIC
        is CivilDate -> Calendar.GREGORIAN
        is NepaliDate -> Calendar.NEPALI
        else -> Calendar.SHAMSI
    }

fun calculateDatePartsDifference(
    fromDate: AbstractDate, toDate: AbstractDate, calendar: Calendar
): Triple<Int, Int, Int> {
    var y = toDate.year - fromDate.year
    var m = toDate.month - fromDate.month
    var d = toDate.dayOfMonth - fromDate.dayOfMonth
    if (d < 0) {
        m--
        d += calendar.getMonthLength(fromDate.year, fromDate.month)
    }
    if (m < 0) {
        y--
        m += calendar.getYearMonths(fromDate.year)
    }
    return Triple(y, m, d)
}

fun calculateDaysDifference(
    resources: Resources,
    jdn: Jdn,
    baseJdn: Jdn,
    calendar: Calendar = mainCalendar,
    isInWidget: Boolean = false
): String {
    val baseDate = baseJdn on calendar
    val date = jdn on calendar
    val (years, months, daysOfMonth) = calculateDatePartsDifference(
        if (baseJdn > jdn) date else baseDate, if (baseJdn > jdn) baseDate else date, calendar
    )
    val days = abs(baseJdn - jdn)
    val daysString = resources.getQuantityString(R.plurals.days, days, numeral.value.format(days))
    val weeks = if (isInWidget || days < 7) 0 else (days / 7.0).roundToInt()
    val result = listOfNotNull(
        if (months == 0 && years == 0) null
        else listOf(
            R.plurals.years to years, R.plurals.months to months, R.plurals.days to daysOfMonth
        ).filter { (_, n) -> n != 0 }.joinToString(spacedAndInDates) { (@PluralsRes pluralId, n) ->
            resources.getQuantityString(pluralId, n, numeral.value.format(n))
        },
        if (weeks == 0) null
        else (if (days % 7 == 0) "" else "~") + resources.getQuantityString(
            R.plurals.weeks,
            weeks,
            numeral.value.format(weeks),
        ),
        run {
            if (years != 0 || isInWidget) return@run null
            val workDays = eventsRepository.value.calculateWorkDays(
                if (baseJdn > jdn) jdn else baseJdn,
                if (baseJdn > jdn) baseJdn else jdn,
            )
            if (workDays == days || workDays == 0) return@run null
            resources.getQuantityString(
                R.plurals.work_days, workDays, numeral.value.format(workDays)
            )
        },
    )
    if (result.isEmpty()) return daysString
    return language.value.inParentheses.format(daysString, result.joinToString(spacedOr))
}

fun formatDate(
    date: AbstractDate,
    calendarNameInLinear: Boolean = true,
    forceNonNumerical: Boolean = false,
): String {
    return if (numericalDatePreferred.value && !forceNonNumerical) buildString {
        append(date.toLinearDate())
        if (calendarNameInLinear) {
            append(" ")
            append(calendarsTitlesAbbr.value[date.calendar].orEmpty())
        }
    }.trim() else language.value.dmy.format(
        numeral.value.format(date.dayOfMonth),
        date.monthName,
        numeral.value.format(date.year),
    )
}

fun AbstractDate.toLinearDate(numeral: Numeral = com.byagowi.persiancalendar.global.numeral.value) =
    language.value.allNumericsDateFormat(year, month, dayOfMonth, numeral)

fun monthFormatForSecondaryCalendar(
    date: AbstractDate,
    secondaryCalendar: Calendar,
    spaced: Boolean = false,
): String {
    val mainCalendar = date.calendar
    val from = Jdn(
        mainCalendar.createDate(date.year, date.month, 1)
    ) on secondaryCalendar
    val to = Jdn(
        mainCalendar.createDate(
            date.year, date.month, date.calendar.getMonthLength(date.year, date.month)
        )
    ) on secondaryCalendar
    val separator = if (spaced) " $EN_DASH " else EN_DASH
    return when {
        from.month == to.month -> language.value.my.format(
            from.monthName, numeral.value.format(from.year)
        )

        from.year != to.year -> listOf(
            from.year to from.month..secondaryCalendar.getYearMonths(from.year),
            to.year to 1..to.month
        ).joinToString(separator) { (year, months) ->
            language.value.my.format(months.joinToString(separator) { month ->
                from.calendar.createDate(year, month, 1).monthName
            }, numeral.value.format(year))
        }

        else -> language.value.my.format(
            (from.month..to.month).joinToString(separator) { month ->
                from.calendar.createDate(from.year, month, 1).monthName
            }, numeral.value.format(from.year)
        )
    }
}

fun getSecondaryCalendarNumeral(secondaryCalendar: Calendar?) = when {
    !language.value.canHaveLocalNumeral -> Numeral.ARABIC
    numeral.value.isArabic -> Numeral.ARABIC
    else -> secondaryCalendar?.preferredNumeral ?: Numeral.ARABIC
}

fun otherCalendarFormat(
    yearViewYear: Int, calendar: Calendar, otherCalendar: Calendar
): String {
    val startOfYear = (Jdn(calendar.createDate(yearViewYear, 1, 1)) on otherCalendar).year
    val endOfYear = ((Jdn(
        calendar.createDate(yearViewYear + 1, 1, 1)
    ) - 1) on otherCalendar).year
    return listOf(startOfYear, endOfYear).distinct().joinToString(EN_DASH) {
        numeral.value.format(it)
    }
}

fun dateStringOfOtherCalendars(jdn: Jdn, separator: String) =
    enabledCalendars.drop(1).joinToString(separator) { formatDate(jdn on it) }
