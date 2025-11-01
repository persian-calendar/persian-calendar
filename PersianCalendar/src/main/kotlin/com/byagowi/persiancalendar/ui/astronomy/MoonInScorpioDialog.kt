package com.byagowi.persiancalendar.ui.astronomy

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.lunarLongitude
import com.byagowi.persiancalendar.utils.searchMoonAgeTime
import com.byagowi.persiancalendar.utils.toCivilDate
import kotlinx.coroutines.launch
import java.util.GregorianCalendar

private data class Entry(
    val startClock: String,
    val startDate: String,
    val endClock: String,
    val endDate: String,
    val upcoming: Boolean,
)

private const val yearPages = 5000

@Composable
fun MoonInScorpioDialog(now: GregorianCalendar, onDismissRequest: () -> Unit) {
    val today = Jdn(now.toCivilDate())
    val currentYear = (today on mainCalendar).year
    val numeral by numeral.collectAsState()
    // Type dialog is Persian only for now
    val types = listOf(
        "صورت فلکی" to Zodiac.SCORPIO.iauRange,
        "برج" to Zodiac.SCORPIO.tropicalRange,
    )
    val yearPagerState = rememberPagerState(initialPage = yearPages / 2, pageCount = { yearPages })
    val coroutineScope = rememberCoroutineScope()
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Crossfade(yearPagerState.currentPage == yearPages / 2) {
                    if (it) Text(
                        Zodiac.SCORPIO.symbol,
                        fontFamily = FontFamily(
                            Font(R.font.notosanssymbolsregularzodiacsubset)
                        ),
                        fontSize = 20.sp,
                    ) else TodayActionButton(true) {
                        coroutineScope.launch { yearPagerState.animateScrollToPage(yearPages / 2) }
                    }
                }
                HorizontalPager(yearPagerState) { page ->
                    val year = page - yearPages / 2 + currentYear
                    Text(
                        stringResource(R.string.moon_in_scorpio) + spacedComma + numeral.format(year),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 9.sp,
                            maxFontSize = LocalTextStyle.current.fontSize,
                        ),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { types.size })
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            divider = {},
            containerColor = Color.Transparent,
            indicator = {
                TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(pagerState.currentPage))
            },
        ) {
            val view = LocalView.current
            types.forEachIndexed { i, (title, _) ->
                Tab(
                    text = { Text(title) },
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                    selected = i == pagerState.currentPage,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        view.performHapticFeedbackVirtualKey()
                        coroutineScope.launch { pagerState.scrollToPage(i) }
                    },
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.animateContentSize()) { page ->
            val year = yearPagerState.currentPage - yearPages / 2 + currentYear
            val entries = remember(currentYear, year) {
                val start = Jdn(mainCalendar.createDate(year, 1, 1))
                val end = Jdn(mainCalendar.createDate(year + 1, 1, 1)) - 1
                val (rangeStart, rangeEnd) = types[page].second
                val range = rangeStart..rangeEnd
                buildList {
                    var firstComing = year == currentYear
                    var day = start
                    while (lunarLongitude(day, hourOfDay = 0) in range) day -= 1
                    while (day <= end) {
                        searchMoonAgeTime(day, rangeStart)?.let parent@{ startClock ->
                            val startDay = day
                            while (true) {
                                searchMoonAgeTime(day, rangeEnd)?.let { endClock ->
                                    val endDay = day
                                    val upcoming = if (firstComing && today <= startDay) {
                                        firstComing = false
                                        true
                                    } else false
                                    add(
                                        Entry(
                                            startClock = startClock.toFormattedString(),
                                            startDate = formatDate(
                                                startDay on mainCalendar,
                                                forceNonNumerical = true,
                                            ),
                                            endClock = endClock.toFormattedString(),
                                            endDate = formatDate(
                                                endDay on mainCalendar,
                                                forceNonNumerical = true,
                                            ),
                                            upcoming = upcoming,
                                        )
                                    )
                                    return@parent
                                }
                                day += 1
                            }
                        }
                        day += 1
                    }
                }
            }
            SelectionContainer {
                Column {
                    @Composable
                    fun Cell(text: String, weight: Float) {
                        Text(
                            text = text,
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 5.sp,
                                maxFontSize = LocalTextStyle.current.fontSize,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(weight),
                        )
                    }
                    Row(Modifier.padding(top = 4.dp)) {
                        Cell("ورود ماه", 1f)
                        Cell("خروج ماه", 1f)
                    }
                    val outlineColor = MaterialTheme.colorScheme.outlineVariant
                    Column(
                        Modifier.drawWithContent {
                            drawContent()
                            drawLine(
                                color = outlineColor,
                                strokeWidth = 1.dp.toPx(),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                            )
                        }
                    ) {
                        entries.forEach { entry ->
                            HorizontalDivider()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .then(
                                        if (entry.upcoming) Modifier.background(
                                            MaterialTheme.colorScheme.surfaceContainerLowest,
                                            MaterialTheme.shapes.medium,
                                        ) else Modifier
                                    )
                                    .padding(vertical = 2.dp, horizontal = 4.dp),
                            ) {
                                Cell(entry.startClock, 1f)
                                Cell(entry.startDate, 2f)
                                Cell(entry.endClock, 1f)
                                Cell(entry.endDate, 2f)
                            }
                        }
                    }
                }
            }
        }
    }
}
