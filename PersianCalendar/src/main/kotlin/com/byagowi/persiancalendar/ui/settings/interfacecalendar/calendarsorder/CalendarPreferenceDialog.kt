package com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder

import android.animation.ValueAnimator
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.appPrefs
import kotlin.random.Random

@Composable
fun CalendarPreferenceDialog(onDismissRequest: () -> Unit) {
    val view = LocalView.current
    val context = LocalContext.current
    // TODO: Make it remember during rotation by using rememberSavable
    var list by remember {
        val enabledCalendarTypes = enabledCalendars
        val orderedCalendarTypes =
            enabledCalendars + (CalendarType.entries - enabledCalendars.toSet()) -
                    // Don't show Nepali on default locales, at least for now.
                    if (language.showNepaliCalendar) emptySet() else setOf(CalendarType.NEPALI)
        mutableStateOf(orderedCalendarTypes.map { it to (it in enabledCalendarTypes) })
    }

    Dialog(
        title = { Text(stringResource(R.string.calendars_priority)) },
        negativeButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        positiveButton = {
            TextButton(onClick = {
                onDismissRequest()
                val result = list.mapNotNull { if (it.second) it.first.name else null }
                if (result.isEmpty()) {
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = 3000L
                    animator.interpolator = AccelerateDecelerateInterpolator()
                    animator.addUpdateListener { view.rotation = it.animatedFraction * 360f }
                    if (Random.nextBoolean()) animator.start() else animator.reverse()
                } else context.appPrefs.edit {
                    putString(PREF_MAIN_CALENDAR_KEY, result.first())
                    putString(PREF_OTHER_CALENDARS_KEY, result.drop(1).joinToString(","))
                }
            }) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        var dragStarted by remember { mutableStateOf(false) }
        ReorderableColumn(
            modifier = Modifier.fillMaxSize(),
            list = list,
            onSettle = { fromIndex, toIndex ->
                list = list.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
            },
            onMove = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
                }
            },
        ) { _, (calendarType, enabled), isDragging ->
            key(calendarType) {
                val blur by animateDpAsState(if (dragStarted) 2.dp else 0.dp, label = "blur")
                fun onClick() {
                    list = list.map {
                        it.first to (if (it.first == calendarType) !it.second else it.second)
                    }
                }

                val interactionSource = remember(calendarType) { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .blur(if (dragStarted && !isDragging) blur else 0.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(),
                            onClick = ::onClick,
                        )
                        .draggableHandle(
                            interactionSource = interactionSource,
                            onDragStarted = {
                                dragStarted = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                                } else {
                                    view.performHapticFeedbackVirtualKey()
                                }
                            },
                            onDragStopped = {
                                dragStarted = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                }
                            },
                        )
                        .height(48.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(12.dp))
                    Checkbox(checked = enabled, onCheckedChange = { onClick() })
                    Text(stringResource(calendarType.title), Modifier.padding(horizontal = 8.dp))
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
                    Spacer(Modifier.width(20.dp))
                }
            }
        }
    }
}
