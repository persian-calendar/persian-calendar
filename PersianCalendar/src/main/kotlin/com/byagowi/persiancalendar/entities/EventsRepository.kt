package com.byagowi.persiancalendar.entities

import android.content.SharedPreferences
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.generated.CalendarRecord
import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.generated.gregorianEvents
import com.byagowi.persiancalendar.generated.islamicEvents
import com.byagowi.persiancalendar.generated.nepaliEvents
import com.byagowi.persiancalendar.generated.persianEvents
import com.byagowi.persiancalendar.global.gregorianMonths
import com.byagowi.persiancalendar.global.islamicMonths
import com.byagowi.persiancalendar.global.nepaliMonths
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.persianMonths
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import org.jetbrains.annotations.VisibleForTesting

data class EventsRepository @VisibleForTesting constructor(
    private val enabledTypes: Set<String> = emptySet(),
    private val language: Language,
) {
    constructor(
        preferences: SharedPreferences, language: Language,
        enabledTypes: Set<String> = getEnabledTypes(preferences, language)
    ) : this(enabledTypes, language)

    val afghanistanHolidays = afghanistanHolidaysKey in enabledTypes
    val afghanistanOthers = afghanistanOthersKey in enabledTypes
    val iranHolidays = iranHolidaysKey in enabledTypes
    val iranAncient = iranAncientKey in enabledTypes
    val iranOthers = iranOthersKey in enabledTypes || /*legacy*/ "iran_islamic" in enabledTypes
    val nepalHolidays = nepalHolidaysKey in enabledTypes
    val nepalOthers = nepalOthersKey in enabledTypes
    val international = internationalKey in enabledTypes
    val isEmpty get() = enabledTypes.isEmpty()
    val onlyIranHolidaysIsEnabled get() = enabledTypes.size == 1 && iranHolidays
    val onlyAfghanistanHolidaysIsEnabled get() = enabledTypes.size == 1 && afghanistanHolidays

    private fun skipEvent(record: CalendarRecord, calendar: Calendar): Boolean {
        return when (record.source) {
            EventSource.Iran if record.isHoliday && iranHolidays -> false
            EventSource.Iran if iranOthers -> false
            EventSource.Afghanistan if record.isHoliday && afghanistanHolidays -> false
            EventSource.Afghanistan if afghanistanOthers -> false
            EventSource.Nepal if record.isHoliday && nepalHolidays -> false
            EventSource.Nepal if nepalOthers -> false
            EventSource.AncientIran if iranAncient -> false
            EventSource.International if international -> false
            // Enable Iranian events of Gregorian calendar even if itself isn't enabled
            EventSource.Iran if international && calendar == Calendar.GREGORIAN -> false
            else -> true
        }
    }

    // Don't mark holidays as holiday if holiday isn't enabled explicitly
    private fun determineIsHoliday(record: CalendarRecord) = when {
        record.source == EventSource.Iran && !iranHolidays -> false
        record.source == EventSource.Afghanistan && !afghanistanHolidays -> false
        else -> record.isHoliday
    }

    private fun multiCountryComment(calendarRecord: CalendarRecord): String {
        return if (calendarRecord.isHoliday && iranHolidays && afghanistanHolidays) {
            when (calendarRecord.source) {
                EventSource.Iran -> "ایران"
                EventSource.Afghanistan -> "افغانستان"
                else -> ""
            } + spacedComma
        } else ""
    }

    init {
        // It is vital to configure calendar before loading of the events
        IslamicDate.useUmmAlQura =
            if (iranHolidays || iranOthers) false
            else afghanistanHolidays || language.mightPreferUmmAlquraIslamicCalendar
    }

    @VisibleForTesting
    val irregularCalendarEventsStore = IrregularCalendarEventsStore(this)
    private val persianCalendarEvents = PersianCalendarEventsStore(persianEvents.mapNotNull {
        createEvent(it, Calendar.SHAMSI)
    })
    private val islamicCalendarEvents = IslamicCalendarEventsStore(islamicEvents.mapNotNull {
        createEvent(it, Calendar.ISLAMIC)
    })
    private val gregorianCalendarEvents = GregorianCalendarEventsStore(gregorianEvents.mapNotNull {
        createEvent(it, Calendar.GREGORIAN)
    })
    private val nepaliCalendarEvents = NepaliCalendarEventsStore(nepaliEvents.mapNotNull {
        createEvent(it, Calendar.NEPALI)
    })

    fun getEvents(jdn: Jdn, deviceEvents: DeviceCalendarEventsStore): List<CalendarEvent<*>> {
        return listOf(
            persianCalendarEvents.getEvents(jdn.toPersianDate(), irregularCalendarEventsStore),
            islamicCalendarEvents.getEvents(jdn.toIslamicDate(), irregularCalendarEventsStore),
            nepaliCalendarEvents.getEvents(jdn.toNepaliDate(), irregularCalendarEventsStore),
            gregorianCalendarEvents
                .getEvents(jdn.toCivilDate(), irregularCalendarEventsStore, deviceEvents)
        ).flatten()
    }

    fun getEnabledEvents(jdn: Jdn): List<CalendarEvent<*>> {
        return listOf(
            persianCalendarEvents.getAllEvents(),
            islamicCalendarEvents.getAllEvents(),
            nepaliCalendarEvents.getAllEvents(),
            gregorianCalendarEvents.getAllEvents()
        ).flatten() + listOf(
            jdn.toPersianDate(),
            jdn.toCivilDate(),
            jdn.toIslamicDate(),
            jdn.toNepaliDate()
        ).flatMap {
            val store = irregularCalendarEventsStore
            val thisYear = store.getEventsList<CalendarEvent<*>>(it.year, it.calendar)
                .filter { event -> event.date.month >= it.month }
            val nextYear = store.getEventsList<CalendarEvent<*>>(it.year + 1, it.calendar)
                .filter { event -> event.date.month < it.month }
            thisYear + nextYear
        }
    }

    private inline fun <reified T : CalendarEvent<out AbstractDate>> createEvent(
        record: CalendarRecord, calendar: Calendar
    ): T? {
        if (skipEvent(record, calendar)) return null
        val multiCountryComment = multiCountryComment(record)
        val dayAndMonth = formatDayAndMonth(calendar, record.day, record.month)
        val title = "${record.title} ($multiCountryComment$dayAndMonth)"

        val holiday = determineIsHoliday(record)
        return (when (calendar) {
            Calendar.SHAMSI -> {
                val date = PersianDate(-1, record.month, record.day)
                CalendarEvent.PersianCalendarEvent(title, holiday, date, record.source)
            }

            Calendar.GREGORIAN -> {
                val date = CivilDate(-1, record.month, record.day)
                CalendarEvent.GregorianCalendarEvent(title, holiday, date, record.source)
            }

            Calendar.ISLAMIC -> {
                val date = IslamicDate(-1, record.month, record.day)
                CalendarEvent.IslamicCalendarEvent(title, holiday, date, record.source)
            }

            Calendar.NEPALI -> {
                val date = NepaliDate(-1, record.month, record.day)
                CalendarEvent.NepaliCalendarEvent(title, holiday, date, record.source)
            }
        } as? T).debugAssertNotNull
    }

    private fun formatDayAndMonth(calendar: Calendar, day: Int, month: Int): String {
        val monthName = when (calendar) {
            Calendar.SHAMSI -> persianMonths
            Calendar.GREGORIAN -> gregorianMonths
            Calendar.ISLAMIC -> islamicMonths
            Calendar.NEPALI -> nepaliMonths
        }.getOrNull(month - 1).debugAssertNotNull.orEmpty()
        return language.dm.format(numeral.value.format(day), monthName)
    }

    fun calculateWorkDays(fromJdn: Jdn, toJdn: Jdn): Int {
        val emptyDeviceCalendar: DeviceCalendarEventsStore = EventsStore.empty()
        val weekEnds = weekEnds.value
        return (fromJdn + 1..toJdn).count {
            it.weekDay !in weekEnds && getEvents(
                it,
                emptyDeviceCalendar
            ).none(CalendarEvent<*>::isHoliday)
        }
    }

    companion object {
        const val iranHolidaysKey = "iran_holidays"
        const val iranOthersKey = "iran_others"
        const val afghanistanHolidaysKey = "afghanistan_holidays"
        const val afghanistanOthersKey = "afghanistan_others"
        const val nepalHolidaysKey = "nepal_holidays"
        const val nepalOthersKey = "nepal_others"
        const val iranAncientKey = "iran_ancient"
        const val internationalKey = "international"
        val iranDefault = setOf(iranHolidaysKey)
        val afghanistanDefault = setOf(afghanistanHolidaysKey)
        val nepalDefault = setOf(nepalHolidaysKey)

        fun getEnabledTypes(preferences: SharedPreferences, language: Language): Set<String> {
            return preferences.getStringSet(PREF_HOLIDAY_TYPES, null)
                ?: if (language.isIranExclusive) iranDefault else emptySet()
        }

        fun empty() = EventsRepository(emptySet(), Language.entries[0])
    }
}
