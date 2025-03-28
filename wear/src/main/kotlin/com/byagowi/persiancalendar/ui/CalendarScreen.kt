package com.byagowi.persiancalendar.ui

import android.icu.text.DateFormatSymbols
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.FadingExpandingLabel
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.byagowi.persiancalendar.EntryType
import com.byagowi.persiancalendar.getEventsOfDay
import com.byagowi.persiancalendar.persianLocale
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.launch
import java.util.GregorianCalendar

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CalendarScreen() {
    val persianLocale = ULocale("fa_IR@calendar=persian")
    val formatSymbols = DateFormatSymbols.getInstance(persianLocale)
    val weekdays =
        formatSymbols.getWeekdays(DateFormatSymbols.STANDALONE, DateFormatSymbols.NARROW).toList()
    val persianMonths = formatSymbols.months.toList()
    val persianDigitsFormatter = run {
        val symbols = DecimalFormatSymbols.getInstance(persianLocale)
        symbols.groupingSeparator = '\u0000'
        DecimalFormat("#", symbols)
    }
    ScreenScaffold {
        var focusedDay by remember { mutableStateOf<Long?>(null) }
        SharedTransitionLayout {
            AnimatedContent(targetState = focusedDay, label = "developers") { state ->
                if (state == null) {
                    CalendarTable(
                        persianDigitsFormatter,
                        weekdays,
                        persianMonths,
                        this,
                    ) { focusedDay = it }
                } else {
                    BackHandler(enabled = true) { focusedDay = null }
                    DayView(
                        state,
                        persianDigitsFormatter,
                        persianMonths,
                        this@AnimatedContent,
                    )
                }
            }
        }
    }
}

private const val SHARED_CONTENT_DAY = "SHARED_CONTENT_DAY"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DayView(
    jdn: Long,
    persianDigitsFormatter: DecimalFormat,
    persianMonths: List<String>,
    animatedContentScope: AnimatedContentScope,
) {
    val persianDate = PersianDate(jdn)
    var text by remember { mutableStateOf("") }
    var showOtherCalendars by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            persianDigitsFormatter.format(persianDate.dayOfMonth),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primaryDim,
            modifier = Modifier
                .padding(top = 16.dp)
//                .sharedBounds(
//                    rememberSharedContentState(key = SHARED_CONTENT_DAY + jdn),
//                    animatedVisibilityScope = animatedContentScope,
//                )
        )
        FadingExpandingLabel(text, textAlign = TextAlign.Center)
    }
    Box(Modifier.fillMaxSize()) {
        val weekDayNames = DateFormatSymbols.getInstance(persianLocale).weekdays.toList()
        val gregorianMonths =
            DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=gregorian")).months.toList()
        val islamicMonths =
            DateFormatSymbols.getInstance(ULocale("fa_IR@calendar=islamic")).months.toList()

        AnimatedVisibility(
            showOtherCalendars,
            enter = fadeIn(spring(stiffness = Spring.StiffnessVeryLow)),
        ) {
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

    LaunchedEffect(Unit) {
        showOtherCalendars = true
        text = persianMonths[persianDate.month - 1] + "\n" + run {
            persianDigitsFormatter.format(persianDate.year)
        } + "/" + run {
            persianDigitsFormatter.format(persianDate.month)
        } + "/" + persianDigitsFormatter.format(persianDate.dayOfMonth)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CalendarTable(
    persianDigitsFormatter: DecimalFormat,
    weekdays: List<String>,
    persianMonths: List<String>,
    animatedContentScope: AnimatedContentScope,
    setDate: (Long) -> Unit,
) {
    val todayJdn = run {
        val gregorianCalendar = GregorianCalendar.getInstance()
        CivilDate(
            gregorianCalendar[Calendar.YEAR],
            gregorianCalendar[Calendar.MONTH] + 1,
            gregorianCalendar[Calendar.DAY_OF_MONTH]
        ).toJdn()
    }
    val weekStartJdn = todayJdn - ((todayJdn + 2) % 7)
    val initialItem = 100
    val state = rememberScalingLazyListState(initialItem)
    val focusedPersianDate = PersianDate(todayJdn + (state.centerItemIndex - initialItem) * 7)
    ScalingLazyColumn(
        state = state,
        verticalArrangement = Arrangement.Top,
    ) {
        items(initialItem * 2) { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                repeat(7) { weekDay ->
                    val jdn = weekStartJdn + weekDay + (row - initialItem) * 7
                    val persianDate = PersianDate(jdn)
                    val civilDate = CivilDate(jdn)
                    val isFocusedMonth =
                        persianDate.year == focusedPersianDate.year && persianDate.month == focusedPersianDate.month
                    val isHoliday = weekDay == 6 || run {
                        getEventsOfDay(emptySet(), civilDate).any { it.type == EntryType.Holiday }
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(
                                2.dp, if (todayJdn == jdn) MaterialTheme.colorScheme.primary
                                else Color.Transparent, RoundedCornerShape(50)
                            )
                            .alpha(if (isFocusedMonth) 1f else .5f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .fillParentMaxSize()
                                .background(
                                    if (isHoliday) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = .7f)
                                    } else Color.Transparent,
                                    RoundedCornerShape(50),
                                )
                                .clickable { setDate(jdn) },
                        )
                        Text(
                            persianDigitsFormatter.format(persianDate.dayOfMonth),
                            color = if (isHoliday) MaterialTheme.colorScheme.onPrimaryContainer
                            else LocalContentColor.current,
                            textAlign = TextAlign.Center,
//                            modifier = Modifier.sharedBounds(
//                                rememberSharedContentState(key = SHARED_CONTENT_DAY + jdn),
//                                animatedVisibilityScope = animatedContentScope,
//                            ),
                        )
                    }
                }
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    Box(
        Modifier
            .clickable { coroutineScope.launch { state.animateScrollToItem(initialItem) } }
            .background(MaterialTheme.colorScheme.background.copy(alpha = .75f)),
    ) {
        ListHeader(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            val formattedYear = persianDigitsFormatter.format(focusedPersianDate.year)
            AnimatedContent(
                targetState = persianMonths[focusedPersianDate.month - 1] + " " + formattedYear,
                transitionSpec = appCrossfadeSpec
            ) { Text(it) }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 12.dp, end = 12.dp),
        ) {
            repeat(7) {
                val a = weekdays[(it + 6) % 7 + 1]
                Text(a, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}
