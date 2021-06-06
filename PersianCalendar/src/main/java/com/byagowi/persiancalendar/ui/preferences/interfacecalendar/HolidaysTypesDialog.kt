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
import kotlin.random.Random

fun Fragment.showHolidaysTypesDialog() {
    val context = context ?: return

    val binding = HolidaysTypesDialogBinding.inflate(layoutInflater)

    val hierarchy = listOf(
        binding.iran to listOf(binding.iranHolidays, binding.iranOthers),
        binding.afghanistan to listOf(binding.afghanistanHolidays, binding.afghanistanOthers)
    )

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
        (context.appPrefs.getStringSet(PREF_HOLIDAY_TYPES, null) ?: setOf("iran_holidays")).map {
            if (it == "iran_islamic") "iran_others" else it // update the legacy key
        }.toSet()
    binding.iranHolidays.isChecked = "iran_holidays" in initial
    binding.iranOthers.isChecked = "iran_others" in initial
    binding.afghanistanHolidays.isChecked = "afghanistan_holidays" in initial
    binding.afghanistanOthers.isChecked = "afghanistan_others" in initial
    binding.iranAncient.isChecked = "iran_ancient" in initial
    binding.international.isChecked = "international" in initial

    // Parent updates logics
    fun updateGroups() = hierarchy.forEach { (parent, children) ->
        parent.isChecked = children.fold(false) { acc, child -> acc || child.isChecked }

        val isMixed = children.fold(false) { acc, child -> acc xor child.isChecked }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parent.buttonDrawable?.alpha = if (isMixed) 127 else 255
        } else {
            parent.alpha = if (isMixed) .5f else 1f
        }
    }
    updateGroups()

    // Install events listeners
    val disabledEventsTag = Random.nextInt()
    hierarchy.forEach { (parent, children) ->
        parent.setOnCheckedChangeListener { _, isChecked ->
            if (parent.getTag(disabledEventsTag) == true) return@setOnCheckedChangeListener
            children.map { it.isChecked }
            val dest = children.fold(false) { acc, child -> acc xor child.isChecked } || isChecked
            children.forEach { it.isChecked = dest }
            updateGroups()
        }
        children.forEach {
            it.setOnCheckedChangeListener { _, _ ->
                parent.setTag(disabledEventsTag, true)
                updateGroups()
                parent.setTag(disabledEventsTag, false)
            }
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
