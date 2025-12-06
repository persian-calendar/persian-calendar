package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.core.os.bundleOf
import com.byagowi.persiancalendar.global.updateStoredPreference
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

    protected fun appWidgetId(): Int {
        return intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: intent?.action?.takeIf { it.startsWith(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.replace(AppWidgetManager.EXTRA_APPWIDGET_ID, "")?.toIntOrNull()
        ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }
}
