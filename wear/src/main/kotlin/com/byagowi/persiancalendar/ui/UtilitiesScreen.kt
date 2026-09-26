package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.byagowi.persiancalendar.icons.material.filled.calendarMonthIcon
import com.byagowi.persiancalendar.icons.material.filled.mapIcon
import com.byagowi.persiancalendar.icons.material.filled.settingsIcon
import com.byagowi.persiancalendar.icons.material.filled.swapVerticalCircleIcon

@Composable
fun UtilitiesScreen(
    navigateToSettings: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToGlobe: () -> Unit,
    navigateToConverter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = scrollState, modifier = modifier) {
        ScalingLazyColumn(state = scrollState) {
            item { ListHeader { Text("ابزارها") } }
            items(
                listOf(
                    Triple(navigateToConverter, swapVerticalCircleIcon, "مبدل"),
                    Triple(navigateToCalendar, calendarMonthIcon, "تقویم"),
                    Triple(navigateToGlobe, mapIcon, "زمین"),
                    Triple(navigateToSettings, settingsIcon, "تنظیمات"),
                ),
            ) { (action, icon, title) ->
                FilledTonalButton(
                    action,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) { Icon(icon, null); Spacer(Modifier.width(4.dp)); Text(title) }
            }
        }
    }
}
