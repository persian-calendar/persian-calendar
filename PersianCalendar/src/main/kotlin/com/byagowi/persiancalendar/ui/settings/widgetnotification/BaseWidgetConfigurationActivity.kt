package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.view.children
import com.byagowi.persiancalendar.DEFAULT_WIDGET_TEXT_SCALE
import com.byagowi.persiancalendar.PREF_WIDGET_TEXT_SCALE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.preferencesUpdateToken
import com.byagowi.persiancalendar.ui.settings.SettingsSlider
import com.byagowi.persiancalendar.utils.getWidgetSize
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.update

abstract class BaseWidgetConfigurationActivity : BaseConfigurationActivity(
    contentNeedsMaxHeight = true,
) {
    final override fun onAcceptClick() {
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    @Suppress("KotlinConstantConditions")
    protected val appWidgetId by lazy(LazyThreadSafetyMode.NONE) {
        val defaultValue = AppWidgetManager.INVALID_APPWIDGET_ID
        val intent = intent ?: return@lazy defaultValue

        run {
            // first try to get it from intent's extras, most of the times it is here
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, defaultValue)
            if (id != defaultValue) return@lazy id
        }

        run {
            // on age widget plain clicks, id is set as the action so next try that
            val id = intent.action?.takeIf { it.startsWith(AppWidgetManager.EXTRA_APPWIDGET_ID) }
                ?.replace(AppWidgetManager.EXTRA_APPWIDGET_ID, "")?.toIntOrNull()
            if (id != null) return@lazy id
        }

        // Shouldn't happen but if everything fails, return invalid id
        defaultValue
    }

    @Composable
    final override fun Header() {
        val size = AppWidgetManager.getInstance(this)?.getWidgetSize(
            LocalResources.current, appWidgetId
        ) ?: DpSize(100.dp, 100.dp)
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
            BoxWithConstraints(
                Modifier.size(
                    width = size.width.coerceAtMost(320.dp),
                    height = size.height.coerceAtMost(240.dp),
                ),
            ) {
                AndroidView(
                    factory = ::FrameLayout,
                    update = { parent ->
                        preferencesUpdateToken.let {}
                        val remoteViews = preview(DpSize(maxWidth, maxHeight))
                        val context = parent.context.applicationContext
                        if (remoteViews.layoutId != parent.children.firstOrNull()?.id) {
                            parent.removeAllViews()
                            parent.addView(remoteViews.apply(context, parent))
                        } else remoteViews.reapply(context, parent)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    @Composable
    protected fun TextScaleSettings() {
        val key = PREF_WIDGET_TEXT_SCALE + appWidgetId
        val preferences = LocalContext.current.preferences
        SettingsSlider(
            title = stringResource(R.string.widget_text_size),
            value = preferencesUpdateToken.let {
                preferences.getFloat(key, DEFAULT_WIDGET_TEXT_SCALE)
            },
            valueRange = .65f..3f,
            visibleScale = 14f,
            defaultValue = 1f,
            onValueChange = { preferences.edit { putFloat(key, it) } },
        )
    }

    abstract fun preview(size: DpSize): RemoteViews
}
