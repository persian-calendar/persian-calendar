package com.byagowi.persiancalendar.ui.calendar.monthview

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.customFontName
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarNumeral
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.preferredSwipeDownAction
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.pagerArrowSizeAndPadding
import com.byagowi.persiancalendar.ui.calendar.detectSwipe
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.monthName


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MonthScreen(
    calendarViewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
    initiallySelectedDay: Jdn,
) {
    val initialItem = ITEMS_COUNT / 2
    val state = rememberLazyListState(initialItem, 0)
    val weekStartJdn = initiallySelectedDay - initiallySelectedDay.weekDay.ordinal
    val focusedJdn by remember {
        derivedStateOf {
            if (state.firstVisibleItemIndex == initialItem) initiallySelectedDay
            else (weekStartJdn + 3 + (state.firstVisibleItemIndex - initialItem) * 7)
        }
    }
    val focusedDate = focusedJdn on mainCalendar
    val preferredSwipeDownAction by preferredSwipeDownAction.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Scaffold(
        modifier = Modifier.detectSwipe {
            { isUp ->
                if (!isLandscape && isUp) when (preferredSwipeDownAction) {
//                    SwipeDownAction.MonthView -> navigateUp()
                    else -> {}
                }
            }
        },
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    val date = focusedJdn on mainCalendar
                    val screenTitle = stringResource(R.string.schedule)
                    Column(Modifier.semantics { this.contentDescription = screenTitle }) {
                        Crossfade(date.monthName, label = "title") { state ->
                            Text(state, style = MaterialTheme.typography.titleLarge)
                        }
                        val numeral by numeral.collectAsState()
                        Crossfade(numeral.format(date.year), label = "subtitle") { state ->
                            Text(state, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
                actions = {},
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val bottomPadding = paddingValues.calculateBottomPadding()
            val pagerSize = calendarPagerSize(isLandscape, maxWidth, maxHeight, bottomPadding)
            val (width, suggestedHeight) = pagerSize
            val cellWidth = (width - (pagerArrowSizeAndPadding * 2).dp) / 7
            val cellHeight = suggestedHeight / 7
            val cellsSizeModifier = Modifier.size(cellWidth, cellHeight)
            val density = LocalDensity.current
            val diameter = min(cellWidth, cellHeight)
            val customFontName by customFontName.collectAsState()
            val daysTextSize = diameter * when {
                mainCalendarNumeral.isArabicIndicVariants && customFontName == null -> 25
                mainCalendarNumeral.isTamil -> 16
                else -> 18
            } / 40
            val daysStyle = LocalTextStyle.current.copy(
                fontSize = with(density) { daysTextSize.toSp() },
            )
            val weekStart by weekStart.collectAsState()

            Column(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    start = pagerArrowSizeAndPadding.dp,
                ),
            ) {
                Row {
                    repeat(7) { column ->
                        Box(contentAlignment = Alignment.Center, modifier = cellsSizeModifier) {
                            val weekDay = weekStart + column
                            val description = stringResource(
                                R.string.week_days_name_column,
                                weekDay.title,
                            )
                            Text(
                                weekDay.shortTitle,
                                fontSize = with(density) { (diameter * .5f).toSp() },
                                modifier = Modifier
                                    .alpha(AppBlendAlpha)
                                    .semantics { this.contentDescription = description },
                            )
                        }
                    }
                }
                LazyColumn(state = state) {
                    items(ITEMS_COUNT) { index ->
                        val jdn = indexToJdn(initiallySelectedDay, index)
                        Row {
                            repeat(7) { column ->
                                val weekDayPosition = weekStart.ordinal + column
                                val dayJdn = jdn + weekDayPosition
                                val dayDate = dayJdn on mainCalendar
//                                val monthStartDate =
//                                    mainCalendar.createDate(dayDate.year, dayDate.month, 1)
//                                val monthStartJdn = Jdn(monthStartDate)
//                                val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
//                                val monthStartWeekOfYear = monthStartJdn.getWeekOfYear(startOfYearJdn)
//                                val weekNumber = monthStartWeekOfYear
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = cellsSizeModifier
//                                        .then(
//                                        if (true) Modifier.sharedBounds(
//                                            rememberSharedContentState(
//                                                "$SHARED_CONTENT_KEY_WEEK_NUMBER$monthStartJdn-$weekNumber"
//                                            ),
//                                            animatedVisibilityScope = animatedContentScope,
//                                        ) else Modifier
//                                    )
                                ) {
                                    Text(
                                        mainCalendarNumeral.format(dayDate.dayOfMonth),
                                        style = daysStyle,
                                        modifier = Modifier.alpha(
                                            if (focusedDate.month == dayDate.month && focusedDate.year == dayDate.year)
                                                1f else AppBlendAlpha

                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .height(0.dp),
            ) {
                ScreenSurface(animatedContentScope = animatedContentScope) {
                    Box(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private fun indexToJdn(baseJdn: Jdn, index: Int) = baseJdn + (index - ITEMS_COUNT / 2) * 7

private const val ITEMS_COUNT = 5000 * 2
