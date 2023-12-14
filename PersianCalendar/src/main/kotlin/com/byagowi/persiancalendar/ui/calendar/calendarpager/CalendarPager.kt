package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.utils.resolveColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPager(modifier: Modifier = Modifier, viewModel: CalendarViewModel) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = applyOffset(0),
        pageCount = ::monthsLimit,
    )

    val scope = rememberCoroutineScope()

    val selectedMonthOffsetCommand by viewModel.selectedMonthOffsetCommand.collectAsState()
    selectedMonthOffsetCommand?.let {
        viewModel.changeSelectedMonthOffsetCommand(null)
        scope.launch { pagerState.animateScrollToPage(applyOffset(-it)) }
    }

    HorizontalPager(state = pagerState, modifier = modifier) { index ->
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxHeight(),
        ) {
            // TODO: Ideally this should be onPrimary
            var height by remember { mutableStateOf(100) }
            val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
            Icon(
                Icons.AutoMirrored.Default.KeyboardArrowLeft,
                contentDescription = null,
                tint = colorOnAppBar,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .height(with(LocalDensity.current) { height.toDp() } / 7)
                    .combinedClickable(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index - 1) }
                        },
                        onClickLabel = stringResource(
                            R.string.previous_x, stringResource(R.string.month)
                        ),
                        onLongClick = {
                            scope.launch { pagerState.animateScrollToPage(index - 12) }
                        },
                        onLongClickLabel = stringResource(
                            R.string.previous_x, stringResource(R.string.year)
                        ),
                    )
                    .alpha(.9f),
            )
            val isCurrentSelection = pagerState.currentPage == index
            if (pagerState.currentPage == index)
                viewModel.changeSelectedMonthOffset(-applyOffset(index))

            val currentMonthOffset = -applyOffset(index)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp)
                    .onSizeChanged { height = (it.height * 1.08).roundToInt() },
            ) { Month(viewModel, currentMonthOffset, isCurrentSelection) }
            Icon(
                Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colorOnAppBar,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(with(LocalDensity.current) { height.toDp() } / 7)
                    .combinedClickable(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index + 1) }
                        },
                        onClickLabel = stringResource(
                            R.string.next_x, stringResource(R.string.month)
                        ),
                        onLongClick = {
                            scope.launch { pagerState.animateScrollToPage(index + 12) }
                        },
                        onLongClickLabel = stringResource(
                            R.string.next_x, stringResource(R.string.year)
                        ),
                    )
                    .alpha(.9f),
            )
        }
    }
}

private val monthsLimit = 5000 // this should be an even number

private fun applyOffset(position: Int) = monthsLimit / 2 - position
