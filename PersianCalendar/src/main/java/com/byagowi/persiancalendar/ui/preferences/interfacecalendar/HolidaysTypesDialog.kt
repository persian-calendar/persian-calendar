package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.os.Build
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.HolidaysTypesDialogBinding
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.checkbox.MaterialCheckBox
import kotlin.random.Random

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
    fun updateGroupsMixedIndicator() {
        fun MaterialCheckBox.setMixedIndicator(value: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.buttonDrawable?.alpha = if (value) 127 else 255
            } else {
                this.alpha = if (value) .5f else 1f
            }
        }
        binding.iran.setMixedIndicator(
            binding.iranHolidays.isChecked xor binding.iranOthers.isChecked
        )
        binding.afghanistan.setMixedIndicator(
            binding.afghanistanHolidays.isChecked xor binding.afghanistanOthers.isChecked
        )
    }
    updateGroupsMixedIndicator()
    binding.iranAncient.isChecked = "iran_ancient" in initial
    binding.international.isChecked = "international" in initial

    // Install events listeners
    val disabledEvents = Random.nextInt()
    binding.iran.setOnCheckedChangeListener { _, isChecked ->
        if (binding.iran.getTag(disabledEvents) == true) return@setOnCheckedChangeListener
        val dest = (binding.iranHolidays.isChecked xor binding.iranOthers.isChecked)
                || isChecked
        binding.iranHolidays.isChecked = dest
        binding.iranOthers.isChecked = dest
        updateGroupsMixedIndicator()
    }
    binding.afghanistan.setOnCheckedChangeListener { _, isChecked ->
        if (binding.afghanistan.getTag(disabledEvents) == true) return@setOnCheckedChangeListener
        val dest = (binding.afghanistanHolidays.isChecked xor binding.afghanistanOthers.isChecked)
                || isChecked
        binding.afghanistanHolidays.isChecked = dest
        binding.afghanistanOthers.isChecked = dest
        updateGroupsMixedIndicator()
    }
    listOf(
        binding.iranHolidays to binding.iran, binding.iranOthers to binding.iran,
        binding.afghanistanHolidays to binding.afghanistan,
        binding.afghanistanOthers to binding.afghanistan
    ).forEach { (child, parent) ->
        child.setOnCheckedChangeListener { _, _ ->
            parent.setTag(disabledEvents, true)
            updateGroupsChecks()
            parent.setTag(disabledEvents, false)
            updateGroupsMixedIndicator()
        }
    }

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
