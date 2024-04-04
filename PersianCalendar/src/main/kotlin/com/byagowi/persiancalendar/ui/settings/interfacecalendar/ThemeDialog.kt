package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_RED_HOLIDAYS
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.isRedHolidays
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
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
    var showMore by rememberSaveable { mutableStateOf(false) }
    AppDialog(
        title = { Text(stringResource(R.string.select_skin)) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        neutralButton = {
            AnimatedVisibility(visible = !showMore && (theme.hasGradient || theme.isDynamicColors)) {
                TextButton(onClick = { showMore = true }) { Text(stringResource(R.string.more)) }
            }
        },
    ) {
        AnimatedVisibility(visible = showMore && theme == Theme.SYSTEM_DEFAULT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                RadioOrLabel(R.string.theme_dark)
                RadioOrLabel(R.string.theme_light)
                Spacer(Modifier.width(8.dp))
            }
        }
        val selectedDarkTheme by systemDarkTheme.collectAsState()
        val selectedLightTheme by systemLightTheme.collectAsState()
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
                    .padding(start = SettingsHorizontalPaddingItem.dp),
            ) {
                RadioButton(selected = entry == theme, onClick = null)
                Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                Text(stringResource(entry.title))
                AnimatedVisibility(visible = showMore && theme == Theme.SYSTEM_DEFAULT) {
                    Spacer(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                    Row {
                        RadioOrLabel(R.string.theme_dark, selectedDarkTheme == entry) {
                            context.appPrefs.edit { putString(PREF_SYSTEM_DARK_THEME, entry.key) }
                        }
                        RadioOrLabel(R.string.theme_light, selectedLightTheme == entry) {
                            context.appPrefs.edit { putString(PREF_SYSTEM_LIGHT_THEME, entry.key) }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showMore && theme.hasGradient,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            val isGradient by isGradient.collectAsState()
            SwitchWithLabel(
                label = stringResource(R.string.color_gradient),
                checked = isGradient,
            ) { context.appPrefs.edit { putBoolean(PREF_THEME_GRADIENT, !isGradient) } }
        }
        AnimatedVisibility(
            visible = showMore && theme.isDynamicColors,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            val isRedHolidays by isRedHolidays.collectAsState()
            SwitchWithLabel(
                label = stringResource(R.string.holidays_in_red),
                checked = isRedHolidays,
            ) { context.appPrefs.edit { putBoolean(PREF_RED_HOLIDAYS, !isRedHolidays) } }
        }
    }
}

@Composable
private fun RadioOrLabel(
    @StringRes label: Int,
    selected: Boolean? = null,
    onClick: () -> Unit = {},
) {
    Box {
        val invisible = Modifier
            .alpha(0f)
            .height(8.dp)
            .align(Alignment.Center)
            .semantics { @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser() }
        val center = Modifier.align(Alignment.Center)

        RadioButton(
            selected = selected ?: false,
            onClick = onClick,
            modifier = if (selected == null) invisible else center,
        )
        Text(stringResource(label), modifier = if (selected == null) center else invisible)
    }
}
