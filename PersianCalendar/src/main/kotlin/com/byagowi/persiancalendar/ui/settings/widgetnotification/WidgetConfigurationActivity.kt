package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.content.Context
import android.content.SharedPreferences
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.PREF_WIDGET_TEXT_SCALE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColorsFlow
import com.byagowi.persiancalendar.global.spacedComma
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
                    create4x1RemoteViews(
                        context, width, height, jdn, today, "", subtitle, clock,
                        scale = textScale.value,
                    )
                }
            },
            settings = {
                WidgetTextScale(key, textScale)
                WidgetColoringSettings()
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

@Composable
fun WidgetPreview(height: Dp = 78.dp, widgetFactory: (Context, Int, Int) -> RemoteViews) {
    BoxWithConstraints(
        Modifier
            .padding(vertical = 16.dp)
            .height(height),
    ) {
        val width = with(LocalDensity.current) { (this@BoxWithConstraints).maxWidth.roundToPx() }
        val height = with(LocalDensity.current) { (this@BoxWithConstraints).maxHeight.roundToPx() }
        val preferences = LocalContext.current.preferences
        var updateCallback by remember { mutableStateOf({}) }
        DisposableEffect(preferences) {
            val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                updateCallback()
            }
            preferences.registerOnSharedPreferenceChangeListener(callback)
            onDispose { preferences.unregisterOnSharedPreferenceChangeListener(callback) }
        }
        AndroidView(
            factory = { context ->
                val preview = FrameLayout(context)
                fun updateWidget() {
                    val remoteViews = widgetFactory(context, width, height)
                    preview.addView(remoteViews.apply(context.applicationContext, preview))
                }
                updateWidget()
                updateCallback = {
                    preview.post {
                        preview.removeAllViews()
                        updateWidget()
                    }
                }
                preview
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
