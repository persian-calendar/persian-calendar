package com.byagowi.persiancalendar.ui.calendar.times

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIMES_ITEM
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.nextTimeColor
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.Date

@Composable
fun SharedTransitionScope.Times(
    isExpanded: Boolean,
    prayTimes: PrayTimes,
    now: Long,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val anyAthanEnabled = remember { getEnabledAlarms(context).isNotEmpty() }
    AnimatedContent(
        targetState = isExpanded,
        modifier = modifier,
    ) { isExpandedState ->
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Int.MAX_VALUE else 3,
        ) {
            val isJafari = calculationMethod.isJafari
            val times = PrayTime.allTimes(isJafari)
            val nextTimeColor by animateColor(nextTimeColor())
            val nextPrayTime = if (isToday) prayTimes.getNextTime(now, times, isExpanded, isJafari)
            else null
            times.forEach { prayTime ->
                if (isExpandedState || prayTime.isAlwaysShown(isJafari)) Column(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .defaultMinSize(minWidth = ItemWidth.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = SHARED_CONTENT_KEY_TIMES_ITEM + prayTime.name,
                            ),
                            animatedVisibilityScope = this@AnimatedContent,
                            boundsTransform = appBoundsTransform,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val textColor by animateColor(
                        if (nextPrayTime == prayTime) nextTimeColor else LocalContentColor.current,
                    )
                    Text(
                        buildString {
                            if (anyAthanEnabled && language.isPersian && prayTime.isAthan) append("اذان ")
                            append(stringResource(prayTime.stringRes))
                        },
                        color = textColor,
                    )
                    AnimatedContent(
                        targetState = prayTimes[prayTime].toFormattedString(),
                        transitionSpec = appCrossfadeSpec,
                    ) { state -> Text(state, color = textColor.copy(AppBlendAlpha)) }
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
