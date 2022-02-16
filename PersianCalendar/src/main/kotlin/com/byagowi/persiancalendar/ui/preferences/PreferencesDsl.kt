package com.byagowi.persiancalendar.ui.preferences

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs

@DslMarker
@Target(AnnotationTarget.FUNCTION)
annotation class PreferencesDsl

@PreferencesDsl
inline fun PreferenceScreen.build(crossinline block: PreferenceScreen.() -> Unit) =
    this.also { block(it) }

@PreferencesDsl
inline fun PreferenceScreen.section(
    @StringRes title: Int, crossinline block: PreferenceCategory.() -> Unit
) {
    val category = PreferenceCategory(context)
    category.key = title.toString() // turns title's int id to string to make expansion logic work
    category.setTitle(title)
    category.isIconSpaceReserved = false
    this.addPreference(category)
    category.block()
}

@PreferencesDsl
inline fun PreferenceCategory.clickable(
    crossinline onClick: () -> Unit, crossinline block: Preference.() -> Unit
) = this.addPreference(Preference(this.context).also {
    it.setOnPreferenceClickListener { onClick(); true /* it captures the click event */ }
    it.isIconSpaceReserved = false
    block(it)
})

@PreferencesDsl
fun Preference.title(@StringRes titleResId: Int) = setTitle(titleResId)

@PreferencesDsl
fun Preference.summary(@StringRes summaryResId: Int) = setSummary(summaryResId)

@PreferencesDsl
inline fun PreferenceCategory.singleSelect(
    key: String, entries: List<String>, entryValues: List<String>, defaultValue: String,
    dialogTitleResId: Int, summaryResId: Int? = null, crossinline block: ListPreference.() -> Unit
) = this.addPreference(ListPreference(this.context).also {
    it.key = key
    it.setDialogTitle(dialogTitleResId)
    it.entries = entries.toTypedArray()
    it.entryValues = entryValues.toTypedArray()
    it.setDefaultValue(defaultValue)
    it.setNegativeButtonText(R.string.cancel)
    it.isIconSpaceReserved = false
    if (summaryResId != null) it.setSummary(summaryResId)
    else it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
    block(it)
})

@PreferencesDsl
inline fun PreferenceCategory.multiSelect(
    key: String,
    entries: List<String>, entryValues: List<String>, defaultValue: Set<String>,
    dialogTitleResId: Int, crossinline block: Preference.() -> Unit
) = this.clickable(
    onClick = {
        val result = (context.appPrefs.getStringSet(key, null) ?: defaultValue).toMutableSet()
        val checkedItems = entryValues.map { it in result }.toBooleanArray()
        AlertDialog.Builder(context)
            .setTitle(dialogTitleResId)
            .setMultiChoiceItems(entries.toTypedArray(), checkedItems) { _, which, isChecked ->
                if (isChecked) result.add(entryValues[which])
                else result.remove(entryValues[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.accept) { _, _ ->
                context.appPrefs.edit { putStringSet(key, result) }
            }
            .show()
    },
    block = block
)

@PreferencesDsl
inline fun PreferenceCategory.switch(
    key: String, defaultValue: Boolean, crossinline block: SwitchPreferenceCompat.() -> Unit
) = this.addPreference(SwitchPreferenceCompat(this.context).also {
    it.key = key
    it.setDefaultValue(defaultValue)
    it.isIconSpaceReserved = false
    block(it)
})
