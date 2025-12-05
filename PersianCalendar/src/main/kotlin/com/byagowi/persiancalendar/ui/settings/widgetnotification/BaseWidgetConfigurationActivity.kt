package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.utils.update

abstract class BaseWidgetConfigurationActivity : BaseActivity() {
    private fun finishAndSuccess() {
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

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appWidgetId = appWidgetId()
        setContent {
            BaseSettingsLayout(
                finish = { if (successOnBack) finishAndSuccess() else finish() },
                header = { Preview(appWidgetId) },
            ) { Settings(appWidgetId) }
        }
    }

    protected open val successOnBack get() = true

    @Composable
    abstract fun Preview(appWidgetId: Int)

    @Composable
    abstract fun ColumnScope.Settings(appWidgetId: Int)
}
