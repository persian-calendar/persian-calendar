package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.LocalLanguage
import com.byagowi.persiancalendar.generated.EventType
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.stringResource
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import org.jetbrains.annotations.VisibleForTesting

@Composable
fun HolidaysTypesDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val language = LocalLanguage.current
    val enabledTypes = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { EventsRepository.getEnabledTypes(context.appPrefs, language).toMutableStateList() }
    AppDialog(title = { Text(stringResource(R.string.events)) }, dismissButton = {
        TextButton(onClick = onDismissRequest) {
            Text(stringResource(R.string.cancel))
        }
    }, confirmButton = {
        TextButton(onClick = {
            onDismissRequest()
            context.appPrefs.edit { putStringSet(PREF_HOLIDAY_TYPES, enabledTypes.toSet()) }
        }) { Text(stringResource(R.string.accept)) }
    }, onDismissRequest = onDismissRequest
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium
        ) {
            if (!language.showNepaliCalendar) {
                @Composable
                fun Iran() {
                    CountryEvents(
                        stringResource(R.string.iran_official_events),
                        EventType.Iran.source,
                        stringResource(R.string.iran_holidays),
                        stringResource(R.string.iran_others),
                        enabledTypes,
                        EventsRepository.iranHolidaysKey,
                        EventsRepository.iranOthersKey,
                    )
                }

                @Composable
                fun Afghanistan() {
                    CountryEvents(
                        stringResource(R.string.afghanistan_events),
                        EventType.Afghanistan.source,
                        stringResource(R.string.afghanistan_holidays),
                        stringResource(R.string.afghanistan_others),
                        enabledTypes,
                        EventsRepository.afghanistanHolidaysKey,
                        EventsRepository.afghanistanOthersKey,
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
                    stringResource(R.string.nepal),
                    EventType.Nepal.source,
                    stringResource(R.string.holiday),
                    stringResource(R.string.other_holidays),
                    enabledTypes,
                    EventsRepository.nepalHolidaysKey,
                    EventsRepository.nepalOthersKey,
                )
            }
            Box(
                Modifier.defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    stringResource(R.string.other_holidays),
                    modifier = Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp),
                )
            }
            if (!language.isAfghanistanExclusive) IndentedCheckBox(
                stringResource(R.string.iran_ancient),
                enabledTypes,
                EventsRepository.iranAncientKey,
            )
            IndentedCheckBox(
                stringResource(R.string.international),
                enabledTypes,
                EventsRepository.internationalKey,
            )
        }
    }
}

@Preview
@Composable
private fun HolidaysTypesDialogPreview() = HolidaysTypesDialog {}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@VisibleForTesting
fun CountryEvents(
    calendarCenterName: String,
    sourceLink: String,
    holidaysTitle: String,
    nonHolidaysTitle: String,
    enabledTypes: SnapshotStateList<String>,
    holidaysKey: String,
    nonHolidaysKey: String,
) {
    Row(
        Modifier
            .fillMaxWidth()
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
            modifier = Modifier
                .padding(start = SettingsHorizontalPaddingItem.dp)
                .align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.width(HolidaysHorizontalPaddingItem.dp))
        FlowRow(verticalArrangement = Arrangement.Center) {
            Text(calendarCenterName, modifier = Modifier.align(Alignment.CenterVertically))
            val context = LocalContext.current
            if (sourceLink.isNotEmpty()) {
                Text(spacedComma)
                ClickableText(
                    AnnotatedString(stringResource(R.string.view_source)),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    runCatching {
                        CustomTabsIntent.Builder().build().launchUrl(context, sourceLink.toUri())
                    }.onFailure(logException)
                }
            }
        }
    }
    IndentedCheckBox(holidaysTitle, enabledTypes, holidaysKey)
    IndentedCheckBox(nonHolidaysTitle, enabledTypes, nonHolidaysKey)
}

@Composable
private fun IndentedCheckBox(
    label: String,
    enabledTypes: SnapshotStateList<String>,
    key: String,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                if (key in enabledTypes) enabledTypes.remove(key) else enabledTypes.add(key)
            }
            .defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp)
            .padding(
                start = (24/*checkbox size*/ + HolidaysHorizontalPaddingItem + SettingsHorizontalPaddingItem).dp,
                end = HolidaysHorizontalPaddingItem.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(key in enabledTypes, onCheckedChange = null)
        Spacer(modifier = Modifier.width(HolidaysHorizontalPaddingItem.dp))
        Text(label)
    }
}

private const val HolidaysSettingsItemHeight = 40
private const val HolidaysHorizontalPaddingItem = 16
