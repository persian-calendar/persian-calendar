package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.shared.DayPickerView

fun showDayPickerDialog(
    context: Context, jdn: Jdn, @StringRes positiveButtonTitle: Int, onSuccess: (jdn: Jdn) -> Unit
) {
    val dayPickerView = DayPickerView(context).also { it.jdn = jdn }
    AlertDialog.Builder(context)
        .setView(dayPickerView)
        .setPositiveButton(positiveButtonTitle) { _, _ -> dayPickerView.jdn?.also(onSuccess) }
        .show()
}
