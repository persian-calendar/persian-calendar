package com.byagowi.persiancalendar.ui.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getShiftWorksInDaysDistance
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.readDayDeviceEvents

@Composable
fun EventsTab(
    navigateToHolidaysSettings: () -> Unit,
    viewModel: CalendarViewModel,
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        val context = LocalContext.current

        val jdn by viewModel.selectedDay.collectAsState()
        val refreshToken by viewModel.refreshToken.collectAsState()
        val shiftWorkTitle = remember(jdn, refreshToken) { getShiftWorkTitle(jdn) }
        AnimatedVisibility(visible = shiftWorkTitle != null) {
            AnimatedContent(
                targetState = shiftWorkTitle ?: "",
                label = "shift work title",
                transitionSpec = appCrossfadeSpec,
            ) { state ->
                SelectionContainer {
                    Text(
                        state,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    )
                }
            }
        }
        val shiftWorkInDaysDistance = remember(jdn) {
            getShiftWorksInDaysDistance(jdn, context.resources)
        }
        AnimatedVisibility(visible = shiftWorkInDaysDistance != null) {
            AnimatedContent(
                targetState = shiftWorkInDaysDistance ?: "",
                label = "shift work days diff",
                transitionSpec = appCrossfadeSpec,
            ) { state ->
                SelectionContainer {
                    Text(
                        state,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        val events = remember(jdn, refreshToken) {
            (eventsRepository?.getEvents(jdn, context.readDayDeviceEvents(jdn))
                ?: emptyList()).sortedBy {
                when {
                    it.isHoliday -> 0L
                    it !is CalendarEvent.DeviceCalendarEvent -> 1L
                    else -> it.start.time
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(events.isEmpty()) {
            Text(
                stringResource(R.string.no_event),
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
            )
        }

        val launcher =
            rememberLauncherForActivityResult(ViewEvent()) { viewModel.refreshCalendar() }

        events.forEach { event ->
            val backgroundColor by animateColorAsState(
                when {
                    event is CalendarEvent.DeviceCalendarEvent -> {
                        runCatching {
                            // should be turned to long then int otherwise gets stupid alpha
                            if (event.color.isEmpty()) null else Color(event.color.toLong().toInt())
                        }.onFailure(logException).getOrNull() ?: MaterialTheme.colorScheme.primary
                    }

                    event.isHoliday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "backgroundColor",
            )

            val eventTime =
                (event as? CalendarEvent.DeviceCalendarEvent)?.time?.let { "\n" + it } ?: ""
            AnimatedContent(
                (if (event.isHoliday) language.value.inParentheses.format(
                    event.title, holidayString
                ) else event.title) + eventTime,
                label = "event title",
                transitionSpec = appCrossfadeSpec,
            ) { title ->
                Row(
                    @OptIn(ExperimentalFoundationApi::class) Modifier
                        .fillMaxWidth()
                        // TODO: Match it with a better number with page's fab
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(backgroundColor)
                        .combinedClickable(
                            enabled = event is CalendarEvent.DeviceCalendarEvent,
                            onClick = {
                                if (event is CalendarEvent.DeviceCalendarEvent) {
                                    runCatching { launcher.launch(event.id) }
                                        .onFailure {
                                            Toast
                                                .makeText(
                                                    context,
                                                    R.string.device_does_not_support,
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                        .onFailure(logException)
                                }
                            },
                        )
                        .padding(8.dp)
                        .semantics {
                            this.contentDescription = if (event.isHoliday) context.getString(
                                R.string.holiday_reason, event.oneLinerTitleWithTime
                            ) else event.oneLinerTitleWithTime
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val contentColor by animateColorAsState(
                        if (backgroundColor.isLight) Color.Black else Color.White,
                        label = "contentColor"
                    )

                    SelectionContainer {
                        Text(
                            title,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    AnimatedVisibility(event is CalendarEvent.DeviceCalendarEvent) {
                        Icon(
                            Icons.AutoMirrored.Default.OpenInNew,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }

        if (PREF_HOLIDAY_TYPES !in context.appPrefs && language.value.isIranExclusive) {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsPromotionButtons(
                header = stringResource(R.string.warn_if_events_not_set),
                discardAction = {
                    context.appPrefs.edit {
                        putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
                    }
                },
                acceptAction = navigateToHolidaysSettings,
            )
        } else if (PREF_SHOW_DEVICE_CALENDAR_EVENTS !in context.appPrefs) {
            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) AskForCalendarPermissionDialog { showDialog = false }

            Spacer(modifier = Modifier.height(16.dp))
            SettingsPromotionButtons(
                header = stringResource(R.string.ask_for_calendar_permission),
                discardAction = {
                    context.appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) }
                },
                acceptButton = stringResource(R.string.yes),
                acceptAction = { showDialog = true },
            )
        }

        // Events addition fab placeholder, so events can be scrolled after it
        Spacer(modifier = Modifier.height(76.dp))
    }
}

private class ViewEvent : ActivityResultContract<Long, Void?>() {
    override fun parseResult(resultCode: Int, intent: Intent?) = null
    override fun createIntent(context: Context, input: Long): Intent {
        return Intent(Intent.ACTION_VIEW).setData(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, input)
        )
    }
}
