package com.byagowi.persiancalendar.entities

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.generated.CalendarRecord
import com.byagowi.persiancalendar.generated.EventType
import com.byagowi.persiancalendar.generated.gregorianEvents
import com.byagowi.persiancalendar.generated.islamicEvents
import com.byagowi.persiancalendar.generated.nepaliEvents
import com.byagowi.persiancalendar.generated.persianEvents
import com.byagowi.persiancalendar.global.gregorianMonths
import com.byagowi.persiancalendar.global.islamicMonths
import com.byagowi.persiancalendar.global.nepaliMonths
import com.byagowi.persiancalendar.global.persianMonths
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate

class EventsRepository(
    private val enabledTypes: Set<String> = emptySet(),
    private val language: Language
) {
    constructor(
        prefs: SharedPreferences, language: Language,
        enabledTypes: Set<String> = getEnabledTypes(prefs, language)
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

    private fun skipEvent(record: CalendarRecord, calendarType: CalendarType): Boolean {
        return when {
            record.type == EventType.Iran && record.isHoliday && iranHolidays -> false
            record.type == EventType.Iran && iranOthers -> false
            record.type == EventType.Afghanistan && record.isHoliday && afghanistanHolidays -> false
            record.type == EventType.Afghanistan && afghanistanOthers -> false
            record.type == EventType.Nepal && record.isHoliday && nepalHolidays -> false
            record.type == EventType.Nepal && nepalOthers -> false
            record.type == EventType.AncientIran && iranAncient -> false
            record.type == EventType.International && international -> false
            // Enable Iranian events of Gregorian calendar even if itself isn't enabled
            record.type == EventType.Iran && international && calendarType == CalendarType.GREGORIAN ->
                false
            else -> true
        }
    }

    // Don't mark holidays as holiday if holiday isn't enabled explicitly
    private fun determineIsHoliday(record: CalendarRecord) = when {
        record.type == EventType.Iran && !iranHolidays -> false
        record.type == EventType.Afghanistan && !afghanistanHolidays -> false
        else -> record.isHoliday
    }

    private fun multiCountryComment(calendarRecord: CalendarRecord): String {
        return if (calendarRecord.isHoliday && iranHolidays && afghanistanHolidays) {
            when (calendarRecord.type) {
                EventType.Iran -> "ایران"
                EventType.Afghanistan -> "افغانستان"
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
        createEvent(it, CalendarType.SHAMSI)
    })
    private val islamicCalendarEvents = IslamicCalendarEventsStore(islamicEvents.mapNotNull {
        createEvent(it, CalendarType.ISLAMIC)
    })
    private val gregorianCalendarEvents = GregorianCalendarEventsStore(gregorianEvents.mapNotNull {
        createEvent(it, CalendarType.GREGORIAN)
    })
    private val nepaliCalendarEvents = NepaliCalendarEventsStore(nepaliEvents.mapNotNull {
        createEvent(it, CalendarType.NEPALI)
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
            persianCalendarEvents.getAllEvents(), islamicCalendarEvents.getAllEvents(),
            nepaliCalendarEvents.getAllEvents(), gregorianCalendarEvents.getAllEvents()
        ).flatten() + listOf(
            jdn.toPersianDate(),
            jdn.toCivilDate(),
            jdn.toIslamicDate(),
            jdn.toNepaliDate()
        ).flatMap {
            val store = irregularCalendarEventsStore
            val thisYear = store.getEventsList<CalendarEvent<*>>(it.year, it.calendarType)
                .filter { event -> event.date.month >= it.month }
            val nextYear = store.getEventsList<CalendarEvent<*>>(it.year + 1, it.calendarType)
                .filter { event -> event.date.month < it.month }
            thisYear + nextYear
        }
    }

    private inline fun <reified T : CalendarEvent<out AbstractDate>> createEvent(
        record: CalendarRecord, calendarType: CalendarType
    ): T? {
        if (skipEvent(record, calendarType)) return null
        val multiCountryComment = multiCountryComment(record)
        val dayAndMonth = formatDayAndMonth(calendarType, record.day, record.month)
        val title = "${record.title} ($multiCountryComment$dayAndMonth)"

        val holiday = determineIsHoliday(record)
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

        fun getEnabledTypes(prefs: SharedPreferences, language: Language): Set<String> {
            return prefs.getStringSet(PREF_HOLIDAY_TYPES, null)
                ?: if (language.isIranExclusive) iranDefault else emptySet()
        }
    }
}
