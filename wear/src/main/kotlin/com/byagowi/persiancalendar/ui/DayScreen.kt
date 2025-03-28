package com.byagowi.persiancalendar.ui

import android.icu.text.DateFormatSymbols
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.ULocale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.getEventsOfDay
import com.byagowi.persiancalendar.preferences
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate

@Composable
fun DayScreen(jdn: Long) {
    val persianLocale = ULocale("fa_IR@calendar=persian")
    val formatSymbols = DateFormatSymbols.getInstance(persianLocale)
    val persianMonths = formatSymbols.months.toList()
    val persianDigitsFormatter = run {
        val symbols = DecimalFormatSymbols.getInstance(persianLocale)
        symbols.groupingSeparator = '\u0000'
        DecimalFormat("#", symbols)
    }
    val persianDate = PersianDate(jdn)
    val preferences by preferences.collectAsState()
    val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
    ScalingLazyColumn(
        state = rememberScalingLazyListState(initialCenterItemIndex = 0),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ListHeader {
                Box(contentAlignment = Alignment.TopCenter) {
                    Text(
                        persianDigitsFormatter.format(persianDate.dayOfMonth),
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Text(
                        persianMonths[persianDate.month - 1],
                        modifier = Modifier.padding(top = 44.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        items(getEventsOfDay(enabledEvents, CivilDate(jdn))) { EntryView(it) }
    }
    Box(Modifier.fillMaxSize()) {
        val weekDayNames = DateFormatSymbols.getInstance(persianLocale).weekdays.toList()
        val gregorianMonths =
            DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=gregorian")).months.toList()
        val islamicMonths =
            DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=islamic")).months.toList()
        OtherCalendars(
            weekDayNames = weekDayNames,
            persianMonths = persianMonths,
            gregorianMonths = gregorianMonths,
            islamicMonths = islamicMonths,
            persianDigitsFormatter = persianDigitsFormatter,
            currentJdn = jdn,
            onTop = true,
        )
    }
}
