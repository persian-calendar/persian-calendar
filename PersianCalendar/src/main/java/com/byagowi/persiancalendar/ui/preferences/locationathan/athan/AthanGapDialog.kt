package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.layoutInflater

fun Fragment.showAthanGapDialog() {
    val context = context ?: return

    val binding = NumericBinding.inflate(context.layoutInflater)
    val gap = context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0
    binding.edit.setText(gap.toString())
    AlertDialog.Builder(context)
        .setTitle(R.string.athan_gap_summary)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            val value = binding.edit.toString().toDoubleOrNull() ?: .0
            this.context?.appPrefs?.edit { putString(PREF_ATHAN_GAP, value.toString()) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
