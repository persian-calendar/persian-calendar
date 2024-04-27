package com.byagowi.persiancalendar.ui.settings.agewidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColorsFlow
import com.byagowi.persiancalendar.ui.calendar.dialogs.DatePickerDialog
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.common.ColorPickerDialog
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetDynamicColorsGlobalSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetPreview
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.createAgeRemoteViews
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.update

class AgeWidgetConfigureActivity : ComponentActivity() {
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
            SystemTheme { AgeWidgetConfigureContent(appWidgetId, ::confirm) }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }
}

@Composable
private fun AgeWidgetConfigureContent(appWidgetId: Int, confirm: () -> Unit) {
    Column(
        Modifier
            .safeDrawingPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        WidgetPreview { context, width, height ->
            createAgeRemoteViews(context, width, height, appWidgetId)
        }
        Column(
            Modifier
                .fillMaxSize()
                .alpha(AppBlendAlpha)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge),
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            ) {
                Button(
                    onClick = confirm,
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                ) {
                    Text(
                        stringResource(R.string.accept),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }

                val context = LocalContext.current
                val initialTitle = remember {
                    context.appPrefs.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, null) ?: ""
                }
                var text by rememberSaveable { mutableStateOf(initialTitle) }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    value = text,
                    onValueChange = {
                        text = it
                        context.appPrefs.edit {
                            putString(
                                PREF_TITLE_AGE_WIDGET + appWidgetId,
                                text
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.age_widget_title)) },
                )

                SettingsClickable(stringResource(R.string.select_date)) { onDismissRequest ->
                    val key = PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId
                    val jdn = context.appPrefs.getJdnOrNull(key) ?: Jdn.today()
                    DatePickerDialog(
                        initialJdn = jdn,
                        onSuccess = { context.appPrefs.edit { putJdn(key, it) } },
                        onDismissRequest = onDismissRequest,
                    )
                }
                val prefersWidgetsDynamicColors by prefersWidgetsDynamicColorsFlow.collectAsState()
                WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
                AnimatedVisibility(!prefersWidgetsDynamicColors) {
                    SettingsClickable(
                        stringResource(R.string.widget_text_color),
                        stringResource(R.string.select_widgets_text_color)
                    ) { onDismissRequest ->
                        ColorPickerDialog(
                            false,
                            PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId,
                            onDismissRequest = onDismissRequest,
                        )
                    }
                }
                AnimatedVisibility(!prefersWidgetsDynamicColors) {
                    SettingsClickable(
                        stringResource(R.string.widget_background_color),
                        stringResource(R.string.select_widgets_background_color)
                    ) { onDismissRequest ->
                        ColorPickerDialog(
                            true,
                            PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId,
                            onDismissRequest = onDismissRequest,
                        )
                    }
                }
            }
        }
    }
}
