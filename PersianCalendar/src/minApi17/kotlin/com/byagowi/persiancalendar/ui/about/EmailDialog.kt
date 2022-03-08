package com.byagowi.persiancalendar.ui.about

import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogEmailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showEmailDialog(activity: FragmentActivity) {
    val emailBinding = DialogEmailBinding.inflate(activity.layoutInflater)
    MaterialAlertDialogBuilder(activity)
        .setView(emailBinding.root)
        .setTitle(R.string.about_email_sum)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            launchEmailIntent(activity,  emailBinding.inputText.text?.toString() ?: "")
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
