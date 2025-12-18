package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.widget.RemoteViews
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
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
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.WeekOfYearSetting
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
    override fun preview(size: DpSize): RemoteViews {
        return create1x1RemoteViews(
            this, size, Jdn.today() on mainCalendar, preferences, appWidgetId
        )
    }

    @Composable
    override fun ColumnScope.Settings() {
        TextScaleSettings()
        WidgetColoringSettings()
    }
}

class Widget4x1ConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize): RemoteViews {
        val jdn = Jdn.today()
        val today = jdn on mainCalendar
        val clock = Clock(GregorianCalendar())
        val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
        val widgetTitle = dayTitleSummary(
            jdn,
            today,
            calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets.value,
        )
        return create4x1RemoteViews(
            this, size, jdn, today, widgetTitle, subtitle, clock,
            preferences, appWidgetId,
        )
    }

    @Composable
    override fun ColumnScope.Settings() {
        TextScaleSettings()
        WidgetSettings()
    }
}

class Widget2x2ConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize): RemoteViews {
        val jdn = Jdn.today()
        val today = jdn on mainCalendar
        val clock = Clock(GregorianCalendar())
        val prayTimes = coordinates.value?.calculatePrayTimes()
        val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
        val widgetTitle = dayTitleSummary(
            jdn,
            today,
            calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets.value,
        )
        return create2x2RemoteViews(
            this, size, jdn, today, widgetTitle, subtitle, prayTimes,
            clock, preferences, appWidgetId,
        )
    }

    @Composable
    override fun ColumnScope.Settings() {
        TextScaleSettings()
        WidgetSettings()
    }
}

class Widget4x2ConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize): RemoteViews {
        val jdn = Jdn.today()
        val date = jdn on mainCalendar
        val clock = Clock(GregorianCalendar())
        return create4x2RemoteViews(
            this,
            size,
            Jdn.today(),
            date,
            clock,
            coordinates.value?.calculatePrayTimes(),
            preferences,
            appWidgetId,
        )
    }

    @Composable
    override fun ColumnScope.Settings() {
        TextScaleSettings()
        WidgetSettings()
        SettingsSectionLayout(R.string.location)
        LocationSettings()
    }
}

class WidgetWeekViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize): RemoteViews {
        val today = Jdn.today()
        val date = today on mainCalendar
        return createWeekViewRemoteViews(this, size, date, today, preferences, appWidgetId)
    }

    @Composable
    override fun ColumnScope.Settings() {
        TextScaleSettings()
        WidgetColoringSettings()
    }
}

class WidgetSunViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize) =
        createSunViewRemoteViews(this, size, coordinates.value?.calculatePrayTimes())

    @Composable
    override fun ColumnScope.Settings() {
        WidgetColoringSettings()
        SettingsSectionLayout(R.string.location)
        LocationSettings()
    }
}

class WidgetMapConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize): RemoteViews =
        createMapRemoteViews(this, size, System.currentTimeMillis())

    @Composable
    override fun ColumnScope.Settings() {
        val prefersWidgetsDynamicColors by prefersWidgetsDynamicColorsFlow.collectAsState()
        WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
    }
}

class WidgetMonthViewConfigurationActivity : BaseWidgetConfigurationActivity() {
    override fun preview(size: DpSize): RemoteViews =
        createMonthViewRemoteViews(this, size, Jdn.today())

    @Composable
    override fun ColumnScope.Settings() {
        WeekOfYearSetting()
        WidgetColoringSettings()
    }
}
