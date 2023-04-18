package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.common.DayPickerView
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showDayPickerDialog(
    activity: FragmentActivity,
    jdn: Jdn,
    @StringRes positiveButtonTitle: Int,
    onSuccess: (jdn: Jdn) -> Unit
) {
    val dayPickerView = DayPickerView(activity).also {
        it.jdn = jdn
        it.setPadding(16.dp.toInt())
    }
    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(dayPickerView)
        .setPositiveButton(positiveButtonTitle) { _, _ -> onSuccess(dayPickerView.jdn) }
        .setNeutralButton(R.string.today, null)
        .show()

    val today = Jdn.today()
    val todayButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL).debugAssertNotNull
    todayButton?.setOnClickListener { dayPickerView.jdn = today }
    todayButton?.isVisible = jdn != today
    dayPickerView.selectedDayListener = { todayButton?.isVisible = it != today }
}
