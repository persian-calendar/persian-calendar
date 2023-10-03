package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
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
    private var selectionIndicator = SelectionIndicator(context) { invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // This is important, don't remove it ever

        val daysAdapter = daysAdapter.debugAssertNotNull ?: return
        selectionIndicator.onDraw(canvas, daysAdapter, this)

        // Widget only tweak
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

    fun selectDay(dayOfMonth: Int) {
        val daysAdapter = daysAdapter.debugAssertNotNull ?: return
        daysAdapter.selectDayInternal(dayOfMonth)
        // Below uses selectedDayPosition which is set in daysAdapter by above selectDayInternal call
        selectionIndicator.selectDay(daysAdapter.selectedDayPosition)
    }
}

private class SelectionIndicator(context: Context, invalidate: (_: ValueAnimator) -> Unit) {
    private var currentPosition = -1
    private var currentX = 0f
    private var currentY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var isReveal = false
    private val transitionAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        it.interpolator = OvershootInterpolator(1.5f)
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

    fun selectDay(selectedDayPosition: Int) {
        if (selectedDayPosition == -1) {
            if (currentPosition != -1) {
                currentPosition = -1
                hideAnimator.start()
            }
        } else {
            isReveal = currentPosition == -1
            currentPosition = selectedDayPosition
            currentX = lastX
            currentY = lastY
            transitionAnimator.start()
        }
    }

    fun onDraw(canvas: Canvas, daysAdapter: DaysAdapter, recyclerView: RecyclerView) {
        val dayPosition = daysAdapter.selectedDayPosition
        if (dayPosition == -1 && currentPosition == -1 &&
            hideAnimator.animatedFraction != 0f && hideAnimator.animatedFraction != 1f
        ) {
            val dayView = recyclerView.findViewHolderForAdapterPosition(0)?.itemView ?: return
            canvas.drawCircle(
                lastX + dayView.width / 2f,
                lastY + dayView.height / 2f,
                DayView.radius(dayView) * (1 - hideAnimator.animatedFraction),
                paint
            )
        } else if (dayPosition != -1 && dayPosition == currentPosition) {
            val dayView =
                recyclerView.findViewHolderForAdapterPosition(dayPosition)?.itemView
                    ?: return
            val fraction = transitionAnimator.animatedFraction
            lastX = MathUtils.lerp(
                currentX, dayView.left * 1f, if (isReveal) 1f else fraction
            )
            lastY = MathUtils.lerp(
                currentY, dayView.top * 1f, if (isReveal) 1f else fraction
            )
            val radius = MathUtils.lerp(
                0f, DayView.radius(dayView), if (isReveal) fraction else 1f
            )
            canvas.drawCircle(
                lastX + dayView.width / 2f, lastY + dayView.height / 2f,
                radius, paint
            )
        }
    }
}
