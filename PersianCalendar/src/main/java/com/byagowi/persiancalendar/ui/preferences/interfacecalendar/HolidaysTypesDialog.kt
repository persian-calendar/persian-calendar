package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.HolidaysTypesDialogBinding
import com.byagowi.persiancalendar.utils.appPrefs

fun Fragment.showHolidaysTypesDialog() {
    val context = context ?: return

    val binding = HolidaysTypesDialogBinding.inflate(layoutInflater)

    // Update labels
    listOf(
        binding.iranHolidays, null, binding.iranAncient, binding.iranOthers,
        binding.international, binding.afghanistanHolidays, binding.afghanistanOthers
    ).zip(resources.getStringArray(R.array.holidays_types)) { view, title -> view?.text = title }
    binding.iran.setText(R.string.iran_holidays)
    binding.afghanistan.setText(R.string.afghanistan_holidays)
    binding.other.setText(R.string.other_holidays)

    // Make links work
    binding.iran.movementMethod = LinkMovementMethod.getInstance()
    binding.afghanistan.movementMethod = LinkMovementMethod.getInstance()

    // Update view from stored settings
    val initial =
        (context.appPrefs.getStringSet(PREF_HOLIDAY_TYPES, null) ?: setOf("iran_holidays"))
            .toList().map {
                if (it == "iran_islamic") "iran_others" else it // update the legacy key
            }.toSet()
    binding.iranHolidays.isChecked = "iran_holidays" in initial
    binding.iranOthers.isChecked = "iran_others" in initial
    binding.afghanistanHolidays.isChecked = "afghanistan_holidays" in initial
    binding.afghanistanOthers.isChecked = "afghanistan_others" in initial
    fun updateGroupsChecks() {
        binding.iran.isChecked =
            binding.iranHolidays.isChecked || binding.iranOthers.isChecked
        binding.afghanistan.isChecked =
            binding.afghanistanHolidays.isChecked || binding.afghanistanOthers.isChecked
    }
    updateGroupsChecks()
    fun updateGroupsOpacity() {
        binding.iran.alpha = if (
            binding.iranHolidays.isChecked xor binding.iranOthers.isChecked
        ) .5f else 1f
        binding.afghanistan.alpha = if (
            binding.afghanistanHolidays.isChecked xor binding.afghanistanOthers.isChecked
        ) .5f else 1f
    }
    updateGroupsOpacity()
    binding.iranAncient.isChecked = "iran_ancient" in initial
    binding.international.isChecked = "international" in initial

    // Install events listeners
    binding.iran.setOnCheckedChangeListener { _, isChecked ->
        val dest = (binding.iranHolidays.isChecked xor binding.iranOthers.isChecked)
                || isChecked
        binding.iranHolidays.isChecked = dest
        binding.iranOthers.isChecked = dest
        updateGroupsOpacity()
    }
    binding.afghanistan.setOnCheckedChangeListener { _, isChecked ->
        val dest = (binding.afghanistanHolidays.isChecked xor binding.afghanistanOthers.isChecked)
                || isChecked
        binding.afghanistanHolidays.isChecked = dest
        binding.afghanistanOthers.isChecked = dest
        updateGroupsOpacity()
    }
    listOf(
        binding.iranHolidays, binding.iranOthers,
        binding.afghanistanHolidays, binding.afghanistanOthers
    ).forEach { it.setOnCheckedChangeListener { _, _ -> updateGroupsChecks(); updateGroupsOpacity() } }

    // Run the dialog
    AlertDialog.Builder(context)
        .setTitle(R.string.events)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            val result = listOf(
                binding.iranHolidays to "iran_holidays", binding.iranOthers to "iran_others",
                binding.afghanistanHolidays to "afghanistan_holidays",
                binding.afghanistanOthers to "afghanistan_others",
                binding.iranAncient to "iran_ancient", binding.international to "international"
            ).mapNotNull { (checkBox, key) -> if (checkBox.isChecked) key else null }.toSet()
            this.context?.appPrefs?.edit { putStringSet(PREF_HOLIDAY_TYPES, result) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
