package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.JdnSaver
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@Composable
fun DatePickerDialog(
    initialJdn: Jdn,
    onDismissRequest: () -> Unit,
    onSuccess: (jdn: Jdn) -> Unit,
) {
    var jdn by rememberSaveable(saver = JdnSaver) { mutableStateOf(initialJdn) }
    var today by remember { mutableStateOf(Jdn.today()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            today = Jdn.today()
        }
    }
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    AppDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row {
                val title = stringResource(R.string.accept)
                val anyPendingAccept = pendingConfirms.isNotEmpty()
                AnimatedVisibility(anyPendingAccept) {
                    AppIconButton(Icons.Default.Done, title) { pendingConfirms.forEach { it() } }
                }
                AnimatedVisibility(!anyPendingAccept) {
                    TextButton(onClick = {
                        onDismissRequest()
                        onSuccess(jdn)
                    }) { Text(title) }
                }
            }
        },
        neutralButton = {
            AnimatedVisibility(visible = jdn != today, enter = fadeIn(), exit = fadeOut()) {
                TextButton(onClick = { jdn = today }) { Text(stringResource(R.string.today)) }
            }
        },
    ) {
        var calendar by rememberSaveable { mutableStateOf(mainCalendar) }
        CalendarsTypesPicker(current = calendar) { calendar = it }

        CompositionLocalProvider(LocalPendingConfirms provides pendingConfirms) {
            DatePicker(calendar, jdn) { jdn = it }
        }
        var showNumberEdit by remember { mutableStateOf(false) }
        Crossfade(showNumberEdit, label = "edit toggle") { isInNumberEdit ->
            if (isInNumberEdit) CompositionLocalProvider(
                LocalPendingConfirms provides pendingConfirms
            ) {
                NumberEdit(
                    dismissNumberEdit = { showNumberEdit = false },
                    initialValue = jdn - today,
                    setValue = { if (it != null && abs(it) < 100_000) jdn = today + it },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else AnimatedContent(
                targetState = if (jdn == today) null else listOf(
                    stringResource(R.string.days_distance), spacedColon,
                    calculateDaysDifference(
                        LocalResources.current,
                        jdn,
                        today,
                        calendar = calendar,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = null,
                            onClickLabel = stringResource(R.string.days_distance),
                        ) { showNumberEdit = true },
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
}
