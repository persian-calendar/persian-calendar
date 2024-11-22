package com.byagowi.persiancalendar.ui.calendar.times

import androidx.annotation.StringRes
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
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
        val timeIds = getTimeNames()
        val nextTimeId = if (isToday) prayTimes.getNextTimeId(now, timeIds, isExpanded) else null
        timeIds.forEach { timeId ->
            AnimatedVisibility(
                visible = isExpanded || when (timeId) {
                    R.string.fajr, R.string.dhuhr, R.string.maghrib -> true
                    else -> false
                },
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                Column(
                    modifier = Modifier.defaultMinSize(minWidth = ItemWidth.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val textColor by animateColor(
                        if (nextTimeId == timeId) MaterialTheme.colorScheme.primary
                        else LocalContentColor.current
                    )
                    Text(stringResource(timeId), color = textColor)
                    AnimatedContent(
                        targetState = prayTimes.getFromStringId(timeId).toFormattedString(),
                        label = "time",
                        transitionSpec = appCrossfadeSpec,
                    ) { state -> Text(state, color = textColor.copy(AppBlendAlpha)) }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@StringRes
private fun PrayTimes.getNextTimeId(now: Long, timeIds: List<Int>, isExpanded: Boolean): Int {
    val clock = Clock(Date(now).toGregorianCalendar()).toHoursFraction()
    return timeIds.firstOrNull {
        when (it) {
            R.string.imsak -> imsak > clock && isExpanded
            R.string.fajr -> fajr > clock
            R.string.sunrise -> sunrise > clock && isExpanded
            R.string.dhuhr -> dhuhr > clock && isExpanded
            R.string.asr -> asr > clock && isExpanded
            R.string.sunset -> sunset > clock && isExpanded
            R.string.maghrib -> maghrib > clock
            R.string.isha -> isha > clock && isExpanded
            R.string.midnight -> midnight > clock && isExpanded
            else -> false
        }
    } ?: timeIds[0]
}
