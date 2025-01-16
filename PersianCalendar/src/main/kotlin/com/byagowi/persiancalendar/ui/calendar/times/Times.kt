package com.byagowi.persiancalendar.ui.calendar.times

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.nextTimeColor
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Times(isExpanded: Boolean, prayTimes: PrayTimes, now: Long, isToday: Boolean) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            // Make tab's footer moves smooth
            .animateContentSize(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        val calculationMethod by calculationMethod.collectAsState()
        val times = PrayTime.allTimes(calculationMethod.isJafari)
        val nextTimeColor = nextTimeColor()
        val nextPrayTime = if (isToday) prayTimes.getNextTime(now, times, isExpanded) else null
        times.forEach { prayTime ->
            AnimatedVisibility(
                visible = isExpanded || prayTime.alwaysShown,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                Column(
                    modifier = Modifier.defaultMinSize(minWidth = ItemWidth.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val textColor by animateColor(
                        if (nextPrayTime == prayTime) nextTimeColor else LocalContentColor.current
                    )
                    Text(stringResource(prayTime.stringRes), color = textColor)
                    AnimatedContent(
                        targetState = prayTime.getClock(prayTimes).toFormattedString(),
                        label = "time",
                        transitionSpec = appCrossfadeSpec,
                    ) { state -> Text(state, color = textColor.copy(AppBlendAlpha)) }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun PrayTimes.getNextTime(
    now: Long, timeIds: List<PrayTime>, isExpanded: Boolean
): PrayTime {
    val clock = Clock(Date(now).toGregorianCalendar()).toHoursFraction()
    return timeIds.firstOrNull {
        (isExpanded || it.alwaysShown) && it.getFraction(this) > clock
    } ?: run {
        if (isExpanded) PrayTime.entries[0]
        else PrayTime.entries.firstOrNull { it.alwaysShown } ?: PrayTime.FAJR
    }
}
