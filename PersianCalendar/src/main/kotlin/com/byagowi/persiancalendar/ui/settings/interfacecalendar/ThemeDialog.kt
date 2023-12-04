package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.utils.appPrefs

@Composable
fun ThemeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val currentTheme = remember {
        context.appPrefs.getString(PREF_THEME, null) ?: Theme.SYSTEM_DEFAULT.key
    }
    Dialog(
        title = { Text(stringResource(R.string.select_skin)) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        neutralButton = {
            if (Theme.supportsGradient(context)) {
                var isChecked by rememberSaveable {
                    mutableStateOf(
                        context.appPrefs.getBoolean(
                            PREF_THEME_GRADIENT,
                            DEFAULT_THEME_GRADIENT
                        )
                    )
                }

                fun onClick() {
                    isChecked = !isChecked
                    context.appPrefs.edit { putBoolean(PREF_THEME_GRADIENT, isChecked) }
                }
                Row(
                    Modifier.clickable { onClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(checked = isChecked, onCheckedChange = { onClick() })
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.color_gradient))
                }
            }
        },
    ) {
        Theme.entries.forEach { theme ->
            fun onClick() {
                onDismissRequest()
                context.appPrefs.edit { putString(PREF_THEME, theme.key) }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(onClick = ::onClick)
                    .padding(horizontal = 10.dp)
            ) {
                RadioButton(selected = theme.key == currentTheme, onClick = ::onClick)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(theme.title))
            }
        }
    }
}
