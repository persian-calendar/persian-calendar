package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogEmailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showEmailDialog(activity: Activity, onSuccess: (String) -> Unit) {
    val emailBinding = DialogEmailBinding.inflate(activity.layoutInflater)
    MaterialAlertDialogBuilder(activity)
        .setView(emailBinding.root)
        .setTitle(R.string.about_email_sum)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            onSuccess(emailBinding.inputText.text?.toString() ?: "")
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
