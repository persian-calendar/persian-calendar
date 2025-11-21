package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.highlightItem
import com.byagowi.persiancalendar.utils.preferences
import org.jetbrains.annotations.VisibleForTesting

@Composable
fun HolidaysTypesDialog(destinationItem: String? = null, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val language by language.collectAsState()
    val enabledTypes = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { EventsRepository.getEnabledTypes(context.preferences, language).toMutableStateList() }
    AppDialog(
        title = { Text(stringResource(R.string.events)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                context.preferences.edit { putStringSet(PREF_HOLIDAY_TYPES, enabledTypes.toSet()) }
            }) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium
        ) {
            if (!language.showNepaliCalendar) {
                @Composable
                fun Iran() {
                    CountryEvents(
                        calendarCenterName = stringResource(R.string.iran_official_events),
                        sourceLink = EventSource.Iran.link,
                        holidaysTitle = stringResource(R.string.iran_holidays) + run {
                            if (!language.isAfghanistanExclusive && EventsRepository.iranHolidaysKey in enabledTypes) {
                                spacedComma + if (language.isArabicScript) "پیش‌فرض برنامه" else "app's default"
                            } else ""
                        },
                        nonHolidaysTitle = stringResource(R.string.iran_others),
                        enabledTypes = enabledTypes,
                        holidaysKey = EventsRepository.iranHolidaysKey,
                        nonHolidaysKey = EventsRepository.iranOthersKey,
                        destinationItem = destinationItem,
                    )
                }

                @Composable
                fun Afghanistan() {
                    CountryEvents(
                        calendarCenterName = stringResource(R.string.afghanistan_events),
                        sourceLink = EventSource.Afghanistan.link,
                        holidaysTitle = stringResource(R.string.afghanistan_holidays),
                        nonHolidaysTitle = stringResource(R.string.afghanistan_others),
                        enabledTypes = enabledTypes,
                        holidaysKey = EventsRepository.afghanistanHolidaysKey,
                        nonHolidaysKey = EventsRepository.afghanistanOthersKey,
                        destinationItem = destinationItem,
                    )
                }

                if (language.isAfghanistanExclusive) {
                    Afghanistan()
                    Iran()
                } else {
                    Iran()
                    Afghanistan()
                }
            } else {
                CountryEvents(
                    calendarCenterName = stringResource(R.string.nepal),
                    sourceLink = EventSource.Nepal.link,
                    holidaysTitle = stringResource(R.string.holiday),
                    nonHolidaysTitle = stringResource(R.string.other_holidays),
                    enabledTypes = enabledTypes,
                    holidaysKey = EventsRepository.nepalHolidaysKey,
                    nonHolidaysKey = EventsRepository.nepalOthersKey,
                    destinationItem = destinationItem,
                )
            }
            Box(
                Modifier.defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    stringResource(R.string.other_holidays),
                    modifier = Modifier
                        .padding(horizontal = SettingsHorizontalPaddingItem.dp)
                        .semantics { this.hideFromAccessibility() },
                )
            }
            if (!language.isAfghanistanExclusive) IndentedCheckBox(
                stringResource(R.string.iran_ancient),
                enabledTypes,
                EventsRepository.iranAncientKey,
                destinationItem = destinationItem,
            )
            IndentedCheckBox(
                stringResource(R.string.international),
                enabledTypes,
                EventsRepository.internationalKey,
                destinationItem = destinationItem,
            )
        }
    }
}

@Preview
@Composable
private fun HolidaysTypesDialogPreview() = HolidaysTypesDialog {}

@Composable
@VisibleForTesting
fun CountryEvents(
    calendarCenterName: String,
    sourceLink: String,
    holidaysTitle: String,
    nonHolidaysTitle: String,
    enabledTypes: MutableCollection<String>,
    holidaysKey: String,
    nonHolidaysKey: String,
    destinationItem: String?,
    hideFromAccessibility: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                if (hideFromAccessibility) {
                    Modifier
                        .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                        .clearAndSetSemantics {}
                } else Modifier,
            )
            .clickable {
                if (holidaysKey in enabledTypes && nonHolidaysKey in enabledTypes) {
                    enabledTypes.remove(holidaysKey)
                    enabledTypes.remove(nonHolidaysKey)
                } else {
                    if (holidaysKey !in enabledTypes) enabledTypes.add(holidaysKey)
                    if (nonHolidaysKey !in enabledTypes) enabledTypes.add(nonHolidaysKey)
                }
            }
            .defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TriStateCheckbox(
            state = when {
                holidaysKey in enabledTypes && nonHolidaysKey in enabledTypes -> ToggleableState.On

                holidaysKey in enabledTypes || nonHolidaysKey in enabledTypes -> ToggleableState.Indeterminate

                else -> ToggleableState.Off
            },
            onClick = null,
            modifier = Modifier.padding(start = SettingsHorizontalPaddingItem.dp),
        )
        Spacer(Modifier.width(HolidaysHorizontalPaddingItem.dp))
        Text(buildAnnotatedString {
            append(calendarCenterName)
            if (sourceLink.isNotEmpty()) {
                append(spacedComma)
                withLink(
                    link = LinkAnnotation.Url(
                        url = sourceLink,
                        styles = TextLinkStyles(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ),
                    ),
                ) { append(stringResource(R.string.view_source)) }
            }
        })
    }
    IndentedCheckBox(holidaysTitle, enabledTypes, holidaysKey, destinationItem)
    IndentedCheckBox(nonHolidaysTitle, enabledTypes, nonHolidaysKey, destinationItem)
}

@Composable
private fun IndentedCheckBox(
    label: String,
    enabledTypes: MutableCollection<String>,
    key: String,
    destinationItem: String?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .highlightItem(destinationItem == key)
            .toggleable(value = key in enabledTypes, role = Role.Checkbox) {
                if (it) enabledTypes.add(key) else enabledTypes.remove(key)
            }
            .defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp)
            .padding(
                start = (24/*checkbox size*/ + HolidaysHorizontalPaddingItem + SettingsHorizontalPaddingItem).dp,
                end = HolidaysHorizontalPaddingItem.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(key in enabledTypes, onCheckedChange = null)
        Spacer(Modifier.width(HolidaysHorizontalPaddingItem.dp))
        Crossfade(label) { Text(it) }
    }
}

private const val HolidaysSettingsItemHeight = 40
private const val HolidaysHorizontalPaddingItem = 16
