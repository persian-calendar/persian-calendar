package com.byagowi.persiancalendar.ui.common

import android.content.ClipData
import android.content.res.Resources
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MOON
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.showMoonInScorpio
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Tithi
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.utils.MoonInScorpioState
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.formatAsSeleucidAndYazdegerdDate
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.generateYearName
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.isOldEra
import com.byagowi.persiancalendar.utils.jalaliAndHistoricalName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.moonInScorpioState
import com.byagowi.persiancalendar.utils.searchMoonAgeTime
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.byagowi.persiancalendar.utils.toLinearDate
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.seasons
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CalendarsOverview(
    jdn: Jdn,
    today: Jdn,
    selectedCalendar: Calendar,
    shownCalendars: List<Calendar>,
    isExpanded: Boolean,
    navigateToAstronomy: (Jdn) -> Unit,
    animatedContentScope: AnimatedContentScope,
) {
    val resources = LocalResources.current
    val language by language.collectAsState()
    val numeral by numeral.collectAsState()
    val isToday = today == jdn
    val isTalkBackEnabled by isTalkBackEnabled.collectAsState()
    Column(
        Modifier.semantics {
            if (isTalkBackEnabled) this.contentDescription = getA11yDaySummary(
                resources,
                jdn,
                isToday,
                EventsStore.empty(),
                withZodiac = true,
                withOtherCalendars = true,
                withTitle = true
            )
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            this.AnimatedVisibility(isExpanded || language.alwaysNeedMoonState) {
                AndroidView(
                    factory = ::MoonView,
                    update = { it.jdn = jdn.value.toFloat() },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .semantics { this.hideFromAccessibility() }
                        .sharedBounds(
                            rememberSharedContentState(key = SHARED_CONTENT_KEY_MOON),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = appBoundsTransform,
                        )
                        .clickable { navigateToAstronomy(jdn) }
                        .size(20.dp)
                )
            }
            val isForcedIranTimeEnabled by isForcedIranTimeEnabled.collectAsState()
            AnimatedContent(
                if (isToday && isForcedIranTimeEnabled) language.inParentheses.format(
                    jdn.weekDay.title,
                    stringResource(R.string.iran_time)
                ) else jdn.weekDay.title,
                transitionSpec = appCrossfadeSpec,
                label = "weekday name",
            ) { SelectionContainer { Text(it, color = MaterialTheme.colorScheme.primary) } }
        }
        Spacer(Modifier.height(8.dp))
        CalendarsFlow(shownCalendars, jdn, isExpanded, numeral)
        Spacer(Modifier.height(4.dp))

        val date = jdn on selectedCalendar
        val equinox = remember(selectedCalendar, jdn, resources) {
            if (date !is PersianDate) return@remember null
            if (date.month == 12 && date.dayOfMonth >= 20 || date.month == 1 && date.dayOfMonth == 1)
                equinoxTitle(date, jdn, resources).first else null
        }

        this.AnimatedVisibility(visible = equinox != null) { AutoSizedBodyText(equinox.orEmpty()) }

        this.AnimatedVisibility(!isToday) {
            AutoSizedBodyText(
                listOf(
                    stringResource(R.string.days_distance),
                    spacedColon,
                    calculateDaysDifference(resources, jdn, today)
                ).joinToString("")
            )
        }

        val moonInScorpioState =
            if (showMoonInScorpio.collectAsState().value) moonInScorpioState(jdn) else null
        this.AnimatedVisibility(moonInScorpioState != null) {
            val text = if (language.isPersianOrDari) when (moonInScorpioState) {
                MoonInScorpioState.Borji -> "قمر در برج عقرب"
                MoonInScorpioState.Falaki -> "قمر در صورت فلکی عقرب"
                is MoonInScorpioState.Start -> {
                    "${moonInScorpioState.clock.toFormattedString()} ورود قمر به برج عقرب"
                }

                is MoonInScorpioState.End -> {
                    "${moonInScorpioState.clock.toFormattedString()} خروج قمر از صورت فلکی عقرب"
                }

                else -> ""
            } else stringResource(R.string.moon_in_scorpio)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
            ) {
                SelectionContainer {
                    Text(
                        text = buildAnnotatedString {
                            append("$text ")
                            withStyle(
                                MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                                    fontFamily = FontFamily(
                                        Font(R.font.notosanssymbolsregularzodiacsubset)
                                    ),
                                )
                            ) { append(Zodiac.SCORPIO.symbol) }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .animateContentSize(appContentSizeAnimationSpec)
                            .semantics { this.contentDescription = text },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
        val persianDate = jdn.toPersianDate()
        this.AnimatedVisibility(isExpanded && isAstronomicalExtraFeaturesEnabled) {
            val yearName = generateYearName(
                resources,
                jdn,
                withOldEraName = persianDate.isOldEra && language.isUserAbleToReadPersian,
                withEmoji = true
            )
            AutoSizedBodyText(yearName)
        }

        this.AnimatedVisibility(isExpanded && isAstronomicalExtraFeaturesEnabled) {
            val zodiacString = stringResource(R.string.zodiac)
            val zodiac = Zodiac.fromTropical(sunPosition(jdn.toAstronomyTime(hourOfDay = 12)).elon)
            val zodiacTitle = stringResource(zodiac.titleId)
            AutoSizedBodyText(
                text = zodiacString + spacedColon + zodiac.symbol + " " + zodiacTitle,
                contentDescriptionOverride = zodiacString + spacedColon + zodiacTitle,
            )
        }

        if (language.isPersian) {
            val eventsRepository by eventsRepository.collectAsState()
            val enableExtra = eventsRepository.iranAncient || isAstronomicalExtraFeaturesEnabled
            this.AnimatedVisibility((enableExtra && isExpanded) || persianDate.isOldEra) {
                AutoSizedBodyText(jalaliAndHistoricalName(persianDate, jdn))
            }
            this.AnimatedVisibility(isAstronomicalExtraFeaturesEnabled && isExpanded) {
                AutoSizedBodyText(formatAsSeleucidAndYazdegerdDate(jdn))
            }
        }

        this.AnimatedVisibility(
            isExpanded && (isAstronomicalExtraFeaturesEnabled || language.isNepali)
        ) {
            val time = jdn.toAstronomyTime(hourOfDay = 12)
            val lunarAge = LunarAge.fromDegrees(eclipticGeoMoon(time).lon - sunPosition(time).elon)
            val phase = lunarAge.toPhase()
            val exactTime = remember(jdn) {
                val targetDegrees = when (phase) {
                    LunarAge.Phase.NEW_MOON -> 0.0
                    LunarAge.Phase.FIRST_QUARTER -> 90.0
                    LunarAge.Phase.FULL_MOON -> 180.0
                    LunarAge.Phase.THIRD_QUARTER -> 270.0
                    else -> return@remember ""
                }
                runCatching {
                    searchMoonAgeTime(jdn, targetDegrees)?.let {
                        spacedColon + it.toFormattedString()
                    }
                }.onFailure(logException).getOrNull().orEmpty()
            }
            val coordinates by coordinates.collectAsState()
            AutoSizedBodyText(
                if (language.isNepali) {
                    phase.emoji(coordinates) + " " + jdn.toNepaliDate().monthName + " " +
                            language.moonNames(phase) +
                            " ~" + Tithi.tithiName(System.currentTimeMillis())
                } else {
                    phase.emoji(coordinates) + " " + language.moonNames(phase) + exactTime
                }
            )
        }

        val startOfYearJdn = Jdn(selectedCalendar, date.year, 1, 1)
        val endOfYearJdn = Jdn(selectedCalendar, date.year + 1, 1, 1) - 1
        val weekStart by weekStart.collectAsState()
        val currentWeek = jdn.getWeekOfYear(startOfYearJdn, weekStart)
        val weeksCount = endOfYearJdn.getWeekOfYear(startOfYearJdn, weekStart)
        val progresses = remember(jdn, selectedCalendar, weekStart) {
            val (passedDaysInSeason, totalSeasonDays) = jdn.getPositionInSeason()
            val monthLength = selectedCalendar.getMonthLength(date.year, date.month)
            listOf(
                Triple(R.string.week, jdn.weekDay - weekStart + 1, 7),
                Triple(R.string.month, date.dayOfMonth, monthLength),
                Triple(R.string.season, passedDaysInSeason, totalSeasonDays),
                Triple(R.string.year, jdn - startOfYearJdn, endOfYearJdn - startOfYearJdn),
            )
        }

        var firstShow by rememberSaveable { mutableStateOf(true) }
        LaunchedEffect(Unit) { firstShow = false }
        val indicatorStrokeWidth by animateDpAsState(
            if (isExpanded && !firstShow) ProgressIndicatorDefaults.CircularStrokeWidth else 0.dp,
            animationSpec = tween(800),
            label = "stroke width",
        )

        this.AnimatedVisibility(isExpanded) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
            ) {
                val indicatorColor = animateColor(ProgressIndicatorDefaults.circularColor).value
                val indicatorTrackColor =
                    animateColor(ProgressIndicatorDefaults.circularDeterminateTrackColor).value
                progresses.forEach { (stringId, current, max) ->
                    val title = stringResource(stringId)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .focusable(true)
                            .semantics {
                                this.contentDescription = "$title$spacedColon$current / $max"
                            }
                            .padding(all = 8.dp),
                    ) {
                        val progress by animateFloatAsState(
                            current.toFloat() / max,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                            label = "progress"
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.semantics { this.hideFromAccessibility() },
                            progress = { progress },
                            strokeWidth = indicatorStrokeWidth,
                            color = indicatorColor,
                            trackColor = indicatorTrackColor,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        this.AnimatedVisibility(isExpanded) {
            AutoSizedBodyText(
                stringResource(
                    R.string.start_of_year_diff,
                    numeral.format(jdn - startOfYearJdn + 1),
                    numeral.format(currentWeek),
                    numeral.format(date.month)
                )
            )
        }
        this.AnimatedVisibility(isExpanded) {
            AutoSizedBodyText(
                stringResource(
                    R.string.end_of_year_diff,
                    numeral.format(endOfYearJdn - jdn),
                    numeral.format(weeksCount - currentWeek),
                    numeral.format(12 - date.month)
                ),
                topPadding = 0.dp,
            )
        }

        Spacer(Modifier.height(8.dp))
        ExpandArrow(
            modifier = Modifier
                .size(with(LocalDensity.current) { 20.sp.toDp() })
                .align(Alignment.CenterHorizontally),
            isExpanded = isExpanded,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun AutoSizedBodyText(
    text: String,
    topPadding: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    contentDescriptionOverride: String? = null,
) {
    Box(
        modifier = Modifier
            .padding(top = topPadding, start = 24.dp, end = 24.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        SelectionContainer {
            Text(
                text = text,
                style = textStyle,
                modifier = Modifier
                    .animateContentSize(appContentSizeAnimationSpec)
                    .semantics {
                        if (contentDescriptionOverride != null) {
                            this.contentDescription = contentDescriptionOverride
                        }
                    },
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                    maxFontSize = textStyle.fontSize,
                ),
            )
        }
    }
}

fun equinoxTitle(date: PersianDate, jdn: Jdn, resources: Resources): Pair<String, Long> {
    val gregorianYear = jdn.toCivilDate().year
    val timestamp = seasons(gregorianYear).marchEquinox.toMillisecondsSince1970()
    val equinoxYear = when (mainCalendar) {
        Calendar.SHAMSI -> date.year + if (date.month == 12) 1 else 0
        else -> gregorianYear
    }
    val calendar = Date(timestamp).toGregorianCalendar()
    return resources.getString(
        R.string.spring_equinox,
        numeral.value.format(equinoxYear),
        calendar.formatDateAndTime(withWeekDay = true)
    ) to timestamp
}

@Composable
private fun CalendarsFlow(
    calendarsToShow: List<Calendar>,
    jdn: Jdn,
    isExpanded: Boolean,
    numeral: Numeral,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        calendarsToShow.forEach { calendar ->
            val date = jdn on calendar
            Column(
                modifier = Modifier.defaultMinSize(minWidth = ItemWidth.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val clipboard = LocalClipboard.current
                val coroutineScope = rememberCoroutineScope()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = null,
                            indication = ripple(bounded = false),
                        ) {
                            val entry = ClipData.newPlainText("date", formatDate(date))
                            coroutineScope.launch { clipboard.setClipEntry(entry.toClipEntry()) }
                        }
                        .semantics { this.contentDescription = formatDate(date) },
                ) {
                    Text(
                        numeral.format(date.dayOfMonth),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .animateContentSize(appContentSizeAnimationSpec)
                            .semantics { this.hideFromAccessibility() },
                    )
                    HandleSacredMonth(isExpanded, date) {
                        Text(
                            date.monthName,
                            modifier = Modifier
                                .animateContentSize(appContentSizeAnimationSpec)
                                .semantics { this.hideFromAccessibility() }
                        )
                    }
                }
                SelectionContainer(Modifier.semantics { this.hideFromAccessibility() }) {
                    Text(
                        date.toLinearDate(),
                        modifier = Modifier.animateContentSize(appContentSizeAnimationSpec)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleSacredMonth(
    isExpanded: Boolean,
    date: AbstractDate,
    content: @Composable () -> Unit
) {
    val displaySacredness = isExpanded && date is IslamicDate && date.isSacredMonths && run {
        val language by language.collectAsState()
        val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
        isAstronomicalExtraFeaturesEnabled && language.isUserAbleToReadPersian
    }
    val backgroundColor by animateColor(
        if (displaySacredness) MaterialTheme.colorScheme.error.copy(alpha = .1f)
        else Color.Transparent
    )
    val tooltipState = rememberTooltipState()
    val coroutine = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text("ماه حرام") } },
        state = tooltipState,
        enableUserInput = false,
        modifier = Modifier
            .background(color = backgroundColor, shape = MaterialTheme.shapes.small)
            .then(
                if (displaySacredness) Modifier
                    .clip(shape = MaterialTheme.shapes.small)
                    .clickable { coroutine.launch { tooltipState.show() } }
                else Modifier,
            )
            .padding(horizontal = 4.dp),
        content = content,
    )
}

// https://en.wikipedia.org/wiki/Sacred_months
private val IslamicDate.isSacredMonths
    get() = when (this.month) {
        1, 7, 11, 12 -> true
        else -> false
    }
