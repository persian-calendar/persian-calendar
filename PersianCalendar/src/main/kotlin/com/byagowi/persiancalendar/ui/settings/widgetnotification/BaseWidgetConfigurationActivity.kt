package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.update

abstract class BaseWidgetConfigurationActivity : BaseConfigurationActivity(
    contentNeedsMaxHeight = true,
) {
    override fun onAcceptClick() {
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId()))
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    protected fun appWidgetId(): Int {
        return intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: intent?.action?.takeIf { it.startsWith(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.replace(AppWidgetManager.EXTRA_APPWIDGET_ID, "")?.toIntOrNull()
        ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    @Composable
    override fun ColumnScope.Content() = Settings(appWidgetId())

    @Composable
    override fun Header() = Preview(appWidgetId())

    @Composable
    abstract fun Preview(appWidgetId: Int)

    @Composable
    abstract fun ColumnScope.Settings(appWidgetId: Int)
}
