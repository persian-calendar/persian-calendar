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

    private var selectionIndicator = SelectionIndicator(context) { invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        selectedDayView?.also {
            selectionIndicator.onDraw(canvas, it.width, it.height, it.top, it.left)
        }
    }

    fun bind(monthStartJdn: Jdn, monthStartDate: AbstractDate) {
        val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
        contentDescription =
            language.my.format(monthStartDate.monthName, formatNumber(monthStartDate.year))

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
        selectionIndicator.selectDay(selectedDayPosition != null)
        if (selectedDayPosition != null)
            selectedDayView = findViewHolderForAdapterPosition(selectedDayPosition)?.itemView
    }

    private var selectedDayView: View? = null
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
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = context.resolveColor(R.attr.colorSelectedDay)
    }
    private val transitionInterpolators = listOf(1f, 1.25f).map(::OvershootInterpolator)
    private val revealInterpolator = OvershootInterpolator(1.5f)

    fun selectDay(isSelection: Boolean) {
        if (!isSelection) {
            if (isCurrentlySelected) {
                isReveal = false
                isCurrentlySelected = false
                hideAnimator.start()
            }
        } else {
            isReveal = !isCurrentlySelected
            isCurrentlySelected = true
            currentX = lastX
            currentY = lastY
            transitionAnimator.start()
        }
    }

    fun onDraw(canvas: Canvas, width: Int, height: Int, top: Int, left: Int) {
        if (hideAnimator.isRunning) canvas.drawCircle(
            left + width / 2f, top + height / 2f,
            lastRadius * (1 - hideAnimator.animatedFraction), paint
        ) else if (isReveal) {
            val fraction = revealInterpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = left.toFloat()
            lastY = top.toFloat()
            lastRadius = DayView.radius(width, height) * fraction
            canvas.drawCircle(
                lastX + width / 2f, lastY + height / 2f,
                DayView.radius(width, height) * fraction, paint
            )
        } else if (isCurrentlySelected) transitionInterpolators.forEach { interpolator ->
            val fraction = interpolator.getInterpolation(transitionAnimator.animatedFraction)
            lastX = lerp(currentX, left.toFloat(), fraction)
            lastY = lerp(currentY, top.toFloat(), fraction)
            lastRadius = DayView.radius(width, height)
            canvas.drawCircle(
                lastX + width / 2f, lastY + height / 2f, lastRadius, paint
            )
        }
    }
}
