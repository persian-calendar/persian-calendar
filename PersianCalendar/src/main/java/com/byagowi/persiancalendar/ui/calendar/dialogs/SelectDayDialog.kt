package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.shared.DayPickerView

class SelectDayDialog : AppCompatDialogFragment() {

    var onSuccess = fun(jdn: Long) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()

        val dayPickerView = DayPickerView(activity).also {
            it.jdn = arguments?.getLong(BUNDLE_KEY, -1L) ?: -1L
        }
        return AlertDialog.Builder(activity)
            .setView(dayPickerView)
            .setCustomTitle(null)
            .setPositiveButton(R.string.go) { _, _ ->
                dayPickerView.jdn.takeIf { it != -1L }?.also(onSuccess)
            }.create()
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = SelectDayDialog().apply {
            arguments = bundleOf(BUNDLE_KEY to jdn)
        }
    }
}
