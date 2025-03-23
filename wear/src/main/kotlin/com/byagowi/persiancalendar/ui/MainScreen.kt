package com.byagowi.persiancalendar.ui

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.byagowi.persiancalendar.Entry
import com.byagowi.persiancalendar.EntryType
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.generateEntries
import com.byagowi.persiancalendar.persianLocal
import com.byagowi.persiancalendar.preferences
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter

@Composable
fun MainScreen(navigateToSettings: () -> Unit) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = navigateToSettings,
                buttonSize = EdgeButtonSize.Medium,
            ) {
                Icon(
                    Icons.Default.Settings, // Icons.Default.Construction,
                    contentDescription = "تنظیمات"
                )
            }
        },
    ) {
        val preferences by preferences.collectAsState()
        val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
        var showWarnDialog by remember {
            val currentYear = Calendar.getInstance(persianLocal).get(Calendar.YEAR)
            val isOutDated = currentYear > IranianIslamicDateConverter.latestSupportedYearOfIran
            mutableStateOf(isOutDated)
        }
        AlertDialog(
            showWarnDialog,
            { showWarnDialog = false },
            title = { Text("برنامه قدیمی است\n\nمناسبت‌ها دقیق نیست") },
            edgeButton = { EdgeButton({ showWarnDialog = false }) { Text("متوجه شدم") } },
        )
        ScalingLazyColumn(Modifier.fillMaxWidth(), state = scrollState) {
            items(items = generateEntries(enabledEvents, days = 14)) { EntryView(it) }
        }
    }
}

@Composable
private fun EntryView(it: Entry) {
    if (it.type == EntryType.Date) ListSubHeader {
        Text(text = it.title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    } else {
        val isHoliday = it.type == EntryType.Holiday
        Text(
            it.title,
            color = if (isHoliday) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                .background(
                    if (isHoliday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(24.dp),
                )
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun MainPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppScaffold { MainScreen {} }
    }
}
