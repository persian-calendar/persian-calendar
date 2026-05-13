package com.byagowi.persiancalendar.entities

import android.content.SharedPreferences
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.generated.CalendarRecord
import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.generated.gregorianEvents
import com.byagowi.persiancalendar.generated.islamicEvents
import com.byagowi.persiancalendar.generated.nepaliEvents
import com.byagowi.persiancalendar.generated.persianEvents
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import org.jetbrains.annotations.VisibleForTesting
import java.util.TimeZone

data class EventsRepository(
    private val enabledTypes: Set<String> = emptySet(),
    private val language: Language,
) {
    constructor(
        preferences: SharedPreferences, language: Language,
        enabledTypes: Set<String> = getEnabledTypes(preferences, language),
    ) : this(enabledTypes, language)

    val afghanistanHolidays = AFGHANISTAN_HOLIDAYS_KEY in enabledTypes
    val afghanistanOthers = AFGHANISTAN_OTHERS_KEY in enabledTypes
    val iranHolidays = IRAN_HOLIDAYS_KEY in enabledTypes
    val iranAncient = IRAN_ANCIENT_KEY in enabledTypes
    val iranOthers = IRAN_OTHERS_KEY in enabledTypes || /*legacy*/ "iran_islamic" in enabledTypes
    val nepalHolidays = NEPAL_HOLIDAYS_KEY in enabledTypes
    val nepalOthers = NEPAL_OTHERS_KEY in enabledTypes
    val international = INTERNATIONAL_KEY in enabledTypes
    val isEmpty = enabledTypes.isEmpty()
    val onlyIranHolidaysIsEnabled = enabledTypes.size == 1 && iranHolidays
    val onlyAfghanistanHolidaysIsEnabled = enabledTypes.size == 1 && afghanistanHolidays

    private fun skipEvent(record: CalendarRecord, calendar: Calendar): Boolean {
        return when (record.source) {
            EventSource.Iran if record.isHoliday && iranHolidays -> false
            EventSource.Iran if iranOthers -> false
            EventSource.Iran if iranHolidays && "روز مادر " in record.title -> false
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
    private fun determineIsHoliday(record: CalendarRecord) = when (record.source) {
        EventSource.Iran if !iranHolidays -> false
        EventSource.Afghanistan if !afghanistanHolidays -> false
        else -> record.isHoliday
    }

    init {
        // It is vital to configure calendar before loading of the events
        IslamicDate.useUmmAlQura = if (iranHolidays || iranOthers) false
        else afghanistanHolidays || language.mightPreferUmmAlquraIslamicCalendar
    }

    @VisibleForTesting
    val irregularCalendarEventsStore = IrregularCalendarEventsStore(this)
    private val persianCalendarEvents = PersianCalendarEventsStore(
        persianEvents.mapNotNull { createEvent(it, Calendar.SHAMSI) },
    )
    private val islamicCalendarEvents = IslamicCalendarEventsStore(
        islamicEvents.mapNotNull { createEvent(it, Calendar.ISLAMIC) },
    )
    private val gregorianCalendarEvents = GregorianCalendarEventsStore(
        gregorianEvents.mapNotNull { createEvent(it, Calendar.GREGORIAN) },
    )
    private val nepaliCalendarEvents = NepaliCalendarEventsStore(
        nepaliEvents.mapNotNull { createEvent(it, Calendar.NEPALI) },
    )

    fun getEvents(jdn: Jdn, deviceEvents: DeviceCalendarEventsStore): List<CalendarEvent<*>> {
        return listOf(
            persianCalendarEvents.getEvents(jdn.toPersianDate(), irregularCalendarEventsStore),
            islamicCalendarEvents.getEvents(jdn.toIslamicDate(), irregularCalendarEventsStore),
            nepaliCalendarEvents.getEvents(jdn.toNepaliDate(), irregularCalendarEventsStore),
            gregorianCalendarEvents.getEvents(
                jdn.toCivilDate(),
                irregularCalendarEventsStore,
                deviceEvents,
            ),
        ).flatten().filterEvents(jdn)
    }

    fun List<CalendarEvent<*>>.filterEvents(jdn: Jdn): List<CalendarEvent<*>> {
        val supported =
            jdn.isYearSupportedOnAppAndNextYear && Jdn.today().isYearSupportedOnAppAndNextYear
        val timeZone = TimeZone.getDefault().id
        return this.filter {
            when {
                !supported && (it.source == EventSource.Afghanistan || it.source == EventSource.Iran) -> false
                "روز مادر" in it.title -> true
                !it.isHoliday && timeZone != IRAN_TIMEZONE_ID && it.source == EventSource.Iran && it.date.calendar == Calendar.ISLAMIC -> false
                it.isHoliday || it.date.calendar in enabledCalendars || it.date.calendar != Calendar.ISLAMIC -> true
                else -> false
            }
        }
    }

    fun getEnabledEvents(jdn: Jdn): List<CalendarEvent<*>> {
        return (listOf(
            persianCalendarEvents,
            islamicCalendarEvents,
            nepaliCalendarEvents,
            gregorianCalendarEvents,
        ).flatMap { it.getAllEvents() } + listOf(
            jdn.toPersianDate(),
            jdn.toCivilDate(),
            jdn.toIslamicDate(),
            jdn.toNepaliDate(),
        ).flatMap {
            val store = irregularCalendarEventsStore
            val thisYear = store.getEventsList<CalendarEvent<*>>(it.year, it.calendar)
                .filter { event -> event.date.month >= it.month }
            val nextYear = store.getEventsList<CalendarEvent<*>>(it.year + 1, it.calendar)
                .filter { event -> event.date.month < it.month }
            thisYear + nextYear
        }).filterEvents(jdn)
    }

    private inline fun <reified T : CalendarEvent<out AbstractDate>> createEvent(
        record: CalendarRecord, calendar: Calendar,
    ): T? {
        if (skipEvent(record, calendar)) return null

        val holiday = determineIsHoliday(record)
        val title =
            (if (record.isHoliday && (record.source == EventSource.Iran || (record.source == EventSource.Afghanistan && language.isPersianOrDari))) "تعطیلی رسمی به مناسبت " else "") + record.title
        return (when (calendar) {
            Calendar.SHAMSI -> {
                val date = PersianDate(everyYear, record.month, record.day)
                CalendarEvent.PersianCalendarEvent(title, holiday, date, record.source)
            }

            Calendar.GREGORIAN -> {
                val date = CivilDate(everyYear, record.month, record.day)
                CalendarEvent.GregorianCalendarEvent(title, holiday, date, record.source)
            }

            Calendar.ISLAMIC -> {
                val date = IslamicDate(everyYear, record.month, record.day)
                CalendarEvent.IslamicCalendarEvent(title, holiday, date, record.source)
            }

            Calendar.NEPALI -> {
                val date = NepaliDate(everyYear, record.month, record.day)
                CalendarEvent.NepaliCalendarEvent(title, holiday, date, record.source)
            }
        } as? T).debugAssertNotNull
    }

    fun calculateWorkDays(fromJdn: Jdn, toJdn: Jdn): Int {
        val emptyDeviceCalendar: DeviceCalendarEventsStore = EventsStore.empty()
        return (fromJdn + 1..toJdn).count {
            it.weekDay !in weekEnds && getEvents(
                it,
                emptyDeviceCalendar,
            ).none(CalendarEvent<*>::isHoliday)
        }
    }

    companion object {
        const val IRAN_HOLIDAYS_KEY = "iran_holidays"
        const val IRAN_OTHERS_KEY = "iran_others"
        const val AFGHANISTAN_HOLIDAYS_KEY = "afghanistan_holidays"
        const val AFGHANISTAN_OTHERS_KEY = "afghanistan_others"
        const val NEPAL_HOLIDAYS_KEY = "nepal_holidays"
        const val NEPAL_OTHERS_KEY = "nepal_others"
        const val IRAN_ANCIENT_KEY = "iran_ancient"
        const val INTERNATIONAL_KEY = "international"
        val iranDefault = setOf(IRAN_HOLIDAYS_KEY)
        val afghanistanDefault = setOf(AFGHANISTAN_HOLIDAYS_KEY)
        val nepalDefault = setOf(NEPAL_HOLIDAYS_KEY)

        fun getEnabledTypes(preferences: SharedPreferences, language: Language): Set<String> {
            return preferences.getStringSet(PREF_HOLIDAY_TYPES, null) ?: when {
                language.isIranExclusive && TimeZone.getDefault().id == IRAN_TIMEZONE_ID -> iranDefault
                language.isAfghanistanExclusive && TimeZone.getDefault().id == AFGHANISTAN_TIMEZONE_ID -> afghanistanDefault
                else -> emptySet()
            }
        }

        fun keyFromDetails(source: EventSource?, isHoliday: Boolean): String? {
            return when (source) {
                null -> null
                EventSource.AncientIran -> IRAN_ANCIENT_KEY
                EventSource.International -> INTERNATIONAL_KEY
                EventSource.Iran -> if (isHoliday) IRAN_HOLIDAYS_KEY else IRAN_OTHERS_KEY
                EventSource.Nepal -> if (isHoliday) NEPAL_HOLIDAYS_KEY else NEPAL_OTHERS_KEY
                EventSource.Afghanistan -> if (isHoliday) AFGHANISTAN_HOLIDAYS_KEY else AFGHANISTAN_OTHERS_KEY
            }
        }

        fun empty() = EventsRepository(emptySet(), Language.entries[0])
    }
}

// This isn't that good approach maybe but is what we used on the project
const val everyYear = -1
