package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
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
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.settings.SettingsSectionLayout
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationSettings
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.create1x1RemoteViews
import com.byagowi.persiancalendar.utils.create4x1RemoteViews
import com.byagowi.persiancalendar.utils.createMapRemoteViews
import com.byagowi.persiancalendar.utils.createMonthViewRemoteViews
import com.byagowi.persiancalendar.utils.createSampleRemoteViews
import com.byagowi.persiancalendar.utils.createSunViewRemoteViews
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.GregorianCalendar

open class WidgetConfigurationActivity : BaseActivity() {
    private fun finishAndSuccess() {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID).also { i ->
            setResult(
                RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i)
            )
        }
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: intent?.action?.takeIf { it.startsWith(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.replace(AppWidgetManager.EXTRA_APPWIDGET_ID, "")?.toIntOrNull()
        ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            BackHandler { finishAndSuccess() }
            SystemTheme { Content(appWidgetId) }
        }
    }

    @Composable
    open fun Content(appWidgetId: Int) {
        BaseLayout(
            preview = {
                WidgetPreview { context, width, height ->
                    createSampleRemoteViews(context, width, height)
                }
            },
            settings = { WidgetSettings() },
        )
    }

    @Composable
    protected fun BaseLayout(
        preview: @Composable () -> Unit,
        settings: @Composable ColumnScope.() -> Unit,
    ) {
        Column(
            Modifier
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            preview()
            Column(
                Modifier
                    .fillMaxSize()
                    .alpha(AppBlendAlpha)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge),
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    Button(
                        onClick = ::finishAndSuccess,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.accept),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                    settings()
                }
            }
        }
    }
}

class Widget1x1ConfigurationActivity : WidgetConfigurationActivity() {
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

class Widget4x1ConfigurationActivity : WidgetConfigurationActivity() {
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

class WidgetSunViewConfigurationActivity : WidgetConfigurationActivity() {
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

class WidgetMapConfigurationActivity : WidgetConfigurationActivity() {
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

class WidgetMonthViewConfigurationActivity : WidgetConfigurationActivity() {
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
