package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.no_event
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

/** A new date never animates through an empty result or another date's event titles. */
@Composable
internal fun SelectedDayEvents(
    jdn: Jdn,
    events: List<CalendarEvent<*>>,
    isLoading: Boolean,
    navigateToHolidaysSettings: (String?) -> Unit,
    viewEvent: (CalendarEvent.DeviceCalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Retain only the layout measurement across dates, never the previous date's events.
    var contentHeight by remember { mutableIntStateOf(0) }
    val loadingHeight = with(LocalDensity.current) { contentHeight.toDp() }.coerceAtLeast(48.dp)
    var showProgress by remember(jdn, isLoading) { mutableStateOf(false) }
    LaunchedEffect(jdn, isLoading) {
        if (isLoading) {
            // Fast queries should not trade the empty-result flash for a flashing spinner.
            delay(150.milliseconds)
            showProgress = true
        }
    }
    Box(
        modifier.fillMaxWidth().heightIn(min = if (isLoading) loadingHeight else 0.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (isLoading) {
            if (showProgress) CircularProgressIndicator(
                modifier = Modifier.padding(12.dp).size(24.dp),
                strokeWidth = 2.dp,
            )
        } else key(jdn) {
            val opacity = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                opacity.animateTo(1f, animationSpec = tween(durationMillis = 300))
            }
            // DayEvents animates title edits. A different day must start fresh, so its rows
            // cannot inherit the previous day's positional AnimatedContent/tooltip state.
            Box(
                Modifier.onSizeChanged { contentHeight = it.height }
                    .graphicsLayer { alpha = opacity.value },
            ) {
                if (events.isEmpty()) Text(
                    text = stringResource(Res.string.no_event),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp).fillMaxWidth(),
                ) else DayEvents(
                    events = events,
                    navigateToHolidaysSettings = navigateToHolidaysSettings,
                    viewEvent = viewEvent,
                    compact = false,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
                )
            }
        }
    }
}
