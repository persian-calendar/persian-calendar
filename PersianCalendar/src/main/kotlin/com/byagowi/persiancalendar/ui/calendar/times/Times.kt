package com.byagowi.persiancalendar.ui.calendar.times

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
import io.github.persiancalendar.praytimes.PrayTimes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Times(isExpanded: Boolean, prayTimes: PrayTimes) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        getTimeNames().forEach { timeId ->
            AnimatedVisibility(
                visible = isExpanded || when (timeId) {
                    com.byagowi.persiancalendar.R.string.fajr, com.byagowi.persiancalendar.R.string.dhuhr, com.byagowi.persiancalendar.R.string.maghrib -> true
                    else -> false
                },
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                AnimatedContent(
                    targetState = prayTimes.getFromStringId(timeId).toFormattedString(),
                    label = "time",
                    transitionSpec = appCrossfadeSpec,
                ) { state ->
                    Column(
                        modifier = Modifier.defaultMinSize(
                            minWidth = dimensionResource(com.byagowi.persiancalendar.R.dimen.time_item_size),
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(timeId))
                        Text(state, modifier = Modifier.alpha(AppBlendAlpha))
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
