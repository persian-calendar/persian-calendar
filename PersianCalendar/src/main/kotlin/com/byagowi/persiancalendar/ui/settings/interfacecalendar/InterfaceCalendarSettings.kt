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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.database.getIntOrNull
import com.byagowi.persiancalendar.DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_TO_EXCLUDE
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.eventCalendarsIdsToExclude
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsHorizontalDivider
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.SettingsSwitchWithInnerState
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder.CalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun ColumnScope.InterfaceCalendarSettings(destination: String? = null) {
    SettingsSection(stringResource(R.string.pref_interface))
    val context = LocalContext.current
    run {
        val themeDisplayName = stringResource(run {
            val currentKey = context.preferences.getString(PREF_THEME, null)
            Theme.entries.firstOrNull { it.key == currentKey } ?: Theme.SYSTEM_DEFAULT
        }.title)
        SettingsClickable(
            title = stringResource(R.string.select_skin),
            summary = themeDisplayName,
            defaultOpen = destination == PREF_THEME,
        ) { onDismissRequest -> ThemeDialog(onDismissRequest) }
    }
    val language by language.collectAsState()
    SettingsClickable(
        title = stringResource(R.string.language),
        summary = language.nativeName,
    ) { onDismissRequest -> LanguageDialog(onDismissRequest) }
    AnimatedVisibility(language.isArabic) {
        SettingsSwitchWithInnerState(
            PREF_EASTERN_GREGORIAN_ARABIC_MONTHS,
            DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS,
            "السنة الميلادية بالاسماء الشرقية",
            "كانون الثاني، شباط، آذار، …"
        )
    }
    AnimatedVisibility(language.isPersian) {
        SettingsSwitchWithInnerState(
            PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
            DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
            "ماه‌های میلادی با نام انگلیسی",
            "جون، جولای، آگوست، …"
        )
    }
    // TODO: To be integrated into the language selection dialog one day
    AnimatedVisibility(language.canHaveLocalDigits) {
        SettingsSwitchWithInnerState(
            PREF_LOCAL_DIGITS,
            true,
            stringResource(R.string.native_digits),
            stringResource(R.string.enable_native_digits)
        )
    }

    SettingsHorizontalDivider()
    SettingsSection(stringResource(R.string.calendar))
    SettingsClickable(
        stringResource(R.string.events), stringResource(R.string.events_summary),
        defaultOpen = destination == PREF_HOLIDAY_TYPES,
    ) { onDismissRequest -> HolidaysTypesDialog(onDismissRequest) }
    run {
        var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
        val isShowDeviceCalendarEvents by isShowDeviceCalendarEvents.collectAsState()
        SettingsSwitch(
            PREF_SHOW_DEVICE_CALENDAR_EVENTS, isShowDeviceCalendarEvents,
            stringResource(R.string.show_device_calendar_events),
            stringResource(R.string.show_device_calendar_events_summary),
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
                    AnimatedVisibility(
                        isShowDeviceCalendarEvents && resolveDeviceCalendars {}.isNotEmpty()
                    ) {
                        FilledIconButton(onClick = { showEventsSettingsDialog = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                }
                if (showEventsSettingsDialog) EventsToExcludeDialog {
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
    SettingsSwitchWithInnerState(
        PREF_ASTRONOMICAL_FEATURES,
        false,
        stringResource(R.string.astronomy),
        stringResource(R.string.astronomical_info_summary)
    )
    SettingsSwitchWithInnerState(
        PREF_SHOW_WEEK_OF_YEAR_NUMBER,
        false,
        stringResource(R.string.week_number),
        stringResource(R.string.week_number_summary)
    )
    run {
        LaunchedEffect(Unit) {
            val preferences = context.preferences
            if (PREF_ISLAMIC_OFFSET in preferences && preferences.isIslamicOffsetExpired) preferences.edit {
                putString(
                    PREF_ISLAMIC_OFFSET,
                    DEFAULT_ISLAMIC_OFFSET
                )
            }
        }
        SettingsSingleSelect(
            PREF_ISLAMIC_OFFSET,
            // One is formatted with locale's numerals and the other used for keys isn't
            (-2..2).map { formatNumber(it.toString()) },
            (-2..2).map { it.toString() },
            DEFAULT_ISLAMIC_OFFSET,
            R.string.islamic_offset,
            stringResource(R.string.islamic_offset),
            R.string.islamic_offset_summary,
        )
    }
    val weekDaysValues = (0..6).map { it.toString() }
    SettingsSingleSelect(
        key = PREF_WEEK_START,
        entries = weekDays,
        entryValues = weekDaysValues,
        defaultValue = language.defaultWeekStart,
        dialogTitleResId = R.string.week_start_summary,
        title = stringResource(R.string.week_start),
    )
    SettingsMultiSelect(
        key = PREF_WEEK_ENDS,
        entries = weekDays,
        entryValues = weekDaysValues,
        defaultValue = language.defaultWeekEnds,
        dialogTitleResId = R.string.week_ends_summary,
        title = stringResource(R.string.week_ends),
    )
}

private data class CalendarsEntry(
    val id: Long,
    val accountName: String,
    val displayName: String,
    val color: Color?,
)

@Composable
private fun EventsToExcludeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val idsToExclude = remember {
        buildList { eventCalendarsIdsToExclude.value.forEach { add(it) } }.toMutableStateList()
    }
    DisposableEffect(Unit) {
        onDispose {
            context.preferences.edit {
                putString(PREF_CALENDARS_IDS_TO_EXCLUDE, idsToExclude.toSet().joinToString(","))
            }
        }
    }
    val calendars = resolveDeviceCalendars {
        Toast.makeText(context, R.string.device_does_not_support, Toast.LENGTH_SHORT).show()
        onDismissRequest()
    }
    AppDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.close)) }
        },
    ) {
        Spacer(Modifier.height(8.dp))
        calendars.forEach { (accountName, values) ->
            Box(Modifier.height(SettingsItemHeight.dp), contentAlignment = Alignment.BottomStart) {
                Text(
                    accountName,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            values.forEach {
                val checked = it.id !in idsToExclude
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            if (checked) idsToExclude.add(it.id) else idsToExclude.remove(it.id)
                        }
                        .height(42.dp)
                        .fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                        colors = CheckboxDefaults.colors().let { colors ->
                            if (it.color != null) colors.copy(
                                checkedBorderColor = it.color,
                                checkedBoxColor = it.color,
                                uncheckedBorderColor = it.color,
                            ) else colors
                        },
                    )
                    Text(it.displayName)
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
