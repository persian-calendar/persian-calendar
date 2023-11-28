package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DayPickerDialogBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.variants.debugAssertNotNull

fun showDayPickerDialog(
    activity: FragmentActivity,
    jdn: Jdn,
    @StringRes positiveButtonTitle: Int,
    onSuccess: (jdn: Jdn) -> Unit
) {
    val binding = DayPickerDialogBinding.inflate(activity.layoutInflater)
    binding.dayPickerView.value = jdn
    binding.root.setupLayoutTransition()
    val dialog = AlertDialog.Builder(activity)
        .setView(binding.root)
        .setPositiveButton(positiveButtonTitle) { _, _ -> onSuccess(binding.dayPickerView.value) }
        .setNeutralButton(R.string.today, null)
        .show()

    binding.calendarsTypes.onValueChangeListener = { binding.dayPickerView.calendarType = it }

    val today = Jdn.today()
    val todayButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL).debugAssertNotNull
    todayButton?.setOnClickListener { binding.dayPickerView.value = today }
    todayButton?.isVisible = jdn != today
    binding.dayPickerView.onValueChangeListener = {
        todayButton?.isVisible = it != today
        binding.daysDistance.text = if (it == today) "" else listOf(
            activity.getString(R.string.days_distance),
            spacedColon,
            calculateDaysDifference(activity.resources, it)
        ).joinToString("")
    }
}
