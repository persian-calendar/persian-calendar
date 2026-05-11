package com.byagowi.persiancalendar.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.R
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
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.YearHoroscopeDialog
import com.byagowi.persiancalendar.ui.icons.AstrologyIcon
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.noTransitionSpec
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.jalaliDayOfYear
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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

@Composable
fun DayEvents(
    events: ImmutableList<CalendarEvent<*>>,
    navigateToHolidaysSettings: ((String?) -> Unit),
    viewEvent: (CalendarEvent.DeviceCalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        events.forEach { event ->
            val backgroundColor by animateColor(eventColor(event))
            AnimatedContent(
                targetState = event.title.let { title ->
                    (event as? CalendarEvent.DeviceCalendarEvent)?.time?.let { "$title\n$it" }
                        ?: title
                },
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
                        viewEvent = viewEvent,
                        language = language,
                        numeral = numeral,
                        coroutineScope = coroutineScope,
                        originalLayoutDirection = originalLayoutDirection,
                    )
                }
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
    viewEvent: (CalendarEvent.DeviceCalendarEvent) -> Unit,
    coroutineScope: CoroutineScope,
    navigateToHolidaysSettings: ((item: String?) -> Unit),
    numeral: Numeral,
    originalLayoutDirection: LayoutDirection,
) {
    val resources = LocalResources.current
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
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable(onClickLabel = stringResource(R.string.view_source)) {
                if (event is CalendarEvent.DeviceCalendarEvent) {
                    viewEvent(event)
                } else if (hasTooltip) coroutineScope.launch {
                    if (tooltipState.isVisible) tooltipState.dismiss() else tooltipState.show()
                }
            }
            .focusable(true)
            .semantics {
                this.contentDescription = if (event.isHoliday) resources.getString(
                    R.string.holiday_reason, event.title,
                ) else event.oneLinerTitleWithTime
            }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val contentColor by animateColor(eventTextColor(backgroundColor))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            SelectionContainer(Modifier.semantics { this.hideFromAccessibility() }) {
                Text(
                    text = title,
                    maxLines = if (event.source == EventSource.Iran) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
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
        AnimatedVisibility(
            when (event) {
                is CalendarEvent.DeviceCalendarEvent -> !language.isPersianOrDari
                is CalendarEvent.EquinoxCalendarEvent -> true
                else -> false
            },
        ) {
            Icon(
                if (event is CalendarEvent.EquinoxCalendarEvent) Icons.Default.Yard
                else Icons.AutoMirrored.Default.OpenInNew,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        AnimatedVisibility(hasTooltip) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above,
                ),
                tooltip = {
                    val text = when {
                        event is CalendarEvent.DeviceCalendarEvent -> "این رویداد شخصی از تقویم دستگاه می‌آید، تقویمی که پیش از این برنامه به‌صورت پیش‌فرض نصب بوده است"
                        event.source == EventSource.Afghanistan -> stringResource(R.string.afghanistan_events)
                        event.source == EventSource.International -> stringResource(R.string.international)
                        event.source == EventSource.AncientIran -> "این رویداد با تقویم جلالی تنظیم شده که طول ماه‌هایش با تقویم شمسی کنونی متفاوت است"
                        event.source == EventSource.Iran -> event.title + """ از تقویم رسمی
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
                                LocalLayoutDirection provides originalLayoutDirection,
                            ) {
                                Box(
                                    Modifier
                                        .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                                        .clearAndSetSemantics {}
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    // It won't use the theme level defined uri handler but, it's ok
                                    // since it's a PDF
                                    val uriHandler = LocalUriHandler.current
                                    FilledTonalButton(
                                        onClick = {
                                            if (event.source == EventSource.Iran) {
                                                runCatching {
                                                    uriHandler.openUri(event.source.link)
                                                }.onFailure(logException)
                                            } else if (event is CalendarEvent.DeviceCalendarEvent) {
                                                viewEvent(event)
                                            }
                                        },
                                    ) {
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
                                } else originalLayoutDirection,
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
                                                    ) + spacedComma,
                                                )
                                                append(stringResource(event.date.calendar.shortTitle))
                                            }
                                            if (event.isHoliday) append(spacedComma + holidayString)
                                        },
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .weight(1f),
                                    )
                                    OutlineSettingsButton(
                                        modifier = Modifier.padding(
                                            top = 8.dp, start = 4.dp, bottom = 8.dp,
                                        ),
                                    ) {
                                        coroutineScope.launch {
                                            tooltipState.dismiss()
                                            navigateToHolidaysSettings(
                                                EventsRepository.keyFromDetails(
                                                    event.source,
                                                    event.isHoliday,
                                                ) ?: PREF_SHOW_DEVICE_CALENDAR_EVENTS,
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
                    onClickLabel = stringResource(R.string.view_source),
                ) {
                    coroutineScope.launch {
                        if (tooltipState.isVisible) tooltipState.dismiss() else tooltipState.show()
                    }
                } else Modifier
                Row(
                    Modifier
                        .padding(start = 8.dp)
                        .clip(MaterialTheme.shapes.small)
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

@Composable
private fun EquinoxCountDown(
    contentColor: Color,
    event: CalendarEvent.EquinoxCalendarEvent,
    backgroundColor: Color,
) {
    val year = event.date.year + 1
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Ltr,
        ) { EquinoxCountDownContent(contentColor, event, backgroundColor) }
        var showHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
        if (showHoroscopeDialog) YearHoroscopeDialog(year) {
            showHoroscopeDialog = false
        }
        if (isAstronomicalExtraFeaturesEnabled) @OptIn(ExperimentalMaterial3Api::class) TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above,
            ),
            tooltip = { PlainTooltip { Text(stringResource(R.string.horoscope)) } },
            state = rememberTooltipState(),
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
        countDownTimeParts.map { (pluralId, interval) ->
            val x = (remainedTime / interval).toInt()
            remainedTime -= interval * x
            x to pluralStringResource(pluralId, x, numeral.format(x))
        }.dropWhile { it.first == 0 }.forEach { (_, x) ->
            val parts = x.split(" ")
            if (parts.size == 2 && parts[0].length <= 3 && !isTalkBackEnabled) Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    parts[1], color = contentColor, style = MaterialTheme.typography.bodyMedium,
                )
            } else Text(x, color = contentColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun readEvents(
    jdn: Jdn,
    deviceEvents: DeviceCalendarEventsStore,
): ImmutableList<CalendarEvent<*>> =
    sortEvents(eventsRepository.getEvents(jdn, deviceEvents), language).toImmutableList()

@Composable
fun readEventsWithEquinox(
    jdn: Jdn,
    now: Long,
    deviceEvents: DeviceCalendarEventsStore,
): ImmutableList<CalendarEvent<*>> {
    val events = sortEvents(eventsRepository.getEvents(jdn, deviceEvents), language)
    return (if (mainCalendar == Calendar.SHAMSI || isAstronomicalExtraFeaturesEnabled) {
        val resources = LocalResources.current
        val date = jdn.toPersianDate()
        val nextPersianYearDate = PersianDate(date.year + 1, 1, 1)
        val nextYearJdn = Jdn(nextPersianYearDate)
        if ((jdn - nextYearJdn) in -1..<0) {
            val gregorianYear = (nextYearJdn - 1).toCivilDate().year
            val equinoxTime = seasons(gregorianYear).marchEquinox.toMillisecondsSince1970()
            val title = resources.getString(
                R.string.spring_equinox,
                numeral.format(
                    when (mainCalendar) {
                        Calendar.SHAMSI -> date.year + if (date.month == 12) 1 else 0
                        else -> gregorianYear
                    },
                ),
            ).let {
                if (isAstronomicalExtraFeaturesEnabled) {
                    val yearString = stringResource(R.string.year)
                    val zodiac = ChineseZodiac.fromPersianCalendar(nextPersianYearDate)
                    val title = zodiac.format(resources, withEmoji = false, isPersian = true)
                    val symbol = zodiac.resolveEmoji(true)
                    language.inParentheses.format(it, "$yearString $title $symbol")
                } else it
            } + "\n" + Date(equinoxTime).toGregorianCalendar().formatDateAndTime(withWeekDay = true)
            val remainedTime = equinoxTime - now
            val event = CalendarEvent.EquinoxCalendarEvent(title, false, date, null, remainedTime)
            listOf(event) + events
        } else events
    } else events).toImmutableList()
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
