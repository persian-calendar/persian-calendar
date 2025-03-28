package com.byagowi.persiancalendar

import android.icu.text.DateFormat
import android.icu.util.Calendar
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
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days

enum class EntryType { Date, NonHoliday, Holiday }
class Entry(val title: String, val type: EntryType, val jdn: Long? = null)

private const val spacedComma = "، "

val persianLocale by lazy(LazyThreadSafetyMode.NONE) { ULocale("fa_IR@calendar=persian") }

fun generateEntries(enabledEvents: Set<String>, days: Int): List<Entry> {
    val locale = persianLocale
    val calendar = Calendar.getInstance(persianLocale)
    val weekDayFormat = DateFormat.getPatternInstance(calendar, DateFormat.ABBR_WEEKDAY, locale)
    val monthDayFormat = DateFormat.getPatternInstance(calendar, DateFormat.MONTH_DAY, locale)
    val oneDayInMillis = 1.days.inWholeMilliseconds
    val gregorianCalendar = GregorianCalendar.getInstance()
    return (0..<days).flatMap { day ->
        val date = gregorianCalendar.time
        val civilDate = CivilDate(
            gregorianCalendar[Calendar.YEAR],
            gregorianCalendar[Calendar.MONTH] + 1,
            gregorianCalendar[Calendar.DAY_OF_MONTH]
        )
        val jdn = civilDate.toJdn()
        val events = getEventsOfDay(enabledEvents, civilDate)
        gregorianCalendar.timeInMillis += oneDayInMillis
        if (events.isNotEmpty() || day == 0) {
            val dateTitle = weekDayFormat.format(date) + spacedComma + monthDayFormat.format(date)
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
