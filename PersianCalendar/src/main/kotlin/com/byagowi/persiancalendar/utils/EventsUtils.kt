package com.byagowi.persiancalendar.utils

import android.content.SharedPreferences
import com.byagowi.persiancalendar.LANG_CKB
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.generated.*
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

var allEnabledEvents = emptyList<CalendarEvent<*>>()
    private set
var persianCalendarEvents: PersianCalendarEventsStore = EventsStore.empty()
    private set
var islamicCalendarEvents: IslamicCalendarEventsStore = EventsStore.empty()
    private set
var gregorianCalendarEvents: GregorianCalendarEventsStore = EventsStore.empty()
    private set

class EnabledHolidays private constructor(val enabledTypes: Set<String>) {
    constructor(prefs: SharedPreferences, defaultSet: Set<String> = iranDefault) :
            this(prefs.getStringSet(PREF_HOLIDAY_TYPES, null) ?: defaultSet)

    val afghanistanHolidays = afghanistanHolidaysKey in enabledTypes
    val afghanistanOthers = afghanistanOthersKey in enabledTypes
    val iranHolidays = iranHolidaysKey in enabledTypes
    val iranAncient = iranAncientKey in enabledTypes
    val iranOthers = iranOthersKey in enabledTypes || /*legacy*/ "iran_islamic" in enabledTypes
    val international = internationalKey in enabledTypes
    val isEmpty get() = enabledTypes.isEmpty()
    val onlyIranHolidaysIsEnabled get() = enabledTypes.size == 1 && iranHolidays
    val onlyAfghanistanHolidaysIsEnabled get() = enabledTypes.size == 1 && afghanistanHolidays

    fun skipEvent(record: CalendarRecord, calendarType: CalendarType) = when {
        record.type == EventType.Iran && record.isHoliday && iranHolidays -> false
        record.type == EventType.Iran && iranOthers -> false
        record.type == EventType.Afghanistan && record.isHoliday && afghanistanHolidays -> false
        record.type == EventType.Afghanistan && afghanistanOthers -> false
        record.type == EventType.AncientIran && iranAncient -> false
        record.type == EventType.International && international -> false
        // Enable Iranian events of Gregorian calendar even if itself isn't enabled
        record.type == EventType.Iran && international && calendarType == CalendarType.GREGORIAN ->
            false
        else -> true
    }

    // Don't mark holidays as holiday if holiday isn't enabled explicitly
    fun determineIsHoliday(record: CalendarRecord) = when {
        record.type == EventType.Iran && !iranHolidays -> false
        record.type == EventType.Afghanistan && !afghanistanHolidays -> false
        else -> record.isHoliday
    }

    fun multiCountryComment(calendarRecord: CalendarRecord): String {
        return if (calendarRecord.isHoliday && iranHolidays && afghanistanOthers) {
            when (calendarRecord.type) {
                EventType.Iran -> "ایران"
                EventType.Afghanistan -> "افغانستان"
                else -> ""
            } + spacedComma
        } else ""
    }

    companion object {
        const val iranHolidaysKey = "iran_holidays"
        const val iranOthersKey = "iran_others"
        const val afghanistanHolidaysKey = "afghanistan_holidays"
        const val afghanistanOthersKey = "afghanistan_others"
        const val iranAncientKey = "iran_ancient"
        const val internationalKey = "international"
        val iranDefault = setOf(iranHolidaysKey)
        val afghanistanDefault = setOf(afghanistanHolidaysKey)
    }
}

fun loadEvents(e: EnabledHolidays) {
    // Creates irregular recurring events instances of this years, the previous and the next year
    // a bit hacky and probably will be replaced with a caching mechanism instead
    val today = Jdn.today
    val irregularEventsInstances = createIrregularRecurringEventsInstances(e, today) +
            createIrregularRecurringEventsInstances(e, today, -1) +
            createIrregularRecurringEventsInstances(e, today, +1)

    allEnabledEvents = run {
        val events = persianEvents.mapNotNull { record ->
            createEvent<CalendarEvent.PersianCalendarEvent>(record, e, CalendarType.SHAMSI)
        } + irregularEventsInstances.filterIsInstance<CalendarEvent.PersianCalendarEvent>()
        persianCalendarEvents = PersianCalendarEventsStore(events)
        events
    } + run {
        val events = islamicEvents.mapNotNull { record ->
            createEvent<CalendarEvent.IslamicCalendarEvent>(record, e, CalendarType.ISLAMIC)
        } + irregularEventsInstances.filterIsInstance<CalendarEvent.IslamicCalendarEvent>()
        islamicCalendarEvents = IslamicCalendarEventsStore(events)
        events
    } + run {
        val events = gregorianEvents.mapNotNull { record ->
            createEvent<CalendarEvent.GregorianCalendarEvent>(record, e, CalendarType.GREGORIAN)
        } + irregularEventsInstances.filterIsInstance<CalendarEvent.GregorianCalendarEvent>()
        gregorianCalendarEvents = GregorianCalendarEventsStore(events)
        events
    }
}

private inline fun <reified T : CalendarEvent<out AbstractDate>> createEvent(
    record: CalendarRecord, enabledHolidays: EnabledHolidays, calendarType: CalendarType
): T? {
    if (enabledHolidays.skipEvent(record, calendarType)) return null
    val multiCountryComment = enabledHolidays.multiCountryComment(record)
    val dayAndMonth = formatDayAndMonth(calendarType, record.day, record.month)
    val title = "${record.title} ($multiCountryComment$dayAndMonth)"

    val holiday = enabledHolidays.determineIsHoliday(record)
    return (when (calendarType) {
        CalendarType.SHAMSI -> {
            val date = PersianDate(-1, record.month, record.day)
            CalendarEvent.PersianCalendarEvent(title, holiday, date)
        }
        CalendarType.GREGORIAN -> {
            val date = CivilDate(-1, record.month, record.day)
            CalendarEvent.GregorianCalendarEvent(title, holiday, date)
        }
        CalendarType.ISLAMIC -> {
            val date = IslamicDate(-1, record.month, record.day)
            CalendarEvent.IslamicCalendarEvent(title, holiday, date)
        }
    } as T?).debugAssertNotNull
}

private fun formatDayAndMonth(calendarType: CalendarType, day: Int, month: Int): String {
    val monthName = when (calendarType) {
        CalendarType.SHAMSI -> persianMonths
        CalendarType.GREGORIAN -> gregorianMonths
        CalendarType.ISLAMIC -> islamicMonths
    }.getOrNull(month - 1).debugAssertNotNull ?: ""
    return when (language) {
        LANG_CKB -> "%sی %s"
        else -> "%s %s"
    }.format(formatNumber(day), monthName)
}

// Create actually usable irregular event of a year based on defined rules and enabled holidays
private fun createIrregularRecurringEventsInstances(
    e: EnabledHolidays, jdn: Jdn, yearOffset: Int = 0
): List<CalendarEvent<out AbstractDate>> {
    return irregularRecurringEvents.filter { event ->
        when {
            event["type"] == "International" && e.international -> true
            event["type"] == "Iran" && e.iranHolidays && event["holiday"] == "true" -> true
            event["type"] == "Iran" && e.iranOthers -> true
            event["type"] == "AncientIran" && e.iranAncient -> true
            else -> false
        }
    }.mapNotNull { event ->
        val type = when (event["calendar"]) {
            "Gregorian" -> CalendarType.GREGORIAN
            "Persian" -> CalendarType.SHAMSI
            "Hijri" -> CalendarType.ISLAMIC
            else -> return@mapNotNull null
        }
        val year = jdn.toCalendar(type).year + yearOffset
        val date = when (event["rule"]) {
            "nth day of year" -> {
                val nth = event["nth"]?.toIntOrNull() ?: return@mapNotNull null
                (Jdn(type, year, 1, 1) + nth - 1).toCalendar(type)
            }
            "end of month" -> {
                val month = event["month"]?.toIntOrNull() ?: return@mapNotNull null
                type.createDate(year, month, type.getMonthLength(year, month))
            }
            "last weekday of month" -> {
                val month = event["month"]?.toIntOrNull() ?: return@mapNotNull null
                val weekDay = event["weekday"]?.toIntOrNull() ?: return@mapNotNull null
                type.createDate(year, month, type.getLastWeekDayOfMonth(year, month, weekDay))
            }
            else -> return@mapNotNull null
        }
        val title = "${(event["title"] ?: return@mapNotNull null)} (${formatNumber(year)})"
        val isHoliday = event["holiday"] == "true"
        when (date) {
            is PersianDate -> CalendarEvent.PersianCalendarEvent(title, isHoliday, date)
            is IslamicDate -> CalendarEvent.IslamicCalendarEvent(title, isHoliday, date)
            is CivilDate -> CalendarEvent.GregorianCalendarEvent(title, isHoliday, date)
            else -> null
        }.debugAssertNotNull
    }
}
