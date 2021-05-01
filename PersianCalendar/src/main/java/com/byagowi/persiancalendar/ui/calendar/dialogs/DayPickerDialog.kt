package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.shared.DayPickerView

fun Fragment.showDayPickerDialog(jdn: Long, onSuccess: (jdn: Long) -> Unit) {
    val activity = activity ?: return
    val dayPickerView = DayPickerView(activity).also { it.jdn = jdn }
    AlertDialog.Builder(activity)
        .setView(dayPickerView)
        .setCustomTitle(null)
        .setPositiveButton(R.string.go) { _, _ ->
            dayPickerView.jdn.takeIf { it != -1L }?.also(onSuccess)
        }.show()
}
