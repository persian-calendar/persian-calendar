package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.bringDate
import com.byagowi.persiancalendar.ui.calendar.reports.monthHtmlReport
import com.byagowi.persiancalendar.ui.common.SetupDialogBlur
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.createMonthEventsList
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.logException
import io.github.persiancalendar.calendar.AbstractDate

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MonthOverviewDialog(
    viewModel: CalendarViewModel, date: AbstractDate, onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val events = formatComposeEventsList(
        createMonthEventsList(context, date), MaterialTheme.colorScheme.primary
    )

    fun showPrintReport(isLongClick: Boolean) {
        runCatching {
            context.openHtmlInBrowser(monthHtmlReport(context, date, wholeYear = isLongClick))
        }.onFailure(logException)
        onDismissRequest()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        SetupDialogBlur()
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
                val interactionSource = remember { MutableInteractionSource() }
                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .indication(interactionSource = interactionSource, indication = ripple()),
                ) {
                    Text(
                        dayTitleSummary(jdn, jdn.inCalendar(mainCalendar)),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .clickable(indication = null, interactionSource = interactionSource) {
                                onDismissRequest()
                                bringDate(viewModel, jdn, context)
                            },
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
