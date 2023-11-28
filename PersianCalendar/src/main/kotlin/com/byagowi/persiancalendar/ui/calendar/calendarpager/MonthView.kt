package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.lerp
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import kotlin.math.min

class MonthView(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {

    init {
        setHasFixedSize(true)
        itemAnimator = null
        layoutManager = GridLayoutManager(context, if (isShowWeekOfYearEnabled) 8 else 7)
    }

    private var daysAdapter: DaysAdapter? = null

    fun initialize(sharedDayViewData: SharedDayViewData, calendarPager: CalendarPager) {
        daysAdapter = DaysAdapter(context, sharedDayViewData, calendarPager)
        adapter = daysAdapter
        addCellSpacing((4 * resources.dp).toInt())
    }

    fun initializeForRendering(
        @ColorInt textColor: Int,
        width: Int,
        height: Int,
        today: AbstractDate
    ) {
        val sharedData = SharedDayViewData(
            context, height / 7f, min(width, height) / 7f, textColor
        )
        daysAdapter = DaysAdapter(context, sharedData, null)
        adapter = daysAdapter
        val jdn = Jdn(mainCalendar, today.year, today.month, 1)
        bind(jdn, jdn.toCalendar(mainCalendar))
    }

    private fun addCellSpacing(space: Int) {
        addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: State
            ) {
                if (parent.paddingBottom != space) {
                    parent.updatePadding(bottom = space)
                    parent.clipToPadding = false
                }
                outRect.set(0, 0, 0, space)
            }
        })
    }

    private var monthName = ""
    private var selectionIndicator = SelectionIndicator(context) { invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        selectionIndicator.onDraw(canvas, this)

        // Widget only drawing
        val daysAdapter = daysAdapter.debugAssertNotNull ?: return
        val widgetFooterTextPaint = daysAdapter.sharedDayViewData.widgetFooterTextPaint ?: return
        canvas.drawText(monthName, width / 2f, height * .95f, widgetFooterTextPaint)
    }

    fun bind(monthStartJdn: Jdn, monthStartDate: AbstractDate) {
        val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
        monthName = language.my.format(monthStartDate.monthName, formatNumber(monthStartDate.year))
        contentDescription = monthName

        daysAdapter?.let {
            val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
            it.startingDayOfWeek = monthStartJdn.dayOfWeek
            it.weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)
            it.weeksCount = (monthStartJdn + monthLength - 1).getWeekOfYear(startOfYearJdn) -
                    it.weekOfYearStart + 1
            it.days = monthStartJdn.createMonthDaysList(monthLength)
            it.initializeMonthEvents()
            it.notifyItemRangeChanged(0, it.itemCount)
        }
    }

    fun initializeMonthEvents() {
        daysAdapter?.initializeMonthEvents()
    }

    fun selectDay(dayOfMonth: Int?) {
        val daysAdapter = daysAdapter.debugAssertNotNull ?: return
        val selectedDayPosition = daysAdapter.selectDayInternal(dayOfMonth)
        selectionIndicator.selectDay(selectedDayPosition)
    }
}

private class SelectionIndicator(context: Context, invalidate: (_: ValueAnimator) -> Unit) {
    private var isCurrentlySelected = false
    private var currentX = 0f
    private var currentY = 0f

    // Last position, regardless of being selected right now or not
    private var lastPosition: Int? = null
    private var lastX = 0f
    private var lastY = 0f
    private var lastRadius = 0f
    private var isReveal = false
    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = LinearInterpolator()
        it.addUpdateListener(invalidate)
        it.doOnEnd { isReveal = false }
    }
    private val hideAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        it.addUpdateListener(invalidate)
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = context.resolveColor(R.attr.colorSelectedDay)
    }
    private val transitionInterpolators = listOf(1f, 1.25f).map(::OvershootInterpolator)
    private val revealInterpolator = OvershootInterpolator(1.5f)

    fun selectDay(selectedDayPosition: Int?) {
        if (selectedDayPosition == null) {
            if (isCurrentlySelected) {
                isReveal = false
                isCurrentlySelected = false
                hideAnimator.start()
            }
        } else {
            isReveal = !isCurrentlySelected
            isCurrentlySelected = true
            lastPosition = selectedDayPosition
            currentX = lastX
            currentY = lastY
            transitionAnimator.start()
        }
    }

    fun onDraw(canvas: Canvas, recyclerView: RecyclerView) {
        val lastPosition = lastPosition ?: return
        val dayView = recyclerView.findViewHolderForAdapterPosition(lastPosition)?.itemView
            ?: return
        if (hideAnimator.isRunning) canvas.drawCircle(
            dayView.left + dayView.width / 2f, dayView.top + dayView.height / 2f,
            lastRadius * (1 - hideAnimator.animatedFraction), paint
        ) else if (isReveal) {
            val fraction = revealInterpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = dayView.left.toFloat()
            lastY = dayView.top.toFloat()
            lastRadius = DayView.radius(dayView) * fraction
            canvas.drawCircle(
                lastX + dayView.width / 2f, lastY + dayView.height / 2f,
                DayView.radius(dayView) * fraction, paint
            )
        } else if (isCurrentlySelected) transitionInterpolators.forEach { interpolator ->
            val fraction = interpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = lerp(currentX, dayView.left.toFloat(), fraction)
            lastY = lerp(currentY, dayView.top.toFloat(), fraction)
            lastRadius = DayView.radius(dayView)
            canvas.drawCircle(
                lastX + dayView.width / 2f, lastY + dayView.height / 2f, lastRadius, paint
            )
        }
    }
}
