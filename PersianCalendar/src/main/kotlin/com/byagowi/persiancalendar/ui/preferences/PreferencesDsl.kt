package com.byagowi.persiancalendar.ui.preferences

import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
    category.layoutResource = R.layout.preference_category_layout
    this.addPreference(category)
    category.block()
}

@PreferencesDsl
inline fun PreferenceCategory.clickable(
    crossinline onClick: () -> Unit, crossinline block: Preference.() -> Unit
) = this.addPreference(Preference(this.context).also {
    it.setOnPreferenceClickListener { onClick(); true /* it captures the click event */ }
    it.isIconSpaceReserved = false
    it.layoutResource = R.layout.preference_layout
    block(it)
})

@PreferencesDsl
fun Preference.title(@StringRes titleResId: Int) = setTitle(titleResId)

@PreferencesDsl
fun Preference.summary(@StringRes summaryResId: Int) = setSummary(summaryResId)

@PreferencesDsl
inline fun PreferenceCategory.singleSelect(
    key: String, entries: List<String>, entryValues: List<String>, defaultValue: String,
    dialogTitleResId: Int, summaryResId: Int? = null, crossinline block: Preference.() -> Unit
) {
    var preference: Preference? = null
    this.clickable(
        onClick = {
            val currentValue = entryValues.indexOf(
                context.appPrefs.getString(key, null) ?: defaultValue
            )
            MaterialAlertDialogBuilder(context)
                .setTitle(dialogTitleResId)
                .setNegativeButton(R.string.cancel, null)
                .setSingleChoiceItems(entries.toTypedArray(), currentValue) { dialog, which ->
                    context.appPrefs.edit { putString(key, entryValues[which]) }
                    preference?.summary = entries[which]
                    dialog.dismiss()
                }
                .show()
        },
        block = {
            preference = this
            if (summaryResId != null) setSummary(summaryResId)
            else summary = entries[entryValues.indexOf(
                context.appPrefs.getString(key, null) ?: defaultValue
            )]
            block()
        }
    )
}

@PreferencesDsl
inline fun PreferenceCategory.multiSelect(
    key: String,
    entries: List<String>, entryValues: List<String>, defaultValue: Set<String>,
    dialogTitleResId: Int, crossinline block: Preference.() -> Unit
) = this.clickable(
    onClick = {
        val result = (context.appPrefs.getStringSet(key, null) ?: defaultValue).toMutableSet()
        val checkedItems = entryValues.map { it in result }.toBooleanArray()
        MaterialAlertDialogBuilder(context)
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
    it.layoutResource = R.layout.preference_layout
    block(it)
})
