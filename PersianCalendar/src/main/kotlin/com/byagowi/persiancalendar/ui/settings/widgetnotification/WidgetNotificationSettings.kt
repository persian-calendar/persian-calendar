package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NON_HOLIDAYS_EVENTS_KEY
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.OWGHAT_KEY
import com.byagowi.persiancalendar.OWGHAT_LOCATION_KEY
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsHorizontalDivider
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.common.ColorPickerDialog
import com.byagowi.persiancalendar.utils.appPrefs
import java.util.TimeZone

@Composable
fun WidgetNotificationSettings() {
    SettingsSection(stringResource(R.string.pref_notification))
    NotificationSettings()
    SettingsHorizontalDivider()
    SettingsSection(stringResource(R.string.pref_widget))
    WidgetConfiguration()
}

@Composable
fun NotificationSettings() {
    val context = LocalContext.current
    val isNotifyDate by isNotifyDate.collectAsState()
    run {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> context.appPrefs.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) } }
        SettingsSwitch(
            key = PREF_NOTIFY_DATE,
            defaultValue = DEFAULT_NOTIFY_DATE,
            title = stringResource(R.string.notify_date),
            summary = stringResource(R.string.enable_notify),
            onBeforeToggle = { value: Boolean ->
                if (value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    false
                } else value
            },
            followChanges = true,
        )
    }
    AnimatedVisibility(isNotifyDate) {
        SettingsSwitch(
            key = PREF_NOTIFY_DATE_LOCK_SCREEN,
            defaultValue = true,
            title = stringResource(R.string.notify_date_lock_screen),
            summary = stringResource(R.string.notify_date_lock_screen_summary)
        )
    }
}

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
@Composable
fun WidgetConfiguration() {
    val context = LocalContext.current
    val appPrefs = remember { context.appPrefs }
    val theme by theme.collectAsState()
    var preferSystemColors by remember(theme) {
        mutableStateOf(
            appPrefs.getBoolean(PREF_WIDGETS_PREFER_SYSTEM_COLORS, theme.isDynamicColors())
        )
    }
    if (theme.isDynamicColors()) {
        SettingsSwitch(
            PREF_WIDGETS_PREFER_SYSTEM_COLORS, true,
            stringResource(R.string.widget_prefer_device_colors),
            onBeforeToggle = { preferSystemColors = it; it },
        )
    }
    AnimatedVisibility(!preferSystemColors) {
        SettingsClickable(
            stringResource(R.string.widget_text_color),
            stringResource(R.string.select_widgets_text_color)
        ) { onDismissRequest ->
            ColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR, onDismissRequest)
        }
    }
    AnimatedVisibility(!preferSystemColors) {
        SettingsClickable(
            stringResource(R.string.widget_background_color),
            stringResource(R.string.select_widgets_background_color)
        ) { onDismissRequest ->
            ColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR, onDismissRequest)
        }
    }
    SettingsSwitch(
        key = PREF_NUMERICAL_DATE_PREFERRED,
        defaultValue = false,
        title = stringResource(R.string.prefer_linear_date),
        summary = stringResource(R.string.prefer_linear_date_summary)
    )
    SettingsSwitch(
        key = PREF_WIDGET_CLOCK,
        defaultValue = true,
        title = stringResource(R.string.clock_on_widget),
        summary = stringResource(R.string.showing_clock_on_widget)
    )
    SettingsSwitch(
        key = PREF_WIDGET_IN_24,
        defaultValue = false,
        title = stringResource(R.string.clock_in_24),
        summary = stringResource(R.string.showing_clock_in_24)
    )
    SettingsSwitch(
        key = PREF_CENTER_ALIGN_WIDGETS,
        defaultValue = true,
        title = stringResource(R.string.center_align_widgets),
        summary = stringResource(R.string.center_align_widgets_summary)
    )
    val isInIranTimeVisible = remember {
        (language.value.showIranTimeOption || mainCalendar == CalendarType.SHAMSI) && TimeZone.getDefault().id != IRAN_TIMEZONE_ID
    }
    if (isInIranTimeVisible) {
        SettingsSwitch(
            key = PREF_IRAN_TIME,
            defaultValue = false,
            title = stringResource(R.string.iran_time),
            summary = stringResource(R.string.showing_iran_time)
        )
    }
    val widgetCustomizations = remember {
        mapOf(
            OTHER_CALENDARS_KEY to R.string.widget_customization_other_calendars,
            NON_HOLIDAYS_EVENTS_KEY to R.string.widget_customization_non_holiday_events,
            OWGHAT_KEY to R.string.widget_customization_owghat,
            OWGHAT_LOCATION_KEY to R.string.widget_customization_owghat_location
        )
    }
    SettingsMultiSelect(
        PREF_WHAT_TO_SHOW_WIDGETS,
        widgetCustomizations.values.map { stringResource(it) },
        widgetCustomizations.keys.toList(),
        DEFAULT_WIDGET_CUSTOMIZATIONS, R.string.which_one_to_show,
        stringResource(R.string.customize_widget),
        stringResource(R.string.customize_widget_summary),
    )
}
