package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.utils.appPrefs

fun showAthanGapDialog(activity: FragmentActivity) {
    val binding = NumericBinding.inflate(activity.layoutInflater)
    val gap = activity.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0
    binding.edit.setText(gap.toString())
    binding.root.setHint(R.string.athan_gap_summary)
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            val value = binding.edit.text.toString().toDoubleOrNull() ?: .0
            activity.appPrefs.edit { putString(PREF_ATHAN_GAP, value.toString()) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
