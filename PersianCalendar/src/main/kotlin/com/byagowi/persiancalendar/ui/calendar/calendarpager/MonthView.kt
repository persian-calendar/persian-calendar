package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.animation.doOnEnd
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.lerp
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class MonthView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val root = ComposeView(context).also(::addView)

    private var sharedDayViewData: SharedDayViewData? = null
    private var calendarPager: CalendarPager? = null

    fun initialize(sharedDayViewData: SharedDayViewData, calendarPager: CalendarPager) {
        this.sharedDayViewData = sharedDayViewData
        this.calendarPager = calendarPager
    }

    fun bind(monthStartJdn: Jdn, monthStartDate: AbstractDate) {
        root.setContent {
            AppTheme {
                Month(monthStartJdn, monthStartDate)
            }
        }
    }

    private val lastSelectedDay = MutableStateFlow(0)
    private val isSelected = MutableStateFlow<Boolean?>(null)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Month(monthStartJdn: Jdn, monthStartDate: AbstractDate) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        // Why the moving circle feels faster this way
        val invalidationFlow = remember { MutableStateFlow(0) }
        val invalidationToken by invalidationFlow.collectAsState()
        val context = LocalContext.current
        val selectionIndicator = remember {
            SelectionIndicator(context) { ++invalidationFlow.value }
        }
        val isSelected by isSelected.collectAsState()
        if (isSelected != true) selectionIndicator.clearSelection()
        LaunchedEffect(null) { if (isSelected == true) selectionIndicator.startSelection() }
        val columnsCount = if (isShowWeekOfYearEnabled) 8 else 7
        val rowsCount = 7

        val startingDayOfWeek = monthStartJdn.dayOfWeek
        val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
        val monthRange = 0..<monthLength
        val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
        val weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)
        val weeksCount = (monthStartJdn + monthLength - 1).getWeekOfYear(startOfYearJdn) -
                weekOfYearStart + 1
        val todayJdn = Jdn.today()

        val monthDeviceEvents = remember(eventsRefreshToken) {
            if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
            else EventsStore.empty()
        }
        val dayPositions = remember(monthStartJdn) { mutableMapOf<Int, Pair<Int, Int>>() }
        val lastSelectedDay by lastSelectedDay.collectAsState()

        Column(
            Modifier
                .fillMaxWidth()
                .drawWithCache {
                    onDrawBehind {
                        drawIntoCanvas {
                            invalidationToken.run {}
                            val (column, row) = dayPositions[lastSelectedDay - 1] ?: (0 to 0)
                            val l = column * width / columnsCount
                            val t = row * height / rowsCount
                            val w = width / columnsCount
                            val h = height / rowsCount
                            selectionIndicator.onDraw(it, if (isRtl) width - l - w else l, t, w, h)
                        }
                    }
                }
        ) {
            Row(Modifier.weight(1f, fill = false)) {
                if (isShowWeekOfYearEnabled) Box(Modifier.weight(1f))
                (0..<7).forEach { column ->
                    val weekDayPosition = revertWeekStartOffsetFromWeekDay(column)
                    val description = stringResource(
                        R.string.week_days_name_column, getWeekDayName(weekDayPosition)
                    )
                    Cell(Modifier.semantics { this.contentDescription = description }) {
                        it.setInitialOfWeekDay(getInitialOfWeekDay(weekDayPosition))
                    }
                }
            }
            (0..<6).forEach { row ->
                Row(Modifier.weight(1f, fill = false)) {
                    if (isShowWeekOfYearEnabled && row < weeksCount) {
                        val weekNumber = formatNumber(weekOfYearStart + row - 1)
                        val description =
                            stringResource(R.string.nth_week_of_year, weekNumber)
                        Cell(Modifier.semantics { this.contentDescription = description }) {
                            it.setWeekNumber(weekNumber)
                        }
                    }
                    (0..<7).forEach RowForEach@{ column ->
                        val dayOffset = (column + row * 7) -
                                applyWeekStartOffsetToWeekDay(startingDayOfWeek)
                        val day = monthStartJdn + dayOffset
                        if (dayOffset !in monthRange) return@RowForEach Cell {}
                        val isToday = day == todayJdn
                        Cell(
                            Modifier
                                .combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        calendarPager?.let { it.onDayClicked(day) }
                                        selectionIndicator.startSelection()
                                    },
                                    onClickLabel = if (isTalkBackEnabled) getA11yDaySummary(
                                        context,
                                        day,
                                        isToday,
                                        EventsStore.empty(),
                                        withZodiac = isToday,
                                        withOtherCalendars = false,
                                        withTitle = true
                                    ) else (dayOffset + 1).toString(),
                                    onLongClickLabel = stringResource(R.string.add_event),
                                    onLongClick = {
                                        calendarPager?.let { it.onDayLongClicked(day) }
                                    }
                                )
                        ) {
                            dayPositions[dayOffset] =
                                (column + if (isShowWeekOfYearEnabled) 1 else 0) to row + 1
                            val events =
                                eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()
                            it.setDayOfMonthItem(
                                isToday, isSelected == true && dayOffset == lastSelectedDay - 1,
                                events.any { it !is CalendarEvent.DeviceCalendarEvent },
                                events.any { it is CalendarEvent.DeviceCalendarEvent },
                                events.any { it.isHoliday }, day, dayOffset + 1,
                                getShiftWorkTitle(day, true)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.Cell(modifier: Modifier = Modifier, update: (DayView) -> Unit) {
        AndroidView(
            factory = ::DayView,
            update = {
                it.sharedDayViewData = sharedDayViewData
                update(it)
            },
            modifier = modifier.then(Modifier.weight(1f)),
        )
    }

    private val eventsRefreshToken = MutableStateFlow(UUID.randomUUID())

    fun initializeMonthEvents() {
        eventsRefreshToken.value = UUID.randomUUID()
    }

    fun selectDay(dayOfMonth: Int?) {
        isSelected.value = dayOfMonth != null
        if (dayOfMonth != null) lastSelectedDay.value = dayOfMonth
    }
}

private class SelectionIndicator(context: Context, invalidate: () -> Unit) {
    private var isCurrentlySelected = false
    private var currentX = 0f
    private var currentY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var lastRadius = 0f
    private var isReveal = false
    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = LinearInterpolator()
        it.addUpdateListener { invalidate() }
        it.doOnEnd { isReveal = false }
    }
    private val hideAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        it.addUpdateListener { invalidate() }
    }
    private val paint = Paint().also {
        it.style = PaintingStyle.Fill
        it.color = Color(context.resolveColor(R.attr.colorSelectedDay))
    }
    private val transitionInterpolators = listOf(1f, 1.25f).map(::OvershootInterpolator)
    private val revealInterpolator = OvershootInterpolator(1.5f)

    fun clearSelection() {
        if (isCurrentlySelected) {
            isReveal = false
            isCurrentlySelected = false
            hideAnimator.start()
        }
    }

    fun startSelection() {
        isReveal = !isCurrentlySelected
        isCurrentlySelected = true
        currentX = lastX
        currentY = lastY
        transitionAnimator.start()
    }

    fun onDraw(canvas: Canvas, left: Int, top: Int, width: Int, height: Int) {
        if (hideAnimator.isRunning) canvas.drawCircle(
            Offset(left + width / 2f, top + height / 2f),
            lastRadius * (1 - hideAnimator.animatedFraction), paint
        ) else if (isReveal) {
            val fraction = revealInterpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = left.toFloat()
            lastY = top.toFloat()
            lastRadius = DayView.radius(width, height) * fraction
            canvas.drawCircle(
                Offset(lastX + width / 2f, lastY + height / 2f),
                DayView.radius(width, height) * fraction, paint
            )
        } else if (isCurrentlySelected) transitionInterpolators.forEach { interpolator ->
            val fraction = interpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = lerp(currentX, left.toFloat(), fraction)
            lastY = lerp(currentY, top.toFloat(), fraction)
            lastRadius = DayView.radius(width, height)
            canvas.drawCircle(
                Offset(lastX + width / 2f, lastY + height / 2f), lastRadius, paint
            )
        }
    }
}
