package com.byagowi.persiancalendar.ui.settings.widgetnotification

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
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
import com.byagowi.persiancalendar.utils.create2x2RemoteViews
import com.byagowi.persiancalendar.utils.create4x1RemoteViews
import com.byagowi.persiancalendar.utils.create4x2RemoteViews
import com.byagowi.persiancalendar.utils.createMapRemoteViews
import com.byagowi.persiancalendar.utils.createMonthViewRemoteViews
import com.byagowi.persiancalendar.utils.createSunViewRemoteViews
import com.byagowi.persiancalendar.utils.createWeekViewRemoteViews
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.preferences
import java.util.GregorianCalendar

class Widget1x1ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        WidgetPreview { context, width, height, appWidgetId ->
            create1x1RemoteViews(
                context, width, height, Jdn.today() on mainCalendar, preferences, appWidgetId
            )
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetTextScale(appWidgetId())
        WidgetColoringSettings()
    }
}

class Widget4x1ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        val jdn = Jdn.today()
        val today = jdn on mainCalendar
        val clock = Clock(GregorianCalendar())
        WidgetPreview { context, width, height, appWidgetId ->
            val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
            val widgetTitle = dayTitleSummary(
                jdn,
                today,
                calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets.value,
            )
            create4x1RemoteViews(
                context, width, height, jdn, today, widgetTitle, subtitle, clock,
                preferences, appWidgetId,
            )
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetTextScale(appWidgetId())
        WidgetSettings()
    }
}

class Widget2x2ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        val jdn = Jdn.today()
        val today = jdn on mainCalendar
        val clock = Clock(GregorianCalendar())
        val coordinates by coordinates.collectAsState()
        val prayTimes = coordinates?.calculatePrayTimes()
        key(prayTimes) {
            WidgetPreview { context, width, height, appWidgetId ->
                val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
                val widgetTitle = dayTitleSummary(
                    jdn,
                    today,
                    calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets.value,
                )
                create2x2RemoteViews(
                    context, width, height, jdn, today, widgetTitle, subtitle, prayTimes,
                    clock, preferences, appWidgetId,
                )
            }
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetTextScale(appWidgetId())
        WidgetSettings()
    }
}

class Widget4x2ConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        val coordinates by coordinates.collectAsState()
        val prayTimes = coordinates?.calculatePrayTimes()
        key(prayTimes) {
            WidgetPreview { context, width, height, appWidgetId ->
                val jdn = Jdn.today()
                val date = jdn on mainCalendar
                val clock = Clock(GregorianCalendar())
                create4x2RemoteViews(
                    context,
                    width,
                    height,
                    Jdn.today(),
                    date,
                    clock,
                    prayTimes,
                    preferences,
                    appWidgetId,
                )
            }
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetTextScale(appWidgetId())
        WidgetSettings()
        SettingsSectionLayout(R.string.location)
        LocationSettings()
    }
}

class WidgetWeekViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        WidgetPreview { context, width, height, appWidgetId ->
            val today = Jdn.today()
            val date = today on mainCalendar
            createWeekViewRemoteViews(context, width, height, date, today, preferences, appWidgetId)
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetTextScale(appWidgetId())
        WidgetColoringSettings()
    }
}

class WidgetSunViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        val coordinates by coordinates.collectAsState()
        val prayTimes = coordinates?.calculatePrayTimes()
        key(prayTimes) {
            WidgetPreview { context, width, height, _ ->
                createSunViewRemoteViews(context, width, height, prayTimes)
            }
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetColoringSettings()
        SettingsSectionLayout(R.string.location)
        LocationSettings()
    }
}

class WidgetMapConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        WidgetPreview { context, width, height, _ ->
            createMapRemoteViews(context, width, height, System.currentTimeMillis())
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        val prefersWidgetsDynamicColors by prefersWidgetsDynamicColorsFlow.collectAsState()
        WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
    }
}

class WidgetMonthViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    @Composable
    override fun Header() {
        WidgetPreview { context, width, height, _ ->
            val today = Jdn.today()
            createMonthViewRemoteViews(context, width, height, true, today)
        }
    }

    @Composable
    override fun ColumnScope.Settings() {
        WidgetColoringSettings()
    }
}
