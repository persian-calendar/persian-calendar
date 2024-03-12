package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.appPrefs

@Composable
fun ThemeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val theme by theme.collectAsState()
    AppDialog(
        title = { Text(stringResource(R.string.select_skin)) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        neutralButton = {
            AnimatedVisibility(theme.hasGradient, enter = fadeIn(), exit = fadeOut()) {
                val isGradient by isGradient.collectAsState()
                SwitchWithLabel(
                    label = stringResource(R.string.color_gradient),
                    checked = isGradient,
                ) { context.appPrefs.edit { putBoolean(PREF_THEME_GRADIENT, !isGradient) } }
            }
        },
    ) {
        Theme.entries.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SettingsItemHeight.dp)
                    .clickable {
                        onDismissRequest()
                        context.appPrefs.edit { putString(PREF_THEME, entry.key) }
                    }
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp),
            ) {
                RadioButton(selected = entry == theme, onClick = null)
                Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                Text(stringResource(entry.title))
            }
        }
    }
}
