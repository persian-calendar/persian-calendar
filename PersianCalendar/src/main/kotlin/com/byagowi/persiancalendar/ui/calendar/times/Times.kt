package com.byagowi.persiancalendar.ui.calendar.times

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIME
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.nextTimeColor
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.Date

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.Times(
    isExpanded: Boolean, prayTimes: PrayTimes, now: Long, isToday: Boolean
) {
    AnimatedContent(isExpanded) { isExpandedState ->
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.SpaceEvenly,
            maxItemsInEachRow = if (
                LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
            ) Int.MAX_VALUE else 3
        ) {
            val calculationMethod by calculationMethod.collectAsState()
            val isJafari = calculationMethod.isJafari
            val times = PrayTime.allTimes(isJafari)
            val nextTimeColor = nextTimeColor()
            val nextPrayTime =
                if (isToday) prayTimes.getNextTime(now, times, isExpanded, isJafari)
                else null
            times.forEach { prayTime ->
                if (isExpandedState || prayTime.isAlwaysShown(isJafari)) Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = ItemWidth.dp)
                        .sharedBounds(
                            rememberSharedContentState(
                                key = SHARED_CONTENT_KEY_TIME + prayTime.name
                            ),
                            animatedVisibilityScope = this@AnimatedContent,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val textColor by animateColor(
                        if (nextPrayTime == prayTime) nextTimeColor else LocalContentColor.current
                    )
                    Text(stringResource(prayTime.stringRes), color = textColor)
                    AnimatedContent(
                        targetState = prayTimes[prayTime].toFormattedString(),
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
    now: Long, timeIds: List<PrayTime>, isExpanded: Boolean, isJafari: Boolean,
): PrayTime {
    val clock = Clock(Date(now).toGregorianCalendar())
    return timeIds.firstOrNull {
        (isExpanded || it.isAlwaysShown(isJafari)) && this[it] > clock
    } ?: run {
        if (isExpanded) PrayTime.entries[0]
        else PrayTime.entries.firstOrNull { it.isAlwaysShown(isJafari) } ?: PrayTime.FAJR
    }
}
