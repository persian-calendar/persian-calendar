package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.generated.CalendarRecord
import com.byagowi.persiancalendar.generated.EventType
import com.byagowi.persiancalendar.generated.gregorianEvents
import com.byagowi.persiancalendar.generated.irregularRecurringEvents
import com.byagowi.persiancalendar.generated.islamicEvents
import com.byagowi.persiancalendar.generated.persianEvents
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

fun loadEvents(enabledTypes: Set<String>) {
    val afghanistanHolidays = "afghanistan_holidays" in enabledTypes
    val afghanistanOthers = "afghanistan_others" in enabledTypes
    val iranHolidays = "iran_holidays" in enabledTypes
    val iranAncient = "iran_ancient" in enabledTypes
    val iranOthers = "iran_others" in enabledTypes || /*legacy*/ "iran_islamic" in enabledTypes
    val international = "international" in enabledTypes

    fun <T> createEvent(record: CalendarRecord, calendarType: CalendarType): T? {
        when { // skip not enabled events if none of the rules matches
            record.type == EventType.Iran && record.isHoliday && iranHolidays -> Unit
            record.type == EventType.Iran && iranOthers -> Unit
            record.type == EventType.Afghanistan && record.isHoliday && afghanistanHolidays -> Unit
            record.type == EventType.Afghanistan && afghanistanOthers -> Unit
            record.type == EventType.AncientIran && iranAncient -> Unit
            record.type == EventType.International && international -> Unit
            // Enable Iranian events of Gregorian calendar even if itself isn't enabled
            record.type == EventType.Iran && international && calendarType == CalendarType.GREGORIAN ->
                Unit
            else -> return null
        }

        val holiday = when {
            !iranHolidays && record.type == EventType.Iran -> false
            !afghanistanHolidays && record.type == EventType.Afghanistan -> false
            else -> record.isHoliday
        }

        val title = """${record.title} (${
            if (holiday && afghanistanHolidays && iranHolidays) {
                when (record.type) {
                    EventType.Iran -> "ایران"
                    EventType.Afghanistan -> "افغانستان"
                    else -> ""
                } + spacedComma
            } else ""
        }${formatDayAndMonth(record.day, persianMonths[record.month - 1])})"""

        @Suppress("UNCHECKED_CAST")
        return when (calendarType) {
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
        } as? T
    }

    // Creates irregular recurring events instances of this years, the previous and the next year
    // a bit hacky and probably will be replaced with a caching mechanism instead
    val today = Jdn.today
    val irregularEventsInstances = listOf(
        createIrregularRecurringEventsInstances(
            international, iranHolidays, iranOthers, iranAncient, today
        ),
        createIrregularRecurringEventsInstances(
            international, iranHolidays, iranOthers, iranAncient, today, -1
        ),
        createIrregularRecurringEventsInstances(
            international, iranHolidays, iranOthers, iranAncient, today, +1
        )
    ).flatten()

    allEnabledEvents =
        run {
            val events = persianEvents.mapNotNull { record ->
                createEvent<CalendarEvent.PersianCalendarEvent>(record, CalendarType.SHAMSI)
            } + irregularEventsInstances.filterIsInstance<CalendarEvent.PersianCalendarEvent>()
            persianCalendarEvents = PersianCalendarEventsStore(events)
            events
        } + run {
            val events = islamicEvents.mapNotNull { record ->
                createEvent<CalendarEvent.IslamicCalendarEvent>(record, CalendarType.ISLAMIC)
            } + irregularEventsInstances.filterIsInstance<CalendarEvent.IslamicCalendarEvent>()
            islamicCalendarEvents = IslamicCalendarEventsStore(events)
            events
        } + run {
            val events = gregorianEvents.mapNotNull { record ->
                createEvent<CalendarEvent.GregorianCalendarEvent>(record, CalendarType.GREGORIAN)
            } + irregularEventsInstances.filterIsInstance<CalendarEvent.GregorianCalendarEvent>()
            gregorianCalendarEvents = GregorianCalendarEventsStore(events)
            events
        }
}

// Create actually usable irregular event of a year based on defined rules and enabled holidays
private fun createIrregularRecurringEventsInstances(
    international: Boolean, iranHolidays: Boolean, iranOthers: Boolean, iranAncient: Boolean,
    jdn: Jdn, yearOffset: Int = 0,
): List<CalendarEvent<out AbstractDate>> {
    return irregularRecurringEvents.filter { event ->
        when {
            event["type"] == "International" && international -> true
            event["type"] == "Iran" && iranHolidays && event["holiday"] == "true" -> true
            event["type"] == "Iran" && iranOthers -> true
            event["type"] == "AncientIran" && iranAncient -> true
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
