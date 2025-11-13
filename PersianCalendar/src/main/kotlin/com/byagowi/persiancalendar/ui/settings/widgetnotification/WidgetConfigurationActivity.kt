package com.byagowi.persiancalendar.ui.settings.widgetnotification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_WIDGET_TEXT_SCALE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColorsFlow
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.whatToShowOnWidgets
import com.byagowi.persiancalendar.ui.settings.SettingsSectionLayout
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationSettings
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.create1x1RemoteViews
import com.byagowi.persiancalendar.utils.create4x1RemoteViews
import com.byagowi.persiancalendar.utils.createMapRemoteViews
import com.byagowi.persiancalendar.utils.createMonthViewRemoteViews
import com.byagowi.persiancalendar.utils.createSampleRemoteViews
import com.byagowi.persiancalendar.utils.createSunViewRemoteViews
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.GregorianCalendar

class Widget1x1ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        val key = PREF_WIDGET_TEXT_SCALE + appWidgetId
        val textScale = remember { MutableStateFlow(preferences.getFloat(key, 1f)) }
        BaseLayout(
            preview = {
                val today = Jdn.today() on mainCalendar
                WidgetPreview { context, width, height ->
                    create1x1RemoteViews(context, width, height, today, textScale.value)
                }
            },
            settings = {
                WidgetTextScale(key, textScale)
                WidgetColoringSettings()
            },
        )
    }
}

class Widget4x1ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        val key = PREF_WIDGET_TEXT_SCALE + appWidgetId
        val textScale = remember { MutableStateFlow(preferences.getFloat(key, 1f)) }
        BaseLayout(
            preview = {
                val jdn = Jdn.today()
                val today = jdn on mainCalendar
                val clock = Clock(GregorianCalendar())
                WidgetPreview { context, width, height ->
                    val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
                    val widgetTitle = dayTitleSummary(
                        jdn,
                        today,
                        calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets.value,
                    )
                    create4x1RemoteViews(
                        context, width, height, jdn, today, widgetTitle, subtitle, clock,
                        scale = textScale.value,
                    )
                }
            },
            settings = {
                WidgetTextScale(key, textScale)
                WidgetSettings()
            },
        )
    }
}

class Widget2x2ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        BaseLayout(
            preview = {
                WidgetPreview { context, width, height ->
                    createSampleRemoteViews(context, width, height)
                }
            },
            settings = { WidgetSettings() },
        )
    }
}

class Widget4x2ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        BaseLayout(
            preview = {
                WidgetPreview { context, width, height ->
                    createSampleRemoteViews(context, width, height)
                }
            },
            settings = { WidgetSettings() },
        )
    }
}

class WidgetWeekViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        BaseLayout(
            preview = {
                WidgetPreview { context, width, height ->
                    createSampleRemoteViews(context, width, height)
                }
            },
            settings = { WidgetSettings() },
        )
    }
}

class WidgetSunViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        val coordinates by coordinates.collectAsState()
        BaseLayout(
            preview = {
                val prayTimes = coordinates?.calculatePrayTimes()
                key(prayTimes) {
                    WidgetPreview { context, width, height ->
                        createSunViewRemoteViews(context, width, height, prayTimes)
                    }
                }
            },
            settings = {
                WidgetColoringSettings()
                SettingsSectionLayout(R.string.location) { null }
                LocationSettings(null)
            },
        )
    }
}

class WidgetMapConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        BaseLayout(
            preview = {
                WidgetPreview { context, width, height ->
                    createMapRemoteViews(context, width, height, System.currentTimeMillis())
                }
            },
            settings = {
                val prefersWidgetsDynamicColors by prefersWidgetsDynamicColorsFlow.collectAsState()
                WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
            },
        )
    }
}

class WidgetMonthViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Content(appWidgetId: Int) {
        BaseLayout(
            preview = {
                WidgetPreview(360.dp) { context, width, height ->
                    val today = Jdn.today()
                    createMonthViewRemoteViews(context, width, height, true, today)
                }
            },
            settings = { WidgetColoringSettings() },
        )
    }
}
