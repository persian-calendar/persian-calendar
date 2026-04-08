package com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_CALENDARS_PRIORITY_OPENED_ONCE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.PREF_SHOW_HISTORICAL_CALENDARS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.secondaryCalendarEnabled
import com.byagowi.persiancalendar.global.showHistoricalCalendars
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.safePerformHapticFeedback
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.launch

@Composable
fun CalendarPreferenceDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val moveUp = stringResource(R.string.move_up)
    val moveDown = stringResource(R.string.move_down)
    val enabledCalendars = rememberSaveable { enabledCalendars.toMutableStateList() }
    val calendars = rememberSaveable {
        val orderedCalendars = enabledCalendars + (Calendar.entries - enabledCalendars.toSet()) -
                // Don't show Nepali on default locales, at least for now.
                if (language.showNepaliCalendar) emptySet() else setOf(Calendar.NEPALI)
        orderedCalendars.toMutableStateList()
    }

    LaunchedEffect(Unit) {
        context.preferences.edit { putBoolean(PREF_CALENDARS_PRIORITY_OPENED_ONCE, true) }
    }
    val coroutineScope = rememberCoroutineScope()
    var showMore by rememberSaveable { mutableStateOf(false) }
    var isInRotation by rememberSaveable { mutableStateOf(false) }
    if (isInRotation) return
    AppDialog(
        title = { Text(stringResource(R.string.calendars_priority)) },
        neutralButton = {
            AnimatedVisibility(!showMore) {
                TextButton(onClick = { showMore = true }) { Text(stringResource(R.string.more)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result =
                        calendars.mapNotNull { if (it in enabledCalendars) it.name else null }
                    if (result.isEmpty()) coroutineScope.launch {
                        isInRotation = true
                        val (from, to) = listOf(0f to 360f, 360f to 0f).random()
                        animate(
                            initialValue = from,
                            targetValue = to,
                            animationSpec = tween(3_000),
                        ) { value, _ -> view.rotation = value }
                        view.rotation = 0f
                        onDismissRequest()
                    } else context.preferences.edit {
                        onDismissRequest()
                        putString(PREF_MAIN_CALENDAR_KEY, result.first())
                        putString(PREF_OTHER_CALENDARS_KEY, result.drop(1).joinToString(","))
                    }
                },
            ) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        var dragStarted by remember { mutableStateOf(false) }
        fun onSettle(fromIndex: Int, toIndex: Int): Unit =
            calendars.add(toIndex, element = calendars.removeAt(fromIndex))
        ReorderableColumn(
            modifier = Modifier.fillMaxSize(),
            items = calendars,
            onSettle = ::onSettle,
            onMove = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    view.safePerformHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
                }
            },
        ) { i, calendar, isDragging ->
            key(calendar) {
                val blur by animateDpAsState(targetValue = if (dragStarted) 2.dp else 0.dp)
                val interactionSource = remember(key1 = calendar) { MutableInteractionSource() }
                val checked = calendar in enabledCalendars
                Row(
                    modifier = Modifier
                        .blur(if (dragStarted && !isDragging) blur else 0.dp)
                        .toggleable(
                            value = checked,
                            interactionSource = interactionSource,
                            indication = ripple(),
                            role = Role.Checkbox,
                        ) { if (it) enabledCalendars += calendar else enabledCalendars -= calendar }
                        .semantics {
                            customActions = listOfNotNull(
                                CustomAccessibilityAction(moveUp) {
                                    onSettle(i, i - 1)
                                    true
                                }.takeIf { i > 0 },
                                CustomAccessibilityAction(moveDown) {
                                    onSettle(i, i + 1)
                                    true
                                }.takeIf { i < calendars.size - 1 },
                            )
                        }
                        .draggableHandle(
                            interactionSource = interactionSource,
                            onDragStarted = {
                                dragStarted = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    view.safePerformHapticFeedback(HapticFeedbackConstants.DRAG_START)
                                } else {
                                    view.performHapticFeedbackVirtualKey()
                                }
                            },
                            onDragStopped = {
                                dragStarted = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    view.safePerformHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                }
                            },
                        )
                        .height(SettingsItemHeight.dp)
                        .padding(horizontal = SettingsHorizontalPaddingItem.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Spacer(Modifier.width(SettingsHorizontalPaddingItem.dp))
                    Text(stringResource(calendar.title))
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.DragHandle, contentDescription = null)
                }
            }
        }
        AnimatedVisibility(showMore) { HorizontalDivider() }
        AnimatedVisibility(
            visible = showMore && enabledCalendars.size > 1,
            modifier = Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp),
        ) {
            val context = LocalContext.current
            SwitchWithLabel(
                label = stringResource(R.string.show_secondary_calendar) + (secondaryCalendar?.let {
                    spacedColon + stringResource(it.title)
                } ?: ""),
                checked = secondaryCalendarEnabled,
            ) { context.preferences.edit { putBoolean(PREF_SECONDARY_CALENDAR_IN_TABLE, it) } }
        }
        AnimatedVisibility(
            visible = showMore && language.isPersian,
            modifier = Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp),
        ) {
            val context = LocalContext.current
            SwitchWithLabel(
                label = "نمایش تقویم‌های تاریخی",
                checked = showHistoricalCalendars,
            ) { context.preferences.edit { putBoolean(PREF_SHOW_HISTORICAL_CALENDARS, it) } }
        }
    }
}
