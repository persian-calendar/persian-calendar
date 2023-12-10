package com.byagowi.persiancalendar.ui.settings.agewidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.common.showColorPickerDialog
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.createAgeRemoteViews
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.getWidgetSize
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.update

class AgeWidgetConfigureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppLanguage(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window?.makeWallpaperTransparency()

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        fun confirm() {
            // Make sure we pass back the original appWidgetId
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            )
            update(this, false)
            finish()
        }

        val appPrefs = appPrefs
        // Put today's jdn if it wasn't set by the dialog, maybe a day counter is meant
        if (appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId) == null)
            appPrefs.edit { putJdn(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, Jdn.today()) }

        setContent {
            SystemTheme { AgeWidgetConfigureContent(this, appWidgetId, appPrefs, ::confirm) }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
    }
}

@Composable
private fun AgeWidgetConfigureContent(
    activity: ComponentActivity,
    appWidgetId: Int,
    appPrefs: SharedPreferences,
    confirm: () -> Unit,
) {
    Column(modifier = Modifier.safeDrawingPadding()) {
        AndroidView(
            factory = { context ->
                val preview = FrameLayout(context)

                val widgetManager = AppWidgetManager.getInstance(context)
                val (width, height) = widgetManager.getWidgetSize(context, appWidgetId)
                fun updateWidget() {
                    preview.addView(
                        createAgeRemoteViews(context, width, height, appWidgetId)
                            .apply(context.applicationContext, preview)
                    )
                }
                updateWidget()

                appPrefs.registerOnSharedPreferenceChangeListener { _, _ ->
                    // TODO: Investigate why sometimes gets out of sync
                    preview.post {
                        preview.removeAllViews()
                        updateWidget()
                    }
                }
                preview
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
        )
        Column(
            Modifier
                .fillMaxSize()
                .alpha(AppBlendAlpha)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge),
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            ) {
                Button(
                    onClick = { confirm() },
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                ) {
                    Text(
                        stringResource(R.string.accept),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }

                val initialTitle = remember {
                    appPrefs.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, null) ?: ""
                }
                var text by rememberSaveable { mutableStateOf(initialTitle) }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    value = text,
                    onValueChange = {
                        text = it
                        appPrefs.edit {
                            putString(
                                PREF_TITLE_AGE_WIDGET + appWidgetId,
                                text
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.age_widget_title)) },
                )

                run {
                    var showDialog by rememberSaveable { mutableStateOf(false) }
                    SettingsClickable(stringResource(R.string.select_date)) { showDialog = true }
                    if (showDialog) {
                        val key = PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId
                        val jdn = appPrefs.getJdnOrNull(key) ?: Jdn.today()
                        DayPickerDialog(
                            initialJdn = jdn,
                            positiveButtonTitle = R.string.accept,
                            onSuccess = { appPrefs.edit { putJdn(key, it) } },
                        ) { showDialog = false }
                    }
                }
                val showColorOptions = remember {
                    !(Theme.isDynamicColor(appPrefs) &&
                            appPrefs.getBoolean(PREF_WIDGETS_PREFER_SYSTEM_COLORS, true))
                }
                if (showColorOptions) {
                    SettingsClickable(
                        stringResource(R.string.widget_text_color),
                        stringResource(R.string.select_widgets_text_color)
                    ) {
                        showColorPickerDialog(
                            activity, false, PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId
                        )
                    }
                    SettingsClickable(
                        stringResource(R.string.widget_background_color),
                        stringResource(R.string.select_widgets_background_color)
                    ) {
                        showColorPickerDialog(
                            activity, true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId
                        )
                    }
                }
            }
        }
    }
}
