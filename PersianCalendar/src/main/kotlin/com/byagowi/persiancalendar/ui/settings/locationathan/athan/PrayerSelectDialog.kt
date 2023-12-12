package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.ATHANS_LIST
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.byagowi.persiancalendar.utils.splitFilterNotEmpty
import com.byagowi.persiancalendar.utils.startAthan

@Composable
fun PrayerSelectDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val alarms = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) {
        (context.appPrefs.getString(PREF_ATHAN_ALARM, null) ?: "")
            .splitFilterNotEmpty(",").toMutableStateList()
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.athan_alarm)) },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                context.appPrefs.edit { putString(PREF_ATHAN_ALARM, alarms.joinToString(",")) }
            }) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        ATHANS_LIST.forEach { key ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { if (key in alarms) alarms.remove(key) else alarms.add(key) }
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
                    .height(SettingsItemHeight.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(getPrayTimeName(key)), Modifier.weight(1f, fill = true))
                Switch(checked = key in alarms, onCheckedChange = null)
            }
        }
    }
}

@Composable
fun PrayerSelectPreviewDialog(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.preview)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        val context = LocalContext.current
        ATHANS_LIST.forEach {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        startAthan(context, it, null)
                    }
                    .height(SettingsItemHeight.dp)
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
            ) { Text(stringResource(getPrayTimeName(it))) }
        }
    }
}
