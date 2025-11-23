package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_BOLD_FONT
import com.byagowi.persiancalendar.PREF_CUSTOM_FONT_NAME
import com.byagowi.persiancalendar.PREF_CUSTOM_IMAGE_NAME
import com.byagowi.persiancalendar.PREF_RED_HOLIDAYS
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.STORED_FONT_NAME
import com.byagowi.persiancalendar.STORED_IMAGE_NAME
import com.byagowi.persiancalendar.global.customFontName
import com.byagowi.persiancalendar.global.customImageName
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.isRedHolidays
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.getFileName
import com.byagowi.persiancalendar.utils.preferences
import java.io.File
import kotlin.random.Random

@Composable
fun ThemeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val language by language.collectAsState()
    val userSetTheme by userSetTheme.collectAsState()
    var showMore by rememberSaveable { mutableStateOf(false) }
    val systemLightTheme by systemLightTheme.collectAsState()
    val systemDarkTheme by systemDarkTheme.collectAsState()
    val themesToCheck = run {
        if (userSetTheme == Theme.SYSTEM_DEFAULT) listOf(systemLightTheme, systemDarkTheme)
        else listOf(userSetTheme)
    }
    val anyThemeHasGradient = themesToCheck.any { it.hasGradient }
    val anyThemeIsDynamicColors = themesToCheck.any { it.isDynamicColors }
    AppDialog(
        title = { Text(stringResource(R.string.select_skin)) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.accept)) }
        },
        neutralButton = {
            AnimatedVisibility(visible = !showMore && (anyThemeHasGradient || anyThemeIsDynamicColors)) {
                TextButton(onClick = { showMore = true }) { Text(stringResource(R.string.more)) }
            }
        },
    ) {
        val invisible = Modifier
            .alpha(0f)
            .height(8.dp)
            .semantics { this.hideFromAccessibility() }
        val systemThemeOptions = listOf(
            Triple(R.string.theme_light, PREF_SYSTEM_LIGHT_THEME, systemLightTheme),
            Triple(R.string.theme_dark, PREF_SYSTEM_DARK_THEME, systemDarkTheme)
        )
        Theme.entries.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        context.preferences.edit {
                            putString(PREF_THEME, entry.key)
                            // Consider returning to system default as some sort of theme reset
                            if (userSetTheme != Theme.SYSTEM_DEFAULT && entry == Theme.SYSTEM_DEFAULT) {
                                remove(PREF_SYSTEM_LIGHT_THEME)
                                remove(PREF_SYSTEM_DARK_THEME)
                                remove(PREF_RED_HOLIDAYS)
                                remove(PREF_THEME_GRADIENT)
                            }
                        }
                    }
                    .padding(start = SettingsHorizontalPaddingItem.dp),
            ) {
                Box(Modifier.height(SettingsItemHeight.dp))
                RadioButton(selected = entry == userSetTheme, onClick = null)
                Spacer(Modifier.width(SettingsHorizontalPaddingItem.dp))
                Column(Modifier.padding(end = 16.dp)) {
                    Text(stringResource(entry.title))
                    if (!showMore && entry == Theme.SYSTEM_DEFAULT && language.isPersianOrDari && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Text(
                        text = "براساس حالت تاریک و رنگ‌بندی پس‌زمینه دستگاه",
                        color = LocalContentColor.current.copy(alpha = AppBlendAlpha),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                this.AnimatedVisibility(visible = showMore) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        systemThemeOptions.forEach { (label, preferenceKey, selectedTheme) ->
                            // To make sure the label and radio button will take the same size
                            Box(contentAlignment = Alignment.TopCenter) {
                                val isDark = entry.isDark == true
                                val disabledRadio =
                                    !isDark.xor(preferenceKey == PREF_SYSTEM_LIGHT_THEME)
                                RadioButton(
                                    selected = selectedTheme == entry,
                                    enabled = !disabledRadio,
                                    onClick = {
                                        context.preferences.edit {
                                            putString(preferenceKey, entry.key)
                                        }
                                    },
                                    modifier = when {
                                        disabledRadio -> invisible
                                        entry == Theme.SYSTEM_DEFAULT -> invisible
                                        entry.isDark == userSetTheme.isDark -> invisible
                                        else -> Modifier
                                    },
                                )
                                Text(
                                    stringResource(label),
                                    modifier = if (entry == Theme.SYSTEM_DEFAULT) Modifier else invisible,
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                    }
                }
            }
        }
        val isGradient by isGradient.collectAsState()
        this.AnimatedVisibility(
            visible = (showMore || !isGradient) && anyThemeHasGradient,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            SwitchWithLabel(
                label = stringResource(R.string.color_gradient),
                checked = isGradient,
            ) { context.preferences.edit { putBoolean(PREF_THEME_GRADIENT, it) } }
        }
        val isRedHolidays by isRedHolidays.collectAsState()
        this.AnimatedVisibility(
            visible = (showMore || isRedHolidays) && anyThemeIsDynamicColors,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            SwitchWithLabel(
                label = stringResource(R.string.holidays_in_red),
                checked = isRedHolidays,
            ) { context.preferences.edit { putBoolean(PREF_RED_HOLIDAYS, it) } }
        }
        val isBoldFont by isBoldFont.collectAsState()
        this.AnimatedVisibility(
            visible = showMore || isBoldFont,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            SwitchWithLabel(
                label = stringResource(R.string.bold_text),
                checked = isBoldFont,
            ) { context.preferences.edit { putBoolean(PREF_BOLD_FONT, it) } }
        }

        FontPicker(onDismissRequest, showMore)
        ImagePicker(showMore)
    }
}

@Composable
private fun ColumnScope.FontPicker(
    onDismissRequest: () -> Unit,
    showMore: Boolean,
) {
    val customFontToken by customFontName.collectAsState()
    val context = LocalContext.current
    val fontPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) context.contentResolver.openInputStream(uri)?.use { inputStream ->
            File(context.filesDir, STORED_FONT_NAME).outputStream().use(inputStream::copyTo)
            // It's funny but if dialog isn't closed before the font change it can cause crash
            // with "Out of order buffers detected for RequestedLayerState" and
            // "Out of order buffers detected for RequestedLayerState" messages and without
            // any vm stacktrace…
            onDismissRequest()
            context.preferences.edit {
                putString(
                    PREF_CUSTOM_FONT_NAME,
                    "${Random.nextDouble()}/${getFileName(context, uri)}",
                )
            }
        }
    }
    this.AnimatedVisibility(visible = showMore || customFontToken != null) {
        val language by language.collectAsState()
        Column(Modifier.padding(start = 24.dp)) {
            Row {
                Button(onClick = {
                    if (language.isPersianOrDari) Toast.makeText(
                        context, "پرونده‌ای در قالب ttf یا otf انتخاب کنید", Toast.LENGTH_LONG
                    ).show()
                    fontPicker.launch(
                        listOf(
                            "font/ttf",
                            "font/otf",
                            "font/sfnt",
                            "font/collection",
                            "font/truetype",
                            "font/opentype",
                            "application/x-font-ttf",
                            "application/x-font-otf",
                        ).let {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) it + "application/octet-stream"
                            else it
                        }.toTypedArray()
                    )
                }) { Text(stringResource(R.string.select_font)) }
                this.AnimatedVisibility(
                    customFontToken != null,
                    Modifier.padding(start = 8.dp),
                ) {
                    OutlinedIconButton({
                        context.preferences.edit { remove(PREF_CUSTOM_FONT_NAME) }
                        File(context.filesDir, STORED_FONT_NAME).delete()
                    }) { Icon(Icons.Default.Delete, stringResource(R.string.remove)) }
                }
            }
            AnimatedVisibility(customFontToken != null) {
                Text(customFontToken.orEmpty().split("/").last().split(".").first())
            }
        }
    }
}

@Composable
private fun ColumnScope.ImagePicker(showMore: Boolean) {
    if (!BuildConfig.DEVELOPMENT) return
    val customImageName by customImageName.collectAsState()
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) context.contentResolver.openInputStream(uri)?.use { inputStream ->
            File(context.filesDir, STORED_IMAGE_NAME).outputStream().use(inputStream::copyTo)
            context.preferences.edit {
                putString(PREF_CUSTOM_IMAGE_NAME, getFileName(context, uri))
            }
        }
    }
    this.AnimatedVisibility(visible = showMore || customImageName != null) {
        Column(Modifier.padding(start = 24.dp)) {
            Row {
                FilledIconButton(onClick = {
                    val mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    imagePicker.launch(PickVisualMediaRequest(mediaType))
                }) { Icon(Icons.Default.Image, null) }
                this.AnimatedVisibility(
                    customImageName != null,
                    Modifier.padding(start = 8.dp),
                ) {
                    OutlinedIconButton({
                        context.preferences.edit { remove(PREF_CUSTOM_IMAGE_NAME) }
                        File(context.filesDir, STORED_FONT_NAME).delete()
                    }) { Icon(Icons.Default.Delete, stringResource(R.string.remove)) }
                }
            }
            AnimatedVisibility(customImageName != null) {
                Text(customImageName.orEmpty())
            }
        }
    }
}
