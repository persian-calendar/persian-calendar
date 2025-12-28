package com.byagowi.persiancalendar

import androidx.collection.IntIntPair
import com.byagowi.persiancalendar.generated.CalendarRecord
import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.generated.gregorianEvents
import com.byagowi.persiancalendar.generated.irregularRecurringEvents
import com.byagowi.persiancalendar.generated.islamicEvents
import com.byagowi.persiancalendar.generated.persianEvents
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

sealed interface EntryType {
    object Date : EntryType
    class NonHoliday(val source: EventSource?) : EntryType
    class Holiday(val source: EventSource?) : EntryType
}

class Entry(val title: String, val type: EntryType, val jdn: Jdn? = null)

private const val spacedComma = "، "

fun generateEntries(
    localeUtils: LocaleUtils,
    startingDay: Jdn,
    enabledEvents: Set<String>,
    days: Int,
    withYear: Boolean,
): List<Entry> {
    var previousYear = 0
    return (0..<days).flatMap { day ->
        val jdn = startingDay + day
        val civilDate = jdn.toCivilDate()
        val persianDate = jdn.toPersianDate()
        if (previousYear == 0) previousYear = persianDate.year
        val events = getEventsOfDay(enabledEvents, civilDate)
        if (events.isNotEmpty() || day == 0) {
            var dateTitle = localeUtils.weekDayName(jdn) + spacedComma +
                    localeUtils.format(persianDate.dayOfMonth) + " " +
                    localeUtils.persianMonth(persianDate)
            if (withYear && (previousYear != persianDate.year || day == 0)) {
                dateTitle += " " + localeUtils.format(persianDate.year)
            }
            previousYear = persianDate.year
            listOf(
                Entry(dateTitle, EntryType.Date, jdn),
            ) + events.ifEmpty { listOf(Entry("رویدادی یافت نشد", EntryType.NonHoliday(null))) }
        } else emptyList()
    }
}

private val groupedPersianEvents by lazy(LazyThreadSafetyMode.NONE) {
    persianEvents.groupBy { IntIntPair(it.month, it.day) }
}
private val groupedIslamicEvents by lazy(LazyThreadSafetyMode.NONE) {
    islamicEvents.groupBy { IntIntPair(it.month, it.day) }
}
private val groupedGregorianEvents by lazy(LazyThreadSafetyMode.NONE) {
    gregorianEvents.groupBy { IntIntPair(it.month, it.day) }
}

private fun MutableList<Entry>.eventsOfCalendar(
    groupedEvents: Map<IntIntPair, List<CalendarRecord>>,
    date: AbstractDate,
    enabledEvents: Set<String>,
) {
    groupedEvents[IntIntPair(date.month, date.dayOfMonth)]?.forEach {
        if (when (it.source) {
                EventSource.Iran -> it.isHoliday || iranNonHolidaysKey in enabledEvents
                EventSource.International -> internationalKey in enabledEvents
                else -> false
            }
        ) add(
            Entry(
                it.title,
                if (it.isHoliday) EntryType.Holiday(it.source)
                else EntryType.NonHoliday(it.source),
            ),
        )
    }
}

const val iranNonHolidaysKey = "iranNonHolidays"
const val internationalKey = "international"

fun getEventsOfDay(enabledEvents: Set<String>, civilDate: CivilDate): List<Entry> {
    val jdn = civilDate.toJdn()
    val persianDate = PersianDate(jdn)
    val islamicDate = IslamicDate(jdn)
    val events = buildList {
        eventsOfCalendar(groupedPersianEvents, persianDate, enabledEvents)
        eventsOfCalendar(groupedIslamicEvents, islamicDate, enabledEvents)
        if (islamicDate.dayOfMonth >= 29) {
            irregularRecurringEvents.forEach {
                val type = EventSource.entries.firstOrNull { entry -> entry.name == it["type"] }
                if (it["type"] == "Iran" && it["calendar"] == "Hijri" && it["rule"] == "end of month" && it["month"]?.toIntOrNull() == islamicDate.month && IslamicDate(
                        jdn + 1,
                    ).month != islamicDate.month && (it["holiday"] == "true" || iranNonHolidaysKey in enabledEvents)
                ) add(
                    Entry(
                        it["title"].orEmpty(),
                        if (it["holiday"] == "true") EntryType.Holiday(type)
                        else EntryType.NonHoliday(type),
                    ),
                )
            }
        }
        eventsOfCalendar(groupedGregorianEvents, civilDate, enabledEvents)
    }
    return events.filter { it.type is EntryType.Holiday } + events.filter { it.type is EntryType.NonHoliday }
}
