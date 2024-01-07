package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.theme.AppMonthColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPager(viewModel: CalendarViewModel, pagerState: PagerState, width: Dp, height: Dp) {
    val scope = rememberCoroutineScope()

    val selectedMonthOffsetCommand by viewModel.selectedMonthOffsetCommand.collectAsState()
    selectedMonthOffsetCommand?.let {
        scope.launch {
            pagerState.animateScrollToPage(applyOffset(-it))
            viewModel.changeSelectedMonthOffsetCommand(null)
        }
    }

    val language by language.collectAsState()
    val monthColors = AppMonthColors()

    HorizontalPager(state = pagerState) { index ->
        Box(modifier = Modifier.height(height)) {
            val iconSize = width / 12
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(iconSize, height / 7 + (if (language.isArabicScript) 4 else 0).dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier
                        .width(iconSize.coerceAtMost(MaterialIconDimension.dp))
                        .combinedClickable(
                            indication = rememberRipple(bounded = false),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index - 1) }
                            },
                            onClickLabel = stringResource(
                                R.string.previous_x, stringResource(R.string.month)
                            ),
                            onLongClick = {
                                scope.launch { pagerState.scrollToPage(index - 12) }
                            },
                            onLongClickLabel = stringResource(
                                R.string.previous_x, stringResource(R.string.year)
                            ),
                        )
                        .align(Alignment.CenterEnd)
                        .alpha(.9f),
                )
            }
            if (pagerState.currentPage == index)
                viewModel.notifySelectedMonthOffset(-applyOffset(index))
            val currentMonthOffset = -applyOffset(index)
            Box(modifier = Modifier.padding(start = iconSize, end = iconSize)) {
                Month(
                    viewModel,
                    currentMonthOffset,
                    DpSize(width - iconSize * 2, height),
                    monthColors
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(iconSize, height / 7 + (if (language.isArabicScript) 4 else 0).dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .width(iconSize.coerceAtMost(MaterialIconDimension.dp))
                        .combinedClickable(
                            indication = rememberRipple(bounded = false),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index + 1) }
                            },
                            onClickLabel = stringResource(
                                R.string.next_x, stringResource(R.string.month)
                            ),
                            onLongClick = {
                                scope.launch { pagerState.scrollToPage(index + 12) }
                            },
                            onLongClickLabel = stringResource(
                                R.string.next_x, stringResource(R.string.year)
                            ),
                        )
                        .align(Alignment.CenterStart)
                        .alpha(.9f),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPagerState(): PagerState {
    return rememberPagerState(initialPage = applyOffset(0), pageCount = ::monthsLimit)
}

private val monthsLimit = 5000 // this should be an even number

private fun applyOffset(position: Int) = monthsLimit / 2 - position
