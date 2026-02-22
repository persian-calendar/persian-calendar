package com.byagowi.persiancalendar.ui.settings

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
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
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.preferencesUpdateToken
import com.byagowi.persiancalendar.ui.settings.common.ColorBox
import com.byagowi.persiancalendar.ui.settings.common.ColorPickerDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appSliderColor
import com.byagowi.persiancalendar.ui.theme.appSwitchColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.highlightItem
import com.byagowi.persiancalendar.ui.utils.performLongPress
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import java.util.Locale
import kotlin.math.roundToInt

fun LazyListScope.settingsSection(
    canScrollBackward: Boolean,
    disableStickyHeader: Boolean,
    @StringRes title: Int,
    subtitle: @Composable () -> String? = { null },
    content: @Composable ColumnScope.() -> Unit,
) {
    if (disableStickyHeader) item { SettingsSectionLayout(title, subtitle) } else stickyHeader {
        Box(
            if (canScrollBackward) Modifier.background(
                Brush.verticalGradient(
                    .75f to animateColor(
                        MaterialTheme.colorScheme.surface.copy(alpha = .9f),
                    ).value,
                    1f to Color.Transparent,
                ),
            ) else Modifier,
        ) { SettingsSectionLayout(title, subtitle) }
    }
    item { Column { content() } }
}

@SuppressLint("ComposableLambdaParameterNaming")
@Composable
fun SettingsSectionLayout(@StringRes title: Int, subtitle: @Composable () -> String? = { null }) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val dividerColor by animateColor(MaterialTheme.colorScheme.outlineVariant)
            HorizontalDivider(color = dividerColor, modifier = Modifier.weight(1f))
            AnimatedContent(
                targetState = stringResource(title),
                contentAlignment = Alignment.Center,
                transitionSpec = appCrossfadeSpec,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) { state ->
                Text(
                    state,
                    style = MaterialTheme.typography.bodyMedium,
                    color = animateColor(MaterialTheme.colorScheme.primary).value,
                )
            }
            HorizontalDivider(color = dividerColor, modifier = Modifier.weight(1f))
        }
        val subtitle = subtitle()
        AnimatedVisibility(visible = subtitle != null) {
            Text(
                subtitle.orEmpty(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(AppBlendAlpha),
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
        modifier = Modifier
            .clickable { showDialog = true }
            .highlightItem(defaultOpen && !showDialog),
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
    val currentColor = remember(preferencesUpdateToken) {
        Color(
            context.preferences.getString(key, null)?.toColorInt()
                ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
                else DEFAULT_SELECTED_WIDGET_TEXT_COLOR,
        )
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
            color = animateColor(currentColor).value,
            size = widgetSize.dp,
            shape = MaterialTheme.shapes.large,
            outlineColor = MaterialTheme.colorScheme.outline,
            outlineWidth = 2.dp,
        )
    }
    if (showDialog) ColorPickerDialog(
        title = title,
        isBackgroundPick = isBackgroundPick,
        initialColor = currentColor,
        persistColor = { color ->
            val colorResult = if (isBackgroundPick) "#%08X".format(
                Locale.ENGLISH, 0xFFFFFFFF and color.toArgb().toLong(),
            ) else "#%06X".format(Locale.ENGLISH, 0xFFFFFF and color.toArgb())
            context.preferences.edit { putString(key, colorResult) }
        },
    ) { showDialog = false }
}

@Composable
fun SettingsSingleSelect(
    key: String,
    entries: ImmutableList<String>,
    entryValues: ImmutableList<String>,
    persistedValue: String,
    dialogTitleResId: Int,
    title: String,
    summaryResId: Int? = null,
) {
    val context = LocalContext.current
    SettingsClickable(
        title = title,
        summary = summaryResId?.let { stringResource(it) } ?: entries[
            entryValues.indexOf(
                persistedValue,
            ),
        ],
    ) { onDismissRequest ->
        AppDialog(
            title = { Text(stringResource(dialogTitleResId)) },
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
            },
            onDismissRequest = onDismissRequest,
        ) {
            Column(Modifier.selectableGroup()) {
                entries.zip(entryValues) { entry, entryValue ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SettingsItemHeight.dp)
                            .selectable(entryValue == persistedValue, role = Role.RadioButton) {
                                context.preferences.edit { putString(key, entryValue) }
                                onDismissRequest()
                            }
                            .padding(horizontal = SettingsHorizontalPaddingItem.dp),
                    ) {
                        RadioButton(selected = entryValue == persistedValue, onClick = null)
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
}

@Composable
fun SettingsMultiSelect(
    key: String,
    entries: ImmutableList<String>,
    entryValues: ImmutableList<String>,
    persistedSet: ImmutableSet<String>,
    dialogTitleResId: Int,
    title: String,
    summary: String? = null,
) {
    val context = LocalContext.current
    SettingsClickable(
        title = title,
        summary = summary ?: persistedSet.map(entryValues::indexOf).sorted()
            .joinToString(spacedComma) { entries[it] },
    ) { onDismissRequest ->
        val result = rememberSaveable {
            (context.preferences.getStringSet(key, null) ?: persistedSet).toList()
                .toMutableStateList()
        }
        AppDialog(
            title = { Text(stringResource(dialogTitleResId)) },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                        context.preferences.edit { putStringSet(key, result.toSet()) }
                    },
                ) { Text(stringResource(R.string.accept)) }
            },
        ) {
            entries.zip(entryValues) { entry, entryValue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SettingsItemHeight.dp)
                        .toggleable(value = entryValue in result, role = Role.Checkbox) {
                            if (it) result += entryValue else result -= entryValue
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

@SuppressLint("ComposableLambdaParameterNaming,ComposableLambdaParameterPosition")
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
    val hapticFeedback = LocalHapticFeedback.current
    SettingsLayout(
        title = title,
        summary = summary,
        extraWidget = extraWidget,
        modifier = Modifier.toggleable(value, role = Role.Switch) { newValue ->
            hapticFeedback.performLongPress()
            val finalValue = onBeforeToggle(newValue)
            if (value != finalValue) context.preferences.edit { putBoolean(key, finalValue) }
        },
    ) { Switch(checked = value, onCheckedChange = null, colors = appSwitchColors()) }
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
        },
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
        val endPadding =
            16 + (if (widget != null) widgetSize + 16f else 0f) + (if (extraWidget != null) widgetSize + 8f else 0f)
        Column(
            Modifier
                .align(alignment = Alignment.CenterStart)
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = endPadding.dp),
        ) {
            AnimatedContent(title, transitionSpec = appCrossfadeSpec) { state ->
                Text(state, style = MaterialTheme.typography.bodyLarge)
            }
            if (summary != null) {
                AnimatedContent(summary, transitionSpec = appCrossfadeSpec) { state ->
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
    visibleScale: Float = 100f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
) {
    Column(
        Modifier
            .padding(top = 16.dp, start = 24.dp, end = 24.dp)
            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
            .clearAndSetSemantics {},
    ) {
        AnimatedContent(title, transitionSpec = appCrossfadeSpec) { state ->
            Text(state, style = MaterialTheme.typography.bodyLarge)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = animateFloatAsState(value).value.coerceIn(valueRange),
                valueRange = valueRange,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                colors = appSliderColor(),
            )
            Spacer(Modifier.width(16.dp))
            val roundedValue = (value * visibleScale).roundToInt()
            Box(contentAlignment = Alignment.Center) {
                Text(
                    numeral.format(100),
                    Modifier.semantics { this.hideFromAccessibility() },
                    color = Color.Transparent,
                )
                Text(numeral.format(roundedValue))
            }
            val isDefault = roundedValue == (defaultValue * visibleScale).roundToInt()
            AnimatedVisibility(visible = isDefault) { Spacer(Modifier.width(16.dp)) }
            AnimatedVisibility(visible = !isDefault) {
                IconButton(onClick = { onValueChange(defaultValue) }) {
                    Icon(Icons.Default.SettingsBackupRestore, stringResource(R.string.cancel))
                }
            }
        }
    }
}
