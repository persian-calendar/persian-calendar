package com.byagowi.persiancalendar.ui.preferences.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.showComposeDialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.godaddy.android.colorpicker.ClassicColorPicker
import java.util.*

fun showColorPickerDialog(activity: FragmentActivity, isBackgroundPick: Boolean, key: String) {
    val initialColor = activity.appPrefs.getString(key, null)
        ?.let(android.graphics.Color::parseColor)
        ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
        else DEFAULT_SELECTED_WIDGET_TEXT_COLOR

    showComposeDialog(activity) {
        ColorPickerDialog(it, Color(initialColor), key, isBackgroundPick)
    }
}

@Composable
private fun ColorPickerDialog(
    closeDialog: () -> Unit,
    initialColor: Color,
    key: String,
    isBackgroundPick: Boolean,
) {
    val color = remember { mutableStateOf(initialColor) }
    AlertDialog(
        onDismissRequest = { closeDialog() },
        dismissButton = {
            TextButton(onClick = { closeDialog() }) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            val context = LocalContext.current
            TextButton(onClick = {
                val result = if (isBackgroundPick) "#%08X".format(
                    Locale.ENGLISH, 0xFFFFFFFF and color.value.value.toLong()
                ) else "#%06X".format(
                    Locale.ENGLISH, 0xFFFFFF and color.value.value.toInt()
                )
                context.appPrefs.edit { this.putString(key, result) }
                closeDialog()
            }) { Text(stringResource(R.string.accept)) }
        },
        title = {
            val titleId =
                if (isBackgroundPick) R.string.widget_background_color else R.string.widget_text_color
            Text(stringResource(titleId))
        },
        text = {
            Column {
                Column {
                    LazyRow {
                        val colors = if (isBackgroundPick)
                            listOf(0x00000000L, 0x20000000L, 0x50000000L, 0xFF000000L)
                        else listOf(0xFFFFFFFFL, 0xFFE65100L, 0xFF00796bL, 0xFFFEF200L, 0xFF202020L)
                        items(colors) { item ->
                            TextButton(onClick = { color.value = Color.Cyan }) { Text("A") }
                        }
                    }
                    ClassicColorPicker(
                        color = color.value,
                        onColorChanged = { color.value = it.toColor() },
                        showAlphaBar = isBackgroundPick
                    )
                }
            }
        }
    )
}
