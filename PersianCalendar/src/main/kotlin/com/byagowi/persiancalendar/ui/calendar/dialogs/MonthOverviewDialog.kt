package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.stringResource
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
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

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MonthOverviewDialog(date: AbstractDate, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val events = formatComposeEventsList(
        createEventsList(context, date), MaterialTheme.colorScheme.primary
    )

    fun showPrintReport(isLongClick: Boolean) {
        runCatching {
            context.openHtmlInBrowser(
                createEventsReport(context, date, wholeYear = isLongClick)
            )
        }.onFailure(logException)
        onDismissRequest()
        createEventsReport(context, date, wholeYear = isLongClick)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        LazyColumn {
            stickyHeader {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        // TODO: Apply long click on the button itself
                        .combinedClickable(
                            onClick = { showPrintReport(isLongClick = false) },
                            onClickLabel = "Print",
                            onLongClick = { showPrintReport(isLongClick = true) },
                            onLongClickLabel = stringResource(R.string.year),
                        ),
                ) {
                    FloatingActionButton(
                        onClick = { showPrintReport(isLongClick = false) },
                        modifier = Modifier.align(Alignment.Center),
                    ) { Icon(Icons.Default.Print, contentDescription = "Print") }
                }
            }
            if (events.isEmpty()) item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                ) {
                    Text(
                        stringResource(R.string.warn_if_events_not_set),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(all = 16.dp),
                    )
                }
            }
            items(events) { (jdn, text) ->
                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                ) {
                    Text(
                        dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    )
                    SelectionContainer(
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) { Text(text) }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

private fun createEventsList(
    context: Context, date: AbstractDate
): Map<Jdn, List<CalendarEvent<*>>> {
    val baseJdn = Jdn(date)
    val deviceEvents = context.readMonthDeviceEvents(baseJdn)
    return (0..<mainCalendar.getMonthLength(date.year, date.month)).map { baseJdn + it }
        .associateWith { eventsRepository?.getEvents(it, deviceEvents) ?: emptyList() }
}

private fun formatComposeEventsList(
    events: Map<Jdn, List<CalendarEvent<*>>>, holidayColor: Color
): List<Pair<Jdn, AnnotatedString>> {
    return events.toList().sortedBy { (jdn, _) -> jdn.value }.mapNotNull { (jdn, events) ->
        val holidays = getEventsTitle(
            events,
            holiday = true,
            compact = false,
            showDeviceCalendarEvents = false,
            insertRLM = false,
            addIsHoliday = false
        )
        val nonHolidays = getEventsTitle(
            events,
            holiday = false,
            compact = false,
            showDeviceCalendarEvents = true,
            insertRLM = false,
            addIsHoliday = false
        )
        if (holidays.isEmpty() && nonHolidays.isEmpty()) null
        else jdn to buildAnnotatedString {
            if (holidays.isNotEmpty()) withStyle(SpanStyle(color = holidayColor)) {
                append(holidays)
            }
            if (nonHolidays.isNotEmpty()) {
                if (holidays.isNotEmpty()) appendLine()
                append(nonHolidays)
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

private fun createEventsReport(
    context: Context, date: AbstractDate, wholeYear: Boolean
) = createHTML().html {
    attributes["lang"] = language.value.language
    attributes["dir"] = if (context.resources.isRtl) "rtl" else "ltr"
    head {
        meta(charset = "utf8")
        style {
            unsafe {
                val calendarColumnsPercent = 100 / if (isShowWeekOfYearEnabled) 8 else 7
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
            val calendar = date.calendarType
            (1..calendar.getYearMonths(date.year)).map { calendar.createDate(date.year, it, 1) }
        } else listOf(date)).forEach { div("page") { generateMonthPage(context, it) } }
        script { unsafe { +"print()" } }
    }
}

private fun DIV.generateMonthPage(context: Context, date: AbstractDate) {
    val events = createEventsList(context, date)
    fun generateDayClasses(jdn: Jdn, weekEndsAsHoliday: Boolean): String {
        val dayEvents = events[jdn] ?: emptyList()
        return listOf(
            "holiday" to ((jdn.isWeekEnd() && weekEndsAsHoliday) || dayEvents.any { it.isHoliday }),
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
            if (isShowWeekOfYearEnabled) th {}
            repeat(7) { th { +getWeekDayName(revertWeekStartOffsetFromWeekDay(it)) } }
        }
        val monthLength = date.calendarType.getMonthLength(date.year, date.month)
        val monthStartJdn = Jdn(date)
        val startingDayOfWeek = monthStartJdn.dayOfWeek
        val fixedStartingDayOfWeek = applyWeekStartOffsetToWeekDay(startingDayOfWeek)
        val startOfYearJdn = Jdn(date.calendarType, date.year, 1, 1)
        (0..<6 * 7).map {
            val index = it - fixedStartingDayOfWeek
            if (index !in (0..<monthLength)) return@map null
            (index + 1) to (monthStartJdn + index)
        }.chunked(7).forEach { row ->
            val firstJdnInWeek = row.firstNotNullOfOrNull { it?.second/*jdn*/ } ?: return@forEach
            tr {
                if (isShowWeekOfYearEnabled) {
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
                                val secondaryDateDay = jdn.toCalendar(it).dayOfMonth
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
                                +formatNumber(jdn.toCalendar(mainCalendar).dayOfMonth)
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
