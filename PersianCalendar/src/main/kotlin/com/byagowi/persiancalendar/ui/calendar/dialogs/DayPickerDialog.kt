package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.CalendarsTypesPicker
import com.byagowi.persiancalendar.ui.common.DayPicker
import com.byagowi.persiancalendar.ui.utils.stringResource
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import kotlinx.coroutines.delay

@Composable
fun DayPickerDialog(
    initialJdn: Jdn,
    @StringRes positiveButtonTitle: Int,
    onSuccess: (jdn: Jdn) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var jdn by rememberSaveable(
        saver = Saver(save = { it.value.value }, restore = { mutableStateOf(Jdn(it)) })
    ) { mutableStateOf(initialJdn) }
    var today by remember { mutableStateOf(Jdn.today()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(THIRTY_SECONDS_IN_MILLIS)
            today = Jdn.today()
        }
    }
    AppDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onSuccess(jdn)
            }) { Text(stringResource(positiveButtonTitle)) }
        },
        neutralButton = {
            AnimatedVisibility(visible = jdn != today, enter = fadeIn(), exit = fadeOut()) {
                TextButton(onClick = { jdn = today }) { Text(stringResource(R.string.today)) }
            }
        },
    ) {
        var calendarType by rememberSaveable { mutableStateOf(mainCalendar) }
        CalendarsTypesPicker(current = calendarType) { calendarType = it }

        DayPicker(calendarType, jdn) { jdn = it }
        AnimatedContent(
            targetState = if (jdn == today) null else listOf(
                stringResource(R.string.days_distance), spacedColon,
                calculateDaysDifference(
                    LocalContext.current.resources,
                    jdn,
                    today,
                    calendarType = calendarType,
                ),
            ).joinToString(""),
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )
            },
            label = "days distance",
        ) { state ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (state != null) SelectionContainer {
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
}
