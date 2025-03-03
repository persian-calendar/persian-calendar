/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.byagowi.persiancalendar

import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.tooling.preview.devices.WearDevices
import java.util.Date
import kotlin.time.Duration.Companion.days

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { WearApp() } }
    }
}

private enum class EntryType { Date, NonHoliday, Holiday }
private class Entry(val title: String, val type: EntryType)

private fun generateEntries(days: Int): List<Entry> {
    val locale = ULocale("fa_IR@calendar=persian")
    val date = Date()
    val weekDayFormat = DateFormat.getPatternInstance(
        Calendar.getInstance(locale),
        DateFormat.ABBR_WEEKDAY,
        locale
    )
    val monthDayFormat = DateFormat.getPatternInstance(
        Calendar.getInstance(locale),
        DateFormat.MONTH_DAY,
        locale
    )
    val oneDayInMillis = 1.days.inWholeMilliseconds
    return (0..<days).flatMap { day ->
        date.time += oneDayInMillis
        val events = if (day % 3 == 2) listOf("لبیلب" to true, "تا" to false) else emptyList()
        if (events.isNotEmpty() || day == 0) {
            val dateTitle = weekDayFormat.format(date) + "، " + monthDayFormat.format(date)
            listOf(
                Entry(dateTitle, EntryType.Date)
            ) + events.map { (title, isHoliday) ->
                Entry(title, if (isHoliday) EntryType.Holiday else EntryType.NonHoliday)
            }.ifEmpty { listOf(Entry("مناسبتی تنظیم نشده", EntryType.NonHoliday)) }
        } else emptyList()
    }
}

@Composable
private fun WearApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.background(MaterialTheme.colors.background)) {
            val entries = generateEntries(14)
            val scrollState = rememberScalingLazyListState()
            TimeText(Modifier.scrollAway(scrollState))
            ScalingLazyColumn(Modifier.fillMaxWidth(), scrollState) {
                items(entries) {
                    when (it.type) {
                        EntryType.Date -> Text(
                            text = it.title,
                            style = MaterialTheme.typography.caption1,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        else -> {
                            val isHoliday = it.type == EntryType.Holiday
                            Text(
                                it.title,
                                color = if (isHoliday) MaterialTheme.colors.onPrimary
                                else MaterialTheme.colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                                    .background(
                                        if (isHoliday) MaterialTheme.colors.primary
                                        else MaterialTheme.colors.surface,
                                        RoundedCornerShape(24.dp),
                                    )
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
            ScrollIndicator(modifier = Modifier.align(Alignment.CenterEnd), state = scrollState)
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() = WearApp()
