package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.content.Context
import android.os.Build
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.HolidaysTypesDialogBinding
import com.byagowi.persiancalendar.generated.EventType
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.EnabledHolidays
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language

fun showHolidaysTypesDialog(context: Context) {
    val binding = HolidaysTypesDialogBinding.inflate(context.layoutInflater)

    val pattern =
        """%s${language.spacedComma}<a href="%s">${context.getString(R.string.view_source)}</a>"""
    binding.iran.text = HtmlCompat.fromHtml(
        pattern.format(
            context.getString(R.string.iran_official_events), EventType.Iran.source
        ), HtmlCompat.FROM_HTML_MODE_COMPACT
    )
    binding.afghanistan.text = HtmlCompat.fromHtml(
        pattern.format(
            context.getString(R.string.afghanistan_events), EventType.Afghanistan.source
        ), HtmlCompat.FROM_HTML_MODE_COMPACT
    )

    // Make links work
    binding.iran.movementMethod = LinkMovementMethod.getInstance()
    binding.afghanistan.movementMethod = LinkMovementMethod.getInstance()

    // Update view from stored settings
    val checkboxToKeyPairs = listOf(
        binding.iranHolidays to EnabledHolidays.iranHolidaysKey,
        binding.iranOthers to EnabledHolidays.iranOthersKey,
        binding.afghanistanHolidays to EnabledHolidays.afghanistanHolidaysKey,
        binding.afghanistanOthers to EnabledHolidays.afghanistanOthersKey,
        binding.iranAncient to EnabledHolidays.iranAncientKey,
        binding.international to EnabledHolidays.internationalKey
    )
    val enabledHolidays = EnabledHolidays(context.appPrefs)
    checkboxToKeyPairs
        .forEach { (checkbox, key) -> checkbox.isChecked = key in enabledHolidays.enabledTypes }

    // Check boxes hierarchy
    val hierarchy = listOf(
        binding.iran to listOf(binding.iranHolidays, binding.iranOthers),
        binding.afghanistan to listOf(binding.afghanistanHolidays, binding.afghanistanOthers)
    )

    // Parents update logic
    fun updateParents() = hierarchy.forEach { (parent, children) ->
        parent.isChecked = children.any { it.isChecked }
        val isMixed = parent.isChecked && !children.all { it.isChecked }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parent.buttonDrawable?.alpha = if (isMixed) 127 else 255
        } else {
            parent.alpha = if (isMixed) .5f else 1f
        }
    }
    updateParents()
    hierarchy.forEach { (parent, children) ->
        // Add check click listeners
        parent.setOnCheckedChangeListener { _, _ ->
            if (!parent.isPressed) return@setOnCheckedChangeListener // Skip non user initiated changes
            val destination =
                !children.all { it.isChecked } // turn clear or mixed state to all checked
            children.forEach { it.isChecked = destination }
            updateParents()
        }
        children.forEach { it.setOnCheckedChangeListener { _, _ -> updateParents() } }
    }

    // Run the dialog
    AlertDialog.Builder(context)
        .setTitle(R.string.events)
        .setView(binding.root)
        .setPositiveButton(R.string.accept) { _, _ ->
            val result = checkboxToKeyPairs
                .mapNotNull { (checkBox, key) -> if (checkBox.isChecked) key else null }.toSet()
            context.appPrefs.edit { putStringSet(PREF_HOLIDAY_TYPES, result) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
