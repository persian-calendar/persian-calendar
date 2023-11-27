package com.byagowi.persiancalendar.ui.settings

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Composable
fun SettingsSection(title: String) {
    Spacer(Modifier.padding(top = 16.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun SettingsClickable(title: String, summary: String? = null, action: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { action() }
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (summary != null) Text(
            summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.alpha(.8f)
        )
    }
}

@Composable
fun SettingsSingleSelect(
    key: String,
    entries: List<String>,
    entryValues: List<String>,
    defaultValue: String,
    dialogTitleResId: Int,
    title: String,
    summaryResId: Int? = null
) {
    val context = LocalContext.current
    var summary by remember {
        mutableStateOf(
            if (summaryResId == null) entries[entryValues.indexOf(
                context.appPrefs.getString(key, null) ?: defaultValue
            )] else context.getString(summaryResId)
        )
    }
    SettingsClickable(title = title, summary = summary) {
        val currentValue = entryValues.indexOf(
            context.appPrefs.getString(key, null) ?: defaultValue
        )
        MaterialAlertDialogBuilder(context).setTitle(dialogTitleResId)
            .setNegativeButton(R.string.cancel, null)
            .setSingleChoiceItems(entries.toTypedArray(), currentValue) { dialog, which ->
                context.appPrefs.edit { putString(key, entryValues[which]) }
                if (summaryResId == null) summary = entries[which]
                dialog.dismiss()
            }.show()
    }
}

@Composable
fun SettingsMultiSelect(
    key: String,
    entries: List<String>,
    entryValues: List<String>,
    defaultValue: Set<String>,
    dialogTitleResId: Int,
    title: String,
    summary: String? = null,
) {
    val context = LocalContext.current
    SettingsClickable(title = title, summary = summary) {
        val result = (context.appPrefs.getStringSet(key, null) ?: defaultValue).toMutableSet()
        val checkedItems = entryValues.map { it in result }.toBooleanArray()
        MaterialAlertDialogBuilder(context).setTitle(dialogTitleResId)
            .setMultiChoiceItems(entries.toTypedArray(), checkedItems) { _, which, isChecked ->
                if (isChecked) result.add(entryValues[which])
                else result.remove(entryValues[which])
            }.setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.accept) { _, _ ->
                context.appPrefs.edit { putStringSet(key, result) }
            }.show()
    }
}

@Composable
fun SettingsSwitch(
    key: String,
    defaultValue: Boolean,
    title: String,
    summary: String? = null,
    onBeforeToggle: (Boolean) -> Boolean = { it },
    watchChanges: Boolean = false,
) {
    val context = LocalContext.current
    val appPrefs = remember { context.appPrefs }
    var currentValue by remember { mutableStateOf(appPrefs.getBoolean(key, defaultValue)) }
    if (watchChanges) {
        DisposableEffect(null) {
            val listener = { prefs: SharedPreferences, changeKey: String? ->
                if (changeKey == key) currentValue = prefs.getBoolean(key, defaultValue)
            }
            appPrefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { appPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
    }
    val toggle = remember {
        {
            val previousValue = currentValue
            currentValue = onBeforeToggle(!currentValue)
            if (previousValue != currentValue) appPrefs.edit { putBoolean(key, currentValue) }
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = toggle),
    ) {
        Column(
            Modifier
                .align(alignment = Alignment.CenterStart)
                // 68 is brought from androidx.preferences
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = (16 + 68).dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (summary != null) Text(
                summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.alpha(.8f)
            )
        }
        Switch(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 16.dp),
            checked = currentValue,
            onCheckedChange = { toggle() },
        )
    }
}
