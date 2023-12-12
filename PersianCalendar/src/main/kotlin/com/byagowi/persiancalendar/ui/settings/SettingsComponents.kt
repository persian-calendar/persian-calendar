package com.byagowi.persiancalendar.ui.settings

import android.app.AlertDialog
import android.content.SharedPreferences
import android.provider.MediaStore.Audio.Radio
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.RadioButton
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalButtonItemSpacer
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItemWithButton
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.appPrefs

@Composable
fun SettingsSection(title: String, subtitle: String? = null) {
    Spacer(Modifier.padding(top = 16.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        AnimatedVisibility(visible = subtitle != null) {
            Text(
                subtitle ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(AppBlendAlpha)
            )
        }
    }
}

@Composable
fun SettingsDivider() = HorizontalDivider(Modifier.padding(horizontal = 8.dp))

@Composable
fun SettingsClickable(title: String, summary: String? = null, action: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { action() }
            .padding(vertical = 16.dp, horizontal = 24.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (summary != null) Text(
            summary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alpha(AppBlendAlpha)
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
    var showDialog by remember { mutableStateOf(false) }
    SettingsClickable(title = title, summary = summary) { showDialog = true }
    if (showDialog) AppDialog(
        title = { Text(stringResource(dialogTitleResId)) },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { showDialog = false },
    ) {
        val currentValue = remember {
            context.appPrefs.getString(key, null) ?: defaultValue
        }
        entries.zip(entryValues) { entry, entryValue ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SettingsItemHeight.dp)
                    .clickable {
                        showDialog = false
                        context.appPrefs.edit { putString(key, entryValue) }
                        if (summaryResId == null) summary = entry
                    }
                    .padding(horizontal = SettingsHorizontalPaddingItemWithButton.dp),
            ) {
                RadioButton(selected = entryValue == currentValue, onClick = null)
                Spacer(modifier = Modifier.width(SettingsHorizontalButtonItemSpacer.dp))
                Text(entry)
            }
        }
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
        AlertDialog.Builder(context).setTitle(dialogTitleResId)
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
    followChanges: Boolean = false,
) {
    val context = LocalContext.current
    val appPrefs = remember { context.appPrefs }
    var currentValue by remember { mutableStateOf(appPrefs.getBoolean(key, defaultValue)) }
    if (followChanges) {
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
            .clickable(onClick = toggle)
            .padding(horizontal = 8.dp),
    ) {
        Column(
            Modifier
                .align(alignment = Alignment.CenterStart)
                // 68 is brought from androidx.preferences
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = (16 + 68).dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (summary != null) Text(
                summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(AppBlendAlpha)
            )
        }
        Switch(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 16.dp),
            checked = currentValue,
            onCheckedChange = null,
        )
    }
}
