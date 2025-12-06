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
}
