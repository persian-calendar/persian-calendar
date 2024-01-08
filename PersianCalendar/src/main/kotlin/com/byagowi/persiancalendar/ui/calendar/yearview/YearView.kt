package com.byagowi.persiancalendar.ui.calendar.yearview

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.calendarpager.DayPainter
import com.byagowi.persiancalendar.ui.calendar.calendarpager.renderMonthWidget
import com.byagowi.persiancalendar.ui.theme.AppDaySelectionColor
import com.byagowi.persiancalendar.ui.theme.AppMonthColors
import com.byagowi.persiancalendar.ui.utils.LargeShapeCornerSize
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.readYearDeviceEvents
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun YearView(viewModel: CalendarViewModel, maxWidth: Dp, maxHeight: Dp, bottomPadding: Dp) {
    val today by viewModel.today.collectAsState()
    val todayDate = today.toCalendar(mainCalendar)
    val selectedMonthOffset = viewModel.selectedMonthOffset.value
    val yearOffsetInMonths = run {
        val selectedMonth =
            mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
        selectedMonth.year - todayDate.year
    }

    val monthNames = mainCalendar.monthsNames
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var scale by remember { mutableStateOf(1f) }
    val horizontalDivisions = if (isLandscape) 4 else 3
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceAtMost(horizontalDivisions.toFloat())
    }

    val width = maxWidth / horizontalDivisions * scale
    val height = ((maxHeight - bottomPadding) / if (isLandscape) 3 else 4) * scale
    val shape = MaterialTheme.shapes.large.copy(CornerSize(LargeShapeCornerSize.dp * scale))

    val titleHeight = (height / 10).coerceAtLeast(20.dp)
    val titleHeightPx = with(LocalDensity.current) { titleHeight.roundToPx() }
    val titleHeightSp = with(LocalDensity.current) { titleHeight.toSp() / 1.6f }
    val padding = 4.dp

    val widthInPx = with(LocalDensity.current) { width.toPx() }
    val heightInPx = with(LocalDensity.current) { height.toPx() }
    val paddingInPx = with(LocalDensity.current) { padding.toPx() }

    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val indicatorColor = AppDaySelectionColor()
    val monthColors = AppMonthColors()
    val dayPainter = remember(monthColors, indicatorColor, widthInPx, heightInPx) {
        DayPainter(
            context.resources,
            (widthInPx - paddingInPx * 2f) / if (isShowWeekOfYearEnabled) 8 else 7,
            (heightInPx - paddingInPx * 2f - titleHeightPx) / 7,/* row count*/
            isRtl,
            monthColors,
            isYearView = true,
            selectedDayColor = indicatorColor.toArgb(),
        )
    }

    val halfPages = 100
    val lazyColumnState = rememberLazyListState(halfPages + yearOffsetInMonths)
    val yearViewCommand by viewModel.yearViewCommand.collectAsState()
    val scope = rememberCoroutineScope()
    yearViewCommand?.let { command ->
        scope.launch {
            when (command) {
                YearViewCommand.PreviousMonth -> {
                    lazyColumnState.animateScrollToItem(lazyColumnState.firstVisibleItemIndex - 1)
                }

                YearViewCommand.NextMonth -> {
                    lazyColumnState.animateScrollToItem(lazyColumnState.firstVisibleItemIndex + 1)
                }

                YearViewCommand.TodayMonth -> lazyColumnState.animateScrollToItem(halfPages)
            }
            viewModel.clearYearViewCommand()
        }
    }

    LazyColumn(state = lazyColumnState, modifier = Modifier.transformable(transformState)) {
        items(halfPages * 2) {
            val yearOffset = it - halfPages

            val yearDeviceEvents: EventsStore<CalendarEvent.DeviceCalendarEvent> =
                remember(yearOffset, today) {
                    val yearStartJdn = Jdn(
                        mainCalendar.createDate(
                            today.toCalendar(mainCalendar).year + yearOffset, 1, 1
                        )
                    )
                    if (isShowDeviceCalendarEvents.value) context.readYearDeviceEvents(yearStartJdn)
                    else EventsStore.empty()
                }

            Column {
                @OptIn(ExperimentalLayoutApi::class) FlowRow(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    repeat(if (isLandscape) 3 else 4) { row ->
                        repeat(if (isLandscape) 4 else 3) { column ->
                            val month = 1 + column + row * if (isLandscape) 4 else 3
                            val offset = yearOffset * 12 + month - todayDate.month
                            val title = language.value.my.format(
                                monthNames[month - 1],
                                formatNumber(yearOffset + todayDate.year),
                            )
                            Column(
                                Modifier
                                    .size(width, height)
                                    .padding(padding)
                                    .clip(shape)
                                    .clickable(onClickLabel = title) {
                                        viewModel.closeYearView()
                                        viewModel.changeSelectedMonthOffsetCommand(offset)
                                    }
                                    .background(
                                        LocalContentColor.current.copy(
                                            alpha = if (offset == selectedMonthOffset) .025f else .1f,
                                        )
                                    ),
                            ) {
                                Text(
                                    title,
                                    Modifier.size(width - padding * 2, titleHeight),
                                    fontSize = titleHeightSp,
                                    textAlign = TextAlign.Center
                                )
                                Canvas(
                                    Modifier.size(
                                        width - padding * 2,
                                        height - titleHeight - padding * 2,
                                    )
                                ) {
                                    drawIntoCanvas { canvas ->
                                        renderMonthWidget(
                                            dayPainter = dayPainter,
                                            width = size.width.roundToInt(),
                                            canvas = canvas.nativeCanvas,
                                            today = today,
                                            baseDate = mainCalendar.getMonthStartFromMonthsDistance(
                                                today, offset
                                            ),
                                            deviceEvents = yearDeviceEvents,
                                            isRtl = isRtl,
                                            isShowWeekOfYearEnabled = isShowWeekOfYearEnabled,
                                            selectedDay = viewModel.selectedDay.value,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(bottomPadding.coerceAtLeast(24.dp)))
                if (yearOffset != halfPages - 1) Text(
                    formatNumber(yearOffset + todayDate.year + 1),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
                        },
                )
            }
        }
    }
}
