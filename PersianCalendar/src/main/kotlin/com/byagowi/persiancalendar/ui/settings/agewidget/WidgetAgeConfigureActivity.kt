package com.byagowi.persiancalendar.ui.settings.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET_START
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColorsFlow
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsColor
import com.byagowi.persiancalendar.ui.settings.widgetnotification.BaseWidgetConfigurationActivity
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetDynamicColorsGlobalSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetPreview
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetTextScale
import com.byagowi.persiancalendar.utils.createAgeRemoteViews
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn

class WidgetAgeConfigureActivity : BaseWidgetConfigurationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val appWidgetId = appWidgetId()
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val preferences = preferences
        // Put today's jdn if it wasn't set by the dialog, maybe a day counter is meant
        if (preferences.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId) == null) {
            preferences.edit {
                putJdn(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, Jdn.today())
            }
        }
    }

    override val successOnBack: Boolean get() = false

    @Composable
    override fun Preview(appWidgetId: Int) {
        WidgetPreview { context, width, height ->
            createAgeRemoteViews(
                context, width, height, appWidgetId, Jdn.today(), preferences
            )
        }
    }

    @Composable
    override fun ColumnScope.Settings(appWidgetId: Int) {
        val context = LocalContext.current
        val initialTitle = remember {
            context.preferences.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, null).orEmpty()
        }
        var text by rememberSaveable { mutableStateOf(initialTitle) }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            value = text,
            onValueChange = {
                text = it
                context.preferences.edit {
                    putString(PREF_TITLE_AGE_WIDGET + appWidgetId, text)
                }
            },
            label = { Text(stringResource(R.string.age_widget_title)) },
        )

        val primaryKey = PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId
        val today = Jdn.today()
        var primaryJdn by remember {
            mutableStateOf(context.preferences.getJdnOrNull(primaryKey) ?: today)
        }
        SettingsClickable(stringResource(R.string.select_date)) { onDismissRequest ->
            DatePickerDialog(initialJdn = primaryJdn, onDismissRequest = onDismissRequest) {
                primaryJdn = it
                context.preferences.edit { putJdn(primaryKey, it) }
            }
        }

        this.AnimatedVisibility(primaryJdn > today) {
            val secondaryKey = PREF_SELECTED_DATE_AGE_WIDGET_START + appWidgetId
            var jdn by remember {
                mutableStateOf(context.preferences.getJdnOrNull(secondaryKey) ?: today)
            }
            SettingsClickable(stringResource(R.string.starting_date)) { onDismissRequest ->
                DatePickerDialog(initialJdn = jdn, onDismissRequest = onDismissRequest) {
                    jdn = it
                    context.preferences.edit { putJdn(secondaryKey, it) }
                }
            }
        }

        WidgetTextScale(appWidgetId)

        val prefersWidgetsDynamicColors by prefersWidgetsDynamicColorsFlow.collectAsState()
        WidgetDynamicColorsGlobalSettings(prefersWidgetsDynamicColors)
        this.AnimatedVisibility(!prefersWidgetsDynamicColors) {
            SettingsColor(
                title = stringResource(R.string.widget_text_color),
                summary = stringResource(R.string.select_widgets_text_color),
                isBackgroundPick = false,
                key = PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId,
            )
        }
        this.AnimatedVisibility(!prefersWidgetsDynamicColors) {
            SettingsColor(
                title = stringResource(R.string.widget_background_color),
                summary = stringResource(R.string.select_widgets_background_color),
                isBackgroundPick = true,
                key = PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId,
            )
        }
    }
}
