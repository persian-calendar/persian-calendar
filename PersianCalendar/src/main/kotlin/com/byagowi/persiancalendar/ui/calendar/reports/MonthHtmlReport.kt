package com.byagowi.persiancalendar.ui.calendar.reports

import android.content.Context
import androidx.annotation.CheckResult
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.createMonthEventsList
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.html.DIV
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.small
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.sub
import kotlinx.html.sup
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import kotlinx.html.unsafe

@CheckResult
fun monthHtmlReport(context: Context, date: AbstractDate, wholeYear: Boolean) = createHTML().html {
    attributes["lang"] = language.value.language
    attributes["dir"] = if (context.resources.isRtl) "rtl" else "ltr"
    head {
        meta(charset = "utf8")
        style {
            unsafe {
                val calendarColumnsPercent = 100 / if (isShowWeekOfYearEnabled.value) 8 else 7
                +"""
                    body { font-family: system-ui }
                    td { vertical-align: top }
                    table.calendar td, table.calendar th {
                        width: $calendarColumnsPercent%;
                        text-align: center;
                        height: 2em;
                    }
                    .holiday { color: red; font-weight: bold }
                    .hasEvents { border-bottom: 1px dotted; }
                    table.events { padding: 1em 0; font-size: 95% }
                    table.events td { width: 50%; padding: 0 1em }
                    table { width: 100% }
                    h1 { text-align: center }
                    .page { break-after: page }
                    sup { font-size: x-small; position: absolute }
                """.trimIndent()
            }
        }
    }
    body {
        (if (wholeYear) {
            val calendar = date.calendar
            (1..calendar.getYearMonths(date.year)).map { calendar.createDate(date.year, it, 1) }
        } else listOf(date)).forEach { div("page") { generateMonthPage(context, it) } }
        script { unsafe { +"print()" } }
    }
}

private fun DIV.generateMonthPage(context: Context, date: AbstractDate) {
    val events = createMonthEventsList(context, date)
    fun generateDayClasses(jdn: Jdn, weekEndsAsHoliday: Boolean): String {
        val dayEvents = events[jdn] ?: emptyList()
        return listOf(
            "holiday" to ((jdn.isWeekEnd && weekEndsAsHoliday) || dayEvents.any { it.isHoliday }),
            "hasEvents" to dayEvents.isNotEmpty()
        ).filter { it.second }.joinToString(" ") { it.first }
    }
    h1 {
        +language.value.my.format(date.monthName, formatNumber(date.year))
        val title = monthFormatForSecondaryCalendar(date, secondaryCalendar ?: return@h1)
        small { +" ($title)" }
    }
    table("calendar") {
        tr {
            if (isShowWeekOfYearEnabled.value) th {}
            repeat(7) { th { +getWeekDayName(revertWeekStartOffsetFromWeekDay(it)) } }
        }
        val monthLength = date.calendar.getMonthLength(date.year, date.month)
        val monthStartJdn = Jdn(date)
        val startingWeekDay = monthStartJdn.weekDay
        val fixedStartingWeekDay = applyWeekStartOffsetToWeekDay(startingWeekDay)
        val startOfYearJdn = Jdn(date.calendar, date.year, 1, 1)
        (0..<6 * 7).map {
            val index = it - fixedStartingWeekDay
            if (index !in (0..<monthLength)) return@map null
            (index + 1) to (monthStartJdn + index)
        }.chunked(7).forEach { row ->
            val firstJdnInWeek = row.firstNotNullOfOrNull { it?.second/*jdn*/ } ?: return@forEach
            tr {
                if (isShowWeekOfYearEnabled.value) {
                    val weekOfYear = firstJdnInWeek.getWeekOfYear(startOfYearJdn)
                    th { sub { small { +formatNumber(weekOfYear) } } }
                }
                row.forEach { pair ->
                    td {
                        val (dayOfMonth, jdn) = pair ?: return@td
                        span(generateDayClasses(jdn, true)) {
                            +formatNumber(dayOfMonth)
                        }
                        listOfNotNull(
                            secondaryCalendar?.let {
                                val secondaryDateDay = jdn.inCalendar(it).dayOfMonth
                                val digits = secondaryCalendarDigits
                                formatNumber(secondaryDateDay, digits)
                            }, getShiftWorkTitle(jdn)
                        ).joinToString(" ").takeIf { it.isNotEmpty() }?.let { sup { +" $it" } }
                    }
                }
            }
        }
    }
    table("events") {
        tr {
            val titles = formatPrintEventsList(events)
            if (titles.isEmpty()) return@tr
            val sizes =
                titles.map { it.second.toString().length }.runningFold(0) { acc, it -> acc + it }
            val halfOfTotal = sizes.last() / 2
            val center = sizes.indexOfFirst { it > halfOfTotal }
            listOf(titles.take(center), titles.drop(center)).forEach {
                td {
                    it.forEach { (jdn, title) ->
                        div {
                            span(generateDayClasses(jdn, false)) {
                                +formatNumber(jdn.inCalendar(mainCalendar).dayOfMonth)
                            }
                            +spacedColon
                            +title.toString()
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrintEventsList(events: Map<Jdn, List<CalendarEvent<*>>>): List<Pair<Jdn, CharSequence>> {
    val result = events.toList().sortedBy { (jdn, _) -> jdn.value }.mapNotNull { (jdn, events) ->
        val holidays = getEventsTitle(
            events,
            holiday = true,
            compact = true,
            showDeviceCalendarEvents = false,
            insertRLM = false,
            addIsHoliday = true
        )
        val nonHolidays = getEventsTitle(
            events,
            holiday = false,
            compact = true,
            showDeviceCalendarEvents = true,
            insertRLM = false,
            addIsHoliday = true
        )
        if (holidays.isEmpty() && nonHolidays.isEmpty()) null
        else jdn to buildSpannedString {
            if (holidays.isNotEmpty()) color(android.graphics.Color.RED) { append(holidays) }
            if (nonHolidays.isNotEmpty()) {
                if (holidays.isNotEmpty()) appendLine()
                append(nonHolidays)
            }
        }
    }
    return result.map { (jdn, title) ->
        jdn to title.toString().replace("\n", " $EN_DASH ")
    }
}
