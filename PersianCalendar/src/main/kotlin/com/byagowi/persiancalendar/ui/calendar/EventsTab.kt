package com.byagowi.persiancalendar.ui.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.entities.everyYear
import com.byagowi.persiancalendar.generated.EventSource
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.YearHoroscopeDialog
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.common.equinoxTitle
import com.byagowi.persiancalendar.ui.icons.AstrologyIcon
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.noTransitionSpec
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getShiftWorksInDaysDistance
import com.byagowi.persiancalendar.utils.jalaliDayOfYear
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.EventsTab(
    navigateToHolidaysSettings: (String?) -> Unit,
    viewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    fabPlaceholderHeight: Dp,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = fabPlaceholderHeight.coerceAtLeast(0.dp)),
    ) {
        val jdn by viewModel.selectedDay.collectAsState()

        val refreshToken by viewModel.refreshToken.collectAsState()
        val shiftWorkTitle = remember(jdn, refreshToken) { getShiftWorkTitle(jdn) }
        this.AnimatedVisibility(visible = shiftWorkTitle != null) {
            AnimatedContent(
                targetState = shiftWorkTitle.orEmpty(),
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
        val shiftWorkInDaysDistance = getShiftWorksInDaysDistance(jdn)
        this.AnimatedVisibility(visible = shiftWorkInDaysDistance != null) {
            AnimatedContent(
                targetState = shiftWorkInDaysDistance.orEmpty(),
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

        Column(Modifier.padding(horizontal = 24.dp)) {
            val context = LocalContext.current
            val deviceEvents = remember(jdn, refreshToken) { context.readDayDeviceEvents(jdn) }
            val events = readEvents(jdn, viewModel, deviceEvents)
            Spacer(Modifier.height(16.dp))
            this.AnimatedVisibility(events.isEmpty()) {
                Text(
                    stringResource(R.string.no_event),
                    Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                Modifier.sharedBounds(
                    rememberSharedContentState(SHARED_CONTENT_KEY_EVENTS),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = appBoundsTransform,
                )
            ) {
                DayEvents(events, navigateToHolidaysSettings) {
                    viewModel.refreshCalendar()
                }
            }
        }

        val language by language.collectAsState()
        val context = LocalContext.current
        if (PREF_HOLIDAY_TYPES !in context.preferences && language.isIranExclusive) {
            Spacer(Modifier.height(16.dp))
            EncourageActionLayout(
                header = stringResource(R.string.warn_if_events_not_set),
                discardAction = {
                    context.preferences.edit {
                        putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
                    }
                },
                acceptAction = { navigateToHolidaysSettings(null) },
            )
        } else if (PREF_SHOW_DEVICE_CALENDAR_EVENTS !in context.preferences) {
            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) AskForCalendarPermissionDialog { showDialog = false }

            Spacer(Modifier.height(16.dp))
            EncourageActionLayout(
                header = stringResource(R.string.ask_for_calendar_permission),
                discardAction = {
                    context.preferences.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) }
                },
                acceptButton = stringResource(R.string.yes),
                acceptAction = { showDialog = true },
            )
        }
    }
}

@Composable
fun eventColor(event: CalendarEvent<*>): Color {
    return when {
        event is CalendarEvent.DeviceCalendarEvent -> runCatching {
            // should be turned to long then int otherwise gets stupid alpha
            if (event.color.isEmpty()) null else Color(event.color.toLong())
        }.onFailure(logException).getOrNull() ?: MaterialTheme.colorScheme.primary

        event.isHoliday || event is CalendarEvent.EquinoxCalendarEvent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

fun ManagedActivityResultLauncher<Long, Void?>.viewEvent(
    event: CalendarEvent.DeviceCalendarEvent, context: Context
) {
    runCatching { this@viewEvent.launch(event.id) }.onFailure {
        Toast.makeText(
            context, R.string.device_does_not_support, Toast.LENGTH_SHORT
        ).show()
    }.onFailure(logException)
}

fun eventTextColor(color: Int): Int = eventTextColor(Color(color)).toArgb()
fun eventTextColor(color: Color): Color = if (color.isLight) Color.Black else Color.White


private val String.directionality
    get() = this.firstNotNullOfOrNull {
        when (Character.getDirectionality(it)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT -> LayoutDirection.Rtl
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC -> LayoutDirection.Rtl
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING -> LayoutDirection.Rtl
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE -> LayoutDirection.Rtl

            Character.DIRECTIONALITY_LEFT_TO_RIGHT -> LayoutDirection.Ltr
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING -> LayoutDirection.Ltr
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE -> LayoutDirection.Ltr

            else -> null
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEvents(
    events: List<CalendarEvent<*>>,
    navigateToHolidaysSettings: ((String?) -> Unit)? = null,
    refreshCalendar: () -> Unit,
) {
    val language by language.collectAsState()
    val numeral by numeral.collectAsState()
    val launcher = rememberLauncherForActivityResult(ViewEventContract()) { refreshCalendar() }
    val coroutineScope = rememberCoroutineScope()
    events.forEach { event ->
        val backgroundColor by animateColor(eventColor(event))
        AnimatedContent(
            targetState = event.title.let { title ->
                (event as? CalendarEvent.DeviceCalendarEvent)?.time?.let { "$title\n$it" } ?: title
            },
            label = "event title",
            transitionSpec = {
                when (event) {
                    is CalendarEvent.EquinoxCalendarEvent -> noTransitionSpec
                    else -> appCrossfadeSpec
                }()
            },
        ) { title ->
            val titleDirection = title.directionality ?: LocalLayoutDirection.current
            val originalLayoutDirection = LocalLayoutDirection.current
            CompositionLocalProvider(LocalLayoutDirection provides titleDirection) {
                DayEventContent(
                    navigateToHolidaysSettings = navigateToHolidaysSettings,
                    backgroundColor = backgroundColor,
                    event = event,
                    title = title,
                    launcher = launcher,
                    language = language,
                    numeral = numeral,
                    coroutineScope = coroutineScope,
                    originalLayoutDirection = originalLayoutDirection,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DayEventContent(
    backgroundColor: Color,
    event: CalendarEvent<*>,
    title: String,
    language: Language,
    launcher: ManagedActivityResultLauncher<Long, Void?>,
    coroutineScope: CoroutineScope,
    navigateToHolidaysSettings: ((item: String?) -> Unit)?,
    numeral: Numeral,
    originalLayoutDirection: LayoutDirection,
) {
    val context = LocalContext.current
    val tooltipState = rememberTooltipState(isPersistent = true)
    val hasTooltip = when {
        language.isPersianOrDari && event is CalendarEvent.DeviceCalendarEvent -> true
        event.source == EventSource.Iran -> true
        event.source == EventSource.AncientIran -> true
        event.source == EventSource.International -> true
        event.source == EventSource.Afghanistan -> true
        else -> false
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable(onClickLabel = stringResource(R.string.view_source)) {
                if (event is CalendarEvent.DeviceCalendarEvent) {
                    launcher.viewEvent(event, context)
                } else if (hasTooltip) {
                    coroutineScope.launch {
                        if (tooltipState.isVisible) tooltipState.dismiss() else tooltipState.show()
                    }
                }
            }
            .focusable(true)
            .semantics {
                this.contentDescription = if (event.isHoliday) context.getString(
                    R.string.holiday_reason, event.title
                ) else event.oneLinerTitleWithTime
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val contentColor by animateColor(eventTextColor(backgroundColor))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            SelectionContainer(Modifier.semantics { this.hideFromAccessibility() }) {
                Text(
                    text = title,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.Content,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (event is CalendarEvent.EquinoxCalendarEvent) {
                EquinoxCountDown(contentColor, event, backgroundColor)
            }
        }
        this.AnimatedVisibility(
            when (event) {
                is CalendarEvent.DeviceCalendarEvent -> !language.isPersianOrDari
                is CalendarEvent.EquinoxCalendarEvent -> true
                else -> false
            }
        ) {
            Icon(
                if (event is CalendarEvent.EquinoxCalendarEvent) Icons.Default.Yard
                else Icons.AutoMirrored.Default.OpenInNew,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        this.AnimatedVisibility(hasTooltip) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = {
                    val text = when {
                        event is CalendarEvent.DeviceCalendarEvent -> "این رویداد شخصی از تقویم دستگاه می‌آید، تقویمی که پیش از این برنامه به‌صورت پیش‌فرض نصب بوده است"
                        event.source == EventSource.Afghanistan -> stringResource(R.string.afghanistan_events)
                        event.source == EventSource.International -> stringResource(R.string.international)
                        event.source == EventSource.AncientIran -> "این رویداد با تقویم جلالی تنظیم شده که طول ماه‌هایش با تقویم شمسی کنونی متفاوت است"
                        event.source == EventSource.Iran -> """تقویم رسمی کشور
تنظیم شورای مرکز تقویم مؤسسهٔ ژئوفیزیک دانشگاه تهران"""

                        else -> ""
                    }
                    RichTooltip(
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.close),
                            indication = null,
                            interactionSource = null,
                        ) { coroutineScope.launch { tooltipState.dismiss() } },
                        maxWidth = 240.dp,
                        tonalElevation = 12.dp,
                        action = if (event.source == EventSource.Iran || event is CalendarEvent.DeviceCalendarEvent) ({
                            CompositionLocalProvider(
                                LocalLayoutDirection provides originalLayoutDirection
                            ) {
                                Box(
                                    Modifier
                                        .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                                        .clearAndSetSemantics {}
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    // It won't use the theme level defined uri handler but it's ok
                                    // since it's a PDF
                                    val uriHandler = LocalUriHandler.current
                                    FilledTonalButton(onClick = {
                                        if (event.source == EventSource.Iran) {
                                            uriHandler.openUri(event.source.link)
                                        } else if (event is CalendarEvent.DeviceCalendarEvent) {
                                            launcher.viewEvent(event, context)
                                        }
                                    }) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.view_source))
                                            Icon(
                                                imageVector = if (event is CalendarEvent.DeviceCalendarEvent) {
                                                    Icons.AutoMirrored.Default.OpenInNew
                                                } else Icons.Default.OpenInBrowser,
                                                contentDescription = null,
                                                tint = LocalContentColor.current,
                                                modifier = Modifier.padding(start = 8.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }) else null,
                        title = {
                            CompositionLocalProvider(
                                LocalLayoutDirection provides if (event.source == EventSource.AncientIran) {
                                    LocalLayoutDirection.current
                                } else originalLayoutDirection
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        buildString {
                                            if (event is CalendarEvent.DeviceCalendarEvent) {
                                                append(stringResource(R.string.show_device_calendar_events))
                                            } else if (event.source == EventSource.AncientIran && event.date is PersianDate) {
                                                append(jalaliDayOfYear(event.date))
                                            } else {
                                                if (event.date.year == everyYear) append(
                                                    language.dm.format(
                                                        numeral.format(event.date.dayOfMonth),
                                                        event.date.monthName,
                                                    ) + spacedComma
                                                )
                                                append(stringResource(event.date.calendar.shortTitle))
                                            }
                                            if (event.isHoliday) append(spacedComma + holidayString)
                                        },
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .weight(1f),
                                    )
                                    if (navigateToHolidaysSettings != null) OutlineSettingsButton(
                                        modifier = Modifier.padding(
                                            top = 8.dp, start = 4.dp, bottom = 8.dp
                                        ),
                                    ) {
                                        coroutineScope.launch {
                                            tooltipState.dismiss()
                                            navigateToHolidaysSettings(
                                                EventsRepository.keyFromDetails(
                                                    event.source,
                                                    event.isHoliday,
                                                ) ?: PREF_SHOW_DEVICE_CALENDAR_EVENTS
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        caretShape = TooltipDefaults.caretShape(),
                    ) {
                        Text(
                            text,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                enableUserInput = false,
                state = tooltipState,
            ) {
                val chipTextColor = when {
                    event is CalendarEvent.DeviceCalendarEvent -> MaterialTheme.colorScheme.onSurface
                    event.isHoliday -> MaterialTheme.colorScheme.onPrimaryFixed
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }.copy(alpha = .65f)
                val chipBackgroundColor = when {
                    event is CalendarEvent.DeviceCalendarEvent -> MaterialTheme.colorScheme.surface
                    event.isHoliday -> MaterialTheme.colorScheme.primaryFixed
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }.copy(alpha = .65f)
                val parts = if (event is CalendarEvent.DeviceCalendarEvent) {
                    listOf("تقویم شخصی")
                } else when (event.source) {
                    EventSource.Iran -> listOf("دانشگاه تهران")
                    EventSource.Afghanistan -> listOf("افغانستان")
                    EventSource.International -> listOf("بین‌المللی")
                    EventSource.AncientIran -> listOf("ایران باستان", "جلالی")
                    EventSource.Nepal, null -> emptyList()
                }
                val isClickable = when {
                    event.source == EventSource.Iran -> true
                    event.source == EventSource.AncientIran -> true
                    event.source == EventSource.International -> true
                    event.source == EventSource.Afghanistan -> true
                    event is CalendarEvent.DeviceCalendarEvent && language.isPersianOrDari -> true
                    else -> false
                }
                val clickModifier = if (isClickable) Modifier.clickable(
                    onClickLabel = stringResource(R.string.view_source)
                ) {
                    coroutineScope.launch {
                        if (tooltipState.isVisible) tooltipState.dismiss() else tooltipState.show()
                    }
                } else Modifier
                Row(
                    Modifier
                        .padding(start = 8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .then(clickModifier),
                ) {
                    parts.forEachIndexed { i, part ->
                        Text(
                            text = part,
                            color = animateColor(chipTextColor).value,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(start = if (i != 0) 2.dp else 0.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .heightIn(min = 20.dp)
                                .background(animateColor(chipBackgroundColor).value)
                                .padding(
                                    start = if (i == 0) 8.dp else 4.dp,
                                    end = if (i == parts.size - 1) 8.dp else 4.dp,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineSettingsButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedIconButton(modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings),
        )
    }
}

private val countDownTimeParts = listOf(
    R.plurals.days to 1.days,
    R.plurals.hours to 1.hours,
    R.plurals.minutes to 1.minutes,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquinoxCountDown(
    contentColor: Color,
    event: CalendarEvent.EquinoxCalendarEvent,
    backgroundColor: Color,
) {
    val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
    val year = event.date.year + 1
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Ltr
        ) { EquinoxCountDownContent(contentColor, event, backgroundColor) }
        var showHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
        if (showHoroscopeDialog) YearHoroscopeDialog(year) {
            showHoroscopeDialog = false
        }
        if (isAstronomicalExtraFeaturesEnabled) TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
            ),
            tooltip = { PlainTooltip { Text(stringResource(R.string.horoscope)) } },
            state = rememberTooltipState()
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.clickable { showHoroscopeDialog = true },
                color = contentColor.copy(alpha = .9f),
                contentColor = backgroundColor,
            ) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(AstrologyIcon, stringResource(R.string.horoscope))
                }
            }
        }
    }
}

@Composable
private fun EquinoxCountDownContent(
    contentColor: Color,
    event: CalendarEvent.EquinoxCalendarEvent,
    backgroundColor: Color,
) {
    var remainedTime = event.remainingMillis.milliseconds
    if (remainedTime !in Duration.ZERO..356.days) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val isGradient by isGradient.collectAsState()
        val foldedCardBrush = if (isGradient) Brush.verticalGradient(
            .25f to contentColor,
            .499f to contentColor.copy(alpha = if (contentColor.isLight) .75f else .5f),
            .5f to contentColor,
        ) else Brush.verticalGradient(
            .49f to contentColor,
            .491f to Color.Transparent,
            .509f to Color.Transparent,
            .51f to contentColor,
        )
        val isTalkBackEnabled by isTalkBackEnabled.collectAsState()
        val numeral by numeral.collectAsState()
        countDownTimeParts.map { (pluralId, interval) ->
            val x = (remainedTime / interval).toInt()
            remainedTime -= interval * x
            x to pluralStringResource(pluralId, x, numeral.format(x))
        }.dropWhile { it.first == 0 }.forEach { (_, x) ->
            val parts = x.split(" ")
            if (parts.size == 2 && parts[0].length <= 3 && !isTalkBackEnabled) Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val digits = parts[0].padStart(2, numeral.format(0)[0])
                    digits.forEach {
                        Text(
                            "$it",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = backgroundColor,
                            modifier = Modifier
                                .background(
                                    foldedCardBrush,
                                    MaterialTheme.shapes.extraSmall,
                                )
                                .width(28.dp),
                        )
                    }
                }
                Text(
                    parts[1], color = contentColor, style = MaterialTheme.typography.bodyMedium
                )
            } else Text(x, color = contentColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun readEvents(
    jdn: Jdn,
    viewModel: CalendarViewModel,
    deviceEvents: DeviceCalendarEventsStore,
): List<CalendarEvent<*>> {
    val resources = LocalResources.current
    val eventsRepository by eventsRepository.collectAsState()
    val language by language.collectAsState()
    val events = sortEvents(eventsRepository.getEvents(jdn, deviceEvents), language)

    val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
    if (mainCalendar == Calendar.SHAMSI || isAstronomicalExtraFeaturesEnabled) {
        val date = jdn.toPersianDate()
        if (jdn + 1 == Jdn(PersianDate(date.year + 1, 1, 1))) {
            val now by viewModel.now.collectAsState()
            val (rawTitle, equinoxTime) = equinoxTitle(date, jdn, resources)
            val title = rawTitle.split(spacedColon).mapIndexed { i, x ->
                if (i == 0 && isAstronomicalExtraFeaturesEnabled) {
                    val yearString = stringResource(R.string.year)
                    val zodiac = ChineseZodiac.fromPersianCalendar(date)
                    val title = zodiac.format(resources, withEmoji = false, isPersian = true)
                    val symbol = zodiac.resolveEmoji(true)
                    language.inParentheses.format(x, "$yearString $title $symbol")
                } else x
            }.joinToString("\n")
            val remainedTime = equinoxTime - now
            val event = CalendarEvent.EquinoxCalendarEvent(title, false, date, null, remainedTime)
            return listOf(event) + events
        }
    }
    return events
}

fun sortEvents(events: List<CalendarEvent<*>>, language: Language): List<CalendarEvent<*>> {
    val isAfghanistan = language.isAfghanistanExclusive
    val noPriority = !isAfghanistan && !language.isIranExclusive
    return events.sortedBy {
        val priority = (isAfghanistan xor (it.source != EventSource.Afghanistan)) || noPriority
        when {
            it.isHoliday -> if (priority) 0L else 1L
            it !is CalendarEvent.DeviceCalendarEvent -> if (priority) 2L else 3L
            else -> it.start.timeInMillis
        }
    }
}

class ViewEventContract : ActivityResultContract<Long, Void?>() {
    override fun parseResult(resultCode: Int, intent: Intent?) = null
    override fun createIntent(context: Context, input: Long): Intent {
        return Intent(Intent.ACTION_VIEW).setData(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, input)
        )
    }
}
