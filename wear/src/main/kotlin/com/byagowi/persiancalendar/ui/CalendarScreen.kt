package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.byagowi.persiancalendar.EntryType
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.getEventsOfDay
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    today: Jdn,
    localeUtils: LocaleUtils,
    preferences: Preferences?,
    navigateToDay: (Jdn) -> Unit,
) {
    ScreenScaffold {
        val weekStartJdn = today - ((today.value + 2) % 7).toInt()
        val initialItem = 100
        val state = rememberScalingLazyListState(initialItem)
        val focusedPersianDate = (if (state.centerItemIndex == initialItem) today
        else (weekStartJdn + 3 + (state.centerItemIndex - initialItem) * 7)).toPersianDate()
        val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
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
                        val persianDate = jdn.toPersianDate()
                        val civilDate = jdn.toCivilDate()
                        val isFocusedMonth =
                            persianDate.year == focusedPersianDate.year && persianDate.month == focusedPersianDate.month
                        val events = getEventsOfDay(enabledEvents, civilDate)
                        val isHoliday = weekDay == 6 || events.any { it.type == EntryType.Holiday }
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(
                                    width = 2.dp,
                                    color = if (today == jdn) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(50),
                                )
                                .alpha(if (isFocusedMonth) 1f else .5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .fillParentMaxSize()
                                    .background(
                                        color = if (isHoliday) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = .7f)
                                        } else Color.Transparent,
                                        shape = RoundedCornerShape(50),
                                    )
                                    .clickable { navigateToDay(jdn) },
                            )
                            val foregroundColor =
                                if (isHoliday) MaterialTheme.colorScheme.onPrimaryContainer
                                else LocalContentColor.current
                            Text(
                                localeUtils.format(persianDate.dayOfMonth),
                                color = foregroundColor,
                                textAlign = TextAlign.Center,
                            )
                            Canvas(
                                Modifier
                                    .padding(top = 16.dp)
                                    .size(2.dp)
                            ) { if (events.isNotEmpty()) drawCircle(foregroundColor) }
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
                val formattedYear = localeUtils.format(focusedPersianDate.year)
                AnimatedContent(
                    targetState = localeUtils.persianMonth(focusedPersianDate) + " " + formattedYear,
                    transitionSpec = appCrossfadeSpec
                ) { Text(it) }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 12.dp, end = 12.dp),
            ) {
                repeat(7) {
                    Text(
                        localeUtils.narrowWeekdays[(it + 6) % 7 + 1],
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondaryDim,
                    )
                }
            }
        }
    }
}
