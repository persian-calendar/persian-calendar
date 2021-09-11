package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.utils.appPrefs

fun showAthanGapDialog(activity: Activity) {
    val binding = NumericBinding.inflate(activity.layoutInflater)
    val gap = activity.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0
    binding.edit.setText(gap.toString())
    AlertDialog.Builder(activity)
        .setTitle(R.string.athan_gap_summary)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            val value = binding.edit.toString().toDoubleOrNull() ?: .0
            activity.appPrefs.edit { putString(PREF_ATHAN_GAP, value.toString()) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
