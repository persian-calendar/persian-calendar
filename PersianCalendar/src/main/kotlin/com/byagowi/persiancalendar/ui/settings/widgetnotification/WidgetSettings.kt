package com.byagowi.persiancalendar.ui.settings.widgetnotification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_WIDGET_TEXT_SCALE
import com.byagowi.persiancalendar.DEFAULT_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NON_HOLIDAYS_EVENTS_KEY
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.OWGHAT_KEY
import com.byagowi.persiancalendar.OWGHAT_LOCATION_KEY
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.PREF_WIDGET_TEXT_SCALE
import com.byagowi.persiancalendar.PREF_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.isCenterAlignWidgets
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isWidgetClock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numericalDatePreferred
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColorsFlow
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.global.whatToShowOnWidgets
import com.byagowi.persiancalendar.global.widgetTransparency
import com.byagowi.persiancalendar.ui.settings.SettingsColor
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSlider
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
@Composable
fun ColumnScope.WidgetSettings() {
    WidgetColoringSettings()
    run {
        val numericalDatePreferred by numericalDatePreferred.collectAsState()
        SettingsSwitch(
            key = PREF_NUMERICAL_DATE_PREFERRED,
            value = numericalDatePreferred,
            title = stringResource(R.string.prefer_linear_date),
            summary = stringResource(R.string.prefer_linear_date_summary)
        )
    }
    run {
        val isWidgetClock by isWidgetClock.collectAsState()
        SettingsSwitch(
            key = PREF_WIDGET_CLOCK,
            value = isWidgetClock,
            title = stringResource(R.string.clock_on_widget),
            summary = stringResource(R.string.showing_clock_on_widget)
        )
    }
    run {
        val clockIn24 by clockIn24.collectAsState()
        SettingsSwitch(
            key = PREF_WIDGET_IN_24,
            value = clockIn24,
            title = stringResource(R.string.clock_in_24),
            summary = stringResource(R.string.showing_clock_in_24)
        )
    }
    run {
        val isCenterAlignWidgets by isCenterAlignWidgets.collectAsState()
        SettingsSwitch(
            key = PREF_CENTER_ALIGN_WIDGETS,
            value = isCenterAlignWidgets,
            title = stringResource(R.string.center_align_widgets),
            summary = stringResource(R.string.center_align_widgets_summary)
        )
    }
    val language by language.collectAsState()
    val isInIranTimeVisible = remember(language) {
        (language.showIranTimeOption || mainCalendar == Calendar.SHAMSI) && TimeZone.getDefault().id != IRAN_TIMEZONE_ID
    }
    if (isInIranTimeVisible) {
        val isForcedIranTimeEnabled by isForcedIranTimeEnabled.collectAsState()
        SettingsSwitch(
            key = PREF_IRAN_TIME,
            value = isForcedIranTimeEnabled,
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
        key = PREF_WHAT_TO_SHOW_WIDGETS,
        entries = widgetCustomizations.values.map { stringResource(it) },
        entryValues = widgetCustomizations.keys.toList(),
        persistedSet = whatToShowOnWidgets.collectAsState().value,
        dialogTitleResId = R.string.which_one_to_show,
        title = stringResource(R.string.customize_widget),
        summary = stringResource(R.string.customize_widget_summary),
    )
}

@Composable
fun ColumnScope.WidgetColoringSettings() {
    val prefersWidgetsDynamicColors by prefersWidgetsDynamicColorsFlow.collectAsState()
    WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
    this.AnimatedVisibility(!prefersWidgetsDynamicColors) {
        SettingsColor(
            title = stringResource(R.string.widget_text_color),
            summary = stringResource(R.string.select_widgets_text_color),
            isBackgroundPick = false,
            key = PREF_SELECTED_WIDGET_TEXT_COLOR
        )
    }
    this.AnimatedVisibility(!prefersWidgetsDynamicColors) {
        SettingsColor(
            title = stringResource(R.string.widget_background_color),
            summary = stringResource(R.string.select_widgets_background_color),
            isBackgroundPick = true,
            key = PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
        )
    }
}

@Composable
private fun WidgetPreferenceDebounce(
    key: String,
    flow: MutableStateFlow<Float>,
    content: @Composable (value: Float, onValueChange: (Float) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    @OptIn(FlowPreview::class) LaunchedEffect(Unit) {
        flow
            // Debounce to not spam preferences much but specially is needed for
            // map widget as its expensive calculations
            .debounce(.25.seconds).collect { context.preferences.edit { putFloat(key, it) } }
    }
    val value by flow.collectAsState()
    Box(
        Modifier
            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
            .clearAndSetSemantics {},
    ) { content(value) { flow.value = it } }
}

@Composable
fun WidgetTextScale(appWidgetId: Int) {
    val key = PREF_WIDGET_TEXT_SCALE + appWidgetId
    val preferences = LocalContext.current.preferences
    val textScale = remember {
        MutableStateFlow(preferences.getFloat(key, DEFAULT_WIDGET_TEXT_SCALE))
    }
    WidgetPreferenceDebounce(key, textScale) { value, onValueChange ->
        SettingsSlider(
            title = stringResource(R.string.widget_text_size),
            value = value,
            valueRange = .65f..2f,
            visibleScale = 14f,
            defaultValue = 1f,
            onValueChange = onValueChange,
        )
    }
}

@Composable
fun ColumnScope.WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors: Boolean) {
    val userSetTheme by userSetTheme.collectAsState()
    if (userSetTheme.isDynamicColors) Box(
        Modifier
            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
            .clearAndSetSemantics {},
    ) {
        SettingsSwitch(
            key = PREF_WIDGETS_PREFER_SYSTEM_COLORS,
            value = prefersWidgetsDynamicColors,
            title = stringResource(R.string.widget_prefer_device_colors),
        )
    }
    this.AnimatedVisibility(prefersWidgetsDynamicColors) {
        val widgetTransparencyFlow = remember { MutableStateFlow(widgetTransparency.value) }
        val key = PREF_WIDGET_TRANSPARENCY
        WidgetPreferenceDebounce(key, widgetTransparencyFlow) { value, onValueChange ->
            SettingsSlider(
                title = stringResource(R.string.widget_background_transparency),
                value = value,
                defaultValue = DEFAULT_WIDGET_TRANSPARENCY,
                onValueChange = onValueChange,
            )
        }
    }
}
