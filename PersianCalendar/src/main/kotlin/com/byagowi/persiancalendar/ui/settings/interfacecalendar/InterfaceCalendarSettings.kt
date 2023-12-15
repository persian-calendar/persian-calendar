package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
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
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsDivider
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder.CalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired

@Composable
fun InterfaceCalendarSettings(destination: String? = null) {
    SettingsSection(stringResource(R.string.pref_interface))
    val context = LocalContext.current
    run {
        val themeDisplayName = stringResource(run {
            val currentKey = context.appPrefs.getString(PREF_THEME, null)
            Theme.entries.firstOrNull { it.key == currentKey } ?: Theme.SYSTEM_DEFAULT
        }.title)
        SettingsClickable(
            title = stringResource(R.string.select_skin), summary = themeDisplayName
        ) { onDismissRequest -> ThemeDialog(onDismissRequest) }
    }
    SettingsClickable(
        title = if (destination == PREF_APP_LANGUAGE) "Language" else stringResource(R.string.language),
        summary = language.nativeName,
    ) { onDismissRequest -> LanguageDialog(onDismissRequest) }
    if (language.isArabic) {
        SettingsSwitch(
            PREF_EASTERN_GREGORIAN_ARABIC_MONTHS,
            DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS,
            "السنة الميلادية بالاسماء الشرقية",
            "كانون الثاني، شباط، آذار، …"
        )
    }
    if (language.isPersian) {
        SettingsSwitch(
            PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
            DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
            "ماه‌های میلادی با نام انگلیسی",
            "جون، جولای، آگوست، …"
        )
    }
    // TODO: To be integrated into the language selection dialog one day
    if (language.canHaveLocalDigits) {
        SettingsSwitch(
            PREF_LOCAL_DIGITS,
            true,
            stringResource(R.string.native_digits),
            stringResource(R.string.enable_native_digits)
        )
    }

    SettingsDivider()
    SettingsSection(stringResource(R.string.calendar))
    run {
        SettingsClickable(
            stringResource(R.string.events), stringResource(R.string.events_summary)
        ) { onDismissRequest -> HolidaysTypesDialog(onDismissRequest) }

        var showDialog by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(null) { if (destination == PREF_HOLIDAY_TYPES) showDialog = true }
        if (showDialog) HolidaysTypesDialog { showDialog = false }
    }
    run {
        var showDialog by remember { mutableStateOf(false) }
        SettingsSwitch(
            PREF_SHOW_DEVICE_CALENDAR_EVENTS, false,
            stringResource(R.string.show_device_calendar_events),
            stringResource(R.string.show_device_calendar_events_summary),
            onBeforeToggle = {
                if (it && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showDialog = true
                    false
                } else it
            },
            followChanges = true,
        )
        if (showDialog) AskForCalendarPermissionDialog { showDialog = false }
    }
    SettingsClickable(
        stringResource(R.string.calendars_priority),
        stringResource(R.string.calendars_priority_summary)
    ) { onDismissRequest -> CalendarPreferenceDialog(onDismissRequest) }
    SettingsSwitch(
        PREF_ASTRONOMICAL_FEATURES,
        false,
        stringResource(R.string.astronomy),
        stringResource(R.string.astronomical_info_summary)
    )
    SettingsSwitch(
        PREF_SHOW_WEEK_OF_YEAR_NUMBER,
        false,
        stringResource(R.string.week_number),
        stringResource(R.string.week_number_summary)
    )
    run {
        LaunchedEffect(null) {
            val appPrefs = context.appPrefs
            if (PREF_ISLAMIC_OFFSET in appPrefs && appPrefs.isIslamicOffsetExpired) appPrefs.edit {
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
        summary = stringResource(R.string.week_ends),
    )
}
