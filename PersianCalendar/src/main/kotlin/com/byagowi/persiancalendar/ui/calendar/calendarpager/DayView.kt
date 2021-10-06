package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.otherCalendars
import kotlin.math.min

class DayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    var jdn: Jdn? = null
        private set
    var dayOfMonth = -1
        private set
    private var isWeekNumber = false
    private var header = ""

    var sharedDayViewData: SharedDayViewData? = null

    private val textBounds = Rect()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val shared = sharedDayViewData ?: return

        // Draw circle around day
        val radius = min(width, height) / 2f
        if (dayIsSelected)
            canvas.drawCircle(width / 2f, height / 2f, radius - 5, shared.selectedPaint)
        if (today) canvas.drawCircle(width / 2f, height / 2f, radius - 5, shared.todayPaint)

        val textPaint = when {
            jdn != null -> when {
                today && !shared.isCurrentDayOutlineOnly -> shared.dayOfMonthNumberCurrentTextPaint
                holiday -> shared.dayOfMonthNumberTextHolidayPaint
                dayIsSelected -> shared.dayOfMonthNumberTextSelectedPaint
                else /*!dayIsSelected*/ -> shared.dayOfMonthNumberTextPaint
            }
            isWeekNumber -> shared.weekNumberTextPaint
            else -> shared.weekDayInitialsTextPaint
        }

        // Measure a sample text to find height for vertical center aligning of the text to draw
        val sample = if (jdn != null) text else if (language.isArabicScript) "س" else "Yy"
        textPaint.getTextBounds(sample, 0, sample.length, textBounds)
        val yPos = (height + textBounds.height()) / 2f
        // Draw day number/label
        canvas.drawText(text, width / 2f, yPos, textPaint)

        // Draw indicators, whether a day has event or appointment
        val offsetDirection = if (layoutDirection == LAYOUT_DIRECTION_RTL) -1 else 1
        indicators.forEachIndexed { i, paint ->
            val xOffset = shared.eventIndicatorsCentersDistance *
                    (i - (indicators.size - 1) / 2f) * offsetDirection
            canvas.drawCircle(
                width / 2f + xOffset, height - shared.eventYOffset,
                shared.eventIndicatorRadius, when {
                    dayIsSelected -> shared.headerTextSelectedPaint
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    isHighTextContrastEnabled && holiday && paint == shared.eventIndicatorPaint ->
                        shared.dayOfMonthNumberTextHolidayPaint
                    else -> paint
                }
            )
        }

        // Draw day header which is used for shit work
        if (header.isNotEmpty()) {
            canvas.drawText(
                header, width / 2f, yPos * 0.87f - textBounds.height(),
                if (dayIsSelected) shared.headerTextSelectedPaint else shared.headerTextPaint
            )
        }

        // Experiment around what happens if we show other calendars day of month
        if ((false)) jdn?.also {
            otherCalendars.forEachIndexed { i, calendar ->
                val offset = (if (layoutDirection == LAYOUT_DIRECTION_RTL) -1 else 1) *
                        if (i == 1) -1 else 1
                canvas.drawText(
                    // better to not calculate this during onDraw
                    formatNumber(it.toCalendar(calendar).dayOfMonth),
                    (width - radius * offset) / 2f,
                    (height + textBounds.height() + radius) / 2f,
                    shared.headerTextPaint
                )
            }
        }
    }

    private fun setAll(
        text: String, isToday: Boolean = false, isSelected: Boolean = false,
        hasEvent: Boolean = false, hasAppointment: Boolean = false, isHoliday: Boolean = false,
        jdn: Jdn? = null, dayOfMonth: Int = -1, header: String = "",
        isWeekNumber: Boolean = false
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isWeekNumber = isWeekNumber
        this.header = header
        sharedDayViewData.debugAssertNotNull?.also { shared ->
            this.indicators = listOf(
                hasAppointment to shared.appointmentIndicatorPaint,
                (hasEvent || (isHighTextContrastEnabled && holiday)) to shared.eventIndicatorPaint
            ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
            if (jdn != null) setBackgroundResource(shared.selectableItemBackground)
        }
        postInvalidate()
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        jdn: Jdn, dayOfMonth: Int, header: String
    ) = setAll(
        text = formatNumber(dayOfMonth), isToday = isToday, isSelected = isSelected,
        hasEvent = hasEvent, hasAppointment = hasAppointment, isHoliday = isHoliday, jdn = jdn,
        dayOfMonth = dayOfMonth, header = header
    )

    fun setInitialOfWeekDay(text: String) = setAll(text)
    fun setWeekNumber(text: String) = setAll(text, isWeekNumber = true)
}
