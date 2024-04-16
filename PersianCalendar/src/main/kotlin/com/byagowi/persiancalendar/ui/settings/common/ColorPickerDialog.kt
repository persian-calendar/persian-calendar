package com.byagowi.persiancalendar.ui.settings.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
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
                if (!isBackgroundPick) view.hideAlphaSeekBar()
                view.setPickedColor(initialColor)
                view
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            (if (isBackgroundPick) listOf(
                Color.Transparent,
                Color(0x20000000),
                Color(0x50000000),
                Color.Black,
            ) else listOf(
                Color.White,
                Color(0xFFE65100),
                Color(0xFF00796B),
                Color(0xFFFEF200),
                Color(0xFF202020),
            )).forEach {
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { colorPickerView?.setPickedColor(it.toArgb()) }
                        .border(BorderStroke(1.dp, Color(0x80808080)))
                        .background(Color.White)
                        .clipToBounds()
                        .drawWithCache {
                            val size = 4.dp.roundToPx()
                            onDrawBehind {
                                drawContext.canvas.nativeCanvas.drawPaint(createCheckerBoard(size))
                            }
                        }
                        .background(it)
                        .size(40.dp),
                )
            }
        }
    }
}
