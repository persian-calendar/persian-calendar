package com.byagowi.persiancalendar.ui.settings.common

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.edit
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

fun showColorPickerDialog(activity: FragmentActivity, isBackgroundPick: Boolean, key: String) {
    val initialColor = activity.appPrefs.getString(key, null)?.let(Color::parseColor)
        ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
        else DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    showColorPickerDialog(activity, isBackgroundPick, initialColor) { colorResult ->
        activity.appPrefs.edit { this.putString(key, colorResult) }
    }
}

private fun showColorPickerDialog(
    activity: FragmentActivity, isBackgroundPick: Boolean, @ColorInt initialColor: Int,
    onResult: (String) -> Unit
) {
    val colorPickerView = ColorPickerView(activity).also {
        it.setColorsToPick(
            if (isBackgroundPick) listOf(0x00000000L, 0x20000000L, 0x50000000L, 0xFF000000L)
            else listOf(0xFFFFFFFFL, 0xFFE65100L, 0xFF00796bL, 0xFFFEF200L, 0xFF202020L)
        )
        if (!isBackgroundPick) it.hideAlphaSeekBar()
        it.setPickedColor(initialColor)
        it.setPadding(10.dp.toInt())
    }
    MaterialAlertDialogBuilder(activity)
        .setTitle(if (isBackgroundPick) R.string.widget_background_color else R.string.widget_text_color)
        .setView(colorPickerView)
        .setPositiveButton(R.string.accept) { _, _ ->
            onResult(
                if (isBackgroundPick) "#%08X".format(
                    Locale.ENGLISH, 0xFFFFFFFF and colorPickerView.pickerColor.toLong()
                ) else "#%06X".format(
                    Locale.ENGLISH, 0xFFFFFF and colorPickerView.pickerColor
                )
            )
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
