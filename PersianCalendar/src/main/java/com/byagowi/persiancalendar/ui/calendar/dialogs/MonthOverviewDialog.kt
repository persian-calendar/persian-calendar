package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.*

@Preview(locale = "fa", showBackground = true)
@Composable
fun MonthOverviewDialog(
    baseJdn: Long = -1L, isDialogOpen: MutableState<Boolean> = mutableStateOf(true)
) {
    val context = LocalContext.current
    val warningIfIsEmpty = stringResource(R.string.warn_if_events_not_set)
    val entries = remember {
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
        }.takeIf { it.isNotEmpty() } ?: listOf(Triple(warningIfIsEmpty, "", ""))
    }

    Surface(color = MaterialTheme.colors.background) {
        if (isDialogOpen.value) {
            val dismissText = stringResource(R.string.close)
            // TODO: Turn it to BottomSheet again
            AlertDialog(
                onDismissRequest = { isDialogOpen.value = false },
                shape = RoundedCornerShape(16.dp),
                text = {
                    LazyColumn {
                        items(entries.size) { index ->
                            val (title: String, holidays: String, nonHolidays: String) = entries[index]
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
                    TextButton(onClick = { isDialogOpen.value = false }) { Text(dismissText) }
                }
            )
        }
    }
}