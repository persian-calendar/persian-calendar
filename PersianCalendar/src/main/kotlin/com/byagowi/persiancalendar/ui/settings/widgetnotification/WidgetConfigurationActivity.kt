package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.createSampleRemoteViews
import com.byagowi.persiancalendar.utils.update

class WidgetConfigurationActivity : ComponentActivity() {
    private fun finishAndSuccess() {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID).also { i ->
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i)
            )
        }
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    private val onBackPressedCloseCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = finishAndSuccess()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppLanguage(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window?.makeWallpaperTransparency()

        onBackPressedDispatcher.addCallback(this, onBackPressedCloseCallback)

        setContent { SystemTheme { WidgetConfigurationContent(::finishAndSuccess) } }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }
}

@Composable
private fun WidgetConfigurationContent(finishAndSuccess: () -> Unit) {
    Column(Modifier.safeDrawingPadding()) {
        run {
            val width = with(LocalDensity.current) { 200.dp.roundToPx() }
            val height = with(LocalDensity.current) { 60.dp.roundToPx() }
            AndroidView(
                factory = { context ->
                    val preview = FrameLayout(context)
                    fun updateWidget() {
                        preview.addView(
                            createSampleRemoteViews(
                                context, width, height
                            ).apply(context.applicationContext, preview)
                        )
                    }
                    updateWidget()

                    context.appPrefs.registerOnSharedPreferenceChangeListener { _, _ ->
                        // TODO: Investigate why sometimes gets out of sync
                        preview.post {
                            preview.removeAllViews()
                            updateWidget()
                        }
                    }
                    preview
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
            )
        }
        Column(
            Modifier
                .alpha(AppBlendAlpha)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge
                    )
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    Button(
                        onClick = finishAndSuccess,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.accept),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }

                    WidgetConfiguration()
                }
            }
        }
    }
}
