package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appWidgetId = appWidgetId()
        setContent {
            BackHandler { if (successOnBack) finishAndSuccess() else finish() }
            SystemTheme { Content(appWidgetId) }
        }
    }

    protected open val successOnBack get() = true

    @Composable
    abstract fun Preview(appWidgetId: Int)

    @Composable
    abstract fun ColumnScope.Settings(appWidgetId: Int)

    @Composable
    private fun Content(appWidgetId: Int) {
        Column(
            Modifier
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Preview(appWidgetId)
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
                    Settings(appWidgetId)
                }
            }
        }
    }
}
