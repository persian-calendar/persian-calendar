package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.common.CalendarsTypes
import com.byagowi.persiancalendar.ui.common.DayPicker
import com.byagowi.persiancalendar.ui.common.Dialog
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.showComposeDialog
import com.byagowi.persiancalendar.utils.calculateDaysDifference

fun showDayPickerDialog(
    activity: ComponentActivity,
    jdn: Jdn,
    @StringRes positiveButtonTitle: Int,
    onSuccess: (jdn: Jdn) -> Unit
) = showComposeDialog(activity) { DayPickerDialog(jdn, positiveButtonTitle, onSuccess, it) }

@Composable
fun DayPickerDialog(
    initialJdn: Jdn,
    @StringRes positiveButtonTitle: Int,
    onSuccess: (jdn: Jdn) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var jdn by remember { mutableStateOf(initialJdn) }
    val today = remember { Jdn.today() }
    var changeToken by remember { mutableIntStateOf(0) }
    Dialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onSuccess(jdn)
            }) { Text(stringResource(positiveButtonTitle)) }
        },
        neutralButton = {
            if (jdn != today) TextButton(onClick = {
                jdn = today
                ++changeToken
            }) { Text(stringResource(R.string.today)) }
        }
    ) {
        var calendarType by remember { mutableStateOf(mainCalendar) }
        val view = LocalView.current
        CalendarsTypes(current = calendarType) {
            view.performHapticFeedbackVirtualKey()
            calendarType = it
        }

        // Ugly code
        var previousCalendarType by remember { mutableStateOf(calendarType) }
        if (previousCalendarType != calendarType) ++changeToken
        previousCalendarType = calendarType
        DayPicker(calendarType, changeToken, jdn) { jdn = it }
        val longAnimationTime = integerResource(android.R.integer.config_longAnimTime)
        AnimatedContent(
            targetState = if (jdn == today) " " else listOf(
                stringResource(R.string.days_distance),
                spacedColon,
                calculateDaysDifference(
                    LocalContext.current.resources,
                    jdn,
                    calendarType = calendarType,
                )
            ).joinToString(""),
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = longAnimationTime)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = longAnimationTime)
                )
            },
            label = ""
        ) { state ->
            SelectionContainer {
                Text(
                    state,
                    modifier = Modifier.fillMaxSize(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
