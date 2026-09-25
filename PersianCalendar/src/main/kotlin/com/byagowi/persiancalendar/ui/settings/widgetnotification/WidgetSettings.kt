package com.byagowi.persiancalendar.ui.settings.widgetnotification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.core.content.edit
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
import com.byagowi.persiancalendar.PREF_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.isCenterAlignWidgets
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isWidgetClock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numericalDatePreferred
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColors
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.global.whatToShowOnWidgets
import com.byagowi.persiancalendar.global.widgetTransparency
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.center_align_widgets
import com.byagowi.persiancalendar.shared.generated.resources.center_align_widgets_summary
import com.byagowi.persiancalendar.shared.generated.resources.clock_in_24
import com.byagowi.persiancalendar.shared.generated.resources.clock_on_widget
import com.byagowi.persiancalendar.shared.generated.resources.customize_widget
import com.byagowi.persiancalendar.shared.generated.resources.customize_widget_summary
import com.byagowi.persiancalendar.shared.generated.resources.iran_time
import com.byagowi.persiancalendar.shared.generated.resources.prefer_linear_date
import com.byagowi.persiancalendar.shared.generated.resources.prefer_linear_date_summary
import com.byagowi.persiancalendar.shared.generated.resources.select_widgets_background_color
import com.byagowi.persiancalendar.shared.generated.resources.select_widgets_text_color
import com.byagowi.persiancalendar.shared.generated.resources.showing_clock_in_24
import com.byagowi.persiancalendar.shared.generated.resources.showing_clock_on_widget
import com.byagowi.persiancalendar.shared.generated.resources.showing_iran_time
import com.byagowi.persiancalendar.shared.generated.resources.which_one_to_show
import com.byagowi.persiancalendar.shared.generated.resources.widget_background_color
import com.byagowi.persiancalendar.shared.generated.resources.widget_background_transparency
import com.byagowi.persiancalendar.shared.generated.resources.widget_customization_non_holiday_events
import com.byagowi.persiancalendar.shared.generated.resources.widget_customization_other_calendars
import com.byagowi.persiancalendar.shared.generated.resources.widget_customization_times
import com.byagowi.persiancalendar.shared.generated.resources.widget_customization_times_location
import com.byagowi.persiancalendar.shared.generated.resources.widget_prefer_device_colors
import com.byagowi.persiancalendar.shared.generated.resources.widget_text_color
import com.byagowi.persiancalendar.ui.settings.SettingsColor
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSlider
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.utils.preferences
import org.jetbrains.compose.resources.stringResource
import java.util.TimeZone

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
@Composable
fun WidgetSettings(modifier: Modifier = Modifier) {
    Column(modifier) {
        WidgetColoringSettings()
        SettingsSwitch(
            key = PREF_NUMERICAL_DATE_PREFERRED,
            value = numericalDatePreferred,
            title = stringResource(Res.string.prefer_linear_date),
            summary = stringResource(Res.string.prefer_linear_date_summary),
        )
        SettingsSwitch(
            key = PREF_WIDGET_CLOCK,
            value = isWidgetClock,
            title = stringResource(Res.string.clock_on_widget),
            summary = stringResource(Res.string.showing_clock_on_widget),
        )
        SettingsSwitch(
            key = PREF_WIDGET_IN_24,
            value = clockIn24,
            title = stringResource(Res.string.clock_in_24),
            summary = stringResource(Res.string.showing_clock_in_24),
        )
        SettingsSwitch(
            key = PREF_CENTER_ALIGN_WIDGETS,
            value = isCenterAlignWidgets,
            title = stringResource(Res.string.center_align_widgets),
            summary = stringResource(Res.string.center_align_widgets_summary),
        )
        val isInIranTimeVisible = remember(language) {
            (language.showIranTimeOption || mainCalendar == Calendar.SHAMSI) && TimeZone.getDefault().id != IRAN_TIMEZONE_ID
        }
        if (isInIranTimeVisible) SettingsSwitch(
            key = PREF_IRAN_TIME,
            value = isForcedIranTimeEnabled,
            title = stringResource(Res.string.iran_time),
            summary = stringResource(Res.string.showing_iran_time),
        )
        val widgetCustomizations = remember {
            mapOf(
                OTHER_CALENDARS_KEY to Res.string.widget_customization_other_calendars,
                NON_HOLIDAYS_EVENTS_KEY to Res.string.widget_customization_non_holiday_events,
                OWGHAT_KEY to Res.string.widget_customization_times,
                OWGHAT_LOCATION_KEY to Res.string.widget_customization_times_location,
            )
        }
        SettingsMultiSelect(
            key = PREF_WHAT_TO_SHOW_WIDGETS,
            entries = widgetCustomizations.values.map { stringResource(it) },
            entryValues = remember { widgetCustomizations.keys.toList() },
            persistedSet = whatToShowOnWidgets,
            dialogTitleRes = Res.string.which_one_to_show,
            title = stringResource(Res.string.customize_widget),
            summary = stringResource(Res.string.customize_widget_summary),
        )
    }
}

@Composable
fun WidgetColoringSettings(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
        AnimatedVisibility(!prefersWidgetsDynamicColors) {
            SettingsColor(
                title = stringResource(Res.string.widget_text_color),
                summary = stringResource(Res.string.select_widgets_text_color),
                isBackgroundPick = false,
                key = PREF_SELECTED_WIDGET_TEXT_COLOR,
            )
        }
        AnimatedVisibility(!prefersWidgetsDynamicColors) {
            SettingsColor(
                title = stringResource(Res.string.widget_background_color),
                summary = stringResource(Res.string.select_widgets_background_color),
                isBackgroundPick = true,
                key = PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
            )
        }
    }
}

@Composable
fun WidgetDynamicColorsGlobalSettings(
    prefersWidgetsDynamicColors: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (userSetTheme.isDynamicColors) Box(
            Modifier
                .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                .clearAndSetSemantics {},
        ) {
            SettingsSwitch(
                key = PREF_WIDGETS_PREFER_SYSTEM_COLORS,
                value = prefersWidgetsDynamicColors,
                title = stringResource(Res.string.widget_prefer_device_colors),
            )
        }
        AnimatedVisibility(prefersWidgetsDynamicColors) {
            val key = PREF_WIDGET_TRANSPARENCY
            val context = LocalContext.current
            SettingsSlider(
                title = stringResource(Res.string.widget_background_transparency),
                value = widgetTransparency,
                defaultValue = DEFAULT_WIDGET_TRANSPARENCY,
                onValueChange = { context.preferences.edit { putFloat(key, it) } },
            )
        }
    }
}
