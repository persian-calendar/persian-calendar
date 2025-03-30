package com.byagowi.persiancalendar

import android.icu.text.DateFormatSymbols
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.ULocale
import androidx.collection.IntIntPair
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

enum class EntryType { Date, NonHoliday, Holiday }
class Entry(val title: String, val type: EntryType, val jdn: Long? = null)

private const val spacedComma = "، "

val persianLocale by lazy(LazyThreadSafetyMode.NONE) { ULocale("fa_IR@calendar=persian") }

fun generateEntries(
    startingDay: Long,
    enabledEvents: Set<String>,
    days: Int,
    withYear: Boolean,
): List<Entry> {
    val persianLocale = ULocale("fa_IR@calendar=persian")
    val persianDigitsFormatter = run {
        val symbols = DecimalFormatSymbols.getInstance(persianLocale)
        symbols.groupingSeparator = '\u0000'
        DecimalFormat("#", symbols)
    }
    val persianSymbols = DateFormatSymbols.getInstance(persianLocale)
    val weekDayNames = persianSymbols.weekdays.toList()
    val persianMonths = persianSymbols.months.toList()
    var previousYear = 0
    return (0..<days).flatMap { day ->
        val jdn = startingDay + day
        val civilDate = CivilDate(jdn)
        val persianDate = PersianDate(jdn)
        val events = getEventsOfDay(enabledEvents, civilDate)
        if (events.isNotEmpty() || day == 0) {
            var dateTitle = weekDayNames[((jdn + 1) % 7 + 1).toInt()] + spacedComma +
                    persianDigitsFormatter.format(persianDate.dayOfMonth) + " " +
                    persianMonths[persianDate.month - 1]
            if (withYear && previousYear != persianDate.year) {
                dateTitle += " " + persianDigitsFormatter.format(persianDate.year)
                previousYear = persianDate.year
            }
            listOf(
                Entry(dateTitle, EntryType.Date, jdn)
            ) + events.ifEmpty { listOf(Entry("رویدادی یافت نشد", EntryType.NonHoliday)) }
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
        if (when (it.type) {
                EventType.Iran -> it.isHoliday || iranNonHolidaysKey in enabledEvents
                EventType.International -> internationalKey in enabledEvents
                else -> false
            }
        ) add(Entry(it.title, if (it.isHoliday) EntryType.Holiday else EntryType.NonHoliday))
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
                if (it["type"] == "Iran" && it["calendar"] == "Hijri" && it["rule"] == "end of month" && it["month"]?.toIntOrNull() == islamicDate.month && IslamicDate(
                        jdn + 1
                    ).month != islamicDate.month && (it["holiday"] == "true" || iranNonHolidaysKey in enabledEvents)
                ) add(
                    Entry(
                        it["title"] ?: "",
                        if (it["holiday"] == "true") EntryType.Holiday else EntryType.NonHoliday
                    )
                )
            }
        }
        eventsOfCalendar(groupedGregorianEvents, civilDate, enabledEvents)
    }
    return events.filter { it.type == EntryType.Holiday } + events.filter { it.type == EntryType.NonHoliday }
}
