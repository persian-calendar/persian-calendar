package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.math.MathUtils
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
    private var movableCircle = MovableCircle(resources) { invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // This is important, don't remove it ever

        val daysAdapter = daysAdapter.debugAssertNotNull ?: return
        val sharedData = daysAdapter.sharedDayViewData

        movableCircle.onDraw(canvas, daysAdapter, sharedData, this)

        // Widget only tweak
        val widgetFooterTextPaint = sharedData.widgetFooterTextPaint ?: return
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

    fun selectDay(dayOfMonth: Int) {
        val daysAdapter = daysAdapter.debugAssertNotNull ?: return
        daysAdapter.selectDayInternal(dayOfMonth)
        movableCircle.selectDay(dayOfMonth, daysAdapter, this)
    }
}

private class MovableCircle(resources: Resources, invalidate: (_: ValueAnimator) -> Unit) {
    private var currentSelectionPosition = -1
    private var currentSelectionX = 0f
    private var currentSelectionY = 0f
    private var lastSelectionX = 0f
    private var lastSelectionY = 0f
    private var isSelectionReveal = false
    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = OvershootInterpolator(1.5f)
        it.addUpdateListener(invalidate)
        it.doOnEnd { isSelectionReveal = false }
    }
    private val fadeAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        it.addUpdateListener(invalidate)
    }

    fun selectDay(dayOfMonth: Int, daysAdapter: DaysAdapter, recyclerView: RecyclerView) {
        if (dayOfMonth == -1) {
            if (currentSelectionPosition != -1) {
                currentSelectionPosition = -1
                fadeAnimator.start()
            }
            return
        }

        val selectedDayPosition = daysAdapter.selectedDayPosition
        if (currentSelectionPosition == -1) {
            isSelectionReveal = true
            val dayView =
                recyclerView.findViewHolderForAdapterPosition(selectedDayPosition)?.itemView
                    ?: return
            lastSelectionX = dayView.left * 1f
            lastSelectionY = dayView.top * 1f
        } else {
            isSelectionReveal = false
        }
        currentSelectionX = lastSelectionX
        currentSelectionY = lastSelectionY
        transitionAnimator.start()
        currentSelectionPosition = selectedDayPosition
    }

    fun onDraw(
        canvas: Canvas, daysAdapter: DaysAdapter, sharedData: SharedDayViewData,
        recyclerView: RecyclerView
    ) {
        val selectedDayPosition = daysAdapter.selectedDayPosition
        if (selectedDayPosition == -1 && currentSelectionPosition == -1 &&
            fadeAnimator.animatedFraction != 0f && fadeAnimator.animatedFraction != 1f
        ) {
            val dayView = recyclerView.findViewHolderForAdapterPosition(0)?.itemView ?: return
            canvas.drawCircle(
                lastSelectionX + dayView.width / 2f,
                lastSelectionY + dayView.height / 2f,
                DayView.radius(dayView) * (1 - fadeAnimator.animatedFraction),
                sharedData.selectedPaint
            )
        } else if (selectedDayPosition != -1 && selectedDayPosition == currentSelectionPosition) {
            val dayView =
                recyclerView.findViewHolderForAdapterPosition(selectedDayPosition)?.itemView
                    ?: return
            val fraction = transitionAnimator.animatedFraction
            lastSelectionX = MathUtils.lerp(currentSelectionX, dayView.left * 1f, fraction)
            lastSelectionY = MathUtils.lerp(currentSelectionY, dayView.top * 1f, fraction)
            val radius = MathUtils.lerp(
                0f, DayView.radius(dayView), if (isSelectionReveal) fraction else 1f
            )
            canvas.drawCircle(
                lastSelectionX + dayView.width / 2f,
                lastSelectionY + dayView.height / 2f,
                radius,
                sharedData.selectedPaint
            )
        }
    }
}
