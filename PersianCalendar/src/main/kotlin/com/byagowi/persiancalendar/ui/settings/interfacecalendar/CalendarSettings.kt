package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.database.getIntOrNull
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_SHOW_MOON_IN_SCORPIO
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_AS_HOLIDAY
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_TO_EXCLUDE
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_MOON_IN_SCORPIO
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.WeekDay
import com.byagowi.persiancalendar.global.eventCalendarsIdsAsHoliday
import com.byagowi.persiancalendar.global.eventCalendarsIdsToExclude
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.islamicCalendarOffset
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.showMoonInScorpio
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder.CalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.utils.highlightItem
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.CalendarSettings(destination: String?, destinationItem: String?) {
    val context = LocalContext.current
    val language by language.collectAsState()
    run {
        var shownOnce by rememberSaveable { mutableStateOf(false) }
        SettingsClickable(
            stringResource(R.string.events), stringResource(R.string.events_summary),
            defaultOpen = destination == PREF_HOLIDAY_TYPES && destinationItem != PREF_SHOW_DEVICE_CALENDAR_EVENTS,
        ) { onDismissRequest ->
            HolidaysTypesDialog(destinationItem.takeIf { !shownOnce }) {
                shownOnce = true
                onDismissRequest()
            }
        }
    }
    Box(Modifier.highlightItem(destinationItem == PREF_SHOW_DEVICE_CALENDAR_EVENTS)) {
        var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
        val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
        SettingsSwitch(
            key = PREF_SHOW_DEVICE_CALENDAR_EVENTS, value = isShowDeviceCalendarEvents,
            title = stringResource(R.string.show_device_calendar_events),
            summary = stringResource(R.string.show_device_calendar_events_summary),
            onBeforeToggle = {
                if (it && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showPermissionDialog = true
                    false
                } else it
            },
            extraWidget = {
                var showEventsSettingsDialog by rememberSaveable { mutableStateOf(false) }
                Row {
                    this.AnimatedVisibility(
                        isShowDeviceCalendarEvents && resolveDeviceCalendars {}.isNotEmpty()
                    ) { FilledSettingsButton { showEventsSettingsDialog = true } }
                }
                if (showEventsSettingsDialog) EventsSettingsDialog {
                    showEventsSettingsDialog = false
                }
            },
        )
        if (showPermissionDialog) AskForCalendarPermissionDialog { showPermissionDialog = false }
    }
    SettingsClickable(
        stringResource(R.string.calendars_priority),
        stringResource(R.string.calendars_priority_summary)
    ) { onDismissRequest -> CalendarPreferenceDialog(onDismissRequest) }
    run {
        val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
        SettingsSwitch(
            key = PREF_SHOW_WEEK_OF_YEAR_NUMBER,
            value = isShowWeekOfYearEnabled,
            title = stringResource(R.string.week_number),
            summary = stringResource(R.string.week_number_summary)
        )
    }
    run {
        val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
        SettingsSwitch(
            key = PREF_ASTRONOMICAL_FEATURES,
            value = isAstronomicalExtraFeaturesEnabled,
            title = stringResource(R.string.astronomy),
            summary = stringResource(R.string.astronomical_info_summary),
            onBeforeToggle = {
                val preferences = context.preferences
                if (PREF_SHOW_MOON_IN_SCORPIO !in preferences) preferences.edit {
                    putBoolean(PREF_SHOW_MOON_IN_SCORPIO, DEFAULT_SHOW_MOON_IN_SCORPIO)
                }
                it
            },
        )
        val showMoonInScorpio by showMoonInScorpio.collectAsState()
        AnimatedVisibility(isAstronomicalExtraFeaturesEnabled) {
            SettingsSwitch(
                key = PREF_SHOW_MOON_IN_SCORPIO,
                value = showMoonInScorpio,
                title = stringResource(R.string.moon_in_scorpio),
                extraWidget = {
                    Row(
                        Modifier
                            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                            .clearAndSetSemantics {},
                    ) {
                        @OptIn(ExperimentalMaterial3Api::class) AnimatedVisibility(showMoonInScorpio) {
                            val coroutine = rememberCoroutineScope()
                            val tooltipState = rememberTooltipState()
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                tooltip = { PlainTooltip { Text("نشان عقرب در ستاره‌شناسی") } },
                                state = tooltipState,
                                enableUserInput = false,
                                modifier = if (language.isPersianOrDari) Modifier.clickable(
                                    indication = null,
                                    interactionSource = null,
                                ) { coroutine.launch { tooltipState.show() } } else Modifier,
                            ) {
                                Text(
                                    Zodiac.SCORPIO.symbol,
                                    fontFamily = FontFamily(
                                        Font(R.font.notosanssymbolsregularzodiacsubset)
                                    ),
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                )
                            }
                        }
                    }
                },
                summary = if (language.isPersianOrDari) "نمایش قمر در عقرب در تقویم" else null,
            )
        }
    }
    run {
        LaunchedEffect(Unit) {
            val preferences = context.preferences
            if (PREF_ISLAMIC_OFFSET in preferences && preferences.isIslamicOffsetExpired) preferences.edit {
                putString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET.toString())
            }
        }
        val numeral by numeral.collectAsState()
        SettingsSingleSelect(
            key = PREF_ISLAMIC_OFFSET,
            // One is formatted with locale's numerals and the other used for keys isn't
            entries = (-2..2).map { numeral.format(it) },
            entryValues = (-2..2).map { it.toString() },
            persistedValue = islamicCalendarOffset.collectAsState().value.toString(),
            dialogTitleResId = R.string.islamic_offset,
            title = stringResource(R.string.islamic_offset),
            summaryResId = R.string.islamic_offset_summary,
        )
    }
    val weekStart by weekStart.collectAsState()
    val weekDays = WeekDay.entries.map { it + weekStart.ordinal }
    val weekDaysTitles = weekDays.map { it.title }
    val weekDaysValues = weekDays.map { it.ordinal.toString() }
    SettingsSingleSelect(
        key = PREF_WEEK_START,
        entries = weekDaysTitles,
        entryValues = weekDaysValues,
        persistedValue = weekStart.ordinal.toString(),
        dialogTitleResId = R.string.week_start_summary,
        title = stringResource(R.string.week_start),
    )
    SettingsMultiSelect(
        key = PREF_WEEK_ENDS,
        entries = weekDaysTitles,
        entryValues = weekDaysValues,
        persistedSet = weekEnds.collectAsState().value.map { it.ordinal.toString() }.toSet(),
        dialogTitleResId = R.string.week_ends_summary,
        title = stringResource(R.string.week_ends),
    )
}

@Composable
private fun FilledSettingsButton(onClick: () -> Unit) {
    val defaultColors = IconButtonDefaults.filledIconButtonColors()
    val colors = defaultColors.copy(
        containerColor = animateColor(defaultColors.containerColor).value,
        contentColor = animateColor(defaultColors.contentColor).value,
    )
    FilledIconButton(colors = colors, onClick = onClick) {
        Icon(
            Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings),
        )
    }
}

private data class CalendarsEntry(
    val id: Long,
    val accountName: String,
    val displayName: String,
    val color: Color?,
)

@Composable
private fun EventsSettingsDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val idsToExclude = remember {
        buildList { eventCalendarsIdsToExclude.value.forEach { add(it) } }.toMutableStateList()
    }
    val holidaysIds = remember {
        buildList { eventCalendarsIdsAsHoliday.value.forEach { add(it) } }.toMutableStateList()
    }
    var showHolidaysToggles by rememberSaveable { mutableStateOf(holidaysIds.isNotEmpty()) }
    val calendars = resolveDeviceCalendars {
        Toast.makeText(context, R.string.device_does_not_support, Toast.LENGTH_SHORT).show()
        onDismissRequest()
    }
    DisposableEffect(Unit) {
        onDispose {
            val shownIds = calendars.values.flatten().map { it.id }.toSet()
            context.preferences.edit {
                putString(
                    PREF_CALENDARS_IDS_TO_EXCLUDE,
                    shownIds.intersect(idsToExclude).joinToString(","),
                )
                putString(
                    PREF_CALENDARS_IDS_AS_HOLIDAY,
                    shownIds.intersect(holidaysIds).joinToString(","),
                )
            }
        }
    }
    val language by language.collectAsState()
    val holidayLabel = if (language.isArabicScript) holidayString
    else holidayString.replaceFirstChar { it.uppercaseChar() }
    AppDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.close)) }
        },
        neutralButton = {
            Row {
                this.AnimatedVisibility(!showHolidaysToggles) {
                    TextButton(onClick = { showHolidaysToggles = true }) { Text(holidayLabel) }
                }
            }
        },
    ) {
        var holidayTextWidth by remember { mutableStateOf(64.dp) }
        calendars.entries.forEachIndexed { i, (accountName, values) ->
            Spacer(Modifier.height((if (i == 0) 24 else 16).dp))
            Row {
                Text(
                    accountName,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = 24.dp, bottom = 8.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                this.AnimatedVisibility(i == 0 && showHolidaysToggles, Modifier.alignByBaseline()) {
                    val density = LocalDensity.current
                    Text(
                        holidayLabel,
                        Modifier
                            .onSizeChanged {
                                holidayTextWidth = with(density) { it.width.toDp() }
                            }
                            .padding(end = 16.dp),
                    )
                }
            }
            values.forEach { entry ->
                val visibility = entry.id !in idsToExclude
                val isHoliday = entry.id in holidaysIds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .toggleable(visibility, role = Role.Checkbox) {
                            if (!it) idsToExclude.add(entry.id) else {
                                idsToExclude.remove(entry.id)
                                if (isHoliday) holidaysIds.remove(entry.id)
                            }
                        }
                        .height(42.dp),
                ) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = visibility,
                            onCheckedChange = null,
                            modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                            colors = CheckboxDefaults.colors().let { colors ->
                                if (entry.color != null) colors.copy(
                                    checkedBorderColor = entry.color,
                                    checkedBoxColor = entry.color,
                                    uncheckedBorderColor = entry.color,
                                ) else colors
                            },
                        )
                        Text(entry.displayName)
                    }
                    this.AnimatedVisibility(showHolidaysToggles && visibility) {
                        Checkbox(
                            checked = isHoliday,
                            onCheckedChange = { value ->
                                if (value) holidaysIds.add(entry.id) else holidaysIds.remove(entry.id)
                            },
                            modifier = Modifier
                                .width(holidayTextWidth)
                                .padding(end = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveDeviceCalendars(onFailure: (Throwable) -> Unit): Map<String, List<CalendarsEntry>> {
    val context = LocalContext.current
    val calendars = remember {
        runCatching {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI.buildUpon().build(), arrayOf(
                    CalendarContract.Calendars._ID, // 0
                    CalendarContract.Calendars.ACCOUNT_NAME, // 1
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 2
                    CalendarContract.Calendars.CALENDAR_COLOR, // 3
                    CalendarContract.Calendars.VISIBLE, // 4
                ), null, null, null
            )?.use {
                generateSequence { if (it.moveToNext()) it else null }.filter {
                    it.getString(4) == "1"
                }.map {
                    CalendarsEntry(
                        it.getLong(0),
                        it.getString(1),
                        it.getString(2),
                        it.getIntOrNull(3)?.let(::Color),
                    )
                }.toList().groupBy { it.accountName }
            }
        }.onFailure(logException).onFailure(onFailure).getOrNull() ?: emptyMap()
    }
    return calendars
}
