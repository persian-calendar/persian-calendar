package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Activity
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.shared.DayPickerView

fun showDayPickerDialog(
    activity: Activity, jdn: Jdn, @StringRes positiveButtonTitle: Int, onSuccess: (jdn: Jdn) -> Unit
) {
    val dayPickerView = DayPickerView(activity).also { it.jdn = jdn }
    AlertDialog.Builder(activity)
        .setView(dayPickerView)
        .setPositiveButton(positiveButtonTitle) { _, _ -> onSuccess(dayPickerView.jdn) }
        .show()
}
