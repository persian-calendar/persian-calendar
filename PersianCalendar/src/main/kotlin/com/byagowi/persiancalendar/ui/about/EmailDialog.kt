package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogEmailBinding

fun showEmailDialog(activity: Activity, onSuccess: (String) -> Unit) {
    val emailBinding = DialogEmailBinding.inflate(activity.layoutInflater)
    AlertDialog.Builder(activity)
        .setView(emailBinding.root)
        .setTitle(R.string.about_email_sum)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            onSuccess(emailBinding.inputText.text?.toString() ?: "")
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
