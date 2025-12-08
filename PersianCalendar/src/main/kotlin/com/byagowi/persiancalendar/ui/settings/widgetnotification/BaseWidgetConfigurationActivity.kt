package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.update

abstract class BaseWidgetConfigurationActivity : BaseConfigurationActivity(
    contentNeedsMaxHeight = true,
) {
    override fun onAcceptClick() {
        val bundle = bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId())
        setResult(RESULT_OK, Intent().putExtras(bundle))
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    @Suppress("KotlinConstantConditions")
    protected fun appWidgetId(): Int {
        val defaultValue = AppWidgetManager.INVALID_APPWIDGET_ID
        val intent = intent ?: return defaultValue

        run {
            // first try to get it from intent's extras, most of the times it is here
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, defaultValue)
            if (id != defaultValue) return id
        }

        run {
            // on age widget plain clicks, id is set as the action so next try that
            val id = intent.action?.takeIf { it.startsWith(AppWidgetManager.EXTRA_APPWIDGET_ID) }
                ?.replace(AppWidgetManager.EXTRA_APPWIDGET_ID, "")?.toIntOrNull()
            if (id != null) return id
        }

        // Shouldn't happen but if everything fails, return invalid id
        return defaultValue
    }

    @Composable
    protected fun WidgetPreview(
        widgetFactory: (context: Context, width: Int, height: Int, appWidgetId: Int) -> RemoteViews,
    ) {
        val appWidgetId = appWidgetId()
        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        Box(
            if (isLandscape) {
                Modifier
                    .padding(end = 16.dp)
                    .fillMaxHeight()
            } else {
                Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            },
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            BoxWithConstraints(
                with(density) {
                    Modifier.size(
                        // TODO: Why these numbers? Who knows…
                        width = (info.minWidth.toDp() * if (isLandscape) 2.7f else 2.2f)
                            .coerceAtMost(320.dp),
                        height = (info.minHeight.toDp() * if (isLandscape) 2.2f else 2.7f)
                            .coerceAtMost(240.dp),
                    )
                },
            ) {
                val preferences = preferences
                var updateToken by remember { mutableIntStateOf(0) }
                DisposableEffect(preferences) {
                    val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                        ++updateToken
                    }
                    preferences.registerOnSharedPreferenceChangeListener(callback)
                    onDispose { preferences.unregisterOnSharedPreferenceChangeListener(callback) }
                }
                val width = with(density) { (this@BoxWithConstraints).maxWidth.roundToPx() }
                val height = with(density) { (this@BoxWithConstraints).maxHeight.roundToPx() }
                AndroidView(
                    factory = ::FrameLayout,
                    update = {
                        updateToken.let {}
                        it.removeAllViews()
                        val remoteViews = widgetFactory(it.context, width, height, appWidgetId)
                        it.addView(remoteViews.apply(it.context.applicationContext, it))
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
