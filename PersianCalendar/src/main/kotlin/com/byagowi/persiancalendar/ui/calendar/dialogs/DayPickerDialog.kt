package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.ui.shared.DayPickerView
import com.byagowi.persiancalendar.utils.Jdn

fun Fragment.showDayPickerDialog(
    jdn: Jdn, @StringRes positiveButtonTitle: Int, onSuccess: (jdn: Jdn) -> Unit
) {
    val dayPickerView = DayPickerView(layoutInflater.context).also { it.jdn = jdn }
    AlertDialog.Builder(layoutInflater.context)
        .setView(dayPickerView)
        .setPositiveButton(positiveButtonTitle) { _, _ -> dayPickerView.jdn?.also(onSuccess) }
        .show()
}
