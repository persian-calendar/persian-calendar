package com.byagowi.persiancalendar.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.settings.common.ColorBox
import com.byagowi.persiancalendar.ui.settings.common.ColorPickerDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.performLongPress
import com.byagowi.persiancalendar.utils.preferences
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsSection(isAtTop: Boolean, title: String, subtitle: String? = null) {
    Column(
        Modifier
            .background(if (isAtTop) Color.Transparent else MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val color by animateColor(MaterialTheme.colorScheme.outlineVariant)
            HorizontalDivider(color = color, modifier = Modifier.weight(1f))
            AnimatedContent(
                title,
                contentAlignment = Alignment.Center,
                label = "title",
                transitionSpec = appCrossfadeSpec,
                modifier = Modifier
                    .padding(horizontal = 16.dp),
            ) { state ->
                Text(
                    state,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            HorizontalDivider(color = color, modifier = Modifier.weight(1f))
        }
        this.AnimatedVisibility(visible = subtitle != null) {
            Text(
                subtitle.orEmpty(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(AppBlendAlpha)
            )
        }
    }
}

@Composable
fun SettingsClickable(
    title: String,
    summary: String? = null,
    defaultOpen: Boolean = false,
    dialog: @Composable (onDismissRequest: () -> Unit) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(defaultOpen) }
    SettingsLayout(
        modifier = Modifier.clickable { showDialog = true },
        title = title,
        summary = summary,
    )
    if (showDialog) dialog { showDialog = false }
}

// 52dp is switch's width, widget is what was called in androidx.preference
private const val widgetSize = 52f

@Composable
fun SettingsColor(
    title: String,
    summary: String? = null,
    isBackgroundPick: Boolean,
    key: String,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var persistedColor by remember {
        val initialColor = Color(
            context.preferences.getString(key, null)?.toColorInt()
                ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
                else DEFAULT_SELECTED_WIDGET_TEXT_COLOR
        )
        mutableStateOf(initialColor)
    }
    SettingsLayout(
        title = title,
        summary = summary,
        modifier = Modifier
            .clickable { showDialog = true }
            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
            .clearAndSetSemantics {},
    ) {
        ColorBox(
            color = animateColor(persistedColor).value,
            size = widgetSize.dp,
            shape = MaterialTheme.shapes.large,
            outlineColor = MaterialTheme.colorScheme.outline,
            outlineWidth = 2.dp,
        )
    }
    if (showDialog) ColorPickerDialog(
        title = title,
        isBackgroundPick = isBackgroundPick,
        initialColor = persistedColor,
        persistColor = { color ->
            persistedColor = color
            val colorResult = if (isBackgroundPick) "#%08X".format(
                Locale.ENGLISH, 0xFFFFFFFF and color.toArgb().toLong()
            ) else "#%06X".format(Locale.ENGLISH, 0xFFFFFF and color.toArgb())
            context.preferences.edit { putString(key, colorResult) }
        },
    ) { showDialog = false }
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
    var summary by remember(language.collectAsState().value) {
        mutableStateOf(
            if (summaryResId == null) entries[entryValues.indexOf(
                context.preferences.getString(key, null) ?: defaultValue
            )] else context.getString(summaryResId)
        )
    }
    SettingsClickable(title = title, summary = summary) { onDismissRequest ->
        AppDialog(
            title = { Text(stringResource(dialogTitleResId)) },
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
            },
            onDismissRequest = onDismissRequest,
        ) {
            val currentValue = remember {
                context.preferences.getString(key, null) ?: defaultValue
            }
            entries.zip(entryValues) { entry, entryValue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SettingsItemHeight.dp)
                        .selectable(entryValue == currentValue, role = Role.RadioButton) {
                            onDismissRequest()
                            context.preferences.edit { putString(key, entryValue) }
                            if (summaryResId == null) summary = entry
                        }
                        .padding(horizontal = SettingsHorizontalPaddingItem.dp),
                ) {
                    RadioButton(selected = entryValue == currentValue, onClick = null)
                    Spacer(Modifier.width(SettingsHorizontalPaddingItem.dp))
                    Text(
                        entry,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 9.sp,
                            maxFontSize = LocalTextStyle.current.fontSize,
                        ),
                    )
                }
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
    fun generateSummary(items: List<String>): String =
        items.map(entryValues::indexOf).sorted().joinToString(spacedComma) { entries[it] }

    val context = LocalContext.current
    var summaryToShow by remember(language.collectAsState().value) {
        val preferences = context.preferences
        val items = preferences.getStringSet(key, null) ?: defaultValue
        mutableStateOf(summary ?: generateSummary(items.toList()))
    }
    SettingsClickable(title = title, summary = summaryToShow) { onDismissRequest ->
        val result = rememberSaveable(
            saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
        ) {
            (context.preferences.getStringSet(key, null) ?: defaultValue).toList()
                .toMutableStateList()
        }
        AppDialog(
            title = { Text(stringResource(dialogTitleResId)) },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissRequest()
                    context.preferences.edit { putStringSet(key, result.toSet()) }
                    if (summary == null) summaryToShow = generateSummary(result)
                }) { Text(stringResource(R.string.accept)) }
            },
        ) {
            entries.zip(entryValues) { entry, entryValue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SettingsItemHeight.dp)
                        .toggleable(value = entryValue in result, role = Role.Checkbox) {
                            if (it) result.add(entryValue) else result.remove(entryValue)
                        }
                        .padding(horizontal = SettingsHorizontalPaddingItem.dp),
                ) {
                    Checkbox(checked = entryValue in result, onCheckedChange = null)
                    Spacer(Modifier.width(SettingsHorizontalPaddingItem.dp))
                    Text(
                        entry,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 9.sp,
                            maxFontSize = LocalTextStyle.current.fontSize,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    key: String,
    value: Boolean,
    title: String,
    summary: String? = null,
    extraWidget: (@Composable () -> Unit)? = null,
    onBeforeToggle: (Boolean) -> Boolean = { it },
) {
    val context = LocalContext.current
    val toggle = { newValue: Boolean ->
        val finalValue = onBeforeToggle(newValue)
        if (value != finalValue) context.preferences.edit { putBoolean(key, finalValue) }
    }
    val hapticFeedback = LocalHapticFeedback.current
    SettingsLayout(
        title = title,
        summary = summary,
        extraWidget = extraWidget,
        modifier = Modifier.toggleable(value, role = Role.Switch) {
            hapticFeedback.performLongPress()
            toggle(it)
        },
    ) { Switch(checked = value, onCheckedChange = null) }
}

@Composable
fun SettingsHelp(title: String) {
    SettingsLayout(
        title = title,
        summary = null,
        modifier = Modifier,
        widget = {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Help,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    )
}

@Composable
fun SettingsLayout(
    title: String,
    summary: String?,
    modifier: Modifier,
    extraWidget: (@Composable () -> Unit)? = null,
    widget: (@Composable () -> Unit)? = null,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .then(modifier)
            .padding(horizontal = 8.dp),
    ) {
        val endPadding = 16 + (if (widget != null) widgetSize + 16f else 0f) +
                (if (extraWidget != null) widgetSize + 8f else 0f)
        Column(
            Modifier
                .align(alignment = Alignment.CenterStart)
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = endPadding.dp),
        ) {
            AnimatedContent(
                title,
                label = "title",
                transitionSpec = appCrossfadeSpec,
            ) { state -> Text(state, style = MaterialTheme.typography.bodyLarge) }
            if (summary != null) {
                AnimatedContent(
                    summary,
                    label = "summary",
                    transitionSpec = appCrossfadeSpec,
                ) { state ->
                    Text(
                        state,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(AppBlendAlpha),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(widgetSize.dp),
            contentAlignment = Alignment.Center,
        ) { if (widget != null) widget() }
        if (extraWidget != null) Box(
            Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = (widgetSize + 16 + 8f).dp)
                .size(widgetSize.dp),
            contentAlignment = Alignment.Center,
        ) { extraWidget() }
    }
}

@Composable
fun SettingsSlider(
    title: String,
    value: Float,
    defaultValue: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)) {
        AnimatedContent(
            title,
            label = "title",
            transitionSpec = appCrossfadeSpec,
        ) { state -> Text(state, style = MaterialTheme.typography.bodyLarge) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = animateFloatAsState(value).value.coerceIn(0f, 1f),
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            val roundedValue = (value * 100).roundToInt()
            val numeral by numeral.collectAsState()
            Box(contentAlignment = Alignment.Center) {
                Text(
                    numeral.format(100),
                    Modifier.semantics { this.hideFromAccessibility() },
                    color = Color.Transparent,
                )
                Text(numeral.format(roundedValue))
            }
            AnimatedVisibility(roundedValue == 0) { Spacer(Modifier.width(16.dp)) }
            AnimatedVisibility(roundedValue != 0) {
                IconButton(onClick = { onValueChange(defaultValue) }) {
                    Icon(Icons.Default.SettingsBackupRestore, stringResource(R.string.cancel))
                }
            }
        }
    }
}
