package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
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

@Composable
fun UtilitiesScreen(
    navigateToSettings: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToConverter: () -> Unit,
) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = scrollState) {
        ScalingLazyColumn(state = scrollState) {
            item { ListHeader { Text("ابزارها") } }
            items(
                listOf(
                    Triple(navigateToConverter, Icons.Default.SwapVerticalCircle, "مبدل"),
                    Triple(navigateToCalendar, Icons.Default.CalendarMonth, "تقویم"),
                    Triple(navigateToSettings, Icons.Default.Settings, "تنظیمات"),
                )
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

