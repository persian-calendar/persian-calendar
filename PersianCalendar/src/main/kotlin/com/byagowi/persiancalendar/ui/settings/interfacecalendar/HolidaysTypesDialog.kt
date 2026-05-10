package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.highlightItem
import com.byagowi.persiancalendar.utils.preferences
import org.jetbrains.annotations.VisibleForTesting
import java.util.TimeZone

@Composable
fun HolidaysTypesDialog(
    modifier: Modifier = Modifier,
    destinationItem: String? = null,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val enabledTypes = rememberSaveable {
        EventsRepository.getEnabledTypes(context.preferences, language).toMutableStateList()
    }
    AppDialog(
        title = { Text(stringResource(R.string.events)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    context.preferences.edit {
                        putStringSet(PREF_HOLIDAY_TYPES, enabledTypes.toSet())
                    }
                },
            ) { Text(stringResource(R.string.accept)) }
        },
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
            LocalLayoutDirection provides if (language.isNepali) LayoutDirection.Ltr else LayoutDirection.Rtl,
        ) {
            if (!language.showNepaliCalendar) {
                @Composable
                fun Iran() {
                    CountryEvents(
                        sourceLink = EventSource.Iran.link,
                        holidaysTitle = "تعطیلات رسمی تنظیم‌شدهٔ مرکز تقویم دانشگاه تهران" + run {
                            if (!language.isAfghanistanExclusive && EventsRepository.IRAN_HOLIDAYS_KEY in enabledTypes) {
                                spacedComma + "پیش‌فرض برنامه"
                            } else ""
                        },
                        nonHolidaysTitle = "مناسبت‌های تقویم رسمی تنظیم‌شدهٔ دانشگاه تهران",
                        enabledTypes = enabledTypes,
                        holidaysKey = EventsRepository.IRAN_HOLIDAYS_KEY,
                        nonHolidaysKey = EventsRepository.IRAN_OTHERS_KEY,
                        destinationItem = destinationItem,
                        title = "مناسبت‌های رسمی تنظیم‌شدهٔ مرکز تقویم دانشگاه تهران",
                    )
                }

                @Composable
                fun Afghanistan() {
                    CountryEvents(
                        sourceLink = EventSource.Afghanistan.link,
                        holidaysTitle = "رخصتی‌های افغانستان",
                        nonHolidaysTitle = "سایر مناسبت‌های افغانستان",
                        enabledTypes = enabledTypes,
                        holidaysKey = EventsRepository.AFGHANISTAN_HOLIDAYS_KEY,
                        nonHolidaysKey = EventsRepository.AFGHANISTAN_OTHERS_KEY,
                        destinationItem = destinationItem,
                        title = "افغانستان",
                    )
                }

                @Composable
                fun International() {
                    ItemCheckBox(
                        AnnotatedString(stringResource(R.string.international)),
                        enabledTypes,
                        EventsRepository.INTERNATIONAL_KEY,
                        destinationItem = destinationItem,
                    )
                }

                if (language.isAfghanistanExclusive || TimeZone.getDefault().id == AFGHANISTAN_TIMEZONE_ID) {
                    Afghanistan()
                    International()
                    Iran()
                } else {
                    Iran()
                    Afghanistan()
                    Box(
                        Modifier.defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            stringResource(R.string.other_holidays),
                            modifier = Modifier
                                .padding(horizontal = SettingsHorizontalPaddingItem.dp)
                                .semantics { this.hideFromAccessibility() },
                        )
                    }
                    if (!language.isAfghanistanExclusive) ItemCheckBox(
                        AnnotatedString("مناسبت‌های ایران باستان"),
                        enabledTypes,
                        EventsRepository.IRAN_ANCIENT_KEY,
                        destinationItem = destinationItem,
                    )
                    International()
                }
            } else {
                CountryEvents(
                    sourceLink = EventSource.Nepal.link,
                    holidaysTitle = stringResource(R.string.holiday),
                    nonHolidaysTitle = stringResource(R.string.other_holidays),
                    enabledTypes = enabledTypes,
                    holidaysKey = EventsRepository.NEPAL_HOLIDAYS_KEY,
                    nonHolidaysKey = EventsRepository.NEPAL_OTHERS_KEY,
                    destinationItem = destinationItem,
                    title = "नेपाल",
                )
            }
        }
    }
}

@Preview
@Composable
internal fun HolidaysTypesDialogPreview() = HolidaysTypesDialog {}

@Composable
@VisibleForTesting
fun CountryEvents(
    sourceLink: String,
    holidaysTitle: String,
    nonHolidaysTitle: String,
    enabledTypes: SnapshotStateList<String>,
    holidaysKey: String,
    nonHolidaysKey: String,
    destinationItem: String?,
    title: String,
    modifier: Modifier = Modifier,
    // This is only not enabled in UI test, in real deployment the a11y service doesn't see the first row
    hideTheFirstRowFromAccessibility: Boolean = true,
) {
    if (!remember { Jdn.today() }.isYearSupportedOnAppAndNextYear) return
    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (hideTheFirstRowFromAccessibility) {
                        Modifier
                            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                            .clearAndSetSemantics {}
                    } else Modifier,
                )
                .clickable {
                    if (holidaysKey in enabledTypes && nonHolidaysKey in enabledTypes) {
                        enabledTypes -= holidaysKey
                        enabledTypes -= nonHolidaysKey
                    } else {
                        if (holidaysKey !in enabledTypes) enabledTypes += holidaysKey
                        if (nonHolidaysKey !in enabledTypes) enabledTypes += nonHolidaysKey
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
            Text(
                buildAnnotatedString {
                    append(title)
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
                },
            )
        }
        listOf(
            holidaysTitle to holidaysKey,
            nonHolidaysTitle to nonHolidaysKey,
        ).map { (title, key) ->
            AnnotatedString(title) to key
        }.forEach { (title, key) -> ItemCheckBox(title, enabledTypes, key, destinationItem) }
    }
}

@Composable
private fun ItemCheckBox(
    label: AnnotatedString,
    enabledTypes: SnapshotStateList<String>,
    key: String,
    destinationItem: String?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .highlightItem(destinationItem == key)
            .toggleable(value = key in enabledTypes, role = Role.Checkbox) {
                if (it) enabledTypes += key else enabledTypes -= key
            }
            .defaultMinSize(minHeight = HolidaysSettingsItemHeight.dp)
            .padding(
                start = SettingsHorizontalPaddingItem.dp,
                end = HolidaysHorizontalPaddingItem.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(HolidaysSettingsItemHeight.dp))
        Checkbox(key in enabledTypes, onCheckedChange = null)
        Spacer(Modifier.width(HolidaysHorizontalPaddingItem.dp))
        Crossfade(targetState = label) { Text(it) }
    }
}

private const val HolidaysSettingsItemHeight = 40
private const val HolidaysHorizontalPaddingItem = 16
