package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.Context
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.getDateFromJdnOfCalendar
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getMonthLength
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents

class MonthOverviewDialogState(baseJdn: Long, context: Context) {
    val isDialogOpen = mutableStateOf(true)
    val entries = run {
        val date = getDateFromJdnOfCalendar(mainCalendar, baseJdn)
        val deviceEvents = readMonthDeviceEvents(context, baseJdn)
        val monthLength = getMonthLength(mainCalendar, date.year, date.month).toLong()
        (0 until monthLength).mapNotNull {
            val jdn = baseJdn + it
            val events = getEvents(jdn, deviceEvents)
            val holidays = getEventsTitle(
                events, holiday = true, compact = false, showDeviceCalendarEvents = false,
                insertRLM = false, addIsHoliday = isHighTextContrastEnabled
            )
            val nonHolidays = getEventsTitle(
                events, holiday = false, compact = false, showDeviceCalendarEvents = true,
                insertRLM = false, addIsHoliday = false
            )
            if (holidays.isEmpty() && nonHolidays.isEmpty()) null
            else Triple(
                dayTitleSummary(
                    getDateFromJdnOfCalendar(mainCalendar, jdn)
                ), holidays, nonHolidays
            )
        }.takeIf { it.isNotEmpty() } ?: listOf(
            Triple(context.getString(R.string.warn_if_events_not_set), "", "")
        )
    }
}

@Composable
fun MonthOverviewDialog(state: MonthOverviewDialogState) {
    val context = LocalContext.current

    Surface(color = MaterialTheme.colors.background) {
        if (state.isDialogOpen.value) {
            val dismissText = stringResource(R.string.close)
            // TODO: Turn it to BottomSheet again
            AlertDialog(
                onDismissRequest = { state.isDialogOpen.value = false },
                shape = RoundedCornerShape(16.dp),
                text = {
                    LazyColumn {
                        items(state.entries.size) { index ->
                            val (title: String, holidays: String, nonHolidays: String) = state.entries[index]
                            Card(
                                elevation = 8.dp,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        copyToClipboard(
                                            View(context), // Well, really a hack
                                            "",
                                            listOf(title, holidays, nonHolidays)
                                                .filter { it.isNotEmpty() }
                                                .joinToString("\n"),
                                            true
                                        )
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(title, style = MaterialTheme.typography.caption)
                                    if (holidays.isNotEmpty())
                                        Text(holidays, color = Color.Magenta)
                                    if (nonHolidays.isNotEmpty())
                                        Text(nonHolidays)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { state.isDialogOpen.value = false }) { Text(dismissText) }
                }
            )
        }
    }
}
