package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Activity
import androidx.annotation.StringRes
import androidx.core.view.setPadding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.common.DayPickerView
import com.byagowi.persiancalendar.ui.utils.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showDayPickerDialog(
    activity: Activity, jdn: Jdn, @StringRes positiveButtonTitle: Int, onSuccess: (jdn: Jdn) -> Unit
) {
    val dayPickerView = DayPickerView(activity).also {
        it.jdn = jdn
        it.setPadding(16.dp.toInt())
    }
    MaterialAlertDialogBuilder(activity)
        .setView(dayPickerView)
        .setPositiveButton(positiveButtonTitle) { _, _ -> onSuccess(dayPickerView.jdn) }
        .show()
}
