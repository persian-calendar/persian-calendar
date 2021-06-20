package com.byagowi.persiancalendar.ui.preferences

import androidx.annotation.StringRes
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.R

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
fun MultiSelectListPreference.dialogTitle(@StringRes dialogTitleResId: Int) =
    setDialogTitle(dialogTitleResId)

@PreferencesDsl
fun ListPreference.dialogTitle(@StringRes dialogTitleResId: Int) =
    setDialogTitle(dialogTitleResId)

@PreferencesDsl
fun Preference.summary(@StringRes summaryResId: Int) = setSummary(summaryResId)

@PreferencesDsl
inline fun PreferenceCategory.singleSelect(
    key: String, entries: List<String>, entryValues: List<String>, defaultValue: String,
    crossinline block: ListPreference.() -> Unit
) = this.addPreference(ListPreference(this.context).also {
    it.key = key
    it.entries = entries.toTypedArray()
    it.entryValues = entryValues.toTypedArray()
    it.setDefaultValue(defaultValue)
    it.setNegativeButtonText(R.string.cancel)
    it.isIconSpaceReserved = false
    block(it)
})

@PreferencesDsl
inline fun PreferenceCategory.multiSelect(
    key: String,
    entries: List<String>, entryValues: List<String>, defaultValue: Set<String>,
    crossinline block: MultiSelectListPreference.() -> Unit
) = this.addPreference(MultiSelectListPreference(this.context).also {
    it.key = key
    it.entries = entries.toTypedArray()
    it.entryValues = entryValues.toTypedArray()
    it.setDefaultValue(defaultValue)
    it.setNegativeButtonText(R.string.cancel)
    it.setPositiveButtonText(R.string.accept)
    it.isIconSpaceReserved = false
    block(it)
})

@PreferencesDsl
inline fun PreferenceCategory.switch(
    key: String, defaultValue: Boolean, crossinline block: SwitchPreferenceCompat.() -> Unit
) = this.addPreference(SwitchPreferenceCompat(this.context).also {
    it.key = key
    it.setDefaultValue(defaultValue)
    it.isIconSpaceReserved = false
    block(it)
})
