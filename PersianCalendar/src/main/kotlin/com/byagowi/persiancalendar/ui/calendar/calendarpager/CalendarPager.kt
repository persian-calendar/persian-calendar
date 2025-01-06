package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CalendarPager(
    viewModel: CalendarViewModel,
    pagerState: PagerState,
    addEvent: (AddEventData) -> Unit,
    today: Jdn,
    size: DpSize,
    navigateToDays: (Jdn, Boolean) -> Unit,
    animatedContentScope: AnimatedContentScope,
) {
    val selectedMonthOffsetCommand by viewModel.selectedMonthOffsetCommand.collectAsState()
    LaunchedEffect(key1 = selectedMonthOffsetCommand) {
        val offset = selectedMonthOffsetCommand ?: return@LaunchedEffect
        val page = applyOffset(-offset)
        if (viewModel.daysScreenSelectedDay.value == null) {
            pagerState.animateScrollToPage(page)
        } else {
            // Apply immediately if we're just coming back from days screen
            pagerState.scrollToPage(page)
            viewModel.changeDaysScreenSelectedDay(null)
        }
        viewModel.changeSelectedMonthOffsetCommand(null)
    }

    val language by language.collectAsState()
    val monthColors = appMonthColors()

    viewModel.notifySelectedMonthOffset(-applyOffset(pagerState.currentPage))

    val scope = rememberCoroutineScope()
    val isHighlighted by viewModel.isHighlighted.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val refreshToken by viewModel.refreshToken.collectAsState()
    val width = size.width
    val height = size.height

    HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
        Box {
            val arrowHeight = height / 7 + (if (language.isArabicScript) 4 else 0).dp
            PagerArrow(arrowHeight, scope, pagerState, page, isPrevious = true)
            DaysTable(
                offset = -applyOffset(page),
                width = width - (pagerArrowSizeAndPadding * 2).dp,
                height = height,
                addEvent = addEvent,
                monthColors = monthColors,
                animatedContentScope = animatedContentScope,
                today = today,
                isHighlighted = isHighlighted,
                refreshToken = refreshToken,
                selectedDay = selectedDay,
                setSelectedDay = { viewModel.changeSelectedDay(it) },
                onWeekClick = navigateToDays,
            )
            PagerArrow(arrowHeight, scope, pagerState, page, isPrevious = false)
        }
    }
}

fun calendarPagerSize(
    isLandscape: Boolean,
    maxWidth: Dp,
    maxHeight: Dp,
    bottomPadding: Dp,
    twoRows: Boolean = false
): DpSize {
    return if (isLandscape) {
        val width = (maxWidth * 45 / 100).coerceAtMost(400.dp)
        val height = 400.dp.coerceAtMost(maxHeight - bottomPadding)
        DpSize(width, height)
    } else DpSize(
        maxWidth,
        (maxHeight / 2f).coerceIn(280.dp, 440.dp).let { if (twoRows) it / 7 * 2 else it }
    )
}

@Composable
fun calendarPagerState(): PagerState =
    rememberPagerState(initialPage = applyOffset(0), pageCount = ::monthsLimit)

private const val monthsLimit = 5000 // this should be an even number

private fun applyOffset(position: Int) = monthsLimit / 2 - position

private const val pagerArrowSize = 40 // 24 + 8 + 8
const val pagerArrowSizeAndPadding = pagerArrowSize + 4

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun BoxScope.PagerArrow(
    arrowHeight: Dp,
    scope: CoroutineScope,
    pagerState: PagerState,
    index: Int,
    isPrevious: Boolean,
    week: Int? = null,
) {
    Box(
        modifier = Modifier
            .align(if (isPrevious) Alignment.TopStart else Alignment.TopEnd)
            .size(pagerArrowSize.dp, arrowHeight),
    ) {
        val stringId = if (isPrevious) R.string.previous_x else R.string.next_x
        val contentDescription = if (week == null) {
            stringResource(stringId, stringResource(R.string.month))
        } else stringResource(R.string.nth_week_of_year, week + if (isPrevious) -1 else 1)
        Icon(
            if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
            else Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = contentDescription,
            modifier = Modifier
                .width(MaterialIconDimension.dp)
                .then(if (week == null) Modifier.combinedClickable(
                    indication = ripple(bounded = false),
                    interactionSource = null,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index + 1 * if (isPrevious) -1 else 1)
                        }
                    },
                    onClickLabel = stringResource(R.string.select_month),
                    onLongClick = {
                        scope.launch {
                            pagerState.scrollToPage(index + 12 * if (isPrevious) -1 else 1)
                        }
                    },
                    onLongClickLabel = stringResource(stringId, stringResource(R.string.year)),
                ) else Modifier.clickable(
                    indication = ripple(bounded = false),
                    interactionSource = null,
                ) {
                    scope.launch {
                        pagerState.animateScrollToPage(index + 1 * if (isPrevious) -1 else 1)
                    }
                })
                .align(if (isPrevious) Alignment.CenterEnd else Alignment.CenterStart)
                .alpha(.9f),
        )
    }
}
