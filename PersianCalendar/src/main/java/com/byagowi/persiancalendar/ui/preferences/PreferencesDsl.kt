package com.byagowi.persiancalendar.ui.preferences

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.R

@DslMarker
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
annotation class PreferencesDsl

// Builds preference screen pair of title ids to list of preferences
// Its only caveat is it turns title's integers to string to be used as category keys
// so they can be referenced later and used for expansion logic.
@PreferencesDsl
inline fun PreferenceScreen.build(crossinline block: PreferenceScreen.() -> Unit) =
    this.also { block(it) }

@PreferencesDsl
inline fun PreferenceScreen.section(
    @StringRes title: Int, crossinline block: PreferenceCategory.() -> Unit
) {
    val category = PreferenceCategory(context)
    category.key = title.toString()
    category.setTitle(title)
    category.isIconSpaceReserved = false
    this.addPreference(category)
    category.block()
}

@PreferencesDsl
inline fun PreferenceCategory.clickable(
    crossinline onClick: () -> Unit, crossinline block: Preference.() -> Unit
) = Preference(this.context).also {
    it.setOnPreferenceClickListener { onClick(); true /* it captures the click event */ }
    it.isIconSpaceReserved = false
    block(it)
    this.addPreference(it)
}

@PreferencesDsl
fun Preference.key(key: String) = setKey(key)

@PreferencesDsl
fun Preference.title(titleResId: Int) = setTitle(titleResId)

@PreferencesDsl
fun MultiSelectListPreference.dialogTitle(dialogTitleResId: Int) = setDialogTitle(dialogTitleResId)

@PreferencesDsl
fun ListPreference.dialogTitle(dialogTitleResId: Int) = setDialogTitle(dialogTitleResId)

@PreferencesDsl
fun Preference.summary(titleResId: Int) = setSummary(titleResId)

@PreferencesDsl
inline fun PreferenceCategory.singleSelect(
    key: String, @ArrayRes entriesResId: Int, @ArrayRes entryValuesResId: Int, defaultValue: String,
    crossinline block: ListPreference.() -> Unit
) = ListPreference(this.context).also {
    it.key = key
    it.setEntries(entriesResId)
    it.setEntryValues(entryValuesResId)
    it.setDefaultValue(defaultValue)
    it.setNegativeButtonText(R.string.cancel)
    it.isIconSpaceReserved = false
    block(it)
    this.addPreference(it)
}

@PreferencesDsl
inline fun PreferenceCategory.multiSelect(
    key: String,
    @ArrayRes entriesResId: Int, @ArrayRes entryValuesResId: Int, defaultValue: Set<String>,
    crossinline block: MultiSelectListPreference.() -> Unit
) = MultiSelectListPreference(this.context).also {
    it.key = key
    it.setEntries(entriesResId)
    it.setEntryValues(entryValuesResId)
    it.setDefaultValue(defaultValue)
    it.setNegativeButtonText(R.string.cancel)
    it.setPositiveButtonText(R.string.accept)
    it.isIconSpaceReserved = false
    block(it)
    this.addPreference(it)
}

@PreferencesDsl
inline fun PreferenceCategory.switch(
    key: String, defaultValue: Boolean, crossinline block: SwitchPreferenceCompat.() -> Unit
) = SwitchPreferenceCompat(this.context).also {
    it.key = key
    it.setDefaultValue(defaultValue)
    it.isIconSpaceReserved = false
    block(it)
    this.addPreference(it)
}
