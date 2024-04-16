package com.byagowi.persiancalendar.ui.settings.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.utils.appPrefs
import java.util.Locale

@Composable
fun ColorPickerDialog(isBackgroundPick: Boolean, key: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val initialColor = remember {
        context.appPrefs.getString(key, null)?.let(android.graphics.Color::parseColor)
            ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
            else DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    }
    // Ugly but for now and till a rewrite in Compose
    var colorPickerView by remember { mutableStateOf<ColorPickerView?>(null) }
    AppDialog(
        title = {
            Text(
                stringResource(
                    if (isBackgroundPick) R.string.widget_background_color
                    else R.string.widget_text_color
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                val colorResult = if (isBackgroundPick) "#%08X".format(
                    Locale.ENGLISH, 0xFFFFFFFF and (colorPickerView?.pickerColor?.toLong() ?: 0L)
                ) else "#%06X".format(
                    Locale.ENGLISH, 0xFFFFFF and (colorPickerView?.pickerColor ?: 0)
                )
                context.appPrefs.edit { this.putString(key, colorResult) }
            }) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        AndroidView(
            factory = { context ->
                val view = ColorPickerView(context)
                colorPickerView = view
                val colors = if (isBackgroundPick) listOf(
                    Color.Transparent,
                    Color(0x20000000L),
                    Color(0x50000000L),
                    Color.Black,
                ) else listOf(
                    Color.White,
                    Color(0xFFE65100L),
                    Color(0xFF00796bL),
                    Color(0xFFFEF200L),
                    Color(0xFF202020L)
                )
                view.setColorsToPick(colors.map { it.toArgb().toLong() })
                if (!isBackgroundPick) view.hideAlphaSeekBar()
                view.setPickedColor(initialColor)
                view
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )
    }
}
