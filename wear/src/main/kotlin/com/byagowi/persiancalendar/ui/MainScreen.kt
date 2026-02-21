package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.wear.compose.foundation.ScrollInfoProvider
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScreenStage
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.byagowi.persiancalendar.Entry
import com.byagowi.persiancalendar.EntryType
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.generateEntries
import com.byagowi.persiancalendar.generated.EventSource
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter

@Composable
fun MainScreen(
    localeUtils: LocaleUtils,
    navigateToUtilities: () -> Unit,
    navigateToDay: (Jdn) -> Unit,
    preferences: Preferences?,
    today: Jdn,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(
        modifier = modifier,
        scrollState = scrollState,
        edgeButton = {
            EdgeButton(
                onClick = navigateToUtilities,
                buttonSize = EdgeButtonSize.Medium,
            ) { Icon(Icons.Default.Construction, contentDescription = "ابزارها") }
        },
    ) {
        OtherCalendars(
            modifier = Modifier.scrollAway(
                scrollInfoProvider = ScrollInfoProvider(scrollState),
                screenStage = {
                    if (scrollState.canScrollBackward) ScreenStage.Scrolling else ScreenStage.Idle
                },
            ),
            localeUtils = localeUtils,
            day = today,
            onTop = true,
            withWeekDayName = false,
        )
        val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
        var showWarningDialog by rememberSaveable {
            val currentYear = today.toPersianDate().year
            val isOutDated = currentYear > IranianIslamicDateConverter.latestSupportedYearOfIran
            mutableStateOf(isOutDated)
        }
        AlertDialog(
            showWarningDialog,
            { showWarningDialog = false },
            title = { Text("برنامه قدیمی است\n\nمناسبت‌ها دقیق نیست") },
            edgeButton = { EdgeButton({ showWarningDialog = false }) { Text("متوجه شدم") } },
        )
        ScalingLazyColumn(Modifier.fillMaxWidth(), state = scrollState) {
            val entries = generateEntries(
                localeUtils, today, enabledEvents, days = 14, withYear = true,
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
fun EventView(it: Entry, modifier: Modifier = Modifier) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isHoliday = it.type is EntryType.Holiday
    EventButton(
        onClick = { isExpanded = !isExpanded },
        isHoliday = isHoliday,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
    ) {
        Column {
            AnimatedContent(isExpanded, transitionSpec = appCrossfadeSpec) { state ->
                Text(
                    it.title,
                    maxLines = if (state) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxWidth(),
                )
            }
            when (val type = it.type) {
                is EntryType.Holiday if type.source == EventSource.Iran -> "تعطیل"
                is EntryType.NonHoliday if type.source == EventSource.Iran -> "رسمی، دانشگاه تهران"
                is EntryType.NonHoliday if type.source == EventSource.International -> "بین‌المللی"
                else -> null
            }?.let { Text(it, Modifier.alpha(.65f)) }
        }
    }
}

@Composable
private fun EventButton(
    onClick: () -> Unit,
    isHoliday: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (isHoliday) Button(onClick, modifier) { content() }
    else FilledTonalButton(onClick, modifier) { content() }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun MainPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val today = remember { Jdn.today() }
        AppScaffold { MainScreen(LocaleUtils(), {}, {}, null, today) }
    }
}
