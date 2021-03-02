package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.shared.DayPickerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SelectDayDialog : AppCompatDialogFragment() {

    var onSuccess = fun(jdn: Long) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val jdn = arguments?.getLong(BUNDLE_KEY, -1L) ?: -1L

        val context = context as Context
        val dayPickerView = DayPickerView(context)
        dayPickerView.setDayJdnOnView(jdn)

        return MaterialAlertDialogBuilder(context)
            .setView(dayPickerView as View)
            .setCustomTitle(null)
            .setPositiveButton(R.string.go) { _, _ ->
                val resultJdn = dayPickerView.dayJdnFromView
                if (resultJdn != -1L) onSuccess(resultJdn)
            }.create()
    }

    companion object {
        private const val BUNDLE_KEY = "jdn"

        fun newInstance(jdn: Long) = SelectDayDialog().apply {
            arguments = Bundle().apply {
                putLong(BUNDLE_KEY, jdn)
            }
        }
    }
}
