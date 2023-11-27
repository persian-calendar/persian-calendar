package com.byagowi.persiancalendar.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.utils.appPrefs

@Composable
fun SettingsClickable(title: String, subtitle: String? = null, action: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = title) { action() }
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SettingsSwitch(key: String, defaultValue: Boolean, title: String, subtitle: String? = null) {
    val context = LocalContext.current
    val appPrefs = remember { context.appPrefs }
    var currentValue by remember { mutableStateOf(appPrefs.getBoolean(key, defaultValue)) }
    val toggle = remember {
        {
            currentValue = !currentValue
            appPrefs.edit { putBoolean(key, currentValue) }
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = title, onClick = toggle),
    ) {
        Column(
            Modifier
                .align(alignment = Alignment.CenterVertically)
                .padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        Switch(
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .padding(end = 16.dp),
            checked = currentValue,
            onCheckedChange = { toggle() },
        )
    }
}
