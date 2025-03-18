package com.byagowi.persiancalendar;

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
import kotlin.time.Duration.Companion.days

enum class EntryType { Date, NonHoliday, Holiday }
class Entry(val title: String, val type: EntryType)

private const val spacedComma = "، "

fun generateEntries(days: Int): List<Entry> {
    val locale = ULocale("fa_IR@calendar=persian")
    val calendar = Calendar.getInstance(locale)
    val weekDayFormat = DateFormat.getPatternInstance(calendar, DateFormat.ABBR_WEEKDAY, locale)
    val monthDayFormat = DateFormat.getPatternInstance(calendar, DateFormat.MONTH_DAY, locale)
    val oneDayInMillis = 1.days.inWholeMilliseconds
    val javaCalendar = java.util.Calendar.getInstance()
    return (0..<days).flatMap { day ->
        val date = javaCalendar.time
        val civilDate = CivilDate(
            javaCalendar[Calendar.YEAR],
            javaCalendar[Calendar.MONTH] + 1,
            javaCalendar[Calendar.DAY_OF_MONTH]
        )
        val events = getEventsOfDay(civilDate)
        javaCalendar.timeInMillis += oneDayInMillis
        if (events.isNotEmpty() || day == 0) {
            val dateTitle = weekDayFormat.format(date) + spacedComma + monthDayFormat.format(date)
            listOf(
                Entry(dateTitle, EntryType.Date)
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
) {
    groupedEvents[IntIntPair(date.month, date.dayOfMonth)]?.forEach {
        if (it.type == EventType.Iran) {
            add(Entry(it.title, if (it.isHoliday) EntryType.Holiday else EntryType.NonHoliday))
        }
    }
}

private fun getEventsOfDay(civilDate: CivilDate): List<Entry> {
    val jdn = civilDate.toJdn()
    val persianDate = PersianDate(jdn)
    val islamicDate = IslamicDate(jdn)
    val events = buildList {
        eventsOfCalendar(groupedPersianEvents, persianDate)
        eventsOfCalendar(groupedIslamicEvents, islamicDate)
        if (islamicDate.dayOfMonth >= 29) {
            irregularRecurringEvents.forEach {
                if (it["type"] == "Iran" && it["calendar"] == "Hijri" &&
                    it["rule"] == "end of month" &&
                    it["month"]?.toIntOrNull() == islamicDate.month &&
                    IslamicDate(jdn + 1).month != islamicDate.month
                ) add(
                    Entry(
                        it["title"] ?: "",
                        if (it["holiday"] == "true") EntryType.Holiday else EntryType.NonHoliday
                    )
                )
            }
        }
        eventsOfCalendar(groupedGregorianEvents, civilDate)
    }
    return events.filter { it.type == EntryType.Holiday } + events.filter { it.type == EntryType.NonHoliday }
}
