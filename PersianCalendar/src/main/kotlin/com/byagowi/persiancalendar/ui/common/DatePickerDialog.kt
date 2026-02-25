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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.enabledCalendarsWithDefaultInCompose
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import kotlin.math.abs

@Composable
fun DatePickerDialog(
    initialJdn: Jdn,
    onDismissRequest: () -> Unit,
    today: Jdn,
    onSuccess: (jdn: Jdn) -> Unit,
) {
    var jdn by rememberSaveable { mutableStateOf(initialJdn) }
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    AppDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row {
                val title = stringResource(R.string.accept)
                val anyPendingConfirm = pendingConfirms.isNotEmpty()
                AnimatedVisibility(anyPendingConfirm) {
                    AppIconButton(
                        Icons.Default.Done,
                        title,
                    ) { pendingConfirms.forEach { it() } }
                }
                AnimatedVisibility(!anyPendingConfirm) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                            onSuccess(jdn)
                        },
                    ) {
                        val description = stringResource(R.string.select_date)
                        Text(
                            title,
                            modifier = Modifier.semantics {
                                this.contentDescription = description
                            },
                        )
                    }
                }
            }
        },
        neutralButton = {
            AnimatedVisibility(visible = jdn != today, enter = fadeIn(), exit = fadeOut()) {
                TextButton(onClick = { jdn = today }) {
                    val description = stringResource(R.string.return_to_today)
                    Text(
                        stringResource(R.string.today),
                        modifier = Modifier.semantics { this.contentDescription = description },
                    )
                }
            }
        },
    ) {
        var calendar by rememberSaveable { mutableStateOf(mainCalendar) }
        CalendarPicker(
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
            items = enabledCalendarsWithDefaultInCompose(),
            backgroundColor = AlertDialogDefaults.containerColor,
            value = calendar,
            betterToUseShortCalendarName = true,
        ) { calendar = it }

        DatePicker(today, calendar, pendingConfirms, jdn) { jdn = it }
        var showNumberEdit by remember { mutableStateOf(false) }
        Crossfade(targetState = showNumberEdit) { isInNumberEdit ->
            if (isInNumberEdit) NumberEdit(
                dismissNumberEdit = { showNumberEdit = false },
                initialValue = jdn - today,
                isValid = { abs(it) < 100_000 },
                modifier = Modifier.fillMaxWidth(),
                pendingConfirms = pendingConfirms,
            ) { jdn = today + it } else AnimatedVisibility(jdn != today) {
                AnimatedContent(
                    targetState = listOf(
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
                            animationSpec = tween(500),
                        ) togetherWith slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(500),
                        )
                    },
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
                        SelectionContainer {
                            Text(
                                text = state,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
