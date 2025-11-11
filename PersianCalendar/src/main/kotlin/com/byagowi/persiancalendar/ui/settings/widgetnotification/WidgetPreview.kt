package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.content.Context
import android.content.SharedPreferences
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun WidgetPreview(height: Dp = 78.dp, widgetFactory: (Context, Int, Int) -> RemoteViews) {
    BoxWithConstraints(
        Modifier
            .padding(vertical = 16.dp)
            .height(height),
    ) {
        val width = with(LocalDensity.current) { (this@BoxWithConstraints).maxWidth.roundToPx() }
        val height = with(LocalDensity.current) { (this@BoxWithConstraints).maxHeight.roundToPx() }
        val preferences = LocalContext.current.preferences
        var updateCallback by remember { mutableStateOf({}) }
        DisposableEffect(preferences) {
            val callback = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                updateCallback()
            }
            preferences.registerOnSharedPreferenceChangeListener(callback)
            onDispose { preferences.unregisterOnSharedPreferenceChangeListener(callback) }
        }
        AndroidView(
            factory = { context ->
                val preview = FrameLayout(context)
                fun updateWidget() {
                    val remoteViews = widgetFactory(context, width, height)
                    preview.addView(remoteViews.apply(context.applicationContext, preview))
                }
                updateWidget()
                updateCallback = {
                    preview.post {
                        preview.removeAllViews()
                        updateWidget()
                    }
                }
                preview
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
