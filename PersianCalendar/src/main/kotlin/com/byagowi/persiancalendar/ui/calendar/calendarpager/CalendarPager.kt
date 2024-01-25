package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPager(
    viewModel: CalendarViewModel,
    pagerState: PagerState,
    addEvent: () -> Unit,
    width: Dp,
    height: Dp,
) {
    val scope = rememberCoroutineScope()

    val selectedMonthOffsetCommand by viewModel.selectedMonthOffsetCommand.collectAsState()
    selectedMonthOffsetCommand?.let {
        scope.launch {
            pagerState.animateScrollToPage(applyOffset(-it))
            viewModel.changeSelectedMonthOffsetCommand(null)
        }
    }

    val language by language.collectAsState()
    val monthColors = appMonthColors()

    viewModel.notifySelectedMonthOffset(-applyOffset(pagerState.currentPage))

    HorizontalPager(state = pagerState) { page ->
        Box(modifier = Modifier.height(height)) {
            val arrowWidth = width / 12
            val arrowHeight = height / 7 + (if (language.isArabicScript) 4 else 0).dp
            PagerArrow(arrowWidth, arrowHeight, scope, pagerState, page, isPrevious = true)
            Box(modifier = Modifier.padding(start = arrowWidth, end = arrowWidth)) {
                Month(
                    viewModel = viewModel,
                    offset = -applyOffset(page),
                    width = width - arrowWidth * 2,
                    height = height,
                    addEvent = addEvent,
                    monthColors = monthColors,
                )
            }
            PagerArrow(arrowWidth, arrowHeight, scope, pagerState, page, isPrevious = false)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun calendarPagerState(): PagerState =
    rememberPagerState(initialPage = applyOffset(0), pageCount = ::monthsLimit)

private const val monthsLimit = 5000 // this should be an even number

private fun applyOffset(position: Int) = monthsLimit / 2 - position

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BoxScope.PagerArrow(
    arrowWidth: Dp,
    arrowHeight: Dp,
    scope: CoroutineScope,
    pagerState: PagerState,
    index: Int,
    isPrevious: Boolean,
) {
    Box(
        modifier = Modifier
            .align(if (isPrevious) Alignment.TopStart else Alignment.TopEnd)
            .size(arrowWidth, arrowHeight),
    ) {
        val stringId = if (isPrevious) R.string.previous_x else R.string.next_x
        val contentDescription = stringResource(stringId, stringResource(R.string.month))
        Icon(
            if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
            else Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = contentDescription,
            modifier = Modifier
                .width(arrowWidth.coerceAtMost(MaterialIconDimension.dp))
                .combinedClickable(
                    indication = rememberRipple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() },
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
                )
                .align(if (isPrevious) Alignment.CenterEnd else Alignment.CenterStart)
                .alpha(.9f),
        )
    }
}
