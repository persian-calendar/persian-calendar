package com.byagowi.persiancalendar.utils

import android.content.SharedPreferences
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.generated.*
import com.byagowi.persiancalendar.global.*
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate

var persianCalendarEvents: PersianCalendarEventsStore = EventsStore.empty()
    private set
var islamicCalendarEvents: IslamicCalendarEventsStore = EventsStore.empty()
    private set
var gregorianCalendarEvents: GregorianCalendarEventsStore = EventsStore.empty()
    private set
var nepaliCalendarEvents: NepaliCalendarEventsStore = EventsStore.empty()
    private set
var irregularCalendarEventsStore = IrregularCalendarEventsStore(EnabledHolidays())

class EnabledHolidays(val enabledTypes: Set<String> = emptySet()) {
    constructor(
        prefs: SharedPreferences,
        defaultSet: Set<String> = if (language.isIranExclusive) iranDefault else emptySet()
    ) : this(prefs.getStringSet(PREF_HOLIDAY_TYPES, null) ?: defaultSet)

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
        return if (calendarRecord.isHoliday && iranHolidays && afghanistanHolidays) {
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

fun loadEvents(enabledTypes: EnabledHolidays, language: Language) {
    // It is vital to configure calendar before loading of the events
    IslamicDate.useUmmAlQura =
        if (enabledTypes.iranHolidays || enabledTypes.iranOthers) false
        else enabledTypes.afghanistanHolidays || language.mightPreferUmmAlquraIslamicCalendar

    irregularCalendarEventsStore = IrregularCalendarEventsStore(enabledTypes)
    persianCalendarEvents = PersianCalendarEventsStore(persianEvents.mapNotNull { record ->
        createEvent(record, enabledTypes, CalendarType.SHAMSI)
    })
    islamicCalendarEvents = IslamicCalendarEventsStore(islamicEvents.mapNotNull { record ->
        createEvent(record, enabledTypes, CalendarType.ISLAMIC)
    })
    gregorianCalendarEvents = GregorianCalendarEventsStore(gregorianEvents.mapNotNull { record ->
        createEvent(record, enabledTypes, CalendarType.GREGORIAN)
    })
    nepaliCalendarEvents = NepaliCalendarEventsStore(gregorianEvents.mapNotNull { record ->
        createEvent(record, enabledTypes, CalendarType.GREGORIAN)
    })
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
        CalendarType.NEPALI -> {
            val date = NepaliDate(-1, record.month, record.day)
            CalendarEvent.NepaliCalendarEvent(title, holiday, date)
        }
    } as? T).debugAssertNotNull
}

private fun formatDayAndMonth(calendarType: CalendarType, day: Int, month: Int): String {
    val monthName = when (calendarType) {
        CalendarType.SHAMSI -> persianMonths
        CalendarType.GREGORIAN -> gregorianMonths
        CalendarType.ISLAMIC -> islamicMonths
        CalendarType.NEPALI -> nepaliMonths
    }.getOrNull(month - 1).debugAssertNotNull ?: ""
    return language.dm.format(formatNumber(day), monthName)
}

class IrregularCalendarEventsStore(private val enabledHolidays: EnabledHolidays) {
    private val persianEvents = mutableMapOf<Int, List<CalendarEvent.PersianCalendarEvent>>()
    private val islamicEvents = mutableMapOf<Int, List<CalendarEvent.IslamicCalendarEvent>>()
    private val gregorianEvents = mutableMapOf<Int, List<CalendarEvent.GregorianCalendarEvent>>()
    private val nepaliEvents = mutableMapOf<Int, List<CalendarEvent.NepaliCalendarEvent>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : CalendarEvent<*>> getEventsList(year: Int, type: CalendarType): List<T> {
        fun <T : CalendarEvent<*>> generate() = generateEntry(year, type) as? List<T> ?: emptyList()
        return when (type) {
            CalendarType.SHAMSI -> persianEvents.getOrPut(year, ::generate)
            CalendarType.ISLAMIC -> islamicEvents.getOrPut(year, ::generate)
            CalendarType.GREGORIAN -> gregorianEvents.getOrPut(year, ::generate)
            CalendarType.NEPALI -> nepaliEvents.getOrPut(year, ::generate)
        } as? List<T> ?: emptyList()
    }

    fun <T : CalendarEvent<out AbstractDate>> getEvents(date: AbstractDate): List<T> =
        getEventsList<T>(date.year, date.calendarType).filter { it.date == date }

    // Create actually usable irregular event of a year based on defined rules and enabled holidays
    private fun generateEntry(year: Int, type: CalendarType): List<CalendarEvent<*>> {
        return irregularRecurringEvents.filter { event ->
            val eventType = when (event["calendar"]) {
                "Gregorian" -> CalendarType.GREGORIAN
                "Persian" -> CalendarType.SHAMSI
                "Hijri" -> CalendarType.ISLAMIC
                "Nepali" -> CalendarType.NEPALI
                else -> return@filter false
            }
            if (eventType != type) return@filter false
            when {
                event["type"] == "International" && enabledHolidays.international -> true
                event["type"] == "Iran" && enabledHolidays.iranHolidays && event["holiday"] == "true" -> true
                event["type"] == "Iran" && enabledHolidays.iranOthers -> true
                event["type"] == "AncientIran" && enabledHolidays.iranAncient -> true
                else -> false
            }
        }.mapNotNull { event ->
            val date = when (event["rule"]) {
                "single event" -> {
                    if (event["year"]?.toIntOrNull() != year) return@mapNotNull null
                    val month = event["month"]?.toIntOrNull() ?: return@mapNotNull null
                    val day = event["day"]?.toIntOrNull() ?: return@mapNotNull null
                    type.createDate(year, month, day)
                }
                "nth day from" -> {
                    val nth = event["nth"]?.toIntOrNull() ?: return@mapNotNull null
                    val day = event["day"]?.toIntOrNull() ?: return@mapNotNull null
                    val month = event["month"]?.toIntOrNull() ?: return@mapNotNull null
                    (Jdn(type, year, month, day) + nth - 1).toCalendar(type)
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
            val title = "${event["title"] ?: return@mapNotNull null} (${formatNumber(year)})"
            val isHoliday = event["holiday"] == "true"
            when (date) {
                is PersianDate -> CalendarEvent.PersianCalendarEvent(title, isHoliday, date)
                is IslamicDate -> CalendarEvent.IslamicCalendarEvent(title, isHoliday, date)
                is CivilDate -> CalendarEvent.GregorianCalendarEvent(title, isHoliday, date)
                is NepaliDate -> CalendarEvent.NepaliCalendarEvent(title, isHoliday, date)
                else -> null
            }.debugAssertNotNull
        }
    }
}
