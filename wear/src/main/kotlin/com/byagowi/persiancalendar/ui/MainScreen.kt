package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.datastore.preferences.core.Preferences
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.byagowi.persiancalendar.Entry
import com.byagowi.persiancalendar.EntryType
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.generateEntries
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter

@Composable
fun MainScreen(
    localeUtils: LocaleUtils,
    navigateToUtilities: () -> Unit,
    navigateToDay: (Jdn) -> Unit,
    preferences: Preferences?,
    today: Jdn,
) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = navigateToUtilities,
                buttonSize = EdgeButtonSize.Medium,
            ) { Icon(Icons.Default.Construction, contentDescription = "تنظیمات") }
        },
    ) {
        Box(Modifier.scrollAway(scrollState)) {
            OtherCalendars(localeUtils, today, onTop = true, withWeekDayName = false)
        }
        val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
        var showWarnDialog by remember {
            val currentYear = today.toPersianDate().year
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
            val entries = generateEntries(
                localeUtils, today, enabledEvents, days = 14, withYear = true
            )
            items(items = entries) {
                if (it.type == EntryType.Date) ListSubHeader {
                    Text(
                        text = it.title,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { it.jdn?.let(navigateToDay) },
                        textAlign = TextAlign.Center,
                    )
                } else EventView(it)
            }
        }
    }
}

@Composable
fun EventView(it: Entry) {
    var isExpanded by remember { mutableStateOf(false) }
    val isHoliday = it.type == EntryType.Holiday
    EventButton(
        onClick = { isExpanded = !isExpanded },
        isHoliday = isHoliday,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        AnimatedContent(isExpanded, transitionSpec = appCrossfadeSpec) { state ->
            Text(
                it.title,
                maxLines = if (state) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EventButton(
    onClick: () -> Unit,
    isHoliday: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    if (isHoliday) Button(onClick, modifier) { content() }
    else FilledTonalButton(onClick, modifier) { content() }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun MainPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppScaffold { MainScreen(LocaleUtils(), {}, {}, null, Jdn.today()) }
    }
}
