package com.byagowi.persiancalendar.ui.settings.common

import android.graphics.Color
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
        context.appPrefs.getString(key, null)?.let(Color::parseColor)
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
                ColorPickerView(context).also {
                    colorPickerView = it
                    it.setColorsToPick(
                        if (isBackgroundPick) listOf(
                            0x00000000L, 0x20000000L, 0x50000000L, 0xFF000000L
                        ) else listOf(
                            0xFFFFFFFFL, 0xFFE65100L, 0xFF00796bL, 0xFFFEF200L, 0xFF202020L
                        )
                    )
                    if (!isBackgroundPick) it.hideAlphaSeekBar()
                    it.setPickedColor(initialColor)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )
    }
}
