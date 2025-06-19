package com.byagowi.persiancalendar.ui.common

import android.content.ClipData
import android.content.Context
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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MOON
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.isAncientIranEnabled
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Tithi
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.utils.MoonInScorpioState
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.formatAsSeleucidAndYazdegerdDate
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.generateYearName
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.jalaliAndHistoricalName
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.moonInScorpioState
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.byagowi.persiancalendar.utils.toLinearDate
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.seasons
import io.github.cosinekitty.astronomy.sunPosition
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
    val context = LocalContext.current
    val isToday = today == jdn
    Column(
        Modifier.semantics {
            if (isTalkBackEnabled) this.contentDescription = getA11yDaySummary(
                context.resources,
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
            val language by language.collectAsState()
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
                        )
                        .clickable { navigateToAstronomy(jdn) }
                        .size(20.dp)
                )
            }
            val isForcedIranTimeEnabled by isForcedIranTimeEnabled.collectAsState()
            AnimatedContent(
                if (isToday && isForcedIranTimeEnabled) language.inParentheses.format(
                    jdn.weekDayName, stringResource(R.string.iran_time)
                ) else jdn.weekDayName,
                transitionSpec = appCrossfadeSpec,
                label = "weekday name",
            ) { SelectionContainer { Text(it, color = MaterialTheme.colorScheme.primary) } }
        }
        Spacer(Modifier.height(8.dp))
        CalendarsFlow(shownCalendars, jdn)
        Spacer(Modifier.height(4.dp))

        val date = jdn on selectedCalendar
        val equinox = remember(selectedCalendar, jdn) {
            if (date !is PersianDate) return@remember null
            if (date.month == 12 && date.dayOfMonth >= 20 || date.month == 1 && date.dayOfMonth == 1)
                equinoxTitle(date, jdn, context).first else null
        }

        this.AnimatedVisibility(visible = equinox != null) { AutoSizedBodyText(equinox.orEmpty()) }

        this.AnimatedVisibility(!isToday) {
            AutoSizedBodyText(
                listOf(
                    stringResource(R.string.days_distance),
                    spacedColon,
                    calculateDaysDifference(context.resources, jdn, today)
                ).joinToString("")
            )
        }

        val language by language.collectAsState()

        val moonInScorpioState = if (isAstronomicalExtraFeaturesEnabled)
            moonInScorpioState(jdn) else null
        this.AnimatedVisibility(moonInScorpioState != null) {
            AutoSizedBodyText(
                if (language.isPersian) when (val state = moonInScorpioState) {
                    MoonInScorpioState.Borji -> "قمر در برج عقرب"
                    MoonInScorpioState.Falaki -> "قمر در صورت فلکی عقرب"
                    is MoonInScorpioState.Start ->
                        "${state.clock.toFormattedString()} قمر وارد برج عقرب می‌شود"

                    is MoonInScorpioState.End ->
                        "${state.clock.toFormattedString()} قمر از صورت فلکی عقرب خارج می‌شود"

                    else -> ""
                } else stringResource(R.string.moon_in_scorpio)
            )
        }

        this.AnimatedVisibility(isExpanded && isAstronomicalExtraFeaturesEnabled) {
            AutoSizedBodyText(generateYearName(context.resources, jdn, withEmoji = true))
        }

        this.AnimatedVisibility(isExpanded && isAstronomicalExtraFeaturesEnabled) {
            val zodiac = Zodiac.fromTropical(sunPosition(jdn.toAstronomyTime(hourOfDay = 12)).elon)
            AutoSizedBodyText(
                stringResource(R.string.zodiac) + spacedColon +
                        zodiac.format(LocalContext.current.resources, true)
            )
        }

        if (language.isPersian) {
            val persianDate = jdn.toPersianDate()
            val enableExtra = isAncientIranEnabled || isAstronomicalExtraFeaturesEnabled
            this.AnimatedVisibility((enableExtra && isExpanded) || persianDate.year < 1304) {
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
            val coordinates by coordinates.collectAsState()
            AutoSizedBodyText(
                if (language.isNepali) {
                    phase.emoji(coordinates) + " " + jdn.toNepaliDate().monthName + " " +
                            language.moonNames(phase) +
                            " ~" + Tithi.tithiName(System.currentTimeMillis())
                } else phase.emoji(coordinates) + " " + language.moonNames(phase)
            )
        }

        val startOfYearJdn = Jdn(selectedCalendar, date.year, 1, 1)
        val endOfYearJdn = Jdn(selectedCalendar, date.year + 1, 1, 1) - 1
        val currentWeek = jdn.getWeekOfYear(startOfYearJdn)
        val weeksCount = endOfYearJdn.getWeekOfYear(startOfYearJdn)
        val progresses = remember(jdn, selectedCalendar) {
            val (passedDaysInSeason, totalSeasonDays) = jdn.getPositionInSeason()
            val monthLength = selectedCalendar.getMonthLength(date.year, date.month)
            listOfNotNull(
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
                progresses.forEach { (stringId, current, max) ->
                    val title = stringResource(stringId)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
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
                            progress = { progress },
                            strokeWidth = indicatorStrokeWidth
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        this.AnimatedVisibility(isExpanded) {
            AutoSizedBodyText(
                stringResource(
                    R.string.start_of_year_diff,
                    formatNumber(jdn - startOfYearJdn + 1),
                    formatNumber(currentWeek),
                    formatNumber(date.month)
                )
            )
        }
        this.AnimatedVisibility(isExpanded) {
            AutoSizedBodyText(
                stringResource(
                    R.string.end_of_year_diff,
                    formatNumber(endOfYearJdn - jdn),
                    formatNumber(weeksCount - currentWeek),
                    formatNumber(12 - date.month)
                ),
                topPadding = 0.dp,
            )
        }

        Spacer(Modifier.height(8.dp))
        ExpandArrow(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterHorizontally),
            isExpanded = isExpanded,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun AutoSizedBodyText(
    text: String,
    topPadding: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val contextColor = LocalContentColor.current
    Box(
        modifier = Modifier
            .padding(top = topPadding, start = 24.dp, end = 24.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        SelectionContainer {
            BasicText(
                text = text,
                color = { contextColor },
                style = textStyle,
                modifier = Modifier.animateContentSize(),
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

fun equinoxTitle(date: PersianDate, jdn: Jdn, context: Context): Pair<String, Long> {
    val gregorianYear = jdn.toCivilDate().year
    val timestamp = seasons(gregorianYear).marchEquinox.toMillisecondsSince1970()
    val equinoxYear = when (mainCalendar) {
        Calendar.SHAMSI -> date.year + if (date.month == 12) 1 else 0
        else -> gregorianYear
    }
    val calendar = Date(timestamp).toGregorianCalendar()
    return context.getString(
        R.string.spring_equinox, formatNumber(equinoxYear), calendar.formatDateAndTime()
    ) to timestamp
}

@Composable
private fun CalendarsFlow(calendarsToShow: List<Calendar>, jdn: Jdn) {
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
                        formatNumber(date.dayOfMonth),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.animateContentSize(),
                    )
                    Text(date.monthName, modifier = Modifier.animateContentSize())
                }
                SelectionContainer {
                    Text(date.toLinearDate(), modifier = Modifier.animateContentSize())
                }
            }
        }
    }
}
