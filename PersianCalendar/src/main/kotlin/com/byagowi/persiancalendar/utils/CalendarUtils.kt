package com.byagowi.persiancalendar.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.provider.CalendarContract
import androidx.annotation.PluralsRes
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calendarTypesTitleAbbr
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numericalDatePreferred
import com.byagowi.persiancalendar.global.preferredDigits
import com.byagowi.persiancalendar.global.spacedAndInDates
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.spacedOr
import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.global.weekDaysInitials
import com.byagowi.persiancalendar.global.weekStartOffset
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

val supportedYearOfIranCalendar: Int get() = IranianIslamicDateConverter.latestSupportedYearOfIran

fun applyWeekStartOffsetToWeekDay(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

fun revertWeekStartOffsetFromWeekDay(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun getWeekDayName(position: Int) = weekDays[position % 7]

fun dayTitleSummary(jdn: Jdn, date: AbstractDate, calendarNameInLinear: Boolean = true): String =
    jdn.dayOfWeekName + spacedComma + formatDate(date, calendarNameInLinear)

fun getInitialOfWeekDay(position: Int) = weekDaysInitials[position % 7]

val AbstractDate.monthName get() = this.calendarType.monthsNames.getOrNull(month - 1) ?: ""

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

    val events = eventsRepository?.getEvents(jdn, deviceCalendarEvents) ?: emptyList()
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

    if (withZodiac && isAstronomicalExtraFeaturesEnabled) {
        appendLine().appendLine()
            .appendLine(generateZodiacInformation(context, jdn, withEmoji = false))
            .append(isMoonInScorpio(context, jdn))
    }
}

fun GregorianCalendar.toCivilDate(): CivilDate {
    return CivilDate(
        this[GregorianCalendar.YEAR],
        this[GregorianCalendar.MONTH] + 1,
        this[GregorianCalendar.DAY_OF_MONTH]
    )
}

fun Date.toGregorianCalendar(forceLocalTime: Boolean = false): GregorianCalendar {
    val calendar = GregorianCalendar()
    if (!forceLocalTime && isForcedIranTimeEnabled)
        calendar.timeZone = TimeZone.getTimeZone(IRAN_TIMEZONE_ID)
    calendar.time = this
    return calendar
}

fun GregorianCalendar.formatDateAndTime(): String {
    return language.timeAndDateFormat.format(
        Clock(this).toFormattedString(forcedIn12 = true),
        formatDate(Jdn(this.toCivilDate()).toCalendar(mainCalendar), forceNonNumerical = true)
    )
}

// Google Meet generates weird and ugly descriptions with lines having such patterns, let's get rid of them
private val descriptionCleaningPattern = Regex("^-::~[:~]+:-$", RegexOption.MULTILINE)

private fun readDeviceEvents(
    context: Context, startingDate: GregorianCalendar, rangeInMillis: Long
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
            CalendarContract.Instances.EVENT_COLOR, // 7
            CalendarContract.Instances.DISPLAY_COLOR // 8
        ), null, null, null
    )?.use {
        generateSequence { if (it.moveToNext()) it else null }.filter {
            it.getString(5) == "1" // is visible
        }.map {
            val startDate = Date(it.getLong(3))
            val endDate = Date(it.getLong(4))
            val startCalendar = startDate.toGregorianCalendar()
            val endCalendar = endDate.toGregorianCalendar()
            fun GregorianCalendar.clock() = Clock(this).toBasicFormatString()
            CalendarEvent.DeviceCalendarEvent(
                id = it.getInt(0),
                title =
                if (it.getString(6) == "1") it.getString(1) ?: ""
                else "${it.getString(1) ?: ""} (${startCalendar.clock()}${
                    (if (it.getLong(3) != it.getLong(4) && it.getLong(4) != 0L)
                        "-${endCalendar.clock()}"
                    else "")
                })",
                description = it.getString(2)?.replace(descriptionCleaningPattern, "") ?: "",
                start = startDate,
                end = endDate,
                date = startCalendar.toCivilDate(),
                color = it.getString(7) ?: it.getString(8) ?: "",
                isHoliday = false
            )
        }.take(1000 /* let's put some limitation */).toList()
    }
}.onFailure(logException).getOrNull() ?: emptyList()

fun Context.readDayDeviceEvents(jdn: Jdn) =
    DeviceCalendarEventsStore(readDeviceEvents(this, jdn.toGregorianCalendar(), DAY_IN_MILLIS))

fun Context.readMonthDeviceEvents(jdn: Jdn) =
    DeviceCalendarEventsStore(
        readDeviceEvents(
            this,
            jdn.toGregorianCalendar(),
            32L * DAY_IN_MILLIS
        )
    )

fun Context.getAllEnabledAppointments() = readDeviceEvents(
    this, GregorianCalendar().apply { add(GregorianCalendar.YEAR, -1) },
    365L * 2L * DAY_IN_MILLIS // all the events of previous and next year from today
)

fun CalendarEvent.DeviceCalendarEvent.formatTitle(): String =
    (title + if (description.isNotBlank())
        " (${description.parseAsHtml(HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()})"
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
        is NepaliDate -> CalendarType.NEPALI
        else -> CalendarType.SHAMSI
    }

fun calculateDatePartsDifference(
    higher: AbstractDate, lower: AbstractDate, calendar: CalendarType
): Triple<Int, Int, Int> {
    var y = higher.year - lower.year
    var m = higher.month - lower.month
    var d = higher.dayOfMonth - lower.dayOfMonth
    if (d < 0) {
        m--
        d += calendar.getMonthLength(lower.year, lower.month)
    }
    if (m < 0) {
        y--
        m += calendar.getYearMonths(lower.year)
    }
    return Triple(y, m, d)
}

fun calculateDaysDifference(
    resources: Resources, jdn: Jdn,
    baseJdn: Jdn = Jdn.today(),
    calendarType: CalendarType = mainCalendar,
    isInWidget: Boolean = false
): String {
    val baseDate = baseJdn.toCalendar(calendarType)
    val date = jdn.toCalendar(calendarType)
    val (years, months, daysOfMonth) = calculateDatePartsDifference(
        if (baseJdn > jdn) baseDate else date, if (baseJdn > jdn) date else baseDate, calendarType
    )
    val days = abs(baseJdn - jdn)
    val daysString = resources.getQuantityString(R.plurals.n_days, days, formatNumber(days))
    val weeks = if (isInWidget || days < 7) 0 else (days / 7.0).roundToInt()
    val result = listOfNotNull(
        if (months == 0 && years == 0) null else listOf(
            R.plurals.n_years to years,
            R.plurals.n_months to months,
            R.plurals.n_days to daysOfMonth
        ).filter { (_, n) -> n != 0 }.joinToString(spacedAndInDates) { (@PluralsRes pluralId, n) ->
            resources.getQuantityString(pluralId, n, formatNumber(n))
        },
        if (weeks == 0) null
        else (if (days % 7 == 0) "" else "~")
                + resources.getQuantityString(R.plurals.n_weeks, weeks, formatNumber(weeks)),
    )
    if (result.isEmpty()) return daysString
    return language.inParentheses.format(daysString, result.joinToString(spacedOr))
}

fun formatDate(
    date: AbstractDate, calendarNameInLinear: Boolean = true, forceNonNumerical: Boolean = false
): String = if (numericalDatePreferred && !forceNonNumerical)
    (date.toLinearDate() + if (calendarNameInLinear) (" " + getCalendarNameAbbr(date)) else "").trim()
else language.dmy.format(formatNumber(date.dayOfMonth), date.monthName, formatNumber(date.year))

fun AbstractDate.toLinearDate(digits: CharArray = preferredDigits) = "%s/%s/%s".format(
    formatNumber(year, digits), formatNumber(month, digits), formatNumber(dayOfMonth, digits)
)

fun monthFormatForSecondaryCalendar(date: AbstractDate, secondaryCalendar: CalendarType): String {
    val mainCalendar = date.calendarType
    val from = Jdn(
        mainCalendar.createDate(date.year, date.month, 1)
    ).toCalendar(secondaryCalendar)
    val to = Jdn(
        mainCalendar.createDate(
            date.year, date.month,
            date.calendarType.getMonthLength(date.year, date.month)
        )
    ).toCalendar(secondaryCalendar)
    return when {
        from.month == to.month -> language.my.format(from.monthName, formatNumber(from.year))
        from.year != to.year -> listOf(
            from.year to from.month..secondaryCalendar.getYearMonths(from.year),
            to.year to 1..to.month
        ).joinToString(EN_DASH) { (year, months) ->
            language.my.format(months.joinToString(EN_DASH) {
                from.calendarType.monthsNames.getOrNull(it - 1).debugAssertNotNull ?: ""
            }, formatNumber(year))
        }

        else -> language.my.format(
            (from.month..to.month).joinToString(EN_DASH) {
                from.calendarType.monthsNames.getOrNull(it - 1).debugAssertNotNull ?: ""
            },
            formatNumber(from.year)
        )
    }
}

private fun getCalendarNameAbbr(date: AbstractDate) =
    calendarTypesTitleAbbr.getOrNull(date.calendarType.ordinal) ?: ""

fun dateStringOfOtherCalendars(jdn: Jdn, separator: String) =
    enabledCalendars.drop(1).joinToString(separator) { formatDate(jdn.toCalendar(it)) }
