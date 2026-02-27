package com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder

import android.animation.ValueAnimator
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.safePerformHapticFeedback
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

@Composable
fun CalendarPreferenceDialog(onDismissRequest: () -> Unit) {
    val view = LocalView.current
    val context = LocalContext.current
    val moveUp = stringResource(R.string.move_up)
    val moveDown = stringResource(R.string.move_down)
    val enabledCalendars = rememberSaveable { enabledCalendars.toMutableStateList() }
    val list = rememberSaveable {
        val orderedCalendars = enabledCalendars + (Calendar.entries - enabledCalendars.toSet()) -
                // Don't show Nepali on default locales, at least for now.
                if (language.showNepaliCalendar) emptySet() else setOf(Calendar.NEPALI)
        orderedCalendars.toMutableStateList()
    }

    AppDialog(
        title = { Text(stringResource(R.string.calendars_priority)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    val result = list.mapNotNull { if (it in enabledCalendars) it.name else null }
                    if (result.isEmpty()) {
                        val animator = ValueAnimator.ofFloat(0f, 1f)
                        animator.duration = 3000L
                        animator.interpolator = AccelerateDecelerateInterpolator()
                        animator.addUpdateListener { view.rotation = it.animatedFraction * 360f }
                        if (Random.nextBoolean()) animator.start() else animator.reverse()
                    } else context.preferences.edit {
                        putString(PREF_MAIN_CALENDAR_KEY, result.first())
                        putString(PREF_OTHER_CALENDARS_KEY, result.drop(1).joinToString(","))
                    }
                },
            ) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        var dragStarted by remember { mutableStateOf(false) }
        fun onSettle(fromIndex: Int, toIndex: Int) {
            list.add(toIndex, list.removeAt(fromIndex))
        }
        ReorderableColumn(
            modifier = Modifier.fillMaxSize(),
            list = list,
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
                                }.takeIf { i < list.size - 1 },
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
    }
}
