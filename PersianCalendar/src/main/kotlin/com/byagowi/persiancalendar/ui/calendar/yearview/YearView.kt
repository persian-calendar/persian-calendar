package com.byagowi.persiancalendar.ui.calendar.yearview

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.util.lruCache
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.calendarpager.DayPainter
import com.byagowi.persiancalendar.ui.calendar.calendarpager.renderMonthWidget
import com.byagowi.persiancalendar.ui.calendar.detectHorizontalSwipe
import com.byagowi.persiancalendar.ui.calendar.detectZoom
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.resolveFontFile
import com.byagowi.persiancalendar.ui.utils.LargeShapeCornerSize
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.otherCalendarFormat
import com.byagowi.persiancalendar.utils.readYearDeviceEvents
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor

@Composable
fun YearView(
    viewModel: CalendarViewModel,
    closeYearView: () -> Unit,
    yearViewCalendar: MutableState<Calendar?>,
    lazyListState: LazyListState,
    scale: Animatable<Float, AnimationVector1D>,
    maxWidth: Dp,
    maxHeight: Dp,
    bottomPadding: Dp,
    today: Jdn,
) {
    if (yearViewCalendar.value == null) yearViewCalendar.value = mainCalendar
    val calendar = yearViewCalendar.value ?: mainCalendar
    val todayDate = today on calendar

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val horizontalDivisions = if (isLandscape) 4 else 3

    val width = floor(maxWidth.value / horizontalDivisions * scale.value).coerceAtLeast(1f).dp
    val height = ((maxHeight - bottomPadding) / if (isLandscape) 3 else 4) * scale.value
    val shape = MaterialTheme.shapes.large.copy(CornerSize(LargeShapeCornerSize.dp * scale.value))

    val titleHeight = with(LocalDensity.current) {
        (height / 10).coerceAtLeast(20.dp).toSp() / 1.6f
    }
    val titleLineHeight = titleHeight * 1.6f
    val padding = 4.dp

    val widthInPx = with(LocalDensity.current) { width.toPx() }
    val paddingInPx = with(LocalDensity.current) { padding.toPx() }

    val resources = LocalResources.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val monthColors = appMonthColors()
    val fontFile = resolveFontFile()
    val isBoldFont = isBoldFont
    val context = LocalContext.current
    val dayPainter = remember(monthColors, widthInPx, fontFile, isBoldFont) {
        lruCache(
            4,
            create = { height: Float ->
                DayPainter(
                    context = context,
                    resources = resources,
                    width = (widthInPx - paddingInPx * 2f) / if (isShowWeekOfYearEnabled) 8 else 7,
                    height = height / 7, /* rows count*/
                    isRtl = isRtl,
                    colors = monthColors,
                    fontFile = fontFile,
                    isYearView = true,
                    holidayCircleColor = monthColors.holidaysFill.toArgb(),
                    isBoldFont = isBoldFont,
                )
            },
        )
    }

    val coroutineScope = rememberCoroutineScope()

    val detectZoom = Modifier.detectZoom {
        coroutineScope.launch {
            val value = scale.value * it
            scale.snapTo(
                value.coerceIn(yearSelectionModeScale, horizontalDivisions.toFloat()),
            )
        }
    }
    val language = language
    val selectedDayMonth = calendar.getMonthStartFromMonthsDistance(
        baseJdn = viewModel.selectedDay,
        monthsDistance = 0,
    )

    LazyColumn(
        state = lazyListState,
        modifier = detectZoom.detectHorizontalSwipe(calendar) {
            { isLeft ->
                val calendars = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
                val index = calendars.indexOf(calendar) + if (isLeft xor isRtl) 1 else -1
                yearViewCalendar.value = calendars[index.mod(calendars.size)]
            }
        },
    ) {
        items(halfPages * 2) {
            val yearOffset = it - halfPages

            Column(Modifier.fillMaxWidth()) {
                if (scale.value != yearSelectionModeScale) {
                    val yearDeviceEvents: DeviceCalendarEventsStore = remember(yearOffset, today) {
                        val yearStartJdn = Jdn(
                            calendar.createDate(
                                (today on calendar).year + yearOffset, 1, 1,
                            ),
                        )
                        if (isShowDeviceCalendarEvents) {
                            context.readYearDeviceEvents(yearStartJdn)
                        } else EventsStore.empty()
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        repeat(if (isLandscape) 3 else 4) { row ->
                            repeat(if (isLandscape) 4 else 3) { column ->
                                val month = 1 + column + row * if (isLandscape) 4 else 3
                                val offset = yearOffset * 12 + month - todayDate.month
                                val monthDate = calendar.getMonthStartFromMonthsDistance(
                                    baseJdn = today,
                                    monthsDistance = offset,
                                )
                                val title = language.my.format(
                                    monthDate.monthName,
                                    numeral.format(yearOffset + todayDate.year),
                                )
                                Column(
                                    Modifier
                                        .size(width, height)
                                        .padding(padding)
                                        .clip(shape)
                                        .then(detectZoom)
                                        .clickable(onClickLabel = stringResource(R.string.select_month)) {
                                            closeYearView()
                                            if (mainCalendar == calendar) {
                                                viewModel.changeSelectedMonthOffsetCommand(offset)
                                            } else viewModel.bringDay(Jdn(monthDate))
                                        }
                                        .background(LocalContentColor.current.copy(alpha = .1f))
                                        .then(
                                            if (offset != viewModel.selectedMonthOffset) Modifier else Modifier.border(
                                                width = 2.dp,
                                                color = LocalContentColor.current.copy(alpha = .15f),
                                                shape = shape,
                                            ),
                                        ),
                                ) {
                                    Text(
                                        title,
                                        Modifier.fillMaxWidth(),
                                        fontSize = titleHeight,
                                        textAlign = TextAlign.Center,
                                        lineHeight = titleLineHeight,
                                    )
                                    Canvas(
                                        Modifier
                                            .fillMaxSize()
                                            .then(
                                                if (monthDate == selectedDayMonth) Modifier.graphicsLayer(
                                                    compositingStrategy = CompositingStrategy.Offscreen,
                                                ) else Modifier,
                                            ),
                                    ) {
                                        drawIntoCanvas { canvas ->
                                            renderMonthWidget(
                                                dayPainter = dayPainter[this.size.height],
                                                width = size.width,
                                                canvas = canvas.nativeCanvas,
                                                today = today,
                                                baseDate = monthDate,
                                                deviceEvents = yearDeviceEvents,
                                                isRtl = isRtl,
                                                isShowWeekOfYearEnabled = isShowWeekOfYearEnabled,
                                                selectedDay = viewModel.selectedDay,
                                                calendar = calendar,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val space = bottomPadding * scale.value.coerceIn(.4f, 1f)
                val alpha = (.15f * (1 - scale.value)).coerceIn(0f, .15f)
                Spacer(Modifier.height(space))
                if (yearOffset != halfPages - 1) Box(Modifier.align(Alignment.CenterHorizontally)) {
                    val yearViewYear = yearOffset + todayDate.year + 1

                    @Suppress("SimplifiableCallChain") val tooltip =
                        enabledCalendars.let { if (it.size > 1) it - calendar else it }
                            .map { otherCalendar ->
                                otherCalendarFormat(
                                    yearViewYear,
                                    calendar,
                                    otherCalendar,
                                ) + " " + stringResource(otherCalendar.title)
                            }.joinToString("\n")
                    @OptIn(ExperimentalMaterial3Api::class) TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above,
                        ),
                        tooltip = { PlainTooltip { Text(tooltip, textAlign = TextAlign.Center) } },
                        state = rememberTooltipState(),
                    ) {
                        Text(
                            numeral.format(yearViewYear),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(LocalContentColor.current.copy(alpha = alpha))
                                .padding((32 * alpha).dp)
                                .then(detectZoom)
                                .clickable(onClickLabel = stringResource(R.string.select_year)) {
                                    coroutineScope.launch {
                                        if (scale.value == yearSelectionModeScale) scale.snapTo(
                                            1f,
                                        )
                                        lazyListState.animateScrollToItem(halfPages + yearOffset + 1)
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun yearViewLazyListState(
    today: Jdn,
    selectedMonthOffset: Int,
    yearViewCalendar: Calendar?,
): LazyListState {
    val calendar = yearViewCalendar ?: mainCalendar
    val yearOffsetInMonths = calendar.getMonthStartFromMonthsDistance(
        baseJdn = today,
        monthsDistance = selectedMonthOffset,
    ).year - (today on calendar).year
    return rememberLazyListState(halfPages + yearOffsetInMonths)
}

sealed interface YearViewCommand {
    object NextMonth : YearViewCommand
    object PreviousMonth : YearViewCommand
    object TodayMonth : YearViewCommand
    object ToggleYearSelection : YearViewCommand

    suspend operator fun invoke(
        yearViewLazyListState: LazyListState?,
        yearViewScale: Animatable<Float, AnimationVector1D>?,
    ) {
        val lazyListState = yearViewLazyListState ?: return
        val scale = yearViewScale ?: return
        when (this) {
            ToggleYearSelection -> scale.snapTo(if (scale.value > .5f) 0.01f else 1f)

            PreviousMonth -> {
                lazyListState.animateScrollToItem(
                    (lazyListState.firstVisibleItemIndex - 1).coerceAtLeast(0),
                )
            }

            NextMonth -> {
                lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex + 1)
            }

            TodayMonth -> {
                scale.animateTo(1f)
                if (abs(lazyListState.firstVisibleItemIndex - halfPages) > 2) {
                    lazyListState.scrollToItem(halfPages)
                } else lazyListState.animateScrollToItem(halfPages)
            }
        }
    }
}

@Composable
fun yearViewOffset(lazyListState: LazyListState?): Int {
    lazyListState ?: return 0
    return remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex - halfPages }
    }.value
}

fun yearViewIsInYearSelection(
    yearViewScale: Animatable<Float, AnimationVector1D>?,
): Boolean = yearViewScale?.value == yearSelectionModeScale

private const val yearSelectionModeScale = .2f
private const val halfPages = 200
